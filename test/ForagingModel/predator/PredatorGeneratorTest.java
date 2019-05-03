package ForagingModel.predator;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import ForagingModel.core.GridPoint;
import ForagingModel.core.NdPoint;
import ForagingModel.core.NumberGenerator;
import ForagingModel.core.TestUtilities;
import ForagingModel.space.ResourceAssemblage;

public class PredatorGeneratorTest 
{
	@AfterMethod
	public void resetNumberGenerator()
	{
		TestUtilities.resetGenerator();
	}

	@Test
	public void testCreatePredator()
	{
		NumberGenerator numGenerator = Mockito.mock(NumberGenerator.class);
		// loc
		Mockito.when(numGenerator.nextDoubleFromTo(Mockito.anyDouble(), Mockito.anyDouble())).thenReturn(0.0);
		// time
		Mockito.when(numGenerator.nextIntFromTo(Mockito.anyInt(), Mockito.anyInt())).thenReturn((long) 0);
		// prob
		Mockito.when(numGenerator.nextDouble()).thenReturn(0.6);
		TestUtilities.injectMockGenerator(numGenerator);

		ResourceAssemblage resources = Mockito.mock(ResourceAssemblage.class);
		Mockito.when(resources.getAverageQuality()).thenReturn(0.2);
		Mockito.when(resources.getIntrinsicQuality(new GridPoint(0,0))).thenReturn(0.4);
		int duration = 10;
		double tradeoff = 0.5;
		int total = 100;
		
		PredatorGenerator generator = new PredatorGenerator(resources, "testCreatePredator", duration, tradeoff, total);
		Predator predator = generator.createPredator();
		
		Assert.assertEquals(predator.isActive(0), true, "start is 0");
		Assert.assertEquals(predator.isActive(duration - 1), true, "stop is 10");
		Assert.assertEquals(predator.isActive(duration), false, "stop is 10");
		Assert.assertEquals(predator.getLocation(), new NdPoint(0,0), "loc is 0,0");
	}
	
	@Test
	public void testGeneratePredators()
	{
		ResourceAssemblage resources = Mockito.mock(ResourceAssemblage.class);
		Mockito.when(resources.getAverageQuality()).thenReturn(0.2);
		Mockito.when(resources.getIntrinsicQuality(Mockito.any(GridPoint.class))).thenReturn(0.4);
		int duration = 10;
		double tradeoff = 0.5;
		int total = 100;
		PredatorGenerator generator = new PredatorGenerator(resources, "testGeneratePredators", duration, tradeoff, total);
		
		PredatorManagerImpl manager = (PredatorManagerImpl) generator.generatePredators();
		
		Assert.assertEquals(manager.getTotalNumberPredators(), total / duration, "Correct number created");

	}
	
	@Test
	public void testPredatorCache()
	{
		ResourceAssemblage resources = Mockito.mock(ResourceAssemblage.class);
		Mockito.when(resources.getAverageQuality()).thenReturn(0.2);
		Mockito.when(resources.getIntrinsicQuality(Mockito.any(GridPoint.class))).thenReturn(0.4);

		PredatorManager manager1 = new PredatorGenerator(resources, "PredatorCache land A", 10, 0, 100).generatePredators();
		PredatorManager manager2 = new PredatorGenerator(resources, "PredatorCache land B", 10, 0, 100).generatePredators();
		PredatorManager manager3 = new PredatorGenerator(resources, "PredatorCache land A", 100, 0, 100).generatePredators();
		Assert.assertNotEquals(manager1, manager2, "different landscapes");
		Assert.assertNotEquals(manager1, manager3, "different durations");
		Assert.assertNotEquals(manager2, manager3, "different landscapes and durations");
		
		// now generate the same again
		Assert.assertEquals(manager1, new PredatorGenerator(resources, "PredatorCache land A", 10, 0, 100).generatePredators(), "same 1");
		Assert.assertEquals(manager2, new PredatorGenerator(resources, "PredatorCache land B", 10, 0, 100).generatePredators(), "same 2");
		Assert.assertEquals(manager3, new PredatorGenerator(resources, "PredatorCache land A", 100, 0, 100).generatePredators(), "same 3");
	}
}
