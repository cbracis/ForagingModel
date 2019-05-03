package ForagingModel.agent.movement;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import ForagingModel.core.Angle;
import ForagingModel.core.DirectionProbabalistic;
import ForagingModel.core.NdPoint;
import ForagingModel.core.Velocity;
import ForagingModel.space.MemoryAssemblage;

public class DirectionalCorrelatedMovementTest 
{
	@Test
	public void testGetNextVelocity()
	{
		double speed = 1.3;
		NdPoint loc = new NdPoint(2.1, 2);
		Angle newAngle = new Angle(3.1);
		MemoryAssemblage memory = Mockito.mock(MemoryAssemblage.class);
		DirectionProbabalistic direction = Mockito.mock(DirectionProbabalistic.class);
		Mockito.when(direction.angle()).thenReturn(newAngle);
		Mockito.when(direction.angleIsSafe(newAngle)).thenReturn(true);
		Mockito.when(memory.getDirectionProbabalistic(loc)).thenReturn(direction);
		MovementProcess movement = MovementFactory.createDirectionalCorrelatedProcess(speed, 0, 
				memory);
		
		Velocity velocity = movement.getNextVelocity(loc, false, null);
		Assert.assertEquals(velocity, Velocity.createPolar(speed, newAngle), "angle from memory");
	}
}
