package ForagingModel.core;

import java.io.File;
import java.util.List;

import ForagingModel.agent.Agent;
import ForagingModel.agent.Agent.Sex;
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
import ForagingModel.space.ScentHistory;
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
			
			// load resources data from file (may be null if no file)
			List<CellData> resourceData = reader.readLandscapeFile(params.getResourceLandscapeFile(), params.getEmptyBorderSize());
			
			// create resources, sets landscape size
			ResourceAssemblage resources = (resourceData == null) ? SpaceFactory.generateTwoPatchResource(scheduler) // not possible to have empty border
																  : SpaceFactory.generateResource(resourceData, scheduler);
			
			if (params.getForagerBySex())
			{
				// need to create foragers and scent tracking by sex
				ScentManager femaleScentManager = null;
				ScentManager maleScentManager = null;
				ScentHistory allFemales = null;
				
				if (params.getScentTracking())
				{
					femaleScentManager = SpaceFactory.createScentManager(locationManager, params.getNumThreads());
					scheduler.register(femaleScentManager, SchedulePriority.ForagerDepositScent);
					scheduler.registerAtEnd(femaleScentManager, SchedulePriority.Shutdown);
					
					allFemales = SpaceFactory.createAllFemalesScentHistory(scheduler, femaleScentManager);
					
					maleScentManager = SpaceFactory.createScentManager(locationManager, params.getNumThreads());
					scheduler.register(maleScentManager, SchedulePriority.ForagerDepositScent);
					scheduler.registerAtEnd(maleScentManager, SchedulePriority.Shutdown);
				}
				
				// forager start at center or randomly depending on StartPointType
				int numFemaleForagers = params.getForagerNumberFemale();
				int numMaleForagers = params.getForagerNumberMale();
				
				// first create female foragers that only avoid each other
				AgentFactory.createAndPlaceForagers(numFemaleForagers, locationManager, 
						Sex.Female, resources, predatorManager, 
						femaleScentManager, null, scheduler);
				
				// then create male foragers that need to be attracted to female foragers too
				AgentFactory.createAndPlaceForagers(numMaleForagers, locationManager, 
						Sex.Male, resources, predatorManager, 
						maleScentManager, allFemales, scheduler);


			}
			else
			{
				// optionally track scent if enabled
				ScentManager scentManager = null;
				if (params.getScentTracking())
				{
					scentManager = SpaceFactory.createScentManager(locationManager, params.getNumThreads());
					scheduler.register(scentManager, SchedulePriority.ForagerDepositScent);
					scheduler.registerAtEnd(scentManager, SchedulePriority.Shutdown);
				}
				
				// forager start at center or randomly depending on StartPointType
				int numForagers = params.getForagerNumber();
				AgentFactory.createAndPlaceForagers(numForagers, locationManager, 
						Sex.Unknown, resources, predatorManager, 
						scentManager, null, scheduler);
			}
			
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
