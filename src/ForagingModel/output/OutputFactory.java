package ForagingModel.output;

import java.io.File;
import java.util.List;

import ForagingModel.agent.Agent;
import ForagingModel.core.Parameters;
import ForagingModel.predator.PredatorManager;
import ForagingModel.schedule.SchedulePriority;
import ForagingModel.schedule.Scheduler;
import ForagingModel.space.LocationManager;
import ForagingModel.space.ResourceAssemblage;

public class OutputFactory 
{
	public static SimulationReporter createSimulationReporter(File results, List<? extends Agent> agents, int numQualityBins, Scheduler scheduler)
	{
		SimulationReporter reporter = new SimulationFileWriter(results, agents, numQualityBins);
		scheduler.registerAtEnd(reporter, SchedulePriority.Last);
		return reporter;
	}

	public static SimulationVisualizer createSimulationVisualizer(ResourceAssemblage resources, 
			LocationManager locationManager, Scheduler scheduler) 
	{
		SimulationVisualizer visualizer = new RVisualizer(resources, locationManager, Parameters.get().getBurnInSteps());
		scheduler.register(visualizer, SchedulePriority.Visualize);
		// skip this for now since it's slow and frequently fails
		// scheduler.registerAtEnd(OutputFactory.createMovieCreator(), SchedulePriority.Last);
		return visualizer;
	}
	
	public static SimulationVisualizer createSimulationVisualizer(ResourceAssemblage resources,
			LocationManager locationManager, PredatorManager predatorManager, Scheduler scheduler) 
	{
		SimulationVisualizer visualizer =  new RVisualizer(resources, locationManager, predatorManager, Parameters.get().getBurnInSteps());
		scheduler.register(visualizer, SchedulePriority.Visualize);
		// skip this for now since it's slow and frequently fails
		// scheduler.registerAtEnd(OutputFactory.createMovieCreator(), SchedulePriority.Last);
		return visualizer;
	}

	public static MovieCreator createMovieCreator()
	{
		return new MovieCreator();
	}

}
