package ForagingModel.agent;

import java.util.ArrayList;
import java.util.List;

import ForagingModel.agent.movement.MovementBehavior;
import ForagingModel.agent.movement.MovementFactory;
import ForagingModel.core.ModelEnvironment;
import ForagingModel.core.NdPoint;
import ForagingModel.core.Parameters;
import ForagingModel.predator.PredatorManager;
import ForagingModel.schedule.SchedulePriority;
import ForagingModel.schedule.Scheduler;
import ForagingModel.space.LocationManager;
import ForagingModel.space.ResourceAssemblage;

public class AgentFactory 
{

	public static Forager createAndPlaceForager(LocationManager space, ResourceAssemblage resource, 
			PredatorManager predatorManager, NdPoint startingLocation, Scheduler scheduler)
	{
		Parameters params = Parameters.get();
		double consumptionRate = params.getConsumptionRate();
		double consumptionSpatialScale = params.getConsumptionSpatialScale();
		double averageConsumption = resource.calculateAvgConsumptionRate(consumptionRate, consumptionSpatialScale);
		int numIntervals = params.getNumIntervals();
		int numBurnInIntervals = params.getNumBurnInIntervals();
		double intervalSize = params.getIntervalSize();

		Recorder recorder = AgentFactory.createRecorder(numIntervals, numBurnInIntervals, resource.getNumPercentileBins(), 
										 intervalSize, predatorManager); // need 1 per forager
		Forager forager = createForager(space, resource, 
				MovementFactory.createMovement(resource, predatorManager, averageConsumption, space, startingLocation, 
						scheduler, recorder), 
				recorder, 
				averageConsumption, consumptionRate, consumptionSpatialScale);
		space.moveTo(forager, startingLocation);
		scheduler.register(forager, SchedulePriority.ForagerMove); // use move for now for both move and consume
		
		return forager;
	}
	
	public static List<Forager> createAndPlaceForagers(int numForagers, LocationManager space, ResourceAssemblage resource, 
			PredatorManager predatorManager, NdPoint startingLocation, Scheduler scheduler)
	{
		List<Forager> foragers = new ArrayList<Forager>(numForagers);
		
		for (int i = 0; i < numForagers; i++)
		{
			foragers.add(createAndPlaceForager(space, resource, predatorManager, startingLocation, scheduler));
		}
		return foragers;
	}
	
	public static Recorder createRecorder(int numIterations, int numBurnInIterations, int numQualityBins, double intervalSize,
			PredatorManager predManager)
	{
		return new Recorder(numIterations, numBurnInIterations, numQualityBins, intervalSize, predManager);
	}
 
	protected static Forager createForager(LocationManager space, 
			ResourceAssemblage resource, MovementBehavior movement, Recorder recorder,
			double averageConsumption, double consumptionRate, double consumptionSpatialScale)
	{
		Forager forager = new Forager(space, resource, movement, recorder, 
				averageConsumption, consumptionRate, consumptionSpatialScale);
		ModelEnvironment.getMovementMapper().register(movement, forager);
		
		return forager;
	}
	
	
}
