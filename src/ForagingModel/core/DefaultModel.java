package ForagingModel.core;

import ForagingModel.schedule.Scheduler;

public class DefaultModel implements Model 
{
	private Scheduler scheduler;
	private double intervalSize;
	private int numSteps;
	
	protected DefaultModel(Scheduler scheduler, double intervalSize, int numSteps)
	{
		this.scheduler = scheduler;
		this.intervalSize = intervalSize;
		this.numSteps = numSteps;
	}

	@Override
	public void run() 
	{
		scheduler.run(intervalSize, numSteps);
	}

}
