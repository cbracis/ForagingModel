package ForagingModel.core;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class AngleTest 
{
	@Test(dataProvider = "createAngles")
	public void testCreate(double testValue, double expectedValue)
	{
		Angle angle = new Angle(testValue);
		Assert.assertEquals(angle.get(), expectedValue, "test " + testValue);
	}

	@DataProvider
	public Object[][] createAngles() 
	{
		return new Object[][] {
				new Object[] { 0, 0 },
				new Object[] { -Math.PI, Math.PI },
				new Object[] { 2 * Math.PI, 0 },
				new Object[] { 1.5, 1.5 },
				new Object[] { -1.5, 2 * Math.PI - 1.5 },
				new Object[] { 5 * Math.PI / 2, Math.PI / 2 }
		};
	}

	@Test(dataProvider = "combineAngles")
	public void testCombine(double angle1, double angle2, double persistance, double expected)
	{
		Angle angle = Angle.combine(angle1, angle2, persistance);
		Assert.assertEquals(angle.get(), expected, 1e-10, "combine angles");
	}

	@DataProvider
	public Object[][] combineAngles() 
	{
		return new Object[][] {
				new Object[] { 0, 0, 0.5, 0 },
				new Object[] { Math.PI, Math.PI, 0.5, Math.PI },
				new Object[] { Math.PI, Math.PI / 2, 0.5, 3 * Math.PI / 4 },
				new Object[] { 0, 3 * Math.PI / 2, 0.5, 7 * Math.PI / 4 },
				new Object[] { 1.3, 4.5, 1, 1.3 },
				new Object[] { 2.1, 5.6, 0, 5.6 },
				new Object[] { 1.0, 2.0, 0.75, 1.233353373 } // compute expected in R
		};
	}

	@Test(dataProvider = "diffAngles")
	public void testAbsDiff(double angle1, double angle2, double expected)
	{
		Angle angle = Angle.absDifference(new Angle(angle1), new Angle(angle2));
		Assert.assertEquals(angle.get(), expected, 1e-10, "diff angles");
	}

	@DataProvider
	public Object[][] diffAngles() 
	{
		return new Object[][] {
				new Object[] { 0, 0, 0 },
				new Object[] { Math.toRadians(350), Math.toRadians(10), Math.toRadians(20) },
				new Object[] { Math.toRadians(10), Math.toRadians(350), Math.toRadians(20) },
				new Object[] { 0, Math.PI, Math.PI },
				new Object[] { Math.toRadians(60), Math.toRadians(90), Math.toRadians(30) },
		};
	}

}
