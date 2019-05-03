package ForagingModel.agent.movement;

import java.util.Arrays;
import java.util.HashSet;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import ForagingModel.core.Angle;
import ForagingModel.core.NdPoint;
import ForagingModel.core.TestUtilities;
import ForagingModel.core.Velocity;
import ForagingModel.space.SpaceUtils;

public class CorrelatedMovementTest 
{
	@AfterMethod
	public void resetNumberGenerator()
	{
		TestUtilities.resetGenerator();
	}

	@Test
	public void testAllNonePersistence()
	{
		double speed = 0.5;
		Velocity initialVel = Velocity.createPolar(speed, 2);
		MovementProcess movement = MovementFactory.createCorrelatedProcess(speed, 1, initialVel, 0, 0, 10, 10, 1);
		Assert.assertEquals(movement.getNextVelocity(new NdPoint(2, 3), false, null), initialVel, "persistance = 1");
		
		TestUtilities.setGeneratorToNextDoubleFromToAlwaysReturnsZero();

		movement = MovementFactory.createCorrelatedProcess(speed, 0);
		Assert.assertEquals(movement.getNextVelocity(new NdPoint(3, 3), false, null), Velocity.createPolar(speed, 0), "persistance = 0");

	}
	
	@Test
	public void testGetNextVelocity()
	{
		TestUtilities.setGeneratorToNextDoubleFromToAlwaysReturnsZero();

		double speed = 0.5;
		double initialAngle = 2;
		double persistence = 0.75;
		Velocity initialVel = Velocity.createPolar(speed, initialAngle);
		Velocity expectedVelocity = Velocity.createPolar(speed, Angle.combine(initialAngle, 0, persistence)); 
		MovementProcess movement = MovementFactory.createCorrelatedProcess(speed, persistence, initialVel, 0, 0, 10, 10, 1);
		Velocity newVelocity = movement.getNextVelocity(new NdPoint(2, 3), false, null);
		Assert.assertEquals(newVelocity.x(), expectedVelocity.x(), 1e-10, "vel. x component, persistance = 0.75");
		Assert.assertEquals(newVelocity.y(), expectedVelocity.y(), 1e-10, "vel. y component, persistance = 0.75");
	}
	
	@Test
	public void testGetEscapeVelocityNearBoundary()
	{
		// test case to reproduce problem where escape from predator was outside boundary and when reflected, went towards predator,
		// resulting in a back and forth loop for eternity
		double speed = 2.0;
		double initialAngle = 7 * Math.PI / 4;
		double persistence = 0.8;
		Velocity initialVel = Velocity.createPolar(speed, initialAngle);
		double maxDim = 50;
		MovementProcess movement = MovementFactory.createCorrelatedProcess(speed, persistence, initialVel, 0, 0, maxDim, maxDim, 1);

		NdPoint forager = new NdPoint(49.1, 39.9);
		NdPoint predator = new NdPoint(45.5, 36.9);
		Velocity velocity = movement.getEscapeVelocity(forager, new HashSet<NdPoint>(Arrays.asList(new NdPoint[] { predator })));
		
		NdPoint newForager = velocity.move(forager);
		Assert.assertTrue(SpaceUtils.getDistance(forager, predator) < SpaceUtils.getDistance(newForager, predator), 
				"Moved farther away with escape velocity " + velocity.toString());
		
	}
	
	@Test
	public void testGetEscapeVelocityTwoPredators()
	{
		// test case to reproduce problem where bounce between 2 predators for eternity
		double speed = 2.0;
		double initialAngle = 7 * Math.PI / 4;
		double persistence = 0.8;
		Velocity initialVel = Velocity.createPolar(speed, initialAngle);
		double maxDim = 20;
		MovementProcess movement = MovementFactory.createCorrelatedProcess(speed, persistence, initialVel, 0, 0, maxDim, maxDim, 1);

		NdPoint foragerStart = new NdPoint(6, 5.1);
		NdPoint pred1 = new NdPoint(2, 5);
		NdPoint pred2 = new NdPoint(12, 5);
		Velocity velocity = movement.getEscapeVelocity(foragerStart, new HashSet<NdPoint>(Arrays.asList(new NdPoint[] { pred1 })));
		
		NdPoint foragerStep1 = velocity.move(foragerStart);
		velocity = movement.getEscapeVelocity(foragerStep1, new HashSet<NdPoint>(Arrays.asList(new NdPoint[] { pred1, pred2 })));
		
		NdPoint foragerStep2 = velocity.move(foragerStep1);
		
		Assert.assertTrue(!foragerStart.equals(foragerStep2), "not back at start");
		Assert.assertTrue(SpaceUtils.getDistance(foragerStart, pred1) < SpaceUtils.getDistance(foragerStep2, pred1), "farther from pred1");
	}

	
	@Test
	public void testGetReverseVelocity()
	{
		double speed = 2.0;
		double initialAngle = 7 * Math.PI / 4;
		double persistence = 0.8;
		Velocity initialVel = Velocity.createPolar(speed, initialAngle);
		double maxDim = 50;
		MovementProcess movement = MovementFactory.createCorrelatedProcess(speed, persistence, initialVel, 0, 0, maxDim, maxDim, 1);
		
		NdPoint forager = new NdPoint(48.26, 1.52);
		Velocity previousVelocity = Velocity.create(-0.107632, 0.488278);
		
		Velocity velocity = movement.getReverseVelocity(forager, previousVelocity);

		System.out.println(velocity);
		
		Assert.assertTrue(SpaceUtils.inBounds(velocity.move(forager), 0, 0, maxDim, maxDim));
	}
	

}
