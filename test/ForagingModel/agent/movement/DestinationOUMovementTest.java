package ForagingModel.agent.movement;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import ForagingModel.core.NdPoint;
import ForagingModel.core.TestUtilities;
import ForagingModel.core.Velocity;

public class DestinationOUMovementTest 
{
	@AfterMethod
	public void resetNumberGenerator()
	{
		TestUtilities.resetGenerator();
	}
	
	@Test
	public void testSetDestination()
	{
		double speed = 1;
		double tau = 2;
		Velocity initVel = Velocity.createPolar(speed, 0);
		Velocity mu = Velocity.createPolar(speed, Math.PI / 3);
		NdPoint location = new NdPoint(3, 5);
		NdPoint destination = mu.move(location, 10);
		
		TestUtilities.setGeneratorToNormalAlwaysReturnsZero();
		
		DestinationProcess movement = MovementFactory.createDestinationOUProcess(speed, tau, 0.1, initVel, 0, 0, 50, 50, 1);

		Velocity expectedVel = initVel.plus( initVel.scaleBy(-1 / tau) );
		Velocity newVel = movement.getNextVelocity(location, false, null);
		
		Assert.assertEquals(newVel, expectedVel, "new velocity with no stochasticity and mu = 0");
		
		movement.setDestination(destination, location);
		
		expectedVel = newVel.plus( mu.minus(newVel).scaleBy(1 / tau) );
		newVel = movement.getNextVelocity(location, false, null);
		
		Assert.assertEquals(newVel, expectedVel, "new velocity with no stochasticity and mu from destination");
		
		NdPoint newLocation = new NdPoint(29.5, 13.7);
		Velocity newMu = Velocity.create(newLocation, destination, speed);

		expectedVel = newVel.plus( newMu.minus(newVel).scaleBy(1 / tau) );
		newVel = movement.getNextVelocity(newLocation, false, null);
		
		Assert.assertEquals(newVel, expectedVel, "new velocity with no stochasticity with same destination but different location");

	}
	
	@Test
	public void testArrived()
	{
		NdPoint destination = new NdPoint(2, 2.2);
		double radius = 3.0;
		DestinationProcess movement = MovementFactory.createDestinationOUProcess(1, 10, radius, Velocity.createPolar(1, 0.5), 0, 0, 50, 50, 1);
		movement.setDestination(destination, new NdPoint(0, 0));
		
		Assert.assertEquals(movement.hasArrived(Velocity.createPolar(2.0, 0.3).move(destination)), true, "within radius");
		Assert.assertEquals(movement.hasArrived(destination), true, "at destination");
		Assert.assertEquals(movement.hasArrived(Velocity.createPolar(5.6, 2.1).move(destination)), false, "outside radius");
		Assert.assertEquals(movement.hasArrived(Velocity.createPolar(3.0, 1.5).move(destination)), false, "at radius");
	}
	
	@Test
	public void testNullDestination()
	{
		TestUtilities.setGeneratorToNormalAlwaysReturnsZero();
		
		double radius = 3.0;
		NdPoint newLocation = new NdPoint(5, 5);
		DestinationProcess movement = MovementFactory.createDestinationOUProcess(1, 1, radius, Velocity.createPolar(1, 0.5), 0, 0, 50, 50, 1);
		movement.setDestination(null, new NdPoint(0, 0));
		Velocity newVel = movement.getNextVelocity(newLocation, false, null);
		
		// the expected is 0 because mu=0 with no destination, so with tau=1 this subtracts the whole current
		// velocity from itself, and we've set the random component to be 0 as well
		Assert.assertEquals(newVel, Velocity.create(0, 0), "get velocity with null destination");
		
		Assert.assertFalse(movement.hasArrived(newLocation), "check has arrived with null destination");
	}

}
