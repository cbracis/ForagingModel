package ForagingModel.agent.movement;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import ForagingModel.agent.Recorder;
import ForagingModel.core.Angle;
import ForagingModel.core.DirectionProbabalistic;
import ForagingModel.core.NdPoint;
import ForagingModel.core.TestUtilities;
import ForagingModel.core.Velocity;
import ForagingModel.space.MemoryAssemblage;

public class DirectionalContinuousCorrelatedMovementTest 
{
	@AfterMethod
	public void resetNumberGenerator()
	{
		TestUtilities.resetGenerator();
	}

	@Test
	public void testGetNextVelocity()
	{
		double speed = 1.3;
		double tau = 2;
		NdPoint loc = new NdPoint(2.1, 2);
		Angle newAngle = new Angle(3.1);
		Velocity initialV = Velocity.create(1, 1);

		Recorder recorder = Mockito.mock(Recorder.class);

		MemoryAssemblage memory = Mockito.mock(MemoryAssemblage.class);
		DirectionProbabalistic direction = Mockito.mock(DirectionProbabalistic.class);
		DirectionUpdater updater = Mockito.mock(DirectionUpdater.class);
		Mockito.when(direction.angle()).thenReturn(newAngle);
		Mockito.when(direction.angleIsSafe(Mockito.any(Angle.class))).thenReturn(true);
		Mockito.when(memory.getDirectionProbabalistic(loc)).thenReturn(direction);
		Mockito.when(updater.updateDirection()).thenReturn(true);
		TestUtilities.setGeneratorToNextDoubleFromToAlwaysReturnsZero();
		
		MovementProcess movement = MovementFactory.createDirectionalContinuousCorrelatedProcess(speed, tau, memory, updater, initialV, 0, 0, 50, 50, 1, recorder);
		Velocity velocity = movement.getNextVelocity(loc, false, null);

		Velocity dV = Velocity.createPolar(speed, newAngle).minus(initialV).scaleBy(1.0 / tau);
		Velocity expectedVelocity = initialV.plus(dV);
		Assert.assertEquals(velocity, expectedVelocity, "new velocity");
	}
}
