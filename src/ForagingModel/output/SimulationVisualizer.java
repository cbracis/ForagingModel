package ForagingModel.output;

import ForagingModel.schedule.Schedulable;

public interface SimulationVisualizer extends Schedulable 
{
	void plotIteration(int iteration);
}
