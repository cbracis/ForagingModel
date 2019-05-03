package ForagingModel.space;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.math3.analysis.function.Exp;
import org.apache.commons.math3.analysis.function.StepFunction;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.util.FastMath;

import ForagingModel.agent.movement.AbstractMovementProcess.AngleBounds;
import ForagingModel.core.Angle;
import ForagingModel.core.DirectionProbabalistic;
import ForagingModel.core.DirectionProbabilityInfo;
import ForagingModel.core.GridPoint;
import ForagingModel.core.MatrixUtils;
import ForagingModel.core.ModelEnvironment;
import ForagingModel.core.NdPoint;
import ForagingModel.core.NumberGenerator;

public abstract class AbstractMemory implements MemoryAssemblage
{
	// large value to avoid infinities in probabilities which turn into NaN when normalized
	protected final double MAX_PROBABILITY_VALUE = 1e300;
	// difference in detectable angle when creating Angle object vs. raw double value (due to modulus arithmetic?)
	private final double ANGLE_TOLERANCE = 1e-6;
	protected NumberGenerator generator;
	protected AngularProbabilityInfo angProbInfo;
	
	// this tracks the current probabilities and selected direction
	// need to be careful since direction is set from abstract class but probabilities from inheriting concrete classes
	protected DirectionProbabilityInfo probabilityCache;

	
	public AbstractMemory(AngularProbabilityInfo angProbInfo)
	{
		generator = ModelEnvironment.getNumberGenerator();
		probabilityCache = new DirectionProbabilityInfo();
		this.angProbInfo = angProbInfo;
	}
	
	public DirectionProbabilityInfo reportCurrentProbabilities()
	{
		return probabilityCache;
	}
	
	@Override
	public void notifyInterval(int currentInterval)
	{
		probabilityCache.clear();
	}
	
	@Override
	public void notifyTimeStep(int currentTimeStep) 
	{
		// do nothing
	}

	public NdPoint getDestinationProbabalistic(NdPoint currentLocation) 
	{
		NdPoint destination;
		
		RealMatrix probabilities = getProbabilities(currentLocation);
		
		if (probabilities == null)
		{
			// no destination (use kinesis) if memory all 0 or negative
			destination = null;
		}
		else
		{
			GridPoint index = generator.nextInt(probabilities); 
			destination = new NdPoint(index.getX() + generator.nextDouble(), index.getY() + generator.nextDouble());
		}
		
		return destination;
	}
	
	public DirectionProbabalistic getDirectionProbabalistic(NdPoint currentLocation) 
	{
		RealVector probabilities = getAngularProbabilities(currentLocation);
		
		long sector = generator.nextInt(probabilities);
		Angle angle = getRandomAngleInSector(sector, probabilities.getDimension());
		DirectionProbabalistic direction = new DirectionProbabalistic(angle, probabilities.getEntry((int)sector), probabilityCache.predatorSafety(), angProbInfo, getEarthMoversDistance(probabilities));
		probabilityCache.updateDirection(direction);
		
		return direction;
	}
	
	public DirectionProbabalistic getDirectionProbabalistic(NdPoint currentLocation, AngleBounds bounds) 
	{
		
		if (bounds.overlapsZero())
		{
			return getDirectionProbabalisticOverlap(currentLocation, bounds.getMin(), bounds.getMax());
		}

		RealVector probabilities = getAngularProbabilities(currentLocation);

		// find starting index and width to use based on angle restrictions
		int startIdx = -1;
		int width = 0;
		List<Double> angles = angProbInfo.getAngles(); // assumes increasing
		
		
		for (int i = 0; i < angles.size(); i++)
		{
			if (angles.get(i) >= bounds.getMin())
			{
				if (startIdx == -1)
				{
					startIdx = i;
				}
				// must be strictly less than, because if prob associated with that angle is selected,
				// sector is from that angle to the next
				// use ANGLE_TOLERANCE due to rounding issues creating angles instead of just 0
				if ( bounds.getMax() - angles.get(i) > ANGLE_TOLERANCE)
				{
					width++;
				}
				else
				{
					break;
				}
			}
		}
		
		// probabilities of that section
		RealVector newProbs = probabilities.getSubVector(startIdx, width).copy();
		normalizeVector(newProbs);
		
		long sector = startIdx + generator.nextInt(newProbs);
		Angle angle = getRandomAngleInSector(sector, probabilities.getDimension(), bounds.getMax());
		assert(angle.get() >= bounds.getMin());
		assert(angle.get() <= bounds.getMax());
		DirectionProbabalistic direction = new DirectionProbabalistic(angle, probabilities.getEntry((int)sector), probabilityCache.predatorSafety(), angProbInfo, getEarthMoversDistance(newProbs));
		probabilityCache.updateDirection(direction);
		
		return direction;
	}
	
	protected DirectionProbabalistic getDirectionProbabalisticOverlap(NdPoint currentLocation, 
			double angleBelowZero, double angleAboveZero) 
	{
		assert(angleAboveZero < angleBelowZero);
		
		RealVector probabilities = getAngularProbabilities(currentLocation);

		// find starting index and width to use based on angle restrictions
		int startIdxAbove = 0;
		int widthAbove = 0;
		int startIdxBelow = -1;
		int widthBelow = 0;

		List<Double> angles = angProbInfo.getAngles(); // assumes increasing
		
		
		for (int i = 0; i < angles.size(); i++)
		{
			// same issue as above, use ANGLE_TOLERANCE instead of 0
			if (angleAboveZero - angles.get(i) > ANGLE_TOLERANCE)
			{
				// first section is from 0 to the angle above
				widthAbove++;
			}
			else if (angles.get(i) >= angleBelowZero)
			{
				// second section is the rest after angle below
				startIdxBelow = i;
				widthBelow = angles.size() - i;
				break;
			}
		}
		
		// probabilities of that section
		RealVector newProbs = new ArrayRealVector(probabilities.getSubVector(startIdxAbove, widthAbove));
		newProbs.append(probabilities.getSubVector(startIdxBelow, widthBelow) );
		
		normalizeVector(newProbs);
		
		long sector = generator.nextInt(newProbs);
		double maxAngle = angleAboveZero;
		if (sector > widthAbove)
		{
			sector = sector - widthAbove + startIdxBelow;
			maxAngle = Angle.MAX_ANGLE;
		}
		Angle angle = getRandomAngleInSector(sector, probabilities.getDimension(), maxAngle);
		DirectionProbabalistic direction = new DirectionProbabalistic(angle, probabilities.getEntry((int)sector), probabilityCache.predatorSafety(), angProbInfo, getEarthMoversDistance(newProbs));
		probabilityCache.updateDirection(direction);
		
		return direction;
	}

	protected Angle getRandomAngleInSector(double sectorIdx, double numSectors) 
	{
		return getRandomAngleInSector(sectorIdx, numSectors, Angle.MAX_ANGLE);
	}
	
	protected Angle getRandomAngleInSector(double sectorIdx, double numSectors, double maxAngle) 
	{
		double from = angProbInfo.getAngles().get((int) sectorIdx);
		double to = Math.min(from + 1.0 / numSectors * 2.0 * Math.PI, maxAngle);
		double angle = generator.nextDoubleFromTo(from, to);
		return new Angle(angle);
	}
	
	protected double getEarthMoversDistance(RealVector probabilities)
	{
		// Earth mover's distance in 1D for pdf is difference in cdf (Cohen and Guibas 1997)
		// but for circular is ||F - G - mu||1, mu = median{F(i) - G(i), 0 <= i <= N-1} (Rabin et al 2011 eq. 11)
		int n = probabilities.getDimension();
		double deltaX = 1.0 / n;
		RealVector probCdf = new ArrayRealVector(n);
		RealVector uniformCdf = new ArrayRealVector(n);
		DescriptiveStatistics probMinusUniformCdf = new DescriptiveStatistics();
		double work = 0;
		
		// calculate cdf's
		probCdf.setEntry(0, probabilities.getEntry(0));
		uniformCdf.setEntry(0, deltaX);
		for (int i = 1; i < n; i++)
		{
			probCdf.setEntry(i, probCdf.getEntry(i - 1) + probabilities.getEntry(i));
			uniformCdf.setEntry(i, (i + 1) * deltaX);
			probMinusUniformCdf.addValue(probCdf.getEntry(i) - uniformCdf.getEntry(i));
		}
		
		double mu = probMinusUniformCdf.getPercentile(50); // median
		
		for (int i = 0; i < n; i++)
		{
			work += FastMath.abs(probCdf.getEntry(i) - uniformCdf.getEntry(i) - mu) * deltaX;
		}
		
		return work;
	}

	/**
	 * Generates 2D probability map the same size as the landscape that is used for probabilistically picking
	 *  a destination.
	 * @param currentLocation the animal's location
	 * @return 2D probability map
	 */
	protected abstract RealMatrix getProbabilities(NdPoint currentLocation);
	
	/**
	 * Generates a probability distribution of angles to use as a heading, which are discretized according to 
	 * AngularProbabilityInfo 
	 * @param currentLocation the animal's location
	 * @return vector of probabilities for angles in [-pi, pi)
	 */
	protected abstract RealVector getAngularProbabilities(NdPoint currentLocation);
	
	/**
	 * Normalizes matrix so that it sums to one. Checks for no memory (i.e. if all memory values are 0 or negative,
	 * then exponentiating gives 1 or smaller, so sum is less than number of cells.
	 * @param matrix the matrix to normalize
	 * @return matrix if the matrix was normalized, null if check fails and matrix wasn't normalized
	 */
	protected RealMatrix normalizeMatrix(RealMatrix matrix)
	{
		RealMatrix normalizedMatrix = null;
		
		// check if there is no memory, if memory values are 0 then each cell is 1
		// so this checks if all memory values are 0 or negative
		double sum = MatrixUtils.sum(matrix);
		if (sum > matrix.getColumnDimension() * matrix.getRowDimension())
		{
			// normalize to 1
			normalizedMatrix = matrix.scalarMultiply(1.0 / sum); //TODO faster to do in place somehow?
		}
		return normalizedMatrix;
	}

	protected boolean normalizeVector(RealVector vector)
	{
		// true = normalized, false = was all zeros so uniform
		return MatrixUtils.normalize(vector);
	}
	
	protected RealVector getExponentialDistanceFactor(double memorySpatialScale)
	{
		// value at location z = M(z) * exp(-distance(z, loc) / gamma.Z)
		// Here compute distance factor (the exponential part)
		return getExponentialDistanceFactor(memorySpatialScale, 0.0);
	}

	protected RealVector getExponentialDistanceFactor(double memorySpatialScale, double encounterRadius)
	{
		// value at location z = M(z) * exp(-(distance(z, loc) - encounterRadius) / gamma.Z) / gamma.Z
		// Here compute distance factor (the exponential part)
		RealVector distanceFactor = angProbInfo.getSampleDistances();
		distanceFactor.mapSubtractToSelf(encounterRadius);
		distanceFactor.mapDivideToSelf(-memorySpatialScale);
		distanceFactor.mapToSelf(new Exp());
		distanceFactor.mapDivideToSelf(memorySpatialScale);
		
		// now multiply by spacing (e.g. delta x) since we are integrating (i.e. summing rectangles)
		distanceFactor.mapMultiplyToSelf(angProbInfo.getSpacing());
		
		return distanceFactor;
	}
	
	protected RealVector getTopHatDistanceFactor(double memorySpatialScale, double radius)
	{
		// value at location z = M(z) * exp(-(distance(z, loc) - encounterRadius) / gamma.Z)
		// Here compute distance factor (the exponential part)
		RealVector distanceFactor = angProbInfo.getSampleDistances();
		// to be a pdf, need to 
		distanceFactor.mapToSelf(new StepFunction(new double[] {0, radius}, 
												  new double[] {1.0 / Math.PI / radius / radius, 0}));
		
		// now multiply by spacing (e.g. delta x) since we are integrating (i.e. summing rectangles)
		distanceFactor.mapMultiplyToSelf(angProbInfo.getSpacing());

		return distanceFactor;
	}


	@Override
	public String toString()
	{
		return ToStringBuilder.reflectionToString(this.reportCurrentState());
	}

}