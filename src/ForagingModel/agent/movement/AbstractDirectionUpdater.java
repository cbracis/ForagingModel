package ForagingModel.agent.movement;

public abstract class AbstractDirectionUpdater 
{
	private double intervalSize;
	private double currentTime;
	private boolean shouldUpdateNow;
	private double nextUpdateTime;

	protected AbstractDirectionUpdater(double intervalSize)
	{
		this.intervalSize = intervalSize;
		currentTime = 0;
		shouldUpdateNow = true;
		nextUpdateTime = 0;
	}

	//@Override
	public void notifyInterval(int currentInterval) 
	{
		currentTime = currentInterval * intervalSize;
		if (currentTime >= nextUpdateTime)
		{
			shouldUpdateNow = true;
			nextUpdateTime = this.getNextUpdateTime(currentTime); 
//			logger.debug("Updating at interval {}. Next update {}", currentInterval, nextUpdateTime);
		}
		else
		{
			shouldUpdateNow = false;
		}
	}

	//@Override
	public void notifyTimeStep(int currentTimeStep) 
	{
	}

	//@Override
	public boolean updateDirection() 
	{
		return shouldUpdateNow;
	}
	
	abstract double getNextUpdateTime(double currentTime);

}
