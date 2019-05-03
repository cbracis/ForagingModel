package ForagingModel.agent;

import org.testng.Assert;
import org.testng.annotations.Test;

import ForagingModel.agent.movement.BehaviorState;
import ForagingModel.core.NdPoint;
import ForagingModel.core.Velocity;

public class RecorderTest 
{
	@Test
	public void testBurnIn()
	{
		Recorder recorder = AgentFactory.createRecorder(20, 10, 10, 0.1, null);
		
		//burn in
		for (int i = 0; i < 10; i++)
		{
			recorder.recordConsumption(0.1);
			recorder.recordIteration(); // next iteration
		}
		
		// recorded
		for (int i = 0; i < 10; i++)
		{
			recorder.recordConsumption(0.7);
			recorder.recordIteration(); // next iteration
		}
		
		Assert.assertEquals(recorder.getTotalConsumption(), 7.0, 1e-10, "total comsumption not including burn in");
	}
	
	@Test
	public void testTotalConsumption()
	{
		Recorder recorder = AgentFactory.createRecorder(50, 0, 10, 0.1, null);
		
		for (int i = 0; i < 10; i++)
		{
			recorder.recordConsumption(0.2);
		}
		
		Assert.assertEquals(recorder.getTotalConsumption(), 2.0, 1e-10, "total comsumption");
	}

	@Test
	public void testSdConsumption()
	{
		Recorder recorder = AgentFactory.createRecorder(50, 0, 10, 0.1, null);
		
		for (int i = 0; i< 10; i++)
		{
			recorder.recordConsumption(0.1 * i);
		}
		
		Assert.assertEquals(recorder.getSdConsumption(), 0.302765, 1e-6, "sd calculated matches R");
	}
	
	@Test
	public void testDistanceTraveled()
	{
		Recorder recorder = AgentFactory.createRecorder(50, 0, 10, 0.1, null);
		Assert.assertEquals(recorder.getDistanceTraveled(), 0.0, "nowhere yet");
		
		recorder.recordLocation(new NdPoint(0, 0));
		Assert.assertEquals(recorder.getDistanceTraveled(), 0.0, "only 1 location");

		recorder.recordLocation(new NdPoint(0, 1));
		Assert.assertEquals(recorder.getDistanceTraveled(), 1.0, "1 step");

		recorder.recordLocation(new NdPoint(1, 2));
		Assert.assertEquals(recorder.getDistanceTraveled(), 1.0 + Math.sqrt(2), "2 steps");
	}
	
	@Test
	public void testSinuosity()
	{
		Recorder recorder = AgentFactory.createRecorder(50, 0, 10, 0.1, null);
		Assert.assertEquals(recorder.getSinuosity(), 0.0, "nowhere yet");
		
		recorder.recordLocation(new NdPoint(0, 0));
		Assert.assertEquals(recorder.getSinuosity(), 0.0, "only 1 location");

		recorder.recordLocation(new NdPoint(0, 1));
		Assert.assertEquals(recorder.getSinuosity(), 1.0, "1 step");

		recorder.recordLocation(new NdPoint(1, 1));
		Assert.assertEquals(recorder.getSinuosity(), 2.0 / Math.sqrt(2), "2 steps");
	}
	
	@Test
	public void testAverageSpeed()
	{
		Recorder recorder = AgentFactory.createRecorder(50, 0, 10, 1, null);
		Assert.assertEquals(recorder.getAverageSpeed(), 0.0, "nowhere yet");
		
		recorder.recordVelocity(Velocity.createPolar(1.2, 1));
		Assert.assertEquals(recorder.getAverageSpeed(), 1.2, "single velocity");

		recorder.recordVelocity(Velocity.createPolar(1.4, 2));
		Assert.assertEquals(recorder.getAverageSpeed(), 1.3, 1e-10, "two velocities");

		recorder.recordVelocity(Velocity.createPolar(0.7, 1));
		Assert.assertEquals(recorder.getAverageSpeed(), 1.1, 1e-10, "three velocities");

	}
	
	@Test
	public void testAverageSpeedTimeStep()
	{
		Recorder recorder = AgentFactory.createRecorder(50, 0, 10, 0.1, null);
		
		for (int i = 0; i < 10; i++)
		{
			recorder.recordVelocity(Velocity.createPolar(0.1, i * 0.1));
		}
		Assert.assertEquals(recorder.getAverageSpeed(), 1.0, 1e-10, "scale by timestep");
	}
	
	@Test
	public void testMeanSin()
	{
		Recorder recorder = AgentFactory.createRecorder(50, 0, 10, 0.1, null);
		Assert.assertEquals(recorder.getMeanSinTurningAngle(), 0.0, "nowhere yet");

		recorder.recordVelocity(Velocity.createPolar(1.0, 0));
		Assert.assertEquals(recorder.getMeanSinTurningAngle(), 0.0, "single velocity");

		recorder.recordVelocity(Velocity.createPolar(1.0, Math.PI / 2)); // turning angle is pi/2
		Assert.assertEquals(recorder.getMeanSinTurningAngle(), 1.0, 1e-10, "two velocities");

		recorder.recordVelocity(Velocity.createPolar(1.0, 2 * Math.PI / 3)); // turning angle is pi/6
		Assert.assertEquals(recorder.getMeanSinTurningAngle(), 0.75, 1e-10, "three velocities");
	}
	
	@Test
	public void testMemoryUsage()
	{
		Recorder recorder = AgentFactory.createRecorder(50, 0, 10, 0.1, null);
		Assert.assertEquals(recorder.getSearchMemoryUsage(), 0.0, "no searching");
		
		recorder.recordState(BehaviorState.Feeding);
		recorder.recordUseMemory(false);
		Assert.assertEquals(recorder.getSearchMemoryUsage(), 0.0, "still no searching");
		
		recorder.recordState(BehaviorState.Searching);
		recorder.recordUseMemory(false);
		Assert.assertEquals(recorder.getSearchMemoryUsage(), 0.0, "searching, no memory");

		recorder.recordState(BehaviorState.Searching);
		recorder.recordUseMemory(true);
		Assert.assertEquals(recorder.getSearchMemoryUsage(), 0.5, "searching and memory");
	}
}
