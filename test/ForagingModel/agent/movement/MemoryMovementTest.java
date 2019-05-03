package ForagingModel.agent.movement;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import ForagingModel.agent.Recorder;
import ForagingModel.core.NdPoint;
import ForagingModel.core.Velocity;
import ForagingModel.predator.PredatorManager;
import ForagingModel.space.LocationManager;
import ForagingModel.space.MemoryAssemblage;

public class MemoryMovementTest 
{
	@Test
	public void testSwitchFeedingToSearching()
	{
		Velocity searchV = Velocity.create(1.3, 0.7);
		Velocity feedV = Velocity.create(0.02, 0.5);
		NdPoint location = new NdPoint(3.2, 1.5);
		NdPoint destination = new NdPoint(5.8, 3.2);
		LocationManager space = Mockito.mock(LocationManager.class);
		PredatorManager predators = Mockito.mock(PredatorManager.class);
		
		// each behavior will be called twice in a row
		
		DestinationProcess searching = Mockito.mock(DestinationProcess.class);
		Mockito.when(searching.getNextVelocity(Mockito.any(NdPoint.class), Mockito.anyBoolean(), Mockito.any(Velocity.class))).thenReturn(searchV);
		
		DestinationProcess feeding = Mockito.mock(DestinationProcess.class);
		Mockito.when(feeding.getNextVelocity(Mockito.any(NdPoint.class), Mockito.anyBoolean(), Mockito.any(Velocity.class))).thenReturn(feedV);
		BehaviorSwitchingRule rule = Mockito.mock(BehaviorSwitchingRule.class);
		
		MemoryAssemblage memory = Mockito.mock(MemoryAssemblage.class);
		Mockito.when(memory.getDestinationProbabalistic(location)).thenReturn(location); // first return for set destination in the constructor
		
		Recorder recorder = Mockito.mock(Recorder.class);

		// create memory movement
		MovementBehavior memoryMovement = MovementFactory.createMemoryMovement(searching, feeding, rule, 
				recorder, memory, space, predators, location);
		Mockito.verify(searching).setDestination(location, location);
		
		// start with feeding
		Mockito.when(rule.decideBehavior(Mockito.anyDouble())).thenReturn(BehaviorState.Feeding);

		Assert.assertEquals(memoryMovement.getNextVelocity(location, 0.01), feedV, "feeding velocity first time");
		Assert.assertEquals(memoryMovement.getNextVelocity(location, 0.01), feedV, "feeding velocity second time");

		// switch to searching
		Mockito.when(rule.decideBehavior(Mockito.anyDouble())).thenReturn(BehaviorState.Searching);
		Mockito.when(memory.getDestinationProbabalistic(location)).thenReturn(destination); // now return destination for switch to searching
		
		Assert.assertEquals(memoryMovement.getNextVelocity(location, 0.01), searchV, "searching velocity first time");
		Mockito.verify(searching).setDestination(destination, location);
		Assert.assertEquals(memoryMovement.getNextVelocity(location, 0.01), searchV, "searching velocity second time");
		Mockito.verify(searching, Mockito.atMost(2)).setDestination(Mockito.any(NdPoint.class), Mockito.any(NdPoint.class));
		
		Mockito.verify(feeding, Mockito.never()).setDestination(Mockito.any(NdPoint.class), Mockito.any(NdPoint.class));
	}
	
	@Test
	public void testSwitchSearchingToFeeding()
	{
		Velocity searchV = Velocity.create(1.3, 0.7);
		Velocity feedV = Velocity.create(0.02, 0.5);
		NdPoint location = new NdPoint(3.2, 1.5);
		NdPoint destination = new NdPoint(5.8, 3.2);
		LocationManager space = Mockito.mock(LocationManager.class);
		PredatorManager predators = Mockito.mock(PredatorManager.class);

		// each behavior will be called twice in a row

		DestinationProcess searching = Mockito.mock(DestinationProcess.class);
		Mockito.when(searching.getNextVelocity(Mockito.any(NdPoint.class), Mockito.anyBoolean(), Mockito.any(Velocity.class))).thenReturn(searchV);
		
		DestinationProcess feeding = Mockito.mock(DestinationProcess.class);
		Mockito.when(feeding.getNextVelocity(Mockito.any(NdPoint.class), Mockito.anyBoolean(), Mockito.any(Velocity.class))).thenReturn(feedV);

		BehaviorSwitchingRule rule = Mockito.mock(BehaviorSwitchingRule.class);
		
		Recorder recorder = Mockito.mock(Recorder.class);

		MemoryAssemblage memory = Mockito.mock(MemoryAssemblage.class);
		Mockito.when(memory.getDestinationProbabalistic(location)).thenReturn(destination); // destination set in the constructor
		
		// create memory movement
		MovementBehavior memoryMovement = MovementFactory.createMemoryMovement(searching, feeding, rule, 
				recorder, memory, space, predators, location);
		Mockito.verify(searching).setDestination(destination, location);
		
		// start with searching
		Mockito.when(rule.decideBehavior(Mockito.anyDouble())).thenReturn(BehaviorState.Searching);

		Assert.assertEquals(memoryMovement.getNextVelocity(location, 0.01), searchV, "searching velocity first time");
		Assert.assertEquals(memoryMovement.getNextVelocity(location, 0.01), searchV, "searching velocity second time");
		Mockito.verify(searching, Mockito.atMost(1)).setDestination(Mockito.any(NdPoint.class), Mockito.any(NdPoint.class));

		// switch to feeding
		Mockito.when(rule.decideBehavior(Mockito.anyDouble())).thenReturn(BehaviorState.Feeding);
		
		Assert.assertEquals(memoryMovement.getNextVelocity(location, 0.01), feedV, "feeding velocity first time");
		Assert.assertEquals(memoryMovement.getNextVelocity(location, 0.01), feedV, "feeding velocity second time");
		Mockito.verify(feeding, Mockito.never()).setDestination(Mockito.any(NdPoint.class), Mockito.any(NdPoint.class));

	}
	
	@Test
	public void testLearningWhenGetNextVelocity()
	{
		DestinationProcess searching = Mockito.mock(DestinationProcess.class);
		MovementProcess feeding = Mockito.mock(MovementProcess.class);
		BehaviorSwitchingRule rule = Mockito.mock(BehaviorSwitchingRule.class);
		Mockito.when(rule.decideBehavior(Mockito.anyDouble())).thenReturn(BehaviorState.Feeding);
		Recorder recorder = Mockito.mock(Recorder.class);
		MemoryAssemblage memory = Mockito.mock(MemoryAssemblage.class);
		NdPoint startLocation = new NdPoint(15.1, 21.5);
		NdPoint learningLocation = new NdPoint(2, 3.4);
		LocationManager space = Mockito.mock(LocationManager.class);
		PredatorManager predators = Mockito.mock(PredatorManager.class);

		MovementBehavior memoryMovement = MovementFactory.createMemoryMovement(searching, feeding, rule, 
				recorder, memory, space, predators, startLocation);
		
		memoryMovement.getNextVelocity(learningLocation, 1);
		
		Mockito.verify(memory).learn(learningLocation);
	}
	
	@Test
	public void testRecording()
	{
		NdPoint location = new NdPoint(4.3, 0.6);
		DestinationProcess searching = Mockito.mock(DestinationProcess.class);
		DestinationProcess feeding = Mockito.mock(DestinationProcess.class);
		BehaviorSwitchingRule rule = Mockito.mock(BehaviorSwitchingRule.class);
		Recorder recorder = Mockito.mock(Recorder.class);
		MemoryAssemblage memory = Mockito.mock(MemoryAssemblage.class);
		LocationManager space = Mockito.mock(LocationManager.class);
		PredatorManager predators = Mockito.mock(PredatorManager.class);
		
		// create memory movement
		MovementBehavior memoryMovement = MovementFactory.createMemoryMovement(searching, feeding, rule, 
				recorder, memory, space, predators, location);

		// start with feeding
		Mockito.when(rule.decideBehavior(Mockito.anyDouble())).thenReturn(BehaviorState.Feeding);
		memoryMovement.getNextVelocity(location, 0.01);
		Mockito.verify(recorder).recordState(BehaviorState.Feeding);
		Mockito.verify(recorder).recordUseMemory(false);		
		
		// now searching WITHOUT a destination
		// mocked search returns null for destination
		Mockito.when(rule.decideBehavior(Mockito.anyDouble())).thenReturn(BehaviorState.Searching);
		memoryMovement.getNextVelocity(location, 0.01);
		Mockito.verify(recorder).recordState(BehaviorState.Searching);
		Mockito.verify(recorder, Mockito.times(2)).recordUseMemory(false);	// 2nd time this call happened	

		// now searching WITH a destination
		Mockito.when(rule.decideBehavior(Mockito.anyDouble())).thenReturn(BehaviorState.Searching);
		Mockito.when(searching.getDestination()).thenReturn(new NdPoint(3.5, 2.6)); 
		memoryMovement.getNextVelocity(location, 0.01);
		Mockito.verify(recorder, Mockito.times(2)).recordState(BehaviorState.Searching); // 2nd time with searching
		Mockito.verify(recorder).recordUseMemory(true);		
	}
}
