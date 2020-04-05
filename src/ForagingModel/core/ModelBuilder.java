package ForagingModel.core;

import java.io.File;
import java.util.List;

import ForagingModel.agent.AgentFactory;
import ForagingModel.input.CellData;
import ForagingModel.input.InputFactory;
import ForagingModel.input.ResourceLandscapeReader;
import ForagingModel.output.OutputFactory;
import ForagingModel.predator.PredatorFactory;
import ForagingModel.predator.PredatorManager;
import ForagingModel.schedule.ScheduleFactory;
import ForagingModel.schedule.SchedulePriority;
import ForagingModel.schedule.Scheduler;
import ForagingModel.space.LocationManager;
import ForagingModel.space.ResourceAssemblage;
import ForagingModel.space.ScentManager;
import ForagingModel.space.SpaceFactory;

public class ModelBuilder 
{
	public Model build() 
	{
		Model model;
		Parameters params = Parameters.get();
		if (params.areParametersValid())
		{
			// reset MovementMapper before anything created to release any previous simulation
			ModelEnvironment.getMovementMapper().reset();
			
			// create scheduler
			Scheduler scheduler = ScheduleFactory.createScheduler();

			ResourceLandscapeReader reader = InputFactory.createResourceLandscapeReader();		
			
			// create predators (could be none)
			PredatorManager predatorManager = null;			
			if (params.getPredation())
			{
				// predators created first because need to use borderless resource file, and want landscape size to be correct
				List<CellData> resourceDataForPredGeneration = reader.readLandscapeFile(params.getResourceLandscapeFile(), 0); // 0 = no border
				ResourceAssemblage resourcesForPredGeneration = (resourceDataForPredGeneration == null) 
						? SpaceFactory.generateTwoPatchResource(ScheduleFactory.createNoOpScheduler()) // not possible to have empty border
						: SpaceFactory.generateResource(resourceDataForPredGeneration, ScheduleFactory.createNoOpScheduler());
				
				predatorManager = PredatorFactory.createPredatorManager(resourcesForPredGeneration, scheduler);
			}
			
			// to track forager
			LocationManager locationManager = SpaceFactory.createLocationManager(); 

			// optionally track scent if enabled
			ScentManager scentManager = null;
			if (params.getScentTracking())
			{
				scentManager = SpaceFactory.createScentManager(locationManager);
				scheduler.register(scentManager, SchedulePriority.ForagerDepositScent);
			}
			
			// load resources data from file (may be null if no file)
			List<CellData> resourceData = reader.readLandscapeFile(params.getResourceLandscapeFile(), params.getEmptyBorderSize());
			
			// create resources, sets landscape size
			ResourceAssemblage resources = (resourceData == null) ? SpaceFactory.generateTwoPatchResource(scheduler) // not possible to have empty border
																  : SpaceFactory.generateResource(resourceData, scheduler);
	
			
			// forager start at center or randomly depending on StartPointType
			int numForagers = params.getForagerNumber();
			AgentFactory.createAndPlaceForagers(numForagers, locationManager, resources, 
					predatorManager, scentManager, scheduler);
			
			// visualization
			if (params.getVisualizeSimulation())
			{
				OutputFactory.createSimulationVisualizer(resources, locationManager, predatorManager, scheduler);
			}
			
			File results = params.getResultsFile();
			OutputFactory.createSimulationReporter(results, locationManager.getAgents(), resources.getNumPercentileBins(), scheduler);
			
			model = new DefaultModel(scheduler, params.getIntervalSize(), params.getNumSteps());
		}
		else
		{
			model = new InvalidModel("Invalid parameter combo: " + params.toString());
		}
		return model;
	}

}
