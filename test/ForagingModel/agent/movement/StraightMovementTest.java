package ForagingModel.agent.movement;

import org.testng.Assert;
import org.testng.annotations.Test;

import ForagingModel.core.NdPoint;
import ForagingModel.core.Velocity;

public class StraightMovementTest extends MovementTest
{

	@Test
	public void testSimpleMovement()
	{
		double speed = 2;
		Velocity initVel = Velocity.createPolar(speed, 0);
		MovementProcess movement = MovementFactory.createStraightProcess(speed, initVel, 0, 0, 50, 50, 1);
		
		Velocity newVel = movement.getNextVelocity(new NdPoint(5, 5), false, null);
		Assert.assertEquals(newVel, initVel, "same velocity for straight movement");
	}
	
	@Test(dataProvider = "movementsOutsideBoundary")
	public void testBoundaries(Velocity velocity, NdPoint location)
	{
		double lowerBound = 0;
		double upperBound = 100;
		MovementProcess movement = MovementFactory.createStraightProcess(velocity.mod(), velocity, 0, 0, 100, 100, 1);
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
		MovementProcess movement = MovementFactory.createStraightProcess(velocity.mod(), velocity, 0, 0, 100, 100, intervalSize);
		
		Velocity newVel = movement.getNextVelocity(new NdPoint(5, 5), false, null);
		Assert.assertEquals(newVel, Velocity.createPolar(speed * intervalSize, angle), "velocity scaled by timestep");
	}

}
