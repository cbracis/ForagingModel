package ForagingModel.core;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import ForagingModel.agent.movement.MovementType;
import ForagingModel.core.Parameters.Parameter;

public class ParametersTest
{
	@AfterMethod
	public void resetParameters()
	{
		Parameters.resetToDefaults();
	}
	
	@Test
	public void testGetParameter()
	{
		Assert.assertEquals(Parameters.get().getIntervalSize(), 1.0, "get time step");
	}


	@Test
	public void testSet() throws Exception
	{
		Parameters parameters = Parameters.get();
		
		parameters.set( Parameter.IntervalSize, "23" );
		Assert.assertEquals( parameters.getIntervalSize(), 23.0 );
		
		parameters.set( Parameter.MemoryIsFullyInformed, "false" );
		Assert.assertEquals(parameters.getIsFullyInformed(), false );

		parameters.set( Parameter.MovementType, "Kinesis" );
		Assert.assertEquals(parameters.getMovementType(), MovementType.Kinesis );

		parameters.set( Parameter.ConsumerConsumptionSpatialScale, 99.5 );
		Assert.assertEquals(parameters.getConsumptionSpatialScale(), 99.5 );
	}	
	
	@Test
	public void testValid()
	{
		Parameters parameters = Parameters.get();
		
		parameters.set( Parameter.LongDecayRate, 0.001 );
		parameters.set( Parameter.ShortDecayRate, 0.01 );
		
		Assert.assertTrue(parameters.areParametersValid(), "Short less than long");
		
		parameters.set( Parameter.LongDecayRate, 0.01 );
		parameters.set( Parameter.ShortDecayRate, 0.001 );
		
		Assert.assertFalse(parameters.areParametersValid(), "Short greater than long");

		parameters.set( Parameter.LongDecayRate, 0.01 );
		parameters.set( Parameter.ShortDecayRate, 0.01 );
		
		Assert.assertFalse(parameters.areParametersValid(), "Short equal to long");

		parameters.set( Parameter.LongDecayRate, 1.0 );
		parameters.set( Parameter.ShortDecayRate, 1.0 );
		
		Assert.assertTrue(parameters.areParametersValid(), "Short equal to long but both 1 (no memory)");
}
}
