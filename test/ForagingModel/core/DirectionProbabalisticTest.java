package ForagingModel.core;

import java.text.ParseException;
import java.util.Arrays;
import java.util.HashSet;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import ForagingModel.core.Parameters.Parameter;
import ForagingModel.predator.PredatorManager;
import ForagingModel.schedule.Scheduler;
import ForagingModel.space.MemoryAssemblage;
import ForagingModel.space.ResourceAssemblage;
import ForagingModel.space.SpaceFactory;

public class DirectionProbabalisticTest 
{
	@AfterMethod
	public void resetParameters()
	{
		Parameters.resetToDefaults();
	}

	@Test
	public void testAngleSafeForagingOnly()
	{
		Scheduler scheduler = Mockito.mock(Scheduler.class);
		ResourceAssemblage resources = SpaceFactory.generateTwoPatchResource(50, 50, scheduler);
		MemoryAssemblage foragingMem = SpaceFactory.createMemoryAssemblage(resources, null, null, null, scheduler);
		
		DirectionProbabalistic direction = foragingMem.getDirectionProbabalistic(new NdPoint(25, 25));
		
		for (double angle = 0; angle < 2 * Math.PI; angle+= 0.001)
		{
			// all foraging angles should be fine
			Assert.assertTrue(direction.angleIsSafe(new Angle(angle)), "Angle safe " + angle);
		}
	}
	
	@Test
	public void testAngleSafePredation() throws ParseException
	{
		Scheduler scheduler = Mockito.mock(Scheduler.class);
		ResourceAssemblage resources = SpaceFactory.generateTwoPatchResource(50, 50, scheduler);
		NdPoint foragerLoc = new NdPoint(25, 25);
		NdPoint learnLoc = new NdPoint(26, 25);
		NdPoint predLoc = new NdPoint(31, 25); // assume encounter radius of 5

		TestUtilities.setParameter(Parameter.TotalPredationPressure, "50");
		PredatorManager predators = Mockito.mock(PredatorManager.class);
		Mockito.when(predators.getActivePredators(Mockito.any(NdPoint.class), Mockito.anyDouble())).thenReturn(new HashSet<NdPoint>(Arrays.asList(predLoc)));

		// need to set predator mem params to reasonable values or angles will never be unsafe
		TestUtilities.setParameter(Parameter.PredatorLearningRate, "50");
		TestUtilities.setParameter(Parameter.PredatorMemoryFactor, "50");
		TestUtilities.setParameter(Parameter.MemorySpatialScalePredation, "10");
		MemoryAssemblage aggregateMem = SpaceFactory.createMemoryAssemblage(resources, predators, null, null, scheduler);
		
		aggregateMem.learn(learnLoc);
		DirectionProbabalistic direction = aggregateMem.getDirectionProbabalistic(foragerLoc);
		
		// near 0 not safe (assume 90 deg chunk but slightly more than that)
		for (double angle = 0; angle < Math.PI / 4; angle+= 0.001)
		{
			Assert.assertFalse(direction.angleIsSafe(new Angle(angle)), "Angle not safe " + angle);
		}
		for (double angle = 7 * Math.PI / 4; angle < 2 * Math.PI; angle+= 0.001)
		{
			Assert.assertFalse(direction.angleIsSafe(new Angle(angle)), "Angle not safe " + angle);
		}

		// the 180 deg away from pred should all be safe
		for (double angle = Math.PI / 2; angle < 3 * Math.PI/ 2; angle+= 0.001)
		{
			Assert.assertTrue(direction.angleIsSafe(new Angle(angle)), "Angle safe " + angle);
		}

		
	}

}
