package ForagingModel.agent.movement;


public interface BehaviorSwitchingRule 
{
	BehaviorState decideBehavior(double currentConsumptionRate);
}
