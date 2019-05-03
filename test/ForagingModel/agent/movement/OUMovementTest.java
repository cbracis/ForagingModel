package ForagingModel.agent.movement;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import ForagingModel.core.NdPoint;
import ForagingModel.core.TestUtilities;
import ForagingModel.core.Velocity;

public class OUMovementTest extends MovementTest
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
		
		TestUtilities.setGeneratorToNormalAlwaysReturnsZero();
		
		MovementProcess movement = MovementFactory.createOUProcess(speed, tau, initVel, 0, 0, 50, 50, 1);

		Velocity newVel = movement.getNextVelocity(new NdPoint(5, 5), false, null);
		Velocity expectedVel = initVel.plus( initVel.scaleBy(-1 / tau) );
		
		Assert.assertEquals(newVel, expectedVel, "new velocity with no stochasticity");
	}
	
	@Test(dataProvider = "movementsOutsideBoundary")
	public void testBoundaries(Velocity velocity, NdPoint location)
	{
		double lowerBound = 0;
		double upperBound = 100;
		MovementProcess movement = MovementFactory.createOUProcess(velocity.mod(), 2, velocity, lowerBound, lowerBound, upperBound, upperBound, 1);
		Velocity newVel = movement.getNextVelocity(location, false, null);
		NdPoint newLoc = newVel.move(location);
		
		assertBoundaries(newLoc, lowerBound, upperBound);
	}
	
	@Test(dataProvider = "intervals")
	public void testTimeStepScaling(double intervalSize)
	{
		double speed = 2;
		double angle = Math.PI / 2;
		Velocity velocity = Velocity.createPolar(speed, angle);
		
		TestUtilities.setGeneratorToNormalAlwaysReturnsZero();
		
		MovementProcess movement = MovementFactory.createOUProcess(velocity.mod(), 0.5, velocity, 0, 0, 100, 100, intervalSize);
		
		Velocity newVel = movement.getNextVelocity(new NdPoint(5, 5), false, null);
		Assert.assertEquals(newVel.mod(), Velocity.createPolar(speed * intervalSize, angle).mod(), "velocity speed scaled by timestep");
	}
}
