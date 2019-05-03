package ForagingModel.agent.movement;

import org.testng.Assert;
import org.testng.annotations.Test;

public class MarginalValueSwitchingTest 
{
	@Test
	public void testMarginalValueSwitching()
	{
		BehaviorSwitchingRule rule = MovementFactory.createMarginalValueRule(0.5);
		
		Assert.assertEquals(rule.decideBehavior(0), BehaviorState.Searching, "searching");
		Assert.assertEquals(rule.decideBehavior(0.1), BehaviorState.Searching, "searching");
		Assert.assertEquals(rule.decideBehavior(0.5), BehaviorState.Searching, "searching");
		Assert.assertEquals(rule.decideBehavior(0.7), BehaviorState.Feeding, "feeding");
		Assert.assertEquals(rule.decideBehavior(10), BehaviorState.Feeding, "feeding");
	}
}
