package ForagingModel.space;

import java.text.ParseException;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ForagingModel.predator.PredatorManager;
import ForagingModel.schedule.Notifiable;
import ForagingModel.schedule.Schedulable;
import ForagingModel.schedule.ScheduleFactory;
import ForagingModel.schedule.SchedulePriority;
import ForagingModel.schedule.SchedulerImpl;
import ForagingModel.space.SpaceFactory;
import ForagingModel.agent.Recorder;
import ForagingModel.agent.movement.MemoryMovementBehavior;
import ForagingModel.agent.movement.MovementBehavior;
import ForagingModel.agent.movement.MovementFactory;
import ForagingModel.agent.movement.MovementProcessType;
import ForagingModel.agent.movement.MovementType;
import ForagingModel.core.MatrixUtils;
import ForagingModel.core.NdPoint;
import ForagingModel.core.Parameters;
import ForagingModel.core.Parameters.Parameter;

public class SpaceFactoryTest 
{
	@AfterMethod
	public void resetParameters()
	{
		Parameters.resetToDefaults();
	}

	@Test
	public void testQualityCalculation() 
	{
		NdPoint center = new NdPoint(1.1, 1.1);
		double radius = 3.1;
		double steepness = 0.5;
		
		// x and y values calculated at
		double near = SpaceFactory.calculatePatchQuality(center, radius, steepness, 1, 1);
		double mid = SpaceFactory.calculatePatchQuality(center, radius, steepness, 2, 3);
		double far = SpaceFactory.calculatePatchQuality(center, radius, steepness, 3, 3);
		double veryfar = SpaceFactory.calculatePatchQuality(center, radius, steepness, 7, 8);
		
		Assert.assertTrue(near > mid, "closest point has highest quality");
		Assert.assertTrue(mid > far, "farther point has lower quality");
		Assert.assertTrue(far > veryfar, "farthest point has lowest quality");
		
		// radius 
		Assert.assertEquals(SpaceFactory.calculatePatchQuality(new NdPoint(1.5, 1.5), 3, steepness, 1, 4), 0.5, 1e-10, 
				"radius is mean, so 50% of cdf"); // grid point (1,4) has center (1.5,4.5)
		double smaller  = SpaceFactory.calculatePatchQuality(center, 1.5, steepness, 2, 3);
		Assert.assertTrue(mid > smaller, "smaller patch has lower quality"); // because just shifting cdf left
		
		// steepness (how sharp patch edges are)
		double steepSteepness = 5;
		double midSteep = SpaceFactory.calculatePatchQuality(center, radius, steepSteepness, 2, 3);
		double veryfarSteep = SpaceFactory.calculatePatchQuality(center, radius, steepSteepness, 7, 8);
		
		Assert.assertTrue(midSteep > mid, "steep patch has higher quality closer");
		Assert.assertTrue(veryfar > veryfarSteep, "steep patch has lower quality farther");
	}
	
	@Test
	public void testTwoPatchLandscape()
	{
		ResourceAssemblage resources = SpaceFactory.generateTwoPatchResource(100, 100, ScheduleFactory.createNoOpScheduler());
		Assert.assertNotNull(resources, "Created resources");
		Assert.assertEquals(MatrixUtils.sum(((ResourceMatrix) resources).getResourceMatrix()), 1.0, 1e-10, "sums to one");
	}
	
	@Test(dataProvider = "gridProperties")
	public void testResourceGenerationSumsToOne(int size, int numPatch, double radius, double steepness )
	{
		ResourceMatrix resources = (ResourceMatrix) SpaceFactory.generateRandomResource(size, size, numPatch, radius, steepness);
		double totalQuality = MatrixUtils.sum(resources.getResourceMatrix());
		
		Assert.assertEquals(totalQuality, 1.0, 1e-10, "Quality sums to 1");
	}

	@DataProvider
	public Object[][] gridProperties() 
	{
		return new Object[][] {
				new Object[] { 100, 20, 2.1, 0.5 },
				new Object[] { 20, 2, 30.0, 0.1 },
				new Object[] { 100, 10, 5.0, 5.0 }
		};
	}
	
	@Test
	public void testCreateMemoryForAggregate() throws ParseException
	{
		Parameters params = Parameters.get();
		params.set(Parameter.MovementType, MovementType.MemoryDirectional.toString());
		params.set(Parameter.MovementProcess, MovementProcessType.Correlated.toString());
		params.set(Parameter.TotalPredationPressure, "100");
		
		ResourceAssemblage resources = SpaceFactory.generateTwoPatchResource(50, 50, ScheduleFactory.createNoOpScheduler());
		PredatorManager predatorManager = Mockito.mock(PredatorManager.class);
		LocationManager locationManager = Mockito.mock(LocationManager.class);
		SchedulerImpl scheduler = Mockito.mock(SchedulerImpl.class);
		Recorder recorder = Mockito.mock(Recorder.class);
		
		MovementBehavior movement = MovementFactory.createMovement(resources, predatorManager, 1.0, locationManager, 
				new NdPoint(5, 5), scheduler, recorder);
	
		Assert.assertTrue(movement instanceof MemoryMovementBehavior, "memory");
		
		MemoryMovementBehavior memMovement = (MemoryMovementBehavior) movement;
		Assert.assertTrue(memMovement.getMemory() instanceof AggregateMemory, "aggregate memory");
		Mockito.verify(scheduler, Mockito.atLeastOnce()).register((Schedulable)memMovement.getMemory(), SchedulePriority.MemoryDecay);
		Mockito.verify(scheduler, Mockito.atLeastOnce()).register((Notifiable)memMovement.getMemory());
	}
}
