package ForagingModel.agent;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ForagingModel.agent.Agent.Sex;
import ForagingModel.agent.movement.MovementBehavior;
import ForagingModel.agent.movement.MovementFactoryTestHelper;
import ForagingModel.core.NdPoint;
import ForagingModel.core.Velocity;
import ForagingModel.space.LocationManager;
import ForagingModel.space.ResourceAssemblage;
import ForagingModel.space.SpaceFactory;

public class ForagerTest 
{
	@Mock
	private ResourceAssemblage resource;	
	@Mock
	private Recorder recorder;
	
	@BeforeTest
	public void createMocks()
	{
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void testConsumption()
	{
		LocationManager locationManager = SpaceFactory.createLocationManager(10);
		MovementBehavior movement = Mockito.mock(MovementBehavior.class);
		Mockito.when(movement.escapedPredator()).thenReturn(false);
		Forager forager = AgentFactory.createForager(locationManager, 
				Sex.Unknown, Integer.MAX_VALUE, resource, movement, 
				AgentFactory.createRecorder(10, 0, 10, 1, null), 1, 2, 1);
		double amtConsumed = 1.2;
		
		Mockito.when(resource.consumeResource(Mockito.any(NdPoint.class), Mockito.anyDouble(), Mockito.anyDouble())).thenReturn(amtConsumed);
		
		forager.consumeResource();
		
		Assert.assertEquals(forager.getReporter().getTotalConsumption(), amtConsumed, "amount consumed matches");
	}
	@Test
	public void testMovementMocked()
	{
		// test with all mocked objects
		MovementBehavior movement = Mockito.mock(MovementBehavior.class);
		LocationManager locationManager = SpaceFactory.createLocationManager(10);

		Forager forager = AgentFactory.createForager(locationManager, Sex.Unknown, Integer.MAX_VALUE, resource, movement, recorder, 1, 2, 1);
		NdPoint location = new NdPoint(5, 5);
		locationManager.moveTo(forager, location);
		
		Velocity velocity = Velocity.createPolar(2, 0);
		
		Mockito.when(movement.getNextVelocity(Mockito.eq(location), Mockito.anyDouble())).thenReturn(velocity);
		
		forager.move();
		
		Assert.assertEquals(locationManager.getLocation(forager), velocity.move(location), "new location");
	}
	
	@Test
	public void testMovementReal()
	{
		double moveSpeed = 2;
		double moveAngle = 0;

		LocationManager locationManager = SpaceFactory.createLocationManager(100);
		MovementBehavior movement = MovementFactoryTestHelper.createStraightMovement(moveSpeed, Velocity.createPolar(moveSpeed, moveAngle), 0, 100, 1);
		
		Forager forager = AgentFactory.createForager(locationManager, Sex.Unknown, Integer.MAX_VALUE, resource, movement, recorder, 1, 2, 1);
		locationManager.moveTo(forager, new NdPoint(1, 1));

		forager.move();
		
		NdPoint newLocation = locationManager.getLocation(forager);
		
		Assert.assertEquals(newLocation.getX(), 3, 1e-10, "x coordinate of new location");
		Assert.assertEquals(newLocation.getY(), 1, 1e-10, "y coordinate of new location");
	}
	
	@Test
	public void testMovementBoundary()
	{
		double moveSpeed = 2;
		double moveAngle = 0;

		// use a real space and grid
		// issue: how to match grid and space dimensions
		LocationManager locationManager = SpaceFactory.createLocationManager(100);

		MovementBehavior movement = Mockito.mock(MovementBehavior.class);
		Mockito.when(movement.getNextVelocity(Mockito.any(NdPoint.class), Mockito.anyDouble())).thenReturn(Velocity.createPolar(moveSpeed, moveAngle));
		
		Forager forager = AgentFactory.createForager(locationManager, Sex.Unknown, Integer.MAX_VALUE, resource, movement, recorder, 1, 2, 1);
		locationManager.moveTo(forager, new NdPoint(97.6, 5)); // so will end up almost at the boundary but not quite

		forager.move();
		
		Assert.assertEquals(locationManager.getLocation(forager), new NdPoint(99.6, 5), "moved to location close to boundary");

	}
	
	@Test(dataProvider = "sampleVelocities")
	public void testMoveByVector(Velocity moveAmount)
	{
		// this is a test of ContinuousSpace.moveByVector. It throws an exception if the number of angles doesn't equal the number of dimensions
		// which seems wrong, and for example, the zombies project just passes 0 for the 2nd angle
		// verify how this method works
		
		LocationManager locationManager = SpaceFactory.createLocationManager(100);

		Forager forager = AgentFactory.createForager(locationManager, Sex.Unknown, Integer.MAX_VALUE, resource, null, recorder, 1, 2, 1);
		NdPoint start = new NdPoint(10, 10);
		locationManager.moveTo(forager, start);
		
		locationManager.moveByVector(forager, moveAmount);
		
		// verify x and y separately since doubles don't exactly match otherwise
		Assert.assertEquals(locationManager.getLocation(forager).getX(), moveAmount.move(start).getX(), 1e-10, "x coord of move by vector matches");
		Assert.assertEquals(locationManager.getLocation(forager).getY(), moveAmount.move(start).getY(), 1e-10, "y coord of move by vector matches");
	}
	
	@DataProvider
	public Object[][] sampleVelocities() 
	{
		return new Object[][] {
				new Object[] { Velocity.create(0, 0) },
				new Object[] { Velocity.create(1.2, 0) },
				new Object[] { Velocity.create(0, 3.4) },
				new Object[] { Velocity.create(1, 3.4) },
				new Object[] { Velocity.create(-0.4, 1.4) },
				new Object[] { Velocity.create(-2.2, -3.1) },
				new Object[] { Velocity.create(1.6, -0.9) },
				new Object[] { Velocity.create(0, -2.2) },
				new Object[] { Velocity.create(-1.6, 0) }
		};
	}
	
}
