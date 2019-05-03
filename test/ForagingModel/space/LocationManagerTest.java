package ForagingModel.space;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ForagingModel.core.ForagingModelException;
import ForagingModel.core.GridPoint;
import ForagingModel.core.NdPoint;
import ForagingModel.agent.Agent;
import ForagingModel.agent.AgentFactoryTestHelper;
import ForagingModel.agent.Forager;
import ForagingModel.agent.Recorder;
import ForagingModel.agent.movement.MovementBehavior;
import ForagingModel.core.Velocity;

public class LocationManagerTest 
{
	@Test
	public void testMovementBoundary()
	{
		LocationManager locationManager = SpaceFactory.createLocationManager(100);

		ResourceAssemblage resource = Mockito.mock(ResourceAssemblage.class);
		MovementBehavior movement = Mockito.mock(MovementBehavior.class);
		Mockito.when(movement.getNextVelocity(Mockito.any(NdPoint.class), Mockito.anyDouble())).thenReturn(Velocity.createPolar(2, 0));
		Recorder recorder = Mockito.mock(Recorder.class);
		
		Forager forager = AgentFactoryTestHelper.createForager(locationManager, resource, movement, recorder, 1, 2, 1);
		locationManager.moveTo(forager, new NdPoint(97.6, 5.1)); // so will end up almost at the boundary but not quite

		forager.move();
		
		Assert.assertEquals(locationManager.getLocation(forager), new NdPoint(99.6, 5.1), "moved to location close to boundary");
		Assert.assertEquals(SpaceUtils.getGridPoint(locationManager.getLocation(forager)), new GridPoint(99, 5), "grid location");
	}
	
	@Test(expectedExceptions = {ForagingModelException.class})
	public void testMoveByVectorCrossingBoundaryThrowsException()
	{
		LocationManager locationManager = SpaceFactory.createLocationManager(10);
		Agent agent = Mockito.mock(Agent.class);
		locationManager.moveTo(agent, new NdPoint(1, 1));
		
		locationManager.moveByVector(agent, Velocity.create(-2, -2));
	}
	
	@Test(expectedExceptions = {ForagingModelException.class})
	public void testMoveToCrossingBoundaryThrowsException()
	{
		LocationManager locationManager = SpaceFactory.createLocationManager(10);
		
		Agent agent = Mockito.mock(Agent.class);
		locationManager.moveTo(agent, new NdPoint(1, 10.1));
	}
	
	@Test
	public void testGetAgents()
	{
		LocationManager locationManager = SpaceFactory.createLocationManager(10);
		Agent agent1 = Mockito.mock(Agent.class);
		Agent agent2 = Mockito.mock(Agent.class);
		locationManager.moveTo(agent1, new NdPoint(2, 3));
		locationManager.moveTo(agent2, new NdPoint(5, 5));
		
		Assert.assertEqualsNoOrder(locationManager.getAgents().toArray(), new Object[] {agent1, agent2});
	}
	
	@Test(expectedExceptions = {ForagingModelException.class}, dataProvider = "locations")
	public void testCheckBounds(NdPoint location)
	{
		LocationMangerImpl locationManager = (LocationMangerImpl) SpaceFactory.createLocationManager(10, 15);
		locationManager.checkBounds(location);
	}
	
	@DataProvider
	public Object[][] locations() 
	{
		return new Object[][] {
				new Object[] { new NdPoint(-1, 5) },
				new Object[] { new NdPoint(3.3, -0.5) },
				new Object[] { new NdPoint(10.1, 1) },
				new Object[] { new NdPoint(3, 15.2) }
		};
	}
}
