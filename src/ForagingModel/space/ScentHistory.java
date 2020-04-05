package ForagingModel.space;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.util.FastMath;

import ForagingModel.core.GridPoint;
import ForagingModel.core.MatrixUtils;
import ForagingModel.core.NdPoint;

/**
 * This class implements a scent map. That is, a record of scent-leaving by all other
 * foragers that is repulsive. It is thus more of a physical memory than a cognitive
 * memory of the individual, but can take advantage of the existing memory machinery.
 * It is modeled closely on PredatorMemory.
 */
public class ScentHistory extends AbstractMemory implements MemoryAssemblage 
{
	private RealMatrix scentMatrix;

	private double depositionRate; 
	private double depositionSpatialScale;
	private double decayRate;
	private double scentResponseFactor; // what to multiply each angular value by so that threat is ~1 near predator and ~0 far away
	private double intervalSize;
	
	private int rows; 
	private int columns;
	private RealVector distanceFactor;
	
	private final double MAX_SCENT_VALUE = 1.0;

	protected ScentHistory(RealMatrix scentMatrix, 
			AngularProbabilityInfo angularProbabilityInfo,
			double depositionRate, double depositionSpatialScale, double decayRate, 
			double scentSpatialScale, double scentResponseFactor,
            double intervalSize)
	{
		super(angularProbabilityInfo);
		this.scentMatrix = scentMatrix;
		this.depositionRate = depositionRate;
		this.depositionSpatialScale = depositionSpatialScale;
		this.decayRate = decayRate;
		this.scentResponseFactor = scentResponseFactor;
		this.intervalSize = intervalSize;
		
		rows =  scentMatrix.getRowDimension();
		columns = scentMatrix.getColumnDimension();

		distanceFactor = getExponentialDistanceFactor(scentSpatialScale);
	}

	@Override
	public void execute(int currentInterval, int priority) 
	{
		decay();
	}

	@Override
	public void learn(NdPoint consumerLocation) 
	{
		// TODO use this for update or create a new interface used for updating?
		// predator memory uses predator manager to get predators nearby then learn from them
		// better might be to make a new method the Scent manager can call and pass in the 
		// locations of the other foragers
	}
	
	public void depositScent(Set<NdPoint> conspecificLocations)
	{
		for (int row = 0; row < rows; row++)
		{
			for(int column = 0; column < columns; column++)
			{
				GridPoint location = new GridPoint(row, column);
				List<Double> distances = new ArrayList<Double>(conspecificLocations.size());
				
				for (NdPoint conspecific : conspecificLocations)
				{
					distances.add(SpaceUtils.getDistance(conspecific, location));
					
				}
				depositScent(row, column, distances);
			}
		}

	}
	
	private void depositScent(int row, int column, List<Double> distances) 
	{
		double scent = scentMatrix.getEntry(row, column);
		

		// new deposit amount,D,  normal kernel, for each conspecific dist away 
		// dDL <- beta.D * exp(-dist^2/gamma.D) / (2 * pi * gamma.D)
		double newAmount = 0;
		for(Double distance : distances)
		{
			newAmount += depositionRate * Math.exp(-distance * distance / depositionSpatialScale) / 
					(2 * Math.PI * depositionSpatialScale);
		}
		// TODO need to check for max??
		scentMatrix.setEntry(row, column, scent + newAmount);
	}


	@Override
	public double[][] reportCurrentState() 
	{
		return scentMatrix.getData();
	}

	@Override
	protected RealMatrix getProbabilities(NdPoint currentLocation) 
	{
		// TODO needed? won't have just scent, will be part of combo with resource
		return null;
	}

	@Override
	protected RealVector getAngularProbabilities(NdPoint currentLocation) 
	{
		// TODO needed? shouldn't be called directly
		return null;
	}
	
	// This is called by AggregateScentMemory to get the safety values to multiply resource memory
	protected RealVector getConspecificSafety(NdPoint currentLocation) 
	{
		// TODO: could improve duplicated code from ResourceMemory and PredatorMemory?
		List<Double> angles = angProbInfo.getAngles();
		RealVector safety = new ArrayRealVector(angles.size());
		
		for (int angleIdx = 0; angleIdx < angles.size(); angleIdx++)
		{
			List<GridPoint> samplePoints = angProbInfo.getSamplePoints(currentLocation, angles.get(angleIdx));
			RealVector sampleValues = new ArrayRealVector(samplePoints.size());
			
			// value at location z = P(z) * exp(-distance(z, loc) * gamma.Z)
			
			// now multiply by memory value by distance factor (exponential part)
			for (int pointIdx = 0; pointIdx < samplePoints.size(); pointIdx++)
			{
				GridPoint point = samplePoints.get(pointIdx);
				sampleValues.setEntry(pointIdx, 
						scentMatrix.getEntry(point.getX(), point.getY()) * 
							distanceFactor.getEntry(pointIdx) * scentResponseFactor);
			}
			
			double value = MatrixUtils.sum(sampleValues); 			
			safety.setEntry(angleIdx, FastMath.min(value, MAX_SCENT_VALUE)); 
		}
		double sum = MatrixUtils.sum(safety);
		
		
		// this gives safety = 1 - threat (no longer a probability distribution)
		safety.mapMultiplyToSelf(-1).mapAddToSelf(1);
		probabilityCache.updatePredator(safety, sum);
		
		return safety;
	}

	protected void decay()
	{
		// new = old - old * decayRate * intervalSize
		double factor = 1.0 - decayRate * intervalSize;
		for (int row = 0; row < rows; row++)
		{
			for (int column = 0; column < columns; column++)
			{
				scentMatrix.setEntry(row, column, scentMatrix.getEntry(row, column) * factor );
			}
		}
	}


}