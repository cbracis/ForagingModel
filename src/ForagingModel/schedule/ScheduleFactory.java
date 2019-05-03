package ForagingModel.schedule;

public class ScheduleFactory 
{
	public static Scheduler createScheduler()
	{
		return new SchedulerImpl();
	}
	
	public static Scheduler createNoOpScheduler()
	{
		return new NoOpScheduler();
	}

}
