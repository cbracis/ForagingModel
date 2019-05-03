package ForagingModel.core;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import ForagingModel.agent.movement.MovementType;

public class ParameterManagerTest
{
	@AfterMethod
	public void resetParameters()
	{
		Parameters.resetToDefaults();
	}

	@Test
	public void testNoFile()
	{
		CoreFactory.createParameterManager( null );
		Parameters parameters = Parameters.get();
		
		Assert.assertEquals( parameters.getIntervalSize(), 1.0 );
		Assert.assertEquals( parameters.getMovementType(), MovementType.MemoryDestination );
		Assert.assertEquals( parameters.getIsFullyInformed(), true );
		
	}
	
	@Test
	public void testSingleOptions() throws Exception
	{
		URI fileName = ClassLoader.getSystemResource( "ForagingModel/core/test.properties" ).toURI();
		CoreFactory.createParameterManager( fileName.getPath() );
		Parameters parameters = Parameters.get();
		
		Assert.assertEquals( parameters.getIntervalSize(), 0.1 );
		Assert.assertEquals( parameters.getMovementType(), MovementType.Kinesis );
		Assert.assertEquals( parameters.getIsFullyInformed(), true );
	}
	
	@Test
	public void testManyOptions() throws Exception
	{
		URI fileName = ClassLoader.getSystemResource( "ForagingModel/core/testmulti.properties" ).toURI();
		ParameterManager pm = CoreFactory.createParameterManager( fileName.getPath() );
		
		int i = 0;
		for ( Parameters parameters : pm )
		{
			switch ( i )
			{
			case 0:
				Assert.assertEquals( parameters.getShortDecayRate(), 0.1 );
				break;
			case 1:
				Assert.assertEquals( parameters.getShortDecayRate(), 0.01 );
				break;
			case 2:
				Assert.assertEquals( parameters.getShortDecayRate(), 0.001 );
				break;
			default:
				Assert.fail( "Too many parameters" );
			}
			Assert.assertEquals( parameters.getIntervalSize(), 0.1 );
			Assert.assertEquals( parameters.getMovementType(), MovementType.Kinesis );
			Assert.assertEquals( parameters.getIsFullyInformed(), true );
			i++;
		}

	}
	
	@Test
	public void testMultipleManyOptions() throws Exception
	{
		List<Double> expectedShortDecays = new ArrayList<Double>( Arrays.asList( new Double[] { 0.1, 0.01, 0.001 } ) ); 
		List<Double> expectedLongDecays = new ArrayList<Double>( Arrays.asList( new Double[] { 0.5, 0.05, 0.005 } ) ); 
		
		URI fileName = ClassLoader.getSystemResource( "ForagingModel/core/test2multi.properties" ).toURI();
		ParameterManager pm = CoreFactory.createParameterManager( fileName.getPath() );
		
		int i = 0, j = 0;
		for ( Parameters parameters : pm )
		{
			Assert.assertEquals( parameters.getShortDecayRate(), expectedShortDecays.get(j), "i = " + i + ", j = " + j );
			Assert.assertEquals( parameters.getLongDecayRate(), expectedLongDecays.get(i % 3), "i = " + i + ", j = " + j );
			Assert.assertEquals( parameters.getIntervalSize(), 0.1 );
			Assert.assertEquals( parameters.getMovementType(), MovementType.Kinesis );
			Assert.assertEquals( parameters.getIsFullyInformed(), true );
			i++;
			if ( i == 3 || i == 6 ) j++;
		}

	}
	
	@Test
	public void testReferentialParameters() throws Exception
	{
		URI fileName = ClassLoader.getSystemResource( "ForagingModel/core/reference.properties" ).toURI();
		CoreFactory.createParameterManager( fileName.getPath() );
		Parameters parameters = Parameters.get();
		
		Assert.assertEquals( parameters.getShortSpatialScale(), 2.0 );
		Assert.assertEquals( parameters.getLongSpatialScale(), 2.0 );
		Assert.assertEquals( parameters.getConsumptionSpatialScale(), 5.0 );
	}
	
	@Test
	public void testReferentialParametersMulti() throws Exception
	{
		URI fileName = ClassLoader.getSystemResource( "ForagingModel/core/referencemulti.properties" ).toURI();
		ParameterManager pm = CoreFactory.createParameterManager( fileName.getPath() );
		
		int i = 0;
		for ( Parameters parameters : pm )
		{
			double expectedScale = (i < 2) ? 2.0 : 5.0;  // first half 2.0 and second half 5.0
			MovementType expectedType = (i % 2 == 0) ? MovementType.MemoryDestination : MovementType.Kinesis; // alternate Memory and Kinesis
			Assert.assertEquals( parameters.getShortSpatialScale(), expectedScale );
			Assert.assertEquals( parameters.getLongSpatialScale(), expectedScale );
			Assert.assertEquals( parameters.getMovementType(), expectedType );
			Assert.assertEquals( parameters.getConsumptionSpatialScale(), 5.0 );
			i++;
		}
	}
	
	
}
