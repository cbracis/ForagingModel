package ForagingModel.space;

import org.apache.commons.math3.linear.RealMatrix;

import ForagingModel.core.GridPoint;
import ForagingModel.core.NdPoint;
import ForagingModel.schedule.Schedulable;

public interface ResourceAssemblage extends Schedulable
{
	/**
	 * Called by consumer to consume resources surrounding its location
	 * @param consumerLocation location of the consumer
	 * @param consumptionRate multiplicative factor for consumption
	 * @param consumptionSpatialScale parameter for how concentrated/spread out the consumption is around the consumer's location
	 * @return total amount of resources consumed
	 */
	double consumeResource(NdPoint consumerLocation, double consumptionRate, double consumptionSpatialScale);
	
	/**
	 * Calculates the average consumption rate of the consumer (if the resources were evenly distributed)
	 * @param consumptionRate multiplicative factor for consumption
	 * @param consumptionSpatialScale parameter for how concentrated/spread out the consumption is around the consumer's location
	 * @return average consumption rate
	 */
	double calculateAvgConsumptionRate(double consumptionRate, double consumptionSpatialScale);
	
	/**
	 * Gets the corresponding initial memory values for this ResourceAssemblage
	 * @param isFullyInformed whether the memory is initialized with the resource values or specified value
	 * @param initialValue the value to initialize memory with if not fully informed
	 * @return the matrix of initial memory values
	 */
	RealMatrix getInitialMemory(boolean isFullyInformed, double initialValue);

	/**
	 * Gets the intrinsic quality (carrying capacity) at the specified location.
	 * @param location the location on the grid
	 * @return intrinsic quality
	 */
	double getIntrinsicQuality(GridPoint location);

	/**
	 * Returns the current state of the resource matrix for plotting or reporting
	 * @return matrix of resource values
	 */
	double[][] reportCurrentState();

	/**
	 * Returns the maximum intrinsic (not current) quality in this ResourceAssemblage.
	 * @return the maximum quality
	 */
	double getMaxQuality();
	
	/**
	 * Returns the maximum intrinsic (not current) quality in this ResourceAssemblage in the specified rectangle.
	 * @param minDimX lower left dimension of rectangle
	 * @param minDimY lower left dimension of rectangle
	 * @param maxDimX upper right dimension of rectangle
	 * @param maxDimY upper right dimension of rectangle
	 * @return the maximum quality
	 */
	double getMaxQuality(int minDimX, int minDimY, int maxDimX, int maxDimY);


	/**
	 * Returns the average intrinsic (not current) quality in this ResourceAssemblage.
	 * @return the average quality
	 */
	double getAverageQuality();
	
	/**
	 * Returns the bin (0 to numPercentileBins -1) that the intrinsic quality at the location is in.
	 * For example, if there were 10 bins, 0 would mean the location is in the 0-10% quality bin
	 * @param location location to determine percentile of intrinsic quality
	 * @return percentile bins
	 */
	int getPercentileBin(NdPoint location);
	
	/**
	 * How many percentile bins there are.
	 * @return Number of bins
	 */
	int getNumPercentileBins();
}
