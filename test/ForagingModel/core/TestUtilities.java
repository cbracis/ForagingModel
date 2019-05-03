package ForagingModel.core;

import java.text.ParseException;
import java.util.List;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.random.GaussianRandomGenerator;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.UncorrelatedRandomVectorGenerator;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import ForagingModel.core.Parameters.Parameter;

public class TestUtilities 
{
	public static void setParameter(Parameter parameter, Object value) throws ParseException 
	{
		Parameters.get().set(parameter, value.toString());
	}
	
	public static void setSimulationIndex(int index)
	{
		ModelEnvironment.setSimulationIndex(index);
	}

	public static void injectMockGenerator(NumberGenerator generator)
	{
		ModelEnvironment.setNumberGenerator(generator);
	}
	
	public static void resetGenerator()
	{
		ModelEnvironment.resetGenerator();
	}
	
	public static void setGeneratorToNormalAlwaysReturnsZero()
	{
		NumberGenerator generator = Mockito.mock(NumberGenerator.class);
		Mockito.when(generator.nextNormal(Mockito.anyDouble(), Mockito.anyDouble())).thenReturn(0.0);
		Mockito.when(generator.nextStandardNormal()).thenReturn(0.0);
		TestUtilities.injectMockGenerator(generator);
	}
	
	public static void setGeneratorToNextDoubleFromToAlwaysReturnsZero()
	{
		NumberGenerator generator = Mockito.mock(NumberGenerator.class);
		Mockito.when(generator.nextDoubleFromTo(Mockito.anyDouble(), Mockito.anyDouble())).thenReturn(0.0);
		TestUtilities.injectMockGenerator(generator);
	}
	
	public static void compareMatrix(RealMatrix a, RealMatrix b, double tolerance)
	{
		Assert.assertEquals(a.getRowDimension(), b.getRowDimension(), "same number of rows");
		Assert.assertEquals(a.getColumnDimension(), b.getColumnDimension(), "same number of columns");

		int rows = a.getRowDimension();
		int columns = a.getColumnDimension();

		for (int i = 0; i < rows; i++)
		{
			for (int j = 0; j < columns; j ++)
			{
				Assert.assertEquals(a.getEntry(i, j), b.getEntry(i, j), tolerance, String.format("Cell %d, %d equal", i, j));
			}
		}
	}
	
	public static void compareVector(RealVector a, RealVector b, double tolerance)
	{
		Assert.assertEquals(a.getDimension(), b.getDimension(), "same dimension");

		int size = a.getDimension();

		for (int i = 0; i < size; i++)
		{
			Assert.assertEquals(a.getEntry(i), b.getEntry(i), tolerance, String.format("Cell %d equal", i));
		}
	}

	public static <T> void compareList(List<T> a, List<T> b, String message)
	{
		Assert.assertEquals(a.size(), b.size(), "same size");

		int size = a.size();

		for (int i = 0; i < size; i++)
		{
			Assert.assertEquals(a.get(i), b.get(i), String.format("%s: Index %d equal", message, i));
		}
	}

	public static RealMatrix createRandomMatrix(int rows, int cols)
	{
		RealMatrix matrix = new Array2DRowRealMatrix(rows, cols);
		RandomGenerator rg = new JDKRandomGenerator();
		UncorrelatedRandomVectorGenerator generator = new UncorrelatedRandomVectorGenerator(cols, new GaussianRandomGenerator(rg));
		  
		for (int i = 0; i < rows; i++)
		{
			matrix.setRow(i, generator.nextVector());
		}
		  
		return matrix;
	}

	@Test
	public void testResetGenerator()
	{
		TestUtilities.setGeneratorToNormalAlwaysReturnsZero();
		
		Assert.assertEquals(ModelEnvironment.getNumberGenerator().nextNormal(1000, 0.1), 0.0, "currently returns 0 - nextNormal");
		Assert.assertEquals(ModelEnvironment.getNumberGenerator().nextStandardNormal(), 0.0, "currently returns 0 - nextStandardNormal");
		
		TestUtilities.resetGenerator();
		
		Assert.assertNotEquals(ModelEnvironment.getNumberGenerator().nextDoubleFromTo(0, 1), 0.0, "should no longer return 0 - nextDoubleFromTo");
		Assert.assertNotEquals(ModelEnvironment.getNumberGenerator().nextNormal(1000, 0.1), 0.0, "should no longer return 0 - nextNormal");
		Assert.assertNotEquals(ModelEnvironment.getNumberGenerator().nextStandardNormal(), 0.0, "should no longer return 0 - nextStandardNormal");
	}
}
