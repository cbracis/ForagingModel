package ForagingModel.schedule;

public class NoOpScheduler implements Scheduler 
{

	@Override
	public void register(Notifiable notifiable) {}

	@Override
	public void register(Schedulable schedulable, int priority) {}

	@Override
	public void register(Schedulable schedulable, SchedulePriority priority) {}

	@Override
	public void registerAtEnd(Schedulable schedulable, int priority) {}

	@Override
	public void registerAtEnd(Schedulable schedulable, SchedulePriority priority) {}

	@Override
	public void run(double intervalSize, int numSteps) {}

	@Override
	public void abortRun() {}

}
