package ForagingModel.space;

import ForagingModel.schedule.Schedulable;

/**
 * A container for a ScentHistory to handle decaying when it is not owned by a
 * specific forager. For example, the shared ScentHistory of all females used
 * jointly by all males.
 */
public class ScentHistoryContainer implements Schedulable 
{
	private ScentHistory scentHistory;
	
	protected ScentHistoryContainer(ScentHistory scentHistory)
	{
		this.scentHistory = scentHistory;
	}

	@Override
	public void execute(int currentInterval, int priority) 
	{
		scentHistory.decay();
	}

}
