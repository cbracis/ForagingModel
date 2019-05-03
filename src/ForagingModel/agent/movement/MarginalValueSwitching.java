package ForagingModel.agent.movement;

public class MarginalValueSwitching implements BehaviorSwitchingRule 
{
	private double landscapeAverageConsumtionRate;
	
	protected MarginalValueSwitching(double landscapeAverageConsumtionRate)
	{
		this.landscapeAverageConsumtionRate = landscapeAverageConsumtionRate;
	}
	
	public BehaviorState decideBehavior(double currentConsumptionRate) 
	{
		BehaviorState behavior;
		
		if (currentConsumptionRate > landscapeAverageConsumtionRate)
		{
			behavior = BehaviorState.Feeding;
		}
		else
		{
			behavior = BehaviorState.Searching;
		}
		return behavior;
	}

}
