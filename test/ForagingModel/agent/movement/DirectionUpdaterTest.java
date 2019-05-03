package ForagingModel.agent.movement;

import org.mockito.Mockito;
import org.testng.Assert;

import ForagingModel.core.NumberGenerator;

public class DirectionUpdaterTest 
{

	public void testTimeStepUpdater()
	{
		DirectionUpdater updater = new TimeStepDirectionUpdater(1, 1);
		Assert.assertEquals(updater.updateDirection(), true, "Should update initially");
		
		updater.notifyTimeStep(0);
		Assert.assertEquals(updater.updateDirection(), true, "Should update time step 0");

		updater.notifyTimeStep(1);
		Assert.assertEquals(updater.updateDirection(), true, "Should update time step 1");

		updater.notifyTimeStep(2);
		Assert.assertEquals(updater.updateDirection(), true, "Should update time step 2");

	}
	
	public void testTimeStepUpdaterEveryOtherStep()
	{
		DirectionUpdater updater = new TimeStepDirectionUpdater(2, 1);
		Assert.assertEquals(updater.updateDirection(), true, "Should update initially");
		
		updater.notifyTimeStep(0);
		Assert.assertEquals(updater.updateDirection(), true, "Should update time step 0");

		updater.notifyTimeStep(1);
		Assert.assertEquals(updater.updateDirection(), false, "No update time step 1");

		updater.notifyTimeStep(2);
		Assert.assertEquals(updater.updateDirection(), true, "Should update time step 2");

		updater.notifyTimeStep(3);
		Assert.assertEquals(updater.updateDirection(), false, "No update time step 3");

		updater.notifyTimeStep(4);
		Assert.assertEquals(updater.updateDirection(), true, "Should update time step 4");
	}
	
	public void testPoissonProcessUpdater()
	{
		NumberGenerator generator = Mockito.mock(NumberGenerator.class);
		double rate = 1.2;
		DirectionUpdater updater = new PoissonProcessDirectionUpdater(rate, 1, generator);
		
		updater.notifyInterval(0);
		Assert.assertEquals(updater.updateDirection(), true, "Should update at 0");
		
		Mockito.when(generator.nextExponential(rate)).thenReturn(1.3);
		updater.notifyInterval(1);
		Assert.assertEquals(updater.updateDirection(), false, "No update interval 1");
		updater.notifyInterval(2);
		Assert.assertEquals(updater.updateDirection(), true, "Update interval 2");
		
		Mockito.when(generator.nextExponential(rate)).thenReturn(0.5);
		updater.notifyInterval(3);
		Assert.assertEquals(updater.updateDirection(), true, "Update interval 3");
		
		Mockito.when(generator.nextExponential(rate)).thenReturn(3.0);
		updater.notifyInterval(4);
		Assert.assertEquals(updater.updateDirection(), false, "No update interval 4");
		updater.notifyInterval(5);
		Assert.assertEquals(updater.updateDirection(), false, "No update interval 5");
		updater.notifyInterval(6);
		Assert.assertEquals(updater.updateDirection(), true, "Update interval 6");

	}

}
