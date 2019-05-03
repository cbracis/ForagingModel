package ForagingModel.predator;

import java.util.List;

import ForagingModel.core.Parameters;
import ForagingModel.schedule.Scheduler;
import ForagingModel.space.ResourceAssemblage;

public class PredatorFactory 
{
	public static PredatorManager createPredatorManager(ResourceAssemblage unboarderedResources, Scheduler scheduler)
	{
		PredatorGenerator generator = PredatorFactory.createPredatorGenerator(unboarderedResources);
		PredatorManager manager = generator.generatePredators();
		scheduler.register(manager);
		return manager;
	}
	
	public static PredatorManager createPredatorManager(List<Predator> predators)
	{
		return new PredatorManagerImpl(predators);
	}
	
	protected static PredatorGenerator createPredatorGenerator(ResourceAssemblage unboarderedResources)
	{
		Parameters params = Parameters.get();
		String resourceId = params.getResourceId();
		int predatorDuration = params.getPredatorDuration();
		double predatorRandomness = params.getPredatorRandomness();
		int totalPredationPressure = params.getTotalPredationPressure();
		return new PredatorGenerator(unboarderedResources, resourceId, predatorDuration, predatorRandomness, totalPredationPressure);
	}

	protected static PredatorGenerator createPredatorGenerator(ResourceAssemblage unboarderedResources, String resourceId,
			int predatorDuration, double predatorRandomness, int totalPredationPressure)
	{
		return new PredatorGenerator(unboarderedResources, resourceId, predatorDuration, predatorRandomness, totalPredationPressure);
	}
}
