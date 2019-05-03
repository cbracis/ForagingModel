package ForagingModel.agent.movement;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import ForagingModel.core.NdPoint;
import ForagingModel.core.TestUtilities;
import ForagingModel.core.Velocity;

public class DestinationStraightMovementTest 
{
	@AfterMethod
	public void resetNumberGenerator()
	{
		TestUtilities.resetGenerator();
	}

	@Test
	public void testChangeDestination()
	{
		double speed = 2.2;
		Velocity initVel = Velocity.createPolar(speed, 0);

		DestinationProcess movement = MovementFactory.createDestinationStraightProcess(speed, 0.1, initVel, 0, 0, 100, 100, 1);
		
		Assert.assertEquals(initVel, movement.getNextVelocity(new NdPoint(1, 1), false, null), "initial velocity");
		
		movement.setDestination(new NdPoint(2, 2), new NdPoint(1, 1));
		
		Assert.assertEquals(Velocity.createPolar(speed, Math.PI /4), movement.getNextVelocity(new NdPoint(1, 1), false, null), "velocity after set destination");
		Assert.assertEquals(Velocity.createPolar(speed, Math.PI /4), movement.getNextVelocity(new NdPoint(2, 2), false, null), "velocity doesn't change if destination set");
	}
	
	@Test
	public void testNullDestination()
	{
		TestUtilities.setGeneratorToNextDoubleFromToAlwaysReturnsZero();
		
		double speed = 1;
		NdPoint newLocation = new NdPoint(5, 5);
		DestinationProcess movement = MovementFactory.createDestinationStraightProcess(speed, 3.0, Velocity.createPolar(1, 0.5), 0, 0, 50, 50, 1);
		movement.setDestination(null, newLocation);
		Velocity newVel = movement.getNextVelocity(newLocation, false, null);
		
		// the expected angle is 0 because we set the generator to always return 0
		Assert.assertEquals(newVel, Velocity.createPolar(speed, 0), "get velocity with null destination");
		
		Assert.assertFalse(movement.hasArrived(newLocation), "check has arrived with null destination");
	}
}
