package ForagingModel.agent.movement;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.mockito.Matchers;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import ForagingModel.agent.Recorder;
import ForagingModel.core.Velocity;
import ForagingModel.core.NdPoint;
import ForagingModel.predator.PredatorEncounterBehavior;
import ForagingModel.predator.PredatorManager;

public class KineticMovementTest 
{

	@Test
	public void testKineticMovement()
	{
		Velocity searchV = Velocity.create(1.3, 0.7);
		Velocity feedV = Velocity.create(0.02, 0.5);
		
		MovementProcess searching = Mockito.mock(MovementProcess.class);
		Mockito.when(searching.getNextVelocity(Mockito.any(NdPoint.class), Mockito.eq(true), Mockito.any(Velocity.class))).thenReturn(searchV); // true since switching each time
		MovementProcess feeding = Mockito.mock(MovementProcess.class);
		Mockito.when(feeding.getNextVelocity(Mockito.any(NdPoint.class), Mockito.eq(true), Mockito.any(Velocity.class))).thenReturn(feedV);
		BehaviorSwitchingRule rule = Mockito.mock(BehaviorSwitchingRule.class);
		Recorder recorder = Mockito.mock(Recorder.class);
		PredatorManager predators = Mockito.mock(PredatorManager.class);
		
		MovementBehavior kinetic = MovementFactory.createKineticMovement(searching, feeding, rule, recorder, predators, 0, PredatorEncounterBehavior.NoReaction);
		
		Mockito.when(rule.decideBehavior(Mockito.anyDouble())).thenReturn(BehaviorState.Feeding);
		Velocity actual = kinetic.getNextVelocity(new NdPoint(3, 2), 0.01);
		Assert.assertEquals(actual, feedV, "feeding velocity");
		Assert.assertEquals(kinetic.escapedPredator(), false, "no predator");

		Mockito.when(rule.decideBehavior(Mockito.anyDouble())).thenReturn(BehaviorState.Searching);
		actual = kinetic.getNextVelocity(new NdPoint(3, 2), 0.01);
		Assert.assertEquals(actual, searchV, "searching velocity");
		Assert.assertEquals(kinetic.escapedPredator(), false, "no predator");
	}
	
	@Test
	public void testAvoidPredator()
	{
		Velocity velocity = Velocity.create(1.3, 2.1);
		MovementProcess searching = Mockito.mock(MovementProcess.class);
		Mockito.when(searching.getEscapeVelocity(Mockito.any(NdPoint.class), Matchers.<Set<NdPoint>>any())).thenReturn(velocity);

		MovementProcess feeding = Mockito.mock(MovementProcess.class);
		BehaviorSwitchingRule rule = Mockito.mock(BehaviorSwitchingRule.class);
		Recorder recorder = Mockito.mock(Recorder.class);

		PredatorManager predators = Mockito.mock(PredatorManager.class);
		Mockito.when(predators.getActivePredators(Mockito.any(NdPoint.class), Mockito.anyDouble())).thenReturn(new HashSet<NdPoint>(Arrays.asList(new NdPoint(1, 1))));

		MovementBehavior kinetic = MovementFactory.createKineticMovement(searching, feeding, rule, recorder, predators, 0, PredatorEncounterBehavior.Escape);

		Velocity actual = kinetic.getNextVelocity(new NdPoint(2, 2), 0.1);
		Assert.assertEquals(actual, velocity, "escape velocity");
		Assert.assertEquals(kinetic.escapedPredator(), true, "escaped predators");
	}
	
	@Test
	public void testNoReactionToPredator()
	{
		Velocity velocity = Velocity.create(1.3, 2.1);
		MovementProcess searching = Mockito.mock(MovementProcess.class);
		Mockito.when(searching.getNextVelocity(Mockito.any(NdPoint.class), Mockito.anyBoolean(), Mockito.any(Velocity.class))).thenReturn(velocity);

		MovementProcess feeding = Mockito.mock(MovementProcess.class);
		BehaviorSwitchingRule rule = Mockito.mock(BehaviorSwitchingRule.class);
		Mockito.when(rule.decideBehavior(Mockito.anyDouble())).thenReturn(BehaviorState.Searching);
		Recorder recorder = Mockito.mock(Recorder.class);

		PredatorManager predators = Mockito.mock(PredatorManager.class);
		Mockito.when(predators.getActivePredators(Mockito.any(NdPoint.class), Mockito.anyDouble())).thenReturn(new HashSet<NdPoint>(Arrays.asList(new NdPoint(1, 1))));

		MovementBehavior kinetic = MovementFactory.createKineticMovement(searching, feeding, rule, recorder, predators, 0, PredatorEncounterBehavior.NoReaction);

		Velocity actual = kinetic.getNextVelocity(new NdPoint(2, 2), 0.1);
		Assert.assertEquals(actual, velocity, "search velocity");
		Assert.assertEquals(kinetic.escapedPredator(), false, "no escape behavior");
	}
}
