package ForagingModel.agent.movement;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import ForagingModel.agent.Recorder;
import ForagingModel.core.NdPoint;
import ForagingModel.core.TestUtilities;
import ForagingModel.core.Velocity;
import ForagingModel.schedule.Scheduler;

public class ContinuousCorrelatedMovementTest 
{
	@AfterMethod
	public void resetNumberGenerator()
	{
		TestUtilities.resetGenerator();
	}

	@Test
	public void testSimpleMovement()
	{
		double speed = 2;
		double tau = 4;
		Velocity initVel = Velocity.createPolar(speed, 0);
		
		TestUtilities.setGeneratorToNextDoubleFromToAlwaysReturnsZero();
		Scheduler scheduler = Mockito.mock(Scheduler.class);
		Recorder recorder = Mockito.mock(Recorder.class);
		
		MovementProcess movement = MovementFactory.createContinuousCorrelatedProcess(speed, tau, initVel, 0, 0, 50, 50, 1, recorder, scheduler);

		Velocity newVel = movement.getNextVelocity(new NdPoint(5, 5), false, null);
		Velocity expectedVel = initVel.plus( Velocity.createPolar(speed, 0).minus(initVel).scaleBy(1 / tau) );
		
		Assert.assertEquals(newVel, expectedVel, "new velocity with no mu");
	}

}
