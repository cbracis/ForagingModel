package ForagingModel.space;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import ForagingModel.core.GridPoint;
import ForagingModel.core.MatrixUtils;
import ForagingModel.core.NdPoint;
import ForagingModel.core.Parameters;

public class ResourceMatrix implements ResourceAssemblage 
{
	private RealMatrix resources;
	
	private RealMatrix carryingCapacities; 
	private double regenerationRate;
	private double intervalSize;

	
	private int rows; 
	private int columns;
	private List<Double> percentiles;
	private final int numPercentiles; 

	// very small amount so doesn't get depleted to 0 and not able to regenerate
	private final double MIN_DEPLETED_STATE = 1e-100;

	protected ResourceMatrix(RealMatrix resources, RealMatrix carryingCapacity, double regenerationRate, double intervalSize)
	{
		this.resources = resources;
		this.carryingCapacities = carryingCapacity;
		this.regenerationRate = regenerationRate;
		this.intervalSize = intervalSize;
		
		rows =  resources.getRowDimension();
		columns = resources.getColumnDimension();
		
		Parameters.get().setLandscapeSizeX(rows);
		Parameters.get().setLandscapeSizeY(columns);
		
		// hard coded here!!
		numPercentiles = 4;
		calculatePercentiles();
	}
	
	protected ResourceMatrix(RealMatrix resources, double regenerationRate, double intervalSize)
	{
		this(resources, resources.copy(), regenerationRate, intervalSize);
	}


	@Override
	public double consumeResource(NdPoint consumerLocation,	double consumptionRate, double consumptionSpatialScale) 
	{
		double totalConsumed = 0;
		for (int row = 0; row < rows; row++)
		{
			for(int column = 0; column < columns; column++)
			{
				double currentQuality = resources.getEntry(row, column);
				double carryingCapacity = carryingCapacities.getEntry(row, column);
				GridPoint location = new GridPoint(row, column);
				double distance = SpaceUtils.getDistance(consumerLocation, location);
				double amount = depleteResource(currentQuality, carryingCapacity, distance, consumptionRate, consumptionSpatialScale);
				
				double newQuality = currentQuality - amount;
				resources.setEntry(row, column, newQuality);
				totalConsumed += amount;
			}
		}
		
		return totalConsumed;
	}

	@Override
	public double calculateAvgConsumptionRate(double consumptionRate, double consumptionSpatialScale) 
	{
		double averageQuality = getAverageQuality();
		
		// location in center of grid
		NdPoint location = new NdPoint(rows / 2, columns / 2);
		
		double avgConsumption = 0;
		for (int row = 0; row < rows; row++)
		{
			for(int column = 0; column < columns; column++)
			{
				GridPoint point = new GridPoint(row, column);
				double distance = SpaceUtils.getDistance(location, point);
				avgConsumption += depleteResource(averageQuality, carryingCapacities.getEntry(row, column), distance, consumptionRate, consumptionSpatialScale); 
			}
		}
		return avgConsumption;
	}

	@Override
	public double getIntrinsicQuality(GridPoint location)
	{
		return carryingCapacities.getEntry(location.getX(), location.getY());
	}

	@Override
	public RealMatrix getInitialMemory(boolean isFullyInformed, double initialValue) 
	{
		RealMatrix longMemories = isFullyInformed 
									? carryingCapacities.copy() 
									: MatrixUtils.createMatrix(rows, columns, initialValue);
		return longMemories;
	}

	protected double depleteResource(double currentQuality, double carryingCapacity, double distance, double consumptionRate, double consumptionSpatialScale)
	{
		// dQ <- beta.C * exp(-Dmatrix^2/gamma.C) / (2 * pi * gamma.C) * Q
		double depletionAmount = consumptionRate * Math.exp(-distance * distance /consumptionSpatialScale) 
				/ (2 * Math.PI * consumptionSpatialScale ) * currentQuality * intervalSize;
		if (carryingCapacity > 0 & currentQuality - depletionAmount < MIN_DEPLETED_STATE) // avoid depleting to 0 for non-0 patches
		{
			depletionAmount = currentQuality - MIN_DEPLETED_STATE;
		}
		return depletionAmount;
	}
	
	@Override
	public double[][] reportCurrentState() 
	{
		return resources.getData();
	}

	@Override
	public double getMaxQuality() 
	{
		return MatrixUtils.max(resources);
	}
	
	@Override
	public double getMaxQuality(int minDimX, int minDimY, int maxDimX, int maxDimY)
	{
		return MatrixUtils.max(resources.getSubMatrix(minDimX, maxDimX, minDimY, maxDimY));
	}
	
	@Override
	public double getAverageQuality() 
	{
		return MatrixUtils.sum(carryingCapacities) / (rows * columns);
	}

	public void growResource()
	{
		for (int row = 0; row < rows; row++)
		{
			for(int column = 0; column < columns; column++)
			{
				double carryingCapacity = carryingCapacities.getEntry(row, column);
				// logistic growth
				// dQ <- beta.R * (Q0-Q) / Q0 * Q
				if (carryingCapacity != 0)
				{
					double currentQuality = resources.getEntry(row, column);
					double growthAmount = regenerationRate * (carryingCapacity - currentQuality) / carryingCapacity * currentQuality * intervalSize;
					
					double newQuality = currentQuality + growthAmount;
					resources.setEntry(row, column, newQuality);
				}
			}
		}
	}
	
	// for testing
	protected RealMatrix getResourceMatrix()
	{
		return resources;
	}
	
	private void calculatePercentiles() 
	{
		// i.e. store percentiles for 10, 20, 30, 40, 50, 60, 70, 80, 90, 100
		percentiles = new ArrayList<Double>(numPercentiles);
		assert(100 % numPercentiles == 0);
		int increment = 100 / numPercentiles;
		
		DescriptiveStatistics stats = new DescriptiveStatistics();
		for (int row = 0; row < carryingCapacities.getRowDimension(); row++)
		{
			for (int col = 0; col < carryingCapacities.getColumnDimension(); col++)
			{
				double value = carryingCapacities.getEntry(row, col);
				// only add non-zero, track zeros separately
				if (value > 0)
				{
					stats.addValue(value);
				}
			}
		}
		
		// 0th bin is for all zero data, then percentiles of rest
		percentiles.add(0.0);
		
		for (int p = increment; p <= 100; p+=increment)
		{
			percentiles.add(stats.getPercentile(p));
		}
	}
	
	@Override
	public int getPercentileBin(NdPoint location) 
	{
		double quality = getIntrinsicQuality(SpaceUtils.getGridPoint(location));
		int bin = 0;
		
		for (int i = 0; i < percentiles.size(); i++)
		{
			if (quality <= percentiles.get(i))
			{
				bin = i;
				break;
			}
		}
		return bin;
	}

	@Override
	public int getNumPercentileBins() 
	{
		return numPercentiles + 1; // plus 1 for 0 data
	}

	@Override
	public void execute(int currentInterval) 
	{
		growResource();
	}


}
