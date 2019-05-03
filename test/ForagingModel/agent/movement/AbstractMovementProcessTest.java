package ForagingModel.agent.movement;

import java.util.Arrays;
import java.util.HashSet;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ForagingModel.agent.movement.AbstractMovementProcess.AngleBounds;
import ForagingModel.agent.movement.AbstractMovementProcess.Bounds;
import ForagingModel.core.Angle;
import ForagingModel.core.NdPoint;

public class AbstractMovementProcessTest 
{
	@Test
	public void testGetNewRandomAngleRegular()
	{
		AbstractMovementProcessHelper movement = new AbstractMovementProcessHelper();
		
		for (Bounds bounds : Bounds.values())
		{
			if (bounds != Bounds.Left && bounds != Bounds.None)
			{
				AngleBounds angleBounds = movement.getNewAngleBounds(bounds);
				
				// just try a bunch
				for (int i = 0; i< 100; i++)
				{
					Angle angle = movement.getNewRandomAngle(bounds);
					Assert.assertTrue(angle.get() > angleBounds.minAngle, 
							String.format("Angle %2f greater than %2f min for bound %s", 
									angle.get(), angleBounds.minAngle, bounds.name()));
					Assert.assertTrue(angle.get() < angleBounds.maxAngle, 
							String.format("Angle %2f less than %2f max for bound %s", 
									angle.get(), angleBounds.maxAngle, bounds.name()));
				}
			}
		}
	}
	
	@Test
	public void testGetNewRandomAngleOverlapsZero()
	{
		AbstractMovementProcessHelper movement = new AbstractMovementProcessHelper();

		// just try a bunch
		for (int i = 0; i< 100; i++)
		{
			Angle angle = movement.getNewRandomAngle(Bounds.Left);
			if (angle.get() < Math.PI)
			{
				Assert.assertTrue(angle.get() > 0.0, " Angle greater than 0: " + angle.get());
				Assert.assertTrue(angle.get() < Math.PI / 2, " Angle less than pi/2: " + angle.get());
			} else
			{
				Assert.assertTrue(angle.get() > 3 * Math.PI / 2, " Angle greater than 3pi/2: " + angle.get());
				Assert.assertTrue(angle.get() < 2 * Math.PI, " Angle less than 2pi: " + angle.get());

			}
		}

	}
	
	@Test(dataProvider = "awayAngles")
	public void testGetAngleAway(NdPoint currentLocation, NdPoint[] predators, double expectedAngle)
	{
		AbstractMovementProcessHelper movement = new AbstractMovementProcessHelper();
		Assert.assertEquals(movement.getAngleAway(currentLocation, new HashSet<NdPoint>(Arrays.asList(predators))).get(), expectedAngle, 1e-10);
	}
	
	@DataProvider
	public Object[][] awayAngles() 
	{
		return new Object[][] {
				new Object[] { new NdPoint(2, 2), new NdPoint[] { new NdPoint(3, 2) }, Math.PI },
				new Object[] { new NdPoint(2, 2), new NdPoint[] { new NdPoint(1, 1) }, Math.PI / 4},
				new Object[] { new NdPoint(2, 2), new NdPoint[] { new NdPoint(1, 1), new NdPoint(1, 3) }, 0 }
		};
	}

}
