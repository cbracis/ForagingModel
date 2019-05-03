package ForagingModel.schedule;

public interface Scheduler {

	public abstract void register(Notifiable notifiable);

	public abstract void register(Schedulable schedulable, int priority);

	public abstract void register(Schedulable schedulable,
			SchedulePriority priority);

	public abstract void registerAtEnd(Schedulable schedulable, int priority);

	public abstract void registerAtEnd(Schedulable schedulable,
			SchedulePriority priority);

	public abstract void run(double intervalSize, int numSteps);

	public abstract void abortRun();

}