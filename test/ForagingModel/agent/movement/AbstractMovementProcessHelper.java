package ForagingModel.agent.movement;

public class AbstractMovementProcessHelper extends AbstractMovementProcess
{

	protected AbstractMovementProcessHelper(double minDimensionX, double minDimensionY, double maxDimensionX, double maxDimensionY, double intervalSize) 
	{
		super(minDimensionX, minDimensionY, maxDimensionX, maxDimensionY, intervalSize);
	}
	
	protected AbstractMovementProcessHelper()
	{
		this(0, 0, 50, 50, 1);
	}

}
