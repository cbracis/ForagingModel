package ForagingModel.agent.movement;

import org.testng.Assert;
import org.testng.annotations.DataProvider;

import ForagingModel.core.NdPoint;
import ForagingModel.core.Velocity;

public abstract class MovementTest 
{
	@DataProvider
	public Object[][] movementsOutsideBoundary() 
	{
		return new Object[][] {
				new Object[] { Velocity.create(0.1, 5), new NdPoint(35, 99) }, //top
				new Object[] { Velocity.create(-5, 5), new NdPoint(1, 99) }, //top left
				new Object[] { Velocity.create(-4.5, 1.2), new NdPoint(0.5, 25) }, //left
				new Object[] { Velocity.create(-5, -5), new NdPoint(1, 0.74) }, //bottom left
				new Object[] { Velocity.create(0.3, -5.5), new NdPoint(67.2, 1) }, //bottom
				new Object[] { Velocity.create(5.6, -4.5), new NdPoint(98.4, 1.2) }, //bottom right
				new Object[] { Velocity.create(3.5, 1), new NdPoint(99.5, 39) }, //right
				new Object[] { Velocity.create(3.2, 4.3), new NdPoint(99, 99) } //top right
		};
	}
	
	@DataProvider
	public Object[][] intervals() 
	{
		return new Object[][] {
				new Object[] { 0.001 },
				new Object[] { 0.01 },
				new Object[] { 0.1 },
				new Object[] { 1 },
				new Object[] { 2 },
				new Object[] { 10 }
		};
	}

	protected void assertBoundaries(NdPoint location, double lowerBound, double upperBound)
	{
		Assert.assertTrue(location.getX() > lowerBound, "bounded left: " + location.getX());
		Assert.assertTrue(location.getX() < upperBound, "bounded right: " + location.getX());
		Assert.assertTrue(location.getY() > lowerBound, "bounded bottom: " + location.getY());
		Assert.assertTrue(location.getY() < upperBound, "bounded top: " + location.getY());

	}
}
