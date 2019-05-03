package ForagingModel.space;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ForagingModel.core.GridPoint;
import ForagingModel.core.NdPoint;

public class SpaceUtilsTest 
{
	@Test(dataProvider = "sampleDistancesGrid")
	public void testGetDistance(NdPoint point1, GridPoint point2, Double distance)
	{
		Assert.assertEquals(SpaceUtils.getDistance(point1, point2), distance, 1e-10, "calculate distance (NdPoint to GridPoint)");
	}
	
	@DataProvider
	public Object[][] sampleDistancesGrid() 
	{
		return new Object[][] {
				new Object[] { new NdPoint(1.5, 1.5), new GridPoint(1, 1), 0.0 },
				new Object[] { new NdPoint(1.5, 1.5), new GridPoint(2, 1), 1.0 },
				new Object[] { new NdPoint(0.5, 0.5), new GridPoint(1, 1), Math.sqrt(2) }
		};
	}
	
	@Test(dataProvider = "sampleDistancesNd")
	public void testGetDistance(NdPoint point1, NdPoint point2, Double distance)
	{
		Assert.assertEquals(SpaceUtils.getDistance(point1, point2), distance, 1e-10, "calculate distance (NdPoint to NdPoint)");
	}
	
	@DataProvider
	public Object[][] sampleDistancesNd() 
	{
		return new Object[][] {
				new Object[] { new NdPoint(1.1, 1.3), new NdPoint(1.1, 1.3), 0.0 },
				new Object[] { new NdPoint(1, 1), new NdPoint(2, 1), 1.0 },
				new Object[] { new NdPoint(1.2, 1.2), new NdPoint(2.2, 2.2), Math.sqrt(2) }
		};
	}

	@Test(dataProvider = "samplePoints")
	public void testGetGridPoint(NdPoint point, GridPoint expectedPoint)
	{
		Assert.assertEquals(SpaceUtils.getGridPoint(point), expectedPoint, "convert to grid point");
	}
	
	@DataProvider
	public Object[][] samplePoints() 
	{
		return new Object[][] {
				new Object[] { new NdPoint(1.1, 1.9), new GridPoint(1, 1)},
				new Object[] { new NdPoint(0.7, 2.3), new GridPoint(0, 2)},
				new Object[] { new NdPoint(3.999, 0.0001), new GridPoint(3, 0)},
				new Object[] { new NdPoint(1, 1), new GridPoint(1, 1)}
		};
	}
	
	@Test(dataProvider = "samplePointsArray")
	public void testToArray(NdPoint point, double[] expectedArray)
	{
		Assert.assertEquals(SpaceUtils.toArray(point), expectedArray, "convert to array");
	}
	
	@DataProvider
	public Object[][] samplePointsArray() 
	{
		return new Object[][] {
				new Object[] { new NdPoint(1.0, 2.5), new double[] { 1.0, 2.5} },
				new Object[] { null, null }
		};
	}
	
	@Test(dataProvider = "inBounds")
	public void testInBounds(GridPoint point, double min, double max, boolean expected)
	{
		Assert.assertEquals(SpaceUtils.inBounds(point, min, min, max, max), expected);
	}
	
	@DataProvider
	public Object[][] inBounds() 
	{
		return new Object[][] {
				new Object[] { new GridPoint(0, 10), 0, 50, true },
				new Object[] { new GridPoint(15, 0), 0, 50, true },
				new Object[] { new GridPoint(49, 11), 0, 50, true },
				new Object[] { new GridPoint(45, 49), 0, 50, true },
				new Object[] { new GridPoint(-1, 10), 0, 50, false },
				new Object[] { new GridPoint(3, -3), 0, 50, false },
				new Object[] { new GridPoint(34, 50), 0, 50, false },
				new Object[] { new GridPoint(50, 19), 0, 50, false },
				new Object[] { new GridPoint(0, 0), 0, 50, true },
				new Object[] { new GridPoint(49, 49), 0, 50, true }
		};
	}

}
