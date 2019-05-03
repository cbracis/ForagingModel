package ForagingModel.agent;

import java.util.List;

import ForagingModel.agent.movement.BehaviorState;
import ForagingModel.core.NdPoint;
import ForagingModel.core.Velocity;
import ForagingModel.predator.Predator;


public interface Reporter 
{
	public enum SummaryMetric { TotalConsumption, StdDevConsumption, 
		DistanceTraveled, Sinuosity, AverageSpeed, AverageSpeedSearch, AverageSpeedFeeding,
		MeanSinTurningAngle, MeanCosTurningAngle,
		TimeSearching, MemorySearchPercentage, TotalPredatorEncounters, PredatorReEncounters,
		ExecutionTime}

	public abstract double getTotalConsumption();

	public abstract double getSdConsumption();

	public abstract double getDistanceTraveled();

	public abstract double getSinuosity();

	public abstract double getAverageSpeed();

	public abstract double getAverageSpeedSearch();

	public abstract double getAverageSpeedFeeding();

	/**
	 * The directional bias of movement direction
	 * @return mean sine of turning angle
	 */
	public abstract double getMeanSinTurningAngle();

	/**
	 * The correlation of movement direction
	 * @return mean cosine of turning angle
	 */
	public abstract double getMeanCosTurningAngle();

	public abstract double getTimeSearching();

	public abstract double getSearchMemoryUsage();

	public abstract int getPredatorEncounters();

	public abstract int getPredatorReEncounters();
	
	public abstract List<Predator> getAllPredators();
	
	public int[] getQualityBins();

	public abstract long getExecutionTime();

	public abstract List<NdPoint> getLocationHistory();

	public abstract List<BehaviorState> getStateHistory();

	public abstract List<Double> getConsumptionHistory();

	public abstract List<Boolean> getMemoryUsageHistory();

	public abstract List<Velocity> getMuHistory();

	public abstract double getSummaryMetic(SummaryMetric metric);

}