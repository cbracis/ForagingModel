package ForagingModel.agent.movement;

import org.testng.Assert;
import org.testng.annotations.Test;

public class LearnedValueSwitchingTest 
{
	@Test
	public void testLearningRateZero()
	{
		BehaviorSwitchingRule rule = MovementFactory.createLearnedValueSwitching(0.5, 0);
		
		// learning rate 0, so anything 0.5 and below is searching
		Assert.assertEquals(rule.decideBehavior(0), BehaviorState.Searching, "searching");
		Assert.assertEquals(rule.decideBehavior(0.1), BehaviorState.Searching, "searching");
		Assert.assertEquals(rule.decideBehavior(0.5), BehaviorState.Searching, "searching");
		Assert.assertEquals(rule.decideBehavior(0.7), BehaviorState.Feeding, "feeding");
		Assert.assertEquals(rule.decideBehavior(10), BehaviorState.Feeding, "feeding");
	}
	
	@Test
	public void testLearningRateOne()
	{
		BehaviorSwitchingRule rule = MovementFactory.createLearnedValueSwitching(0.5, 1);
		
		// learning rate 1, so anything below last value is searching 
		Assert.assertEquals(rule.decideBehavior(0), BehaviorState.Searching, "searching");
		Assert.assertEquals(rule.decideBehavior(0.1), BehaviorState.Feeding, "feeding");
		Assert.assertEquals(rule.decideBehavior(0.11), BehaviorState.Feeding, "feeding");
		Assert.assertEquals(rule.decideBehavior(0.1), BehaviorState.Searching, "searching");
		Assert.assertEquals(rule.decideBehavior(1), BehaviorState.Feeding, "feeding");
	}
	
	@Test
	public void testLearningRateHalf()
	{
		BehaviorSwitchingRule rule = MovementFactory.createLearnedValueSwitching(0.5, 0.5);
		
		// learning rate 0.5, so need to track avg
		Assert.assertEquals(rule.decideBehavior(0), BehaviorState.Searching, "searching"); // now 0.25
		Assert.assertEquals(rule.decideBehavior(1.25), BehaviorState.Feeding, "feeding"); // now 0.75
		Assert.assertEquals(rule.decideBehavior(0.25), BehaviorState.Searching, "searching"); // now 0.5
		Assert.assertEquals(rule.decideBehavior(0.6), BehaviorState.Feeding, "feeding"); // now 0.55
		Assert.assertEquals(rule.decideBehavior(0.5), BehaviorState.Searching, "searching"); 
	}



}
