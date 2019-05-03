package ForagingModel.agent.movement;

public class LearnedValueSwitching implements BehaviorSwitchingRule 
{
	private double averageConsumptionRate;
	private double learningRate;
	
	protected LearnedValueSwitching(double startAverageConsumtionRate, double learningRate)
	{
		this.averageConsumptionRate = startAverageConsumtionRate;
		this.learningRate = learningRate;
	}


	@Override
	public BehaviorState decideBehavior(double currentConsumptionRate) 
	{
		BehaviorState behavior;
		
		if (currentConsumptionRate > averageConsumptionRate)
		{
			behavior = BehaviorState.Feeding;
		}
		else
		{
			behavior = BehaviorState.Searching;
		}
		
		learn(currentConsumptionRate);
		
		return behavior;
	}
	
	private void learn(double currentConsumptionRate)
	{
		averageConsumptionRate += learningRate * (currentConsumptionRate - averageConsumptionRate);
	}

}
