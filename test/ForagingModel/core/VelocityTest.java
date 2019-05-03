package ForagingModel.core;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ForagingModel.core.NdPoint;

public class VelocityTest 
{
	@Test(dataProvider = "fromTo")
	public void testFromTo(NdPoint from, NdPoint to, double speed)
	{
		Velocity velocity = Velocity.create(from, to, speed);
		
		Assert.assertEquals(velocity.mod(), speed, 1e-10, "speed of created velocity correct");
		double expectedAngle = Math.atan2(to.getY() - from.getY(), to.getX() - from.getX());
		Assert.assertEquals(velocity.arg(), expectedAngle, 1e-10, "angle of created velocity correct");
	}
	
	@DataProvider
	public Object[][] fromTo() 
	{
		return new Object[][] {
				new Object[] { new NdPoint(1.1, 1.7), new NdPoint(1, 1), 0.5 },
				new Object[] { new NdPoint(1, 1), new NdPoint(2.3, 0.9), 1.2 },
				new Object[] { new NdPoint(0, 0), new NdPoint(1, 1), 2 }
		};
	}

}
