package ForagingModel.space;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.List;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ForagingModel.core.MatrixUtils;
import ForagingModel.core.NdPoint;
import ForagingModel.core.Parameters;
import ForagingModel.core.Parameters.Parameter;
import ForagingModel.core.TestUtilities;
import ForagingModel.input.CellData;
import ForagingModel.input.InputFactory;
import ForagingModel.input.ResourceLandscapeReader;
import ForagingModel.schedule.ScheduleFactory;
import ForagingModel.space.MemoryAssemblage.State;

public class MemoryMatrixTest 
{
	private ResourceAssemblage mockResources;
	private AngularProbabilityInfo mockAngInfo;
	
	@BeforeClass
	public void setUp()
	{
		  mockResources = Mockito.mock(ResourceAssemblage.class);
		  mockAngInfo = Mockito.mock(AngularProbabilityInfo.class);
		  
		  RealVector distances = new ArrayRealVector(new double[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 });
		  Mockito.when(mockAngInfo.getSampleDistances()).thenReturn(distances);
	}
	
	@AfterMethod
	public void tearDown()
	{
		TestUtilities.resetGenerator();
	}


	@Test
	public void testLearningHeuristic()
	{
		// in the case of same rates for short and long with factor of 1, should stay 0 since will balance
		RealMatrix shortMemories = new Array2DRowRealMatrix(new double[][] {{ 0 }});
		RealMatrix longMemories = new Array2DRowRealMatrix(new double[][] {{ 0 }});
		ResourceMemory memory = new ResourceMemory(shortMemories, longMemories, mockResources, mockAngInfo,
				0.7, 0.7, 1, 1, 0.1, 0.1, 1, 1, 1, 0, 1);
		memory.learn(0, 0, 0.1, 0.5);
		Assert.assertTrue(shortMemories.getEntry(0, 0) > 0, "short memory value increased");
		Assert.assertTrue(longMemories.getEntry(0, 0) > 0, "long memory value increased");
		Assert.assertEquals(memory.getMemoryValue(0, 0), 0.0, "memory value 0 when short and long rates the same");
		
		MatrixUtils.set(shortMemories, 0.0);
		MatrixUtils.set(longMemories, 0.0);
		memory = new ResourceMemory(shortMemories, longMemories, mockResources, mockAngInfo,
				0.7, 0.9, 1, 1, 0.1, 0.1, 1, 1, 1, 0, 1);
		memory.learn(0, 0, 0.1, 0.5);
		Assert.assertTrue(shortMemories.getEntry(0, 0) > 0, "short memory value increased");
		Assert.assertTrue(longMemories.getEntry(0, 0) > 0, "long memory value increased");
		Assert.assertTrue(memory.getMemoryValue(0, 0) > 0.0, "memory value positive when long learning rate greater");

		MatrixUtils.set(shortMemories, 0);
		MatrixUtils.set(longMemories, 0);
		memory = new ResourceMemory(shortMemories, longMemories, mockResources, mockAngInfo,
				0.3, 0.29, 1, 1, 0.1, 0.1, 1, 1, 1, 0, 1);
		memory.learn(0, 0, 0.1, 0.5);
		Assert.assertTrue(shortMemories.getEntry(0, 0) > 0, "short memory value increased");
		Assert.assertTrue(longMemories.getEntry(0, 0) > 0, "long memory value increased");
		Assert.assertTrue(memory.getMemoryValue(0, 0) < 0.0, "memory value negative when short learning rate greater");
	}

	@Test
	public void testLearning()
	{
		double factor = 2;
		double dist = 0.1;
		double quality = 0.5;

		RealMatrix shortMemories = new Array2DRowRealMatrix(new double[][] {{ 0 }});
		RealMatrix longMemories = new Array2DRowRealMatrix(new double[][] {{ 0 }});
		ResourceMemory memory = new ResourceMemory(shortMemories, longMemories, mockResources, mockAngInfo,
				1, 1, 1, 1, 0.1, 0.1, factor, 1, 1, 0, 1);
		memory.learn(0, 0, 0.1, 0.5);

		// since short and long have same rates, and factor is 2 (and no decay)
		double expected = -Math.exp(-dist * dist) / (2 * Math.PI) * quality;

		Assert.assertEquals(memory.getMemoryValue(0, 0), expected, "memory after learning");
	}

	@Test
	public void testLearningStaysBelowIntrinsicQuality()
	{
		double intrinsicQuality = 0.5;
		RealMatrix shortMemories = new Array2DRowRealMatrix(new double[][] {{ 0 }});
		RealMatrix longMemories = new Array2DRowRealMatrix(new double[][] {{ intrinsicQuality - 0.1 }});
		ResourceMemory memory = new ResourceMemory(shortMemories, longMemories, mockResources, mockAngInfo,
				0, 0.9, 1, 1, 0.1, 0.1, 1, 1, 1, 0, 1);

		for (int i = 0; i < 10000; i++)
		{
			memory.learn(0, 0, 0.1, intrinsicQuality);
		}
		Assert.assertTrue(memory.getMemoryValue(0, 0) <= intrinsicQuality, "value at intrinsic quality or below");

	}
	
	@Test
	public void testDecay()
	{
		//only decaying long since using initial value
		double decay = 0.5;
		double initial = 0.7;
		RealMatrix shortMemories = new Array2DRowRealMatrix(new double[][] {{ 0 }});
		RealMatrix longMemories = new Array2DRowRealMatrix(new double[][] {{ initial }});
		ResourceMemory memory = new ResourceMemory(shortMemories, longMemories, mockResources, mockAngInfo,
				1, 1, 1, 1, decay, decay, 1, 1, 1, 0, 1);

		Assert.assertEquals(memory.getMemoryValue(0, 0), initial, "starting value");

		memory.decay();

		Assert.assertEquals(memory.getMemoryValue(0, 0), initial * decay, "decay once"); // would be 1 - decay if decay wasn't 0.5

		memory.decay();

		Assert.assertEquals(memory.getMemoryValue(0, 0), initial * decay * decay, "decay twice");
	}
	
	@Test
	public void testDecayNever()
	{
		//only decaying long since using initial value
		double decay = 0.0;
		double initial = 0.7;
		RealMatrix shortMemories = new Array2DRowRealMatrix(new double[][] {{ 0 }});
		RealMatrix longMemories = new Array2DRowRealMatrix(new double[][] {{ initial }});
		ResourceMemory memory = new ResourceMemory(shortMemories, longMemories, mockResources, mockAngInfo,
				1, 1, 1, 1, decay, decay, 1, 1, 1, 0, 1);

		Assert.assertEquals(memory.getMemoryValue(0, 0), initial, "starting value");

		memory.decay();

		Assert.assertEquals(memory.getMemoryValue(0, 0), initial, "decay once");

		memory.decay();

		Assert.assertEquals(memory.getMemoryValue(0, 0), initial, "decay twice");
	}
	
	@Test
	public void testDecayComplete()
	{
		//only decaying long since using initial value
		double decay = 1.0;
		double initial = 0.7;
		RealMatrix shortMemories = new Array2DRowRealMatrix(new double[][] {{ 0 }});
		RealMatrix longMemories = new Array2DRowRealMatrix(new double[][] {{ initial }});
		ResourceMemory memory = new ResourceMemory(shortMemories, longMemories, mockResources, mockAngInfo,
				1, 1, 1, 1, decay, decay, 1, 1, 1, 0, 1);

		Assert.assertEquals(memory.getMemoryValue(0, 0), initial, "starting value");

		memory.decay();

		Assert.assertEquals(memory.getMemoryValue(0, 0), 0.0, "decay once");

		memory.decay();

		Assert.assertEquals(memory.getMemoryValue(0, 0), 0.0, "decay twice");
	}

	@Test
	public void testDecayDifferentRates()
	{
		double decayShort = 0.5;
		double decayLong = 0.1;

		RealMatrix shortMemories = new Array2DRowRealMatrix(new double[][] {{ 0 }});
		RealMatrix longMemories = new Array2DRowRealMatrix(new double[][] {{ 0 }});
		ResourceMemory memory = new ResourceMemory(shortMemories, longMemories, mockResources, mockAngInfo,
				1, 1, 1, 1, decayShort, decayLong, 1, 1, 1, 0, 1);

		Assert.assertEquals(memory.getMemoryValue(0, 0), 0.0, "starting value");

		memory.learn(0, 0, 0, 2 * Math.PI); 
		Assert.assertEquals(memory.getMemoryValue(0, 0), 0.0, "short and long both 1, so net is 0");

		memory.decay();

		Assert.assertEquals(memory.getMemoryValue(0, 0), (1.0 - decayLong) - (1.0 - decayShort), "after short and long decay");
	}

	@Test
	public void testDecayFromZero()
	{
		RealMatrix shortMemories = new Array2DRowRealMatrix(new double[][] {{ 0 }});
		RealMatrix longMemories = new Array2DRowRealMatrix(new double[][] {{ 0 }});
		ResourceMemory memory = new ResourceMemory(shortMemories, longMemories, mockResources, mockAngInfo,
				1, 1, 1, 1, 0.5, 0.5, 1, 1, 1, 0, 1);

		memory.decay();

		Assert.assertEquals(memory.getMemoryValue(0, 0), 0.0, "value still 0");
	}

	@Test
	public void testDecayStaysPositive()
	{
		RealMatrix shortMemories = new Array2DRowRealMatrix(new double[][] {{ 0 }});
		RealMatrix longMemories = new Array2DRowRealMatrix(new double[][] {{ 0.01 }});
		ResourceMemory memory = new ResourceMemory(shortMemories, longMemories, mockResources, mockAngInfo,
				1, 1, 1, 1, 0.9, 0.9, 1, 1, 1, 0, 1);

		for (int i = 0; i < 10000; i++)
		{
			memory.decay();
		}
		Assert.assertTrue(memory.getMemoryValue(0, 0) >= 0.0, "value 0 or above");
	}

	@Test
	public void testLearn()
	{
		RealMatrix shortMemories = new Array2DRowRealMatrix(new double[][] {{ 0, 0 }, { 0, 0 }});
		RealMatrix longMemories = new Array2DRowRealMatrix(new double[][] {{ 0, 0 }, { 0, 0 }});
		RealMatrix resources = new Array2DRowRealMatrix(new double[][] {{ 1, 1 }, { 1, 1 }});
		ResourceMatrix resourceAssemblage = new ResourceMatrix(resources, 0.5, 1);
		ResourceMemory memory = new ResourceMemory(shortMemories, longMemories, resourceAssemblage, mockAngInfo,
				1, 1, 1, 1, 0.5, 0.5, 1, 1, 1, 0, 1);

		memory.learn(new NdPoint(1.1, 0.5));
		Assert.assertEquals(shortMemories, longMemories, "same learning rate");
		Assert.assertEquals(MatrixUtils.cardinality(shortMemories), 4, "all cells non-zero");
		Assert.assertEquals(MatrixUtils.cardinality(longMemories), 4, "all cells non-zero");
	}
	
	@Test
	public void testTimeStepLearn()
	{
		RealMatrix shortMemories = new Array2DRowRealMatrix(5, 5);
		RealMatrix longMemories = new Array2DRowRealMatrix(5, 5);
		RealMatrix resources = new Array2DRowRealMatrix(5, 5);
		MatrixUtils.set(resources, 1.0 / 25.0);
		ResourceMatrix resourceAssemblage = new ResourceMatrix(resources, 0.5, 1);
		NdPoint location = new NdPoint(2.6, 2.7);
		
		ResourceMemory memoryTs1 = new ResourceMemory(shortMemories, longMemories, resourceAssemblage, mockAngInfo,
				1, 1, 1, 1, 0.5, 0.5, 1, 1, 1, 0, 1);
		ResourceMemory memoryTsPoint1 = new ResourceMemory(shortMemories.copy(), longMemories.copy(), resourceAssemblage, mockAngInfo,
				1, 1, 1, 1, 0.5, 0.5, 1, 1, 1, 0, 0.1);
		ResourceMemory memoryTs2 = new ResourceMemory(shortMemories.copy(), longMemories.copy(), resourceAssemblage, mockAngInfo,
				1, 1, 1, 1, 0.5, 0.5, 1, 1, 1, 0, 2);
		
		memoryTs1.learn(location);
		
		for (int i = 0; i < 10; i++)
		{
			memoryTsPoint1.learn(location);
		}
		TestUtilities.compareMatrix(memoryTsPoint1.getLongMemory(), memoryTs1.getLongMemory(), 5e-4);
		TestUtilities.compareMatrix(memoryTsPoint1.getShortMemory(), memoryTs1.getLongMemory(), 5e-4);

		memoryTs1.learn(location);
		memoryTs2.learn(location);

		// should these be closer even though it is non-linear?
		TestUtilities.compareMatrix(memoryTs2.getLongMemory(), memoryTs1.getLongMemory(), 1e-3);
		TestUtilities.compareMatrix(memoryTs2.getShortMemory(), memoryTs1.getLongMemory(), 1e-3);
	}
	
	@Test
	public void testTimeStepDecay()
	{
		RealMatrix resources = new Array2DRowRealMatrix(5, 5);
		MatrixUtils.set(resources, 1.0 / 25.0);
		RealMatrix shortMemories = resources.copy();
		RealMatrix longMemories = resources.copy();
		ResourceMatrix resourceAssemblage = new ResourceMatrix(resources, 0.5, 1);
		
		ResourceMemory memoryTs1 = new ResourceMemory(shortMemories, longMemories, resourceAssemblage, mockAngInfo,
				1, 1, 1, 1, 0.5, 0.1, 1, 1, 1, 0, 1);
		ResourceMemory memoryTsPoint1 = new ResourceMemory(shortMemories.copy(), longMemories.copy(), resourceAssemblage, mockAngInfo,
				1, 1, 1, 1, 0.1, 0.01, 1, 1, 1, 0, 0.1);
		ResourceMemory memoryTs2 = new ResourceMemory(shortMemories.copy(), longMemories.copy(), resourceAssemblage, mockAngInfo,
				1, 1, 1, 1, 0.1, 0.01, 1, 1, 1, 0, 2);
		
		memoryTs1.decay();
		
		for (int i = 0; i < 10; i++)
		{
			memoryTsPoint1.decay();
		}
		// surprising how different these are (due to discretizing non-linear decay)
		TestUtilities.compareMatrix(memoryTsPoint1.getLongMemory(), memoryTs1.getLongMemory(), 4e-3);
		TestUtilities.compareMatrix(memoryTsPoint1.getShortMemory(), memoryTs1.getLongMemory(), 4e-3);

		memoryTs1.decay();
		memoryTs2.decay();

		// here too
		TestUtilities.compareMatrix(memoryTs2.getLongMemory(), memoryTs1.getLongMemory(), 7e-3);
		TestUtilities.compareMatrix(memoryTs2.getShortMemory(), memoryTs1.getLongMemory(), 7e-3);
	}
	
	@Test
	public void testReportCurrentState()
	{
		RealMatrix shortMemories = new Array2DRowRealMatrix(new double[][] {{ 1, 1 }, { 0, 1.5 }});
		RealMatrix longMemories = new Array2DRowRealMatrix(new double[][] {{ 10, 0 }, { 5, 1.5 }});
		ResourceAssemblage resourceAssemblage = Mockito.mock(ResourceAssemblage.class);
		double shortMemoryFactor = 2;

		RealMatrix expected = new Array2DRowRealMatrix(new double[][] {{ 8, -2 }, { 5, -1.5 }});

		ResourceMemory memory = new ResourceMemory(shortMemories, longMemories, resourceAssemblage, mockAngInfo,
				1, 1, 1, 1, 0.5, 0.5, shortMemoryFactor, 1, 1, 0, 1);
		double[][] state = memory.reportCurrentState(State.Resource);
		
		TestUtilities.compareMatrix(new Array2DRowRealMatrix(state), expected, 1e-10);

	}

	@Test
	public void testProbabilitiesSumToOne()
	{
		NdPoint location = new NdPoint(6.2, 4.8);
		
		int dimension = 100;
		ResourceAssemblage resources = SpaceFactory.generateUniformResource(dimension, dimension);
		ResourceMemory memory = SpaceFactory.createResourceMemory(resources, true);
		
		RealMatrix probabilities = memory.getProbabilities(location);
		
		Assert.assertEquals(MatrixUtils.sum(probabilities), 1.0, 1e-10, "Probabilities must sum to 1");
	}
	
	@Test
	public void testProbablilitiesNullWhenMemoryZero()
	{
		RealMatrix resources = new Array2DRowRealMatrix(5, 5);
		MatrixUtils.set(resources, 1.0 / 25.0);
		RealMatrix shortMemories = new Array2DRowRealMatrix(5, 5);
		RealMatrix longMemories = new Array2DRowRealMatrix(5, 5);
		ResourceMatrix resourceAssemblage = new ResourceMatrix(resources, 0.5, 1);
		ResourceMemory memory = new ResourceMemory(shortMemories, longMemories, resourceAssemblage, mockAngInfo,
				1, 1, 1, 1, 0.5, 0.1, 1, 1, 1, 0, 1);
		
		NdPoint location = new NdPoint(2.5, 2.5);
		NdPoint destination = memory.getDestinationProbabalistic(location);
		Assert.assertEquals(destination, null, "destiantion null");
	}
	
	@Test
	public void testProbablilitiesNullWhenMemoryNegative()
	{
		RealMatrix resources = new Array2DRowRealMatrix(5, 5);
		MatrixUtils.set(resources, 1.0 / 25.0);
		RealMatrix shortMemories = new Array2DRowRealMatrix(5, 5);
		MatrixUtils.set(shortMemories, 0.01);
		RealMatrix longMemories = new Array2DRowRealMatrix(5, 5);
		MatrixUtils.set(longMemories, 0.001);
		ResourceMatrix resourceAssemblage = new ResourceMatrix(resources, 0.5, 1);
		ResourceMemory memory = new ResourceMemory(shortMemories, longMemories, resourceAssemblage, mockAngInfo,
				1, 1, 1, 1, 0.5, 0.1, 1, 1, 1, 0, 1);
		
		NdPoint location = new NdPoint(2.5, 2.5);
		NdPoint destination = memory.getDestinationProbabalistic(location);
		Assert.assertEquals(destination, null, "destiantion null");
	}
	
	@Test
	public void testProbablilitiesNullWhenMemoryMixNegativePositive()
	{
		// this test points out that the destination will still be null even when there are points of attraction if 
		// there are more (or larger) points of repulsion, meaning the behavior will still be kinesis (random) until
		// short decays enough (or far enough away from negative) that attraction outweighs repulsion
		
		RealMatrix resources = new Array2DRowRealMatrix(5, 5);
		MatrixUtils.set(resources, 1.0 / 25.0);
		RealMatrix shortMemories = new Array2DRowRealMatrix(5, 5);
		shortMemories.setEntry(1, 2, 0.01);
		RealMatrix longMemories = new Array2DRowRealMatrix(5, 5);
		longMemories.setEntry(4, 4, 0.01);
		ResourceMatrix resourceAssemblage = new ResourceMatrix(resources, 0.5, 1);
		ResourceMemory memory = new ResourceMemory(shortMemories, longMemories, resourceAssemblage, mockAngInfo,
				1, 1, 1, 1, 0.5, 0.1, 1, 1, 1, 0, 1);
		
		NdPoint location = new NdPoint(2.5, 2.5);
		NdPoint destination = memory.getDestinationProbabalistic(location);
		Assert.assertEquals(destination, null, "destiantion null");
	}
	
	@Test
	public void testGetLocationProbabalisticRealResources()
	{
		NdPoint location = new NdPoint(23, 15);
		
		int dimension = 2;
		ResourceAssemblage resources = SpaceFactory.generateUniformResource(dimension, dimension);
		ResourceMemory memory = SpaceFactory.createResourceMemory(resources, true);
		
		NdPoint destination1 = memory.getDestinationProbabalistic(location);
		NdPoint destination2 = memory.getDestinationProbabalistic(location);
		NdPoint destination3 = memory.getDestinationProbabalistic(location);
		
		Assert.assertNotEquals(destination1, destination2, "each location different, 1 and 2");
		Assert.assertNotEquals(destination2, destination3, "each location different, 2 and 3");
	}
	
	@Test
	public void testGetLocationProbabalisticCompareToRCode() throws URISyntaxException, ParseException
	{
		// set parameters
		TestUtilities.setParameter(Parameter.MemorySpatialScaleForaging, 50); // gamma.Z
		TestUtilities.setParameter(Parameter.MemoryAlpha, 2500);	// alpha
		
		NdPoint location = new NdPoint(5.5, 5.5); // compare to 0.5, 0.5 in R code
		
		int dimension = 10;
		ResourceAssemblage resources = SpaceFactory.generateUniformResource(dimension, dimension);
		ResourceMemory memory = SpaceFactory.createResourceMemory(resources, true);
		
		RealMatrix probabilities = memory.getProbabilities(location);
		
		// expected probabilities generated from R code
		// temp = MakePatchyWorld(x = seq(-4.5, 4.5, len = 10), y = seq(-4.5, 4.5, len = 10), npatches = 5, radius = 2, steepness = 0.5)
		// tempConst = temp*0 +mean(temp)
		// dist = getDmatrix(getXYcoords(tempConst)$x, getXYcoords(tempConst)$y, complex(re=0.5, im=0.5))
		// mods = tempConst * exp(-dist/50)
		// mods[which.min(dist)] = 0
		// mods = exp(2500 * mods)
		// probs = mods / sum(mods)
		// write.csv(probs, file = "ProbsUniformGamma50Alpha2500.csv", quote = FALSE, row.names = FALSE)

		URI filename = ClassLoader.getSystemResource("ForagingModel/space/ProbsUniformGamma50Alpha2500.csv").toURI();
		File inputFile = new File(filename);
		
		ResourceLandscapeReader reader = InputFactory.createResourceLandscapeReader();
		List<CellData> data = reader.readLandscapeFile(inputFile, 0);
		
		for (CellData cell : data)
		{
			// 'carrying capacity' is actually probability value
			Assert.assertEquals(probabilities.getEntry(cell.getX(), cell.getY()), cell.getCarryingCapacity(), 1e-8, 
					String.format("Matching probabilities at (%d, %d)", cell.getX(), cell.getY()));
		}
	}
	
	@Test
	public void testGetDestinationFromTroubleLandscape() throws URISyntaxException
	{
		// This file previously threw an exception trying to initialize destination because the nearby high-value
		// patch would overflow the probability for that cell (infinity)
		URI filename = ClassLoader.getSystemResource("ForagingModel/space/land105.csv").toURI();
		File inputFile = new File(filename);
		ResourceLandscapeReader reader = InputFactory.createResourceLandscapeReader();
		List<CellData> data = reader.readLandscapeFile(inputFile, 0);
		
		ResourceAssemblage resources = SpaceFactory.generateResource(data, ScheduleFactory.createNoOpScheduler());
		ResourceMemory memory = SpaceFactory.createResourceMemory(resources, true);
		
		NdPoint center = Parameters.get().getStartingLocation();
		NdPoint destination = memory.getDestinationProbabalistic(center);
		
		Assert.assertNotNull(destination, "Could pick destination from nearby concentrated patch");
	}
	
	
//	@SuppressWarnings("unchecked")
//	@Test
//	public void testGetLocationProbabalistic()
//	{
//		IGridValueLayer valueLayer = Mockito.mock(IGridValueLayer.class);
//		Context<MemoryBit> memoryContext = Contexts.createContext(MemoryBit.class, Constants.MEMORY_CONTEXT);
//		Context<Agent> mainContext = Contexts.createContext(Agent.class, Constants.MODEL_CONTEXT);
//		mainContext.addSubContext(memoryContext);
//		
//		Grid<Agent> grid = SpaceFactory.createGrid(mainContext, 2);
//		MemoryAssemblage memories = SpaceFactory.createMemoryAssemblage(memoryContext, grid, valueLayer, 1, 1);
//
//		for (int i = 0; i < 2; i++)
//		{
//			for (int j = 0; j < 2; j++)
//			{
//				// add memory
//				MemoryBit memory = Mockito.mock(MemoryBit.class);
//				Mockito.when(memory.getValue()).thenReturn((double) (i + 2* j + 0.1)); // add 0.1 so value isn't 0 for both 1st grid sq and current location
//				memoryContext.add(memory);
//				grid.moveTo(memory, i, j);
//			}
//		}
//
//		@SuppressWarnings("rawtypes")
//		ArgumentCaptor<List> probabilities = ArgumentCaptor.forClass(List.class);
//		NumberGenerator generator = Mockito.mock(NumberGenerator.class);
//		Mockito.when(generator.nextInt(probabilities.capture())).thenReturn(2);
//		Mockito.when(generator.nextDouble()).thenReturn(0.0);
//		TestUtilities.injectMockGenerator(generator);
//		
//		Memory expectedPoint = memoryContext.getObjects(Memory.class).get(2); // returning 2 from mock
//		
//		NdPoint dest = memories.getDestinationProbabalistic(new NdPoint(0.5, 1.1));
//		Assert.assertEquals(new GridPoint((int) dest.getX(), (int) dest.getY()), grid.getLocation(expectedPoint), "returned correct point");
//		
//		double probSum = 0;
//		for (int i = 0; i < probabilities.getValue().size(); i++)
//		{
//			probSum += (Double) probabilities.getValue().get(i);
//		}
//		Assert.assertEquals(probSum, 1.0, "probabilities sum to one");
//		
//		IndexedIterable<MemoryBit> memoryItems = memoryContext.getObjects(Memory.class);
//		double expectedVerySmallest = 0, expectedSmallest = 0, expectedMedium = 0, expectedLargest = 0;
//		for (int i = 0; i < memoryItems.size(); i ++)
//		{
//			// System.out.printf("%f %f\n", memoryItems.get(i).getValue(), probabilities.getValue().get(i));
//			// verify corresponding probability by index, id memories by value
//			switch ((int) memoryItems.get(i).getValue())
//			{
//			case 0: // i = 0, j = 0
//				expectedSmallest = (Double) probabilities.getValue().get(i);
//				break;
//			case 1: // i = 1, j = 0
//				expectedMedium = (Double) probabilities.getValue().get(i);
//				break;
//			case 2: // i = 0, j = 1
//				expectedVerySmallest = (Double) probabilities.getValue().get(i);
//				break;
//			case 3: // i = 1, j = 1
//				expectedLargest = (Double) probabilities.getValue().get(i);
//				break;
//			}
//		}
//		Assert.assertTrue(expectedVerySmallest < expectedSmallest, "grid square currently in should have near 0 probability");
//		Assert.assertTrue(expectedSmallest < expectedMedium, "smallest value memory has smallest probability");
//		Assert.assertTrue(expectedMedium < expectedLargest, "largest value memory has largest probability");
//	}
}
