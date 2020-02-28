package ForagingModel.space;

import java.util.List;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import ForagingModel.core.EnsurePositive;
import ForagingModel.core.GridPoint;
import ForagingModel.core.MatrixUtils;
import ForagingModel.core.NdPoint;

public class ResourceMemory extends AbstractMemory implements MemoryAssemblage
{
	private RealMatrix shortMemories;
	private RealMatrix longMemories;
	private ResourceAssemblage resourceAssemblage;
	
	private double shortLearningRate;
	private double longLearningRate;
	private double shortSpatialScale;
	private double longSpatialScale;
	private double shortDecayRate;
	private double longDecayRate;
	private double shortMemoryFactor;
	private double alpha;
	private double memorySpatialScale;
	private double intervalSize;
	
	private int rows; 
	private int columns;
	private RealVector distanceFactor;
	private double avgAngularProbTransect;

	protected ResourceMemory(RealMatrix shortMemories, RealMatrix longMemories, 
						   ResourceAssemblage resources, AngularProbabilityInfo angularProbabilityInfo,
	                       double shortLearningRate, double longLearningRate, double shortSpatialScale, double longSpatialScale, 
	                       double shortDecayRate, double longDecayRate, double shortMemoryFactor, 
	                       double alpha, double memorySpatialScale, double intervalSize)
	{
		super(angularProbabilityInfo);
		this.shortMemories = shortMemories;
		this.longMemories = longMemories;
		this.resourceAssemblage = resources;
		
		this.shortLearningRate = shortLearningRate;
		this.longLearningRate = longLearningRate;
		this.shortSpatialScale = shortSpatialScale;
		this.longSpatialScale = longSpatialScale;
		this.shortDecayRate = shortDecayRate;
		this.longDecayRate = longDecayRate;
		this.shortMemoryFactor = shortMemoryFactor;
		this.alpha = alpha;
		this.memorySpatialScale = memorySpatialScale;
		this.intervalSize = intervalSize;
		
		rows =  longMemories.getRowDimension();
		columns = longMemories.getColumnDimension();
		assert(shortMemories.getRowDimension() == rows);
		assert(shortMemories.getColumnDimension() == columns);
		
		distanceFactor = getExponentialDistanceFactor(memorySpatialScale);
		avgAngularProbTransect = getAverageAngularProbabilityTransect();
	}
	
	protected ResourceMemory(RealMatrix longMemories, ResourceAssemblage resources, AngularProbabilityInfo angularProbabilityInfo,
            double shortLearningRate, double longLearningRate, double shortSpatialScale, double longSpatialScale, 
            double shortDecayRate, double longDecayRate, double shortMemoryFactor, 
            double alpha, double memorySpatialScale, double intervalSize)
	{
		this(new Array2DRowRealMatrix(longMemories.getRowDimension(), longMemories.getColumnDimension()), 
				longMemories, resources, angularProbabilityInfo,
				shortLearningRate, longLearningRate, shortSpatialScale, longSpatialScale, 
				shortDecayRate, longDecayRate, shortMemoryFactor, alpha, memorySpatialScale, intervalSize);
	}

	@Override
	public void learn(NdPoint consumerLocation)
	{
		for (int row = 0; row < rows; row++)
		{
			for(int column = 0; column < columns; column++)
			{
				GridPoint location = new GridPoint(row, column);
				double distance = SpaceUtils.getDistance(consumerLocation, location);
				double quality = resourceAssemblage.getIntrinsicQuality(location);
				learn(row, column, distance, quality);
			}
		}
	}

	@Override
	protected RealMatrix getProbabilities(NdPoint currentLocation)
	{
		RealMatrix probabilities = new Array2DRowRealMatrix(rows, columns);
		GridPoint currentPoint = SpaceUtils.getGridPoint(currentLocation);
		 
		for (int row = 0; row < rows; row++)
		{
			for(int column = 0; column < columns; column++)
			{
				// myMods <- M*exp(-Dmatrix/gamma.Z)
				// myMods <- exp(alpha * myMods)
				double memory = getMemoryValue(row, column);
				double dist = SpaceUtils.getDistance(currentLocation, new GridPoint(row, column));
				double prob = Math.exp(alpha * memory * Math.exp(-dist / memorySpatialScale));
				if ( currentPoint.getX() == row && currentPoint.getY() == column )
				{
					// make current grid square 0, otherwise it will have high prob due to being close and direction of center is arbitrary
					// note that this is 1 not 0 since R code sets to 0 before multiplying by alpha and exp(0)=1
					prob = 1;
				}
				if (prob > MAX_PROBABILITY_VALUE) // e.g. infinity
				{
					// if memory is large (i.e. for example very concentrated patch) and nearby, the exponential can overflow and return infinity
					prob = MAX_PROBABILITY_VALUE;
				}
				probabilities.setEntry(row, column, prob);
			}
		}

		// can be set to null to avoid choosing a destination
		probabilities = normalizeMatrix(probabilities);

		return probabilities;
	}
	
	@Override
	protected RealVector getAngularProbabilities(NdPoint currentLocation) 
	{
		List<Double> angles = angProbInfo.getAngles();
		RealVector probs = new ArrayRealVector(angles.size());

		for (int angleIdx = 0; angleIdx < angles.size(); angleIdx++)
		{
			List<GridPoint> samplePoints = angProbInfo.getSamplePoints(currentLocation, angles.get(angleIdx));
			RealVector sampleValues = new ArrayRealVector(samplePoints.size());
			
			// *** WARNING: formula is also used in getAverageAngularProbabilityTransect() ***
			// *** make sure to change in both places ***
			// WAS: // value at location z = M(z) * exp(-distance(z, loc) * gamma.Z) 
			// but this is wrong, should be  z = M(z) * exp(-distance(z, loc) / gamma.Z) / gamma.Z
			// now multiply by memory value, M(z) * distanceFactor
			for (int pointIdx = 0; pointIdx < samplePoints.size(); pointIdx++)
			{
				GridPoint point = samplePoints.get(pointIdx);
				sampleValues.setEntry(pointIdx, 
						getMemoryValue(point.getX(), point.getY()) * distanceFactor.getEntry(pointIdx));
			}
			
			// subtract average quality transect, 
			// so values will be positive if better than average and negative if worse than average
			// try not doing this
			probs.setEntry(angleIdx, MatrixUtils.sum(sampleValues)); // - avgAngularProbTransect);
		}

		// ensure positive and not all 0
		probs.mapToSelf(new EnsurePositive());
		assert(probs.getMinValue() >= 0);
		
		double sum = MatrixUtils.sum(probs);

		normalizeVector(probs);
		probabilityCache.updateForaging(probs, sum);
		
		return probs;
	}

	protected void learn(int row, int column, double distance, double quality) 
	{
		double shortMemory = shortMemories.getEntry(row, column);
		double longMemory = longMemories.getEntry(row, column);
		
		// dL <- beta.L * exp(-Dmatrix^2/gamma.L) / (2 * pi * gamma.L)*(Q-L) 
		// dS <- beta.S * exp(-Dmatrix^2/gamma.S) / (2 * pi * gamma.S)*(Q-S) 
		double shortLearnAmount = shortLearningRate * Math.exp(-distance * distance / shortSpatialScale) / (2 * Math.PI * shortSpatialScale) * (quality - shortMemory) * intervalSize;
		double longLearnAmount = longLearningRate * Math.exp(-distance * distance / longSpatialScale) / (2 * Math.PI * longSpatialScale) * (quality - longMemory) * intervalSize;
		
		shortMemories.setEntry(row, column, shortMemory + shortLearnAmount);
		longMemories.setEntry(row, column, longMemory + longLearnAmount);
	}
	
	protected double getMemoryValue(int row, int column)
	{
		return longMemories.getEntry(row, column) - shortMemoryFactor * shortMemories.getEntry(row, column);
	}
	
	protected RealMatrix getLongMemory()
	{
		return longMemories;
	}

	protected RealMatrix getShortMemory()
	{
		return shortMemories;
	}

	protected RealMatrix getMemory()
	{
		// longMemories - shortMemoryFactor * shortMemories
		RealMatrix memory = longMemories.subtract(shortMemories.scalarMultiply(shortMemoryFactor));
		return memory;
	}

	protected void decay() 
	{
		for (int row = 0; row < rows; row++)
		{
			for (int column = 0; column < columns; column++)
			{
				double shortMemory = shortMemories.getEntry(row, column);
				double longMemory = longMemories.getEntry(row, column);

				// + phi.L*(Q.bar-L)
				// + phi.S*(Q.bar-S)
				double shortDecayAmount = shortDecayRate * shortMemory * intervalSize;
				double longDecayAmount = longDecayRate * longMemory * intervalSize;
				
				shortMemories.setEntry(row, column, shortMemory - shortDecayAmount);
				longMemories.setEntry(row, column, longMemory - longDecayAmount);
			}
		}
	}
	
	@Override
	public double[][] reportCurrentState() 
	{
		return getMemory().getData();
	}
	
	@Override
	public void execute(int currentInterval, int priority) 
	{
		decay();
	}
	
	private double getAverageAngularProbabilityTransect()
	{
		RealVector sampleValues = new ArrayRealVector(distanceFactor.getDimension());
		double avgQuality = resourceAssemblage.getAverageQuality();
		
		// *** WARNING: formula is also used in getAngularProbabilities() ***
		// *** make sure to change in both places ***
		// now multiply by memory value, M(z) * distanceFactor
		for (int pointIdx = 0; pointIdx < distanceFactor.getDimension(); pointIdx++)
		{
			sampleValues.setEntry(pointIdx, avgQuality * distanceFactor.getEntry(pointIdx));
		}
		return MatrixUtils.sum(sampleValues);
	}
	
	
}
