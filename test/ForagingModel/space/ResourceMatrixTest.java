package ForagingModel.space;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ForagingModel.core.GridPoint;
import ForagingModel.core.MatrixUtils;
import ForagingModel.core.NdPoint;
import ForagingModel.core.TestUtilities;
import ForagingModel.input.CellData;
import ForagingModel.input.InputFactory;
import ForagingModel.input.ResourceLandscapeReader;
import ForagingModel.schedule.ScheduleFactory;

public class ResourceMatrixTest 
{
	private RealMatrix mockResourceValues;

	@BeforeClass
	public void setUp()
	{
		mockResourceValues = Mockito.mock(RealMatrix.class);
		Mockito.when(mockResourceValues.getRowDimension()).thenReturn(2);
		Mockito.when(mockResourceValues.getColumnDimension()).thenReturn(2);
		Mockito.when(mockResourceValues.copy()).thenReturn(mockResourceValues);
	}

	@Test
	public void testConsumption()
	{
		RealMatrix resourceValues = new Array2DRowRealMatrix(new double[][] { {2.0, 2.0}, {2.0, 2.0} });
		double initialResources = MatrixUtils.sum(resourceValues);
		ResourceMatrix resources = new ResourceMatrix(resourceValues, 0.5, 1);

		double consumption = resources.consumeResource(new NdPoint(0.5, 0.5), 5, 1);
		double resourceChange = initialResources - MatrixUtils.sum(resourceValues);
		Assert.assertEquals(consumption, resourceChange, "Amount consumed equals the change across all the resources");
	}

	@Test
	public void testAvgConsumtionRate()
	{
		int size = 10;
		RealMatrix resourceValues = new Array2DRowRealMatrix(size, size);
		MatrixUtils.normalize(resourceValues);
		ResourceMatrix resources = new ResourceMatrix(resourceValues, 0.5, 1);

		NdPoint center = new NdPoint(size/2, size/2);
		double avgQuality = 1.0 / size / size; // sums to 1

		double totalConsumption = 0;
		for (int i = 0; i < size; i++)
		{
			for (int j = 0; j < size; j ++)
			{
				double dist = SpaceUtils.getDistance(center, new GridPoint(i, j));
				totalConsumption += Math.exp(-dist * dist);
			}
		}
		totalConsumption *= avgQuality / 2 / Math.PI; // quality and normal const.

		double rate = resources.calculateAvgConsumptionRate(1, 1);

		Assert.assertEquals(rate, totalConsumption, 1e-10, "expected avg consumtion");
	}
	
	@Test
	public void testAvgConsumtionRateAcrossParameters()
	{
		ResourceAssemblage resources = SpaceFactory.generateTwoPatchResource(100, 100, ScheduleFactory.createNoOpScheduler());
		
		// different spatial scales should give similar results for flat grid if scale << grid size
		double rate = resources.calculateAvgConsumptionRate(2, 1);
		Assert.assertEquals(resources.calculateAvgConsumptionRate(2, 0.5), rate, 1e-5, "same avg consumption rate for different spatial scales, 0.5");
		Assert.assertEquals(resources.calculateAvgConsumptionRate(2, 1.5), rate, 1e-5, "same avg consumption rate for different spatial scales, 1.5");
		Assert.assertEquals(resources.calculateAvgConsumptionRate(2, 5), rate, 1e-5, "same avg consumption rate for different spatial scales, 5");
		Assert.assertEquals(resources.calculateAvgConsumptionRate(2, 10), rate, 1e-5, "same avg consumption rate for different spatial scales, 10");
		
		// different consumption rates should be multiplicative
		rate = resources.calculateAvgConsumptionRate(1, 1);
		Assert.assertEquals(resources.calculateAvgConsumptionRate(0.5, 1), 0.5 * rate, 1e-5, "avg consumption rate proportional for different consumption rates, 0.5");
		Assert.assertEquals(resources.calculateAvgConsumptionRate(1.5, 1), 1.5 * rate, 1e-5, "avg consumption rate proportional for different consumption rates, 1.5");
		Assert.assertEquals(resources.calculateAvgConsumptionRate(5, 1), 5 * rate, 1e-5, "avg consumption rate proportional for different consumption rates, 5");
		Assert.assertEquals(resources.calculateAvgConsumptionRate(10, 1), 10 * rate, 1e-5, "avg consumption rate proportional for different consumption rates, 10");
	}
	
	@Test
	public void testAvgConsumptionRateCompareToRCode()
	{
		// patches don't matter, just grid size since we're using a uniform grid all of average quality (sums to 1)
		// compare with R code
		// temp = MakePatchyWorld(x = seq(-4.5, 4.5, len = 10), y = seq(-4.5, 4.5, len = 10), npatches = 5, radius = 2, steepness = 0.5)
		// sum(getdQ_consumption(0, temp*0 + mean(temp), 1, 1))
		ResourceAssemblage resources = SpaceFactory.generateTwoPatchResource(10, 10, ScheduleFactory.createNoOpScheduler());
		double avgConsumption = resources.calculateAvgConsumptionRate(1, 1);
		Assert.assertEquals(avgConsumption, 0.004998966, 1e-9, "Average consumption matches that from R code");
	}
	
	@Test
	public void testConsumptionDiffSizeLandscape() throws URISyntaxException
	{
		ResourceLandscapeReader reader = InputFactory.createResourceLandscapeReader();

		// big is just 5x5 version of small
		List<CellData> data = reader.readLandscapeFile(new File( ClassLoader.getSystemResource("land82.csv").toURI()), 0);
		ResourceAssemblage small = SpaceFactory.generateResource(data, ScheduleFactory.createNoOpScheduler());

		data = reader.readLandscapeFile(new File( ClassLoader.getSystemResource("landTransLoc82.csv").toURI()), 0);
		ResourceAssemblage big = SpaceFactory.generateResource(data, ScheduleFactory.createNoOpScheduler());
		
		// (40, 15) is maximum resource value
		NdPoint loc = new NdPoint(40.2, 15.2);
		GridPoint gridLoc = SpaceUtils.getGridPoint(loc);
		
		double smallAmt = small.consumeResource(loc, 1, 1);
		double bigAmt = big.consumeResource(loc, 1, 1);
		
		Assert.assertEquals(small.getIntrinsicQuality(gridLoc), big.getIntrinsicQuality(gridLoc), "Quality same");
		Assert.assertEquals(smallAmt, bigAmt, "Consumption same");
		Assert.assertEquals(small.getAverageQuality(), big.getAverageQuality(), 1e-10, "Average quality same");
	}

	@Test
	public void testRegeneration() 
	{
		double K = 0.2;
		double rate = 0.1;
		double initial = 0.1;

		RealMatrix resourceValues = new Array2DRowRealMatrix(new double[][] { {initial} });
		RealMatrix carryingCapacity = new Array2DRowRealMatrix(new double[][] { {K} });
		ResourceMatrix resources = new ResourceMatrix(resourceValues, carryingCapacity, rate, 1);

		resources.growResource();

		double newQuality = initial + rate * (K - initial) * initial / K;
		Assert.assertEquals(resourceValues.getEntry(0,  0), newQuality, "1 step of regeneration");
	}

	@Test
	public void testRegenerationFromZero()
	{
		RealMatrix resourceValues = new Array2DRowRealMatrix(new double[][] { {0} });
		ResourceMatrix resources = new ResourceMatrix(resourceValues, 0.5, 1);
		resources.growResource();
		Assert.assertEquals(resourceValues.getEntry(0,  0), 0, 1e-10, "Zero capacity resource shouldn't grow");

		resourceValues = new Array2DRowRealMatrix(new double[][] { {0} });
		RealMatrix carryingCapacity = new Array2DRowRealMatrix(new double[][] { {0.33} });
		resources = new ResourceMatrix(resourceValues, carryingCapacity, 0.5, 1);
		resources.growResource();
		Assert.assertEquals(resourceValues.getEntry(0,  0), 0, 1e-10, "Resource that gets down to zero shouldn't blow up");
	}

	@Test
	public void testRegenerationDoesntExceedCarryingCapacity()
	{
		double K = 0.512;
		RealMatrix resourceValues = new Array2DRowRealMatrix(new double[][] { {K - 0.01} });
		RealMatrix carryingCapacity = new Array2DRowRealMatrix(new double[][] { {K} });
		ResourceMatrix resources = new ResourceMatrix(resourceValues, carryingCapacity, 0.5, 1);

		for (int i = 0; i < 10000; i++)
		{
			resources.growResource();
		}
		Assert.assertTrue(resourceValues.getEntry(0,  0) <= K, "Regeneration should never exceen max quality Q0");
	}

	@Test
	public void testDepletion()
	{
		double dist = 3.4;
		double betaC = 5.0;
		double gammaC = 2.5;
		double quality = 0.1;
		double regenerationRate = 0.5;
		double intervalSize = 1;

		ResourceMatrix resources = new ResourceMatrix(mockResourceValues, regenerationRate, intervalSize);

		double amount = resources.depleteResource(quality, quality, dist, betaC, gammaC);
		double expectedAmount = betaC * Math.exp(-dist * dist/gammaC) / (2 * Math.PI * gammaC) * quality;

		Assert.assertEquals(amount, expectedAmount, "amount depleted");
	}

	@Test
	public void testDepletionZeroQuality()
	{
		double quality = 0;

		ResourceMatrix resources = new ResourceMatrix(mockResourceValues, 0.5, 1);

		double amount = resources.depleteResource(quality, quality, 0.2, 3, 1);

		Assert.assertEquals(amount, 0.0, "nothing depleted");
	}

	@Test
	public void testDepletionStaysPositive()
	{
		ResourceMatrix resources = new ResourceMatrix(mockResourceValues, 0.5, 1);

		double quality = 0.5;
		for (int i = 0; i < 10000; i++)
		{
			quality -= resources.depleteResource(quality, 0.5, 0.1, 5, 1);
		}
		Assert.assertTrue(quality > 0, "quality still above 0");
	}

	
	  @Test
	  public void testTimeStepGrow()
	  {
		  // TODO: why did need to increase threshold with change of matrix provider? 
		  // cern colt did have issues with small values but that wouldn't seem to be applicable here
		  
		  RealMatrix k = TestUtilities.createRandomMatrix(5, 5);
		  MatrixUtils.normalize(k);
		  RealMatrix init = k.copy();
		  MatrixUtils.multiply(init, 0.1);
		  
		  ResourceMatrix resourceTs1 = new ResourceMatrix(init.copy(), k, 0.2, 1);
		  resourceTs1.growResource();
		  
		  // time step 0.1
		  ResourceMatrix resourceTsPoint1 = new ResourceMatrix(init.copy(), k, 0.2, 0.1);
		  for (int i = 0; i < 10; i++)
		  {
			  resourceTsPoint1.growResource();
		  }
		  // since growth is non-linear, these don't exactly match. 
		  TestUtilities.compareMatrix(resourceTsPoint1.getResourceMatrix(), resourceTs1.getResourceMatrix(), 1e-3);
		  
		  // time step 2
		  ResourceMatrix resourceTs2 = new ResourceMatrix(init.copy(), k, 0.2, 2);
		  resourceTs2.growResource();
		  resourceTs1.growResource(); // grow again to match 2 time steps
		  
		  // larger time step so slightly less accurate
		  TestUtilities.compareMatrix(resourceTs2.getResourceMatrix(), resourceTs1.getResourceMatrix(), 1e-3);
	  }
	  
	  @Test
	  public void testTimeStepDeplete()
	  {
		  RealMatrix resource = TestUtilities.createRandomMatrix(5, 5);
		  MatrixUtils.normalize(resource);
		  NdPoint location = new NdPoint(2.1, 3.4);
		  
		  ResourceMatrix resourceTs1 = new ResourceMatrix(resource, resource, 0.2, 1);
		  resourceTs1.consumeResource(location, 1, 1);
		  
		  // time step 0.1
		  ResourceMatrix resourceTsPoint1 = new ResourceMatrix(resource, resource, 0.2, 0.1);
		  for (int i = 0; i < 10; i++)
		  {
			  resourceTsPoint1.consumeResource(location, 1, 1);
		  }
		  Assert.assertEquals(resourceTsPoint1.getResourceMatrix(), resourceTs1.getResourceMatrix(), "time step 0.1");
		  
		  // time step 2
		  ResourceMatrix resourceTs2 = new ResourceMatrix(resource, resource, 0.2, 2);
		  resourceTs2.consumeResource(location, 1, 1);
		  resourceTs1.consumeResource(location, 1, 1); // consume again to match 2 time steps
		  
		  Assert.assertEquals(resourceTs2.getResourceMatrix(), resourceTs1.getResourceMatrix(), "time step 2");
	  }
	  
	  @Test
	  public void testMax()
	  {
		  int size = 100;
		  RealMatrix resource = TestUtilities.createRandomMatrix(size, size);
		  ResourceMatrix resourceMatrix = new ResourceMatrix(resource, resource, 0.2, 1);
		  
		  double max = Double.NEGATIVE_INFINITY;
		  for (int i = 0; i < size; i++)
		  {
			  for (int j = 0; j < size; j++)
			  {
				  if (resource.getEntry(i, j) > max)
				  {
					  max = resource.getEntry(i, j);
				  }
			  }
		  }

		  Assert.assertEquals(resourceMatrix.getMaxQuality(), max, "Maximum quality");
	  }
	  
	  @Test
	  public void testNonSquareMatrix()
	  {
		  RealMatrix resource = TestUtilities.createRandomMatrix(150, 300);
		  ResourceMatrix resourceMatrix = new ResourceMatrix(resource, resource, 0.2, 1);
		  
		  resourceMatrix.calculateAvgConsumptionRate(1, 1);
		  resourceMatrix.consumeResource(new NdPoint(100, 200), 1, 1);
		  resourceMatrix.growResource();
		  resourceMatrix.reportCurrentState();
	  }
	  
}
