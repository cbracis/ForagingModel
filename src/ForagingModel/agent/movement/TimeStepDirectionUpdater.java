package ForagingModel.agent.movement;

public class TimeStepDirectionUpdater extends AbstractDirectionUpdater implements DirectionUpdater 
{
	private int timeStepUpdateSize; // or double
	
	protected TimeStepDirectionUpdater(int timeStepUpdateSize, double intervalSize)
	{
		super(intervalSize);
		this.timeStepUpdateSize = timeStepUpdateSize;
	}

	protected double getNextUpdateTime(double currentTime)
	{
		return currentTime + timeStepUpdateSize;
	}

}
