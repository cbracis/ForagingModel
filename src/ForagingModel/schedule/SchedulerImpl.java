package ForagingModel.schedule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class SchedulerImpl implements Scheduler
{
	private List<Notifiable> registeredNotifiables;
	private Map<Integer, List<Schedulable>> registeredSchedulables;
	private Map<Integer, List<Schedulable>> registeredAtEndSchedulables;

	protected SchedulerImpl()
	{
		reset();
	}
	
	/* (non-Javadoc)
	 * @see ForagingModel.schedule.Scheduler#register(ForagingModel.schedule.Notifiable)
	 */
	@Override
	public void register(Notifiable notifiable)
	{
		registeredNotifiables.add(notifiable);
	}

	/* (non-Javadoc)
	 * @see ForagingModel.schedule.Scheduler#register(ForagingModel.schedule.Schedulable, int)
	 */
	@Override
	public void register(Schedulable schedulable, int priority)
	{
		register(schedulable, priority, registeredSchedulables);
	}
	
	/* (non-Javadoc)
	 * @see ForagingModel.schedule.Scheduler#register(ForagingModel.schedule.Schedulable, ForagingModel.schedule.SchedulePriority)
	 */
	@Override
	public void register(Schedulable schedulable, SchedulePriority priority)
	{
		register(schedulable, priority.value());
	}

	/* (non-Javadoc)
	 * @see ForagingModel.schedule.Scheduler#registerAtEnd(ForagingModel.schedule.Schedulable, int)
	 */
	@Override
	public void registerAtEnd(Schedulable schedulable, int priority)
	{
		register(schedulable, priority, registeredAtEndSchedulables);
	}
	
	/* (non-Javadoc)
	 * @see ForagingModel.schedule.Scheduler#registerAtEnd(ForagingModel.schedule.Schedulable, ForagingModel.schedule.SchedulePriority)
	 */
	@Override
	public void registerAtEnd(Schedulable schedulable, SchedulePriority priority)
	{
		registerAtEnd(schedulable, priority.value());
	}

	/* (non-Javadoc)
	 * @see ForagingModel.schedule.Scheduler#run(double, int)
	 */
	@Override
	public void run(double intervalSize, int numSteps)
	{
		int intervals = (int) ((double) numSteps / intervalSize);
		boolean intervalBiggerThanStep = (intervals < numSteps);
		int intervalsPerStep = (intervals < numSteps) ? 0 : intervals / numSteps; // if fewer intervals than steps, each interval is multiple steps
		int stepsPerInterval = (intervals < numSteps) ? numSteps / intervals : 0;
		for (int interval = 0; interval < intervals; interval++)
		{
			// first notify new interval about to start
			for (Notifiable notifiable : registeredNotifiables)
			{
				notifiable.notifyInterval(interval);
			}
			
			// and is it a new time step
			if (intervalBiggerThanStep)
			{
				// always new time step
				for (Notifiable notifiable : registeredNotifiables)
				{
					notifiable.notifyTimeStep(interval * stepsPerInterval);
				}
			}
			else if (interval % intervalsPerStep == 0)
			{
				// new time step every intervalsPerStep
				for (Notifiable notifiable : registeredNotifiables)
				{
					notifiable.notifyTimeStep(interval / intervalsPerStep);
				}
			}
			

			runInterval(registeredSchedulables, interval);
		}
		// intervals is greater than last interval
		runInterval(registeredAtEndSchedulables, intervals);

		reset();
	}

	/* (non-Javadoc)
	 * @see ForagingModel.schedule.Scheduler#abortRun()
	 */
	@Override
	public void abortRun()
	{
		reset();
	}

	private void register(Schedulable schedulable, int priority,
							Map<Integer, List<Schedulable>> schedulables)
	{
		List<Schedulable> schedulablesAtASinglePriority = schedulables.get(priority);
		if (schedulablesAtASinglePriority == null)
		{
			schedulablesAtASinglePriority = new ArrayList<Schedulable>();
			schedulables.put(priority, schedulablesAtASinglePriority);
		}
		schedulablesAtASinglePriority.add(schedulable);
	}

	private void runInterval(Map<Integer, List<Schedulable>> schedulables, int currentInterval)
	{
		for (Integer priority : schedulables.keySet())
		{
			List<Schedulable> schedulablesAtPriority = schedulables.get(priority);
			// now randomize subset schedulablesAtPriority
			Collections.shuffle(schedulablesAtPriority);
			
			for (Schedulable schedulable : schedulablesAtPriority)
			{
				schedulable.execute(currentInterval, priority);
			}
		}
	}

	private void reset()
	{
		registeredSchedulables = new TreeMap<Integer, List<Schedulable>>();
		registeredAtEndSchedulables = new TreeMap<Integer, List<Schedulable>>();
		registeredNotifiables = new ArrayList<Notifiable>();
	}

}
