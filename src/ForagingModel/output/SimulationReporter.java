package ForagingModel.output;

import ForagingModel.schedule.Schedulable;

public interface SimulationReporter extends Schedulable
{
	public void reportSummaryResults();
}
