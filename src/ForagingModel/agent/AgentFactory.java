package ForagingModel.agent;

import java.util.ArrayList;
import java.util.List;

import ForagingModel.agent.Agent.Sex;
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
import ForagingModel.space.ScentHistory;
import ForagingModel.space.ScentManager;

public class AgentFactory 
{

	public static Forager createAndPlaceForager(LocationManager space, 
			Sex sex, ResourceAssemblage resource, 
			PredatorManager predatorManager, 
			ScentManager scentManager, ScentHistory allFemales,
			NdPoint startingLocation, Scheduler scheduler)
	{
		Parameters params = Parameters.get();
		double consumptionRate = params.getConsumptionRate();
		double consumptionSpatialScale = params.getConsumptionSpatialScale();
		double averageConsumption = resource.calculateAvgConsumptionRate(consumptionRate, consumptionSpatialScale);
		int numIntervals = params.getNumIntervals();
		int numBurnInIntervals = params.getNumBurnInIntervals();
		int lifespan = params.getForagerLifespan();
		double intervalSize = params.getIntervalSize();

		Recorder recorder = AgentFactory.createRecorder(numIntervals, numBurnInIntervals, resource.getNumPercentileBins(), 
										 intervalSize, predatorManager); // need 1 per forager
		Forager forager = createForager(space, sex, lifespan, resource, 
				MovementFactory.createMovement(resource, predatorManager, 
						scentManager, allFemales,
						averageConsumption, space, startingLocation, 
						scheduler, recorder), 
				recorder, 
				averageConsumption, consumptionRate, consumptionSpatialScale);
		
		space.moveTo(forager, startingLocation);
		if (null != scentManager)
		{
			scentManager.add(forager);
		}
		scheduler.register(forager, SchedulePriority.ForagerMove); 
		scheduler.register(forager, SchedulePriority.ForagerConsume); 
		
		return forager;
	}
		
	public static List<Forager> createAndPlaceForagers(int numForagers, LocationManager space, 
			Sex sex, ResourceAssemblage resource, 
			PredatorManager predatorManager, ScentManager scentManager, ScentHistory allFemales,
			Scheduler scheduler)
	{
		List<Forager> foragers = new ArrayList<Forager>(numForagers);
		
		for (int i = 0; i < numForagers; i++)
		{
			NdPoint startingLocation = Parameters.get().getStartingLocation();
			foragers.add(createAndPlaceForager(space, sex, resource, predatorManager, 
					scentManager, allFemales,
					startingLocation, scheduler));
		}
		return foragers;
	}
		
	public static Recorder createRecorder(int numIterations, int numBurnInIterations, int numQualityBins, double intervalSize,
			PredatorManager predManager)
	{
		return new Recorder(numIterations, numBurnInIterations, numQualityBins, intervalSize, predManager);
	}
 
	protected static Forager createForager(LocationManager space, Sex sex, int lifespan,
			ResourceAssemblage resource, MovementBehavior movement, Recorder recorder,
			double averageConsumption, double consumptionRate, double consumptionSpatialScale)
	{
		Forager forager = new Forager(space, sex, lifespan, resource, movement, recorder, 
				averageConsumption, consumptionRate, consumptionSpatialScale);
		ModelEnvironment.getMovementMapper().register(movement, forager);
		
		return forager;
	}
	
	
}
