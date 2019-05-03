package ForagingModel.space;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ForagingModel.agent.movement.AbstractMovementProcess.AngleBounds;
import ForagingModel.agent.movement.MovementFactoryTestHelper;
import ForagingModel.core.Angle;
import ForagingModel.core.DirectionProbabalistic;
import ForagingModel.core.DirectionProbabilityInfo;
import ForagingModel.core.MatrixUtils;
import ForagingModel.core.NdPoint;
import ForagingModel.predator.PredatorManager;

public class AbstractMemoryTest 
{

	@Test(dataProvider = "angles")
	public void testGetRandomAngleInSector(int sectorIdx, int numSectors, double lowerLim, double upperLim)
	{
		AbstractMemory memory = new AbstractMemoryHelper(AngularProbabilityInfo.create(10, 1, numSectors));
		
		//just run 100 cases and verify within limits
		for (int i = 0; i < 100; i++)
		{
			Angle angle = memory.getRandomAngleInSector(sectorIdx, numSectors);
			Assert.assertTrue(angle.get() < upperLim, String.format("%1f < %2f for sector %3d of %4d", 
					angle.get(), upperLim, sectorIdx, numSectors));
			Assert.assertTrue(angle.get() >= lowerLim, String.format("%1f > %2f for sector %3d of %4d", 
					angle.get(), lowerLim, sectorIdx, numSectors));
		}
	}
	
	@DataProvider
	public Object[][] angles() 
	{
		return new Object[][] {
				new Object[] { 0, 4, 0, Math.PI / 2 },
				new Object[] { 1, 4, Math.PI / 2, Math.PI },
				new Object[] { 2, 4, Math.PI, 3 * Math.PI / 2 },
				new Object[] { 3, 4, 3 * Math.PI / 2, 2 * Math.PI },
				new Object[] { 0, 360, 0, Math.toRadians(1) },
				new Object[] { 359, 360, Math.toRadians(359), Math.toRadians(360) }
		};
	}
	
	@Test(dataProvider = "minMax")
	public void testGetDirectionProbabalisticMinMax(double min, double max, int numSectors)
	{
		AbstractMemory memory = new AbstractMemoryHelper(AngularProbabilityInfo.create(10, 1, numSectors));
		NdPoint location = new NdPoint(2,  2);
		
		//just run 100 cases and verify within limits
		for (int i = 0; i < 100; i++)
		{
			Angle angle = memory.getDirectionProbabalistic(location, MovementFactoryTestHelper.createAngleBounds(min, max)).angle();
			Assert.assertTrue(angle.get() <= max, String.format("%1f < %2f, i=%3d", angle.get(), max, i));
			Assert.assertTrue(angle.get() >= min, String.format("%1f > %2f, i=%3d", angle.get(), min, i));
		}
	}
	
	@DataProvider
	public Object[][] minMax() 
	{
		return new Object[][] {
				new Object[] { 0, Math.PI, 360 },
				new Object[] { Math.PI, 3 * Math.PI / 2, 360 },
				new Object[] { Math.PI, 3 * Math.PI / 2, 8 },
				new Object[] { Math.toRadians(350), Math.toRadians(353), 360 },
				new Object[] { 0, 2 * Math.PI - 0.00001, 360 }, // 2pi becomes 0 (modulus)
		};
	}

	@Test(dataProvider = "minMaxOverlap")
	public void testGetDirectionProbabalisticMinMaxOverlap(double belowZero, double aboveZero, int numSectors)
	{
		AbstractMemory memory = new AbstractMemoryHelper(AngularProbabilityInfo.create(10, 1, numSectors));
		NdPoint location = new NdPoint(2,  2);
		AngleBounds bounds = MovementFactoryTestHelper.createAngleBounds(belowZero, aboveZero);
		
		//just run 100 cases and verify within limits
		// but this time intervals that overlap 0
		for (int i = 0; i < 100; i++)
		{
			Angle angle = memory.getDirectionProbabalistic(location, bounds).angle();
			double angleTolerance = 1e-6;
			
			// use ANGLE_TOLERANCE, 1e-6, due to rounding issues creating angles instead of just 0
			if (angle.get() < Math.PI)
			{
				Assert.assertTrue(angle.get() > 0.0, String.format("Angle %1f above 0, i=%d", angle.get(), i));
				Assert.assertTrue(angle.get() - aboveZero <= angleTolerance, 
						String.format("Angle %1f below %2f, i=%d", angle.get(), aboveZero, i));
			} else
			{
				Assert.assertTrue(angle.get() > belowZero, 
					      String.format("Angle %1f above %2f, i=%d", angle.get(), belowZero, i));
				Assert.assertTrue(angle.get() < 2 * Math.PI,
						String.format("Angle %1f below 2 pi, i=%d", angle.get(), i));
			}
		}
	}
	
	@DataProvider
	public Object[][] minMaxOverlap() 
	{
		return new Object[][] {
				new Object[] { 3 * Math.PI / 2, Math.PI / 2, 360 }, 
				new Object[] { Math.toRadians(357), Math.toRadians(3), 360 },
		};
	}
	
	public void testGetRandomAngleInSector()
	{
		AbstractMemory memory = new AbstractMemoryHelper(AngularProbabilityInfo.create(10, 1, 360));
		Angle angle = memory.getRandomAngleInSector(352, 360, Math.toRadians(353));
		Assert.assertTrue(angle.get() < Math.toRadians(352), angle.get() + " < upper lim " + Math.toRadians(353));
		Assert.assertTrue(angle.get() >= Math.toRadians(353), angle.get() + " >= lower lim " + Math.toRadians(352));

	}
	
	@Test(dataProvider = "matrices")
	public void testNormalizeMatrix(RealVector matrix)
	{
		AbstractMemory memory = new AbstractMemoryHelper(AngularProbabilityInfo.create());
		memory.normalizeVector(matrix);
		Assert.assertEquals(MatrixUtils.sum(matrix), 1.0, 1e-10, "matrix sums to 1");
	}

	@DataProvider
	public Object[][] matrices() 
	{
		RealVector smallest = new ArrayRealVector(100, 0);
		smallest.setEntry(0, Double.MIN_VALUE);
		
		return new Object[][] {
				new Object[] { new ArrayRealVector(100, 0) }, 
				new Object[] { new ArrayRealVector(100, 1e-300) },
				new Object[] { new ArrayRealVector(100, 1e-320) },
				new Object[] { new ArrayRealVector(100, Double.MIN_VALUE) },
				new Object[] { smallest },
		};
	}
	
	@Test
	public void testEarthMoversDistance()
	{
		RealVector uniform = new ArrayRealVector(360, 1.0 / 360.0);
		
		AbstractMemory memory = new AbstractMemoryHelper(AngularProbabilityInfo.create());
		
		Assert.assertEquals(memory.getEarthMoversDistance(uniform), 0, 1e-15, "uniform distribution"); 
		
		for (int i = 0; i < 360; i += 10)
		{
			RealVector prop = new ArrayRealVector(360);
			prop.setEntry(i, 1);
			Assert.assertEquals(memory.getEarthMoversDistance(prop), 0.25, 1e-15, "all probability at " + i);
		}		
		
	}
	
	@Test
	public void testTopHatDistance()
	{
		double radius = 5;
		AngularProbabilityInfo angInfo = AngularProbabilityInfo.create();
		AbstractMemory memory = new AbstractMemoryHelper(angInfo);
		RealVector topHat = memory.getTopHatDistanceFactor(10, radius);
		RealVector distances = angInfo.getSampleDistances();
		double expected = 1 / Math.PI / radius / radius * angInfo.getSpacing();
		
		for (int i = 0; i < distances.getDimension(); i++)
		{
			if (distances.getEntry(i) < radius)
			{
				Assert.assertEquals(topHat.getEntry(i), expected, "inside circle " + distances.getEntry(i));
			} else
			{
				Assert.assertEquals(topHat.getEntry(i), 0.0, "outside circle " + distances.getEntry(i));
			}
		}

	}
	
	@Test
	public void testReportProbabilities()
	{
		// check that each memory type reports angle & prob vector(s) correctly
		
		int gridSize = 50;
		ResourceAssemblage  mockResources = Mockito.mock(ResourceAssemblage.class);
		RealMatrix shortMemories = new Array2DRowRealMatrix(gridSize, gridSize);
		RealMatrix longMemories = new Array2DRowRealMatrix(gridSize, gridSize);
		AngularProbabilityInfo angProbInfo = AngularProbabilityInfo.create();
		ResourceMemory resourceMemory = new ResourceMemory(shortMemories, longMemories, mockResources, angProbInfo,
				0.7, 0.7, 1, 1, 0.1, 0.1, 1, 1, 1, 1);

		
		PredatorManager predatorManager = Mockito.mock(PredatorManager.class);
		RealMatrix predMem = new Array2DRowRealMatrix(gridSize, gridSize);
		PredatorMemory predMemory = SpaceFactory.createPredatorMemory(predMem, predatorManager, 
				angProbInfo, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0);
		
		AggregateMemory aggMemory = SpaceFactory.createAggregateMemory(resourceMemory, predMemory);
		
		verifyDirectionProbabilityInfo(resourceMemory.probabilityCache, null, null, null, null);
		Assert.assertSame(aggMemory.probabilityCache, resourceMemory.probabilityCache, "Aggregate memory aggregates resource cache too, so all use same");
		Assert.assertSame(aggMemory.probabilityCache, predMemory.probabilityCache, "Aggregate memory aggregates predator cache too, so all use same");

		DirectionProbabalistic direction = aggMemory.getDirectionProbabalistic(new NdPoint(5.3, 4.5));
		
		RealVector uniform = new ArrayRealVector(angProbInfo.getAngles().size(), 1.0 / angProbInfo.getAngles().size());
		MatrixUtils.normalize(uniform);
		RealVector predUniform = new ArrayRealVector(angProbInfo.getAngles().size(), 1.0); // all ~ 1 (safe)
		RealVector aggUniform = uniform.copy(); 
		aggUniform.mapMultiplyToSelf(predUniform.getEntry(0)); // times predator safety
		MatrixUtils.normalize(aggUniform);  // normalize to recreate exact same double values (almost same as uniform but not quite
		verifyDirectionProbabilityInfo(aggMemory.probabilityCache, direction.angle(), uniform, predUniform, aggUniform);
	
		direction = aggMemory.getDirectionProbabalistic(new NdPoint(2.2, 3.1)); // new angle
		
		verifyDirectionProbabilityInfo(aggMemory.probabilityCache, direction.angle(), uniform, predUniform, aggUniform);

 	}
	
	private void verifyDirectionProbabilityInfo(DirectionProbabilityInfo info, Angle angle, RealVector resourceProb, RealVector predProb, RealVector aggProb)
	{
		Assert.assertEquals(info.angle(), angle, "angle");
		Assert.assertEquals(info.foragingProbabilities(), resourceProb, "resource probabilities");
		Assert.assertEquals(info.predatorSafety(), predProb, "predator probabilities");
		Assert.assertEquals(info.aggregateProbabilities(), aggProb, "aggregate probabilities");
	}
	

}
