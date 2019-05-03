package ForagingModel.core;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.RealVectorPreservingVisitor;
import org.testng.Assert;
import org.testng.annotations.Test;

public class MatrixUtilsTest 
{
	@Test
	public void testSet()
	{
		double value = 0.1;
		RealMatrix matrix = new Array2DRowRealMatrix(10, 10);
		MatrixUtils.set(matrix, value);
		for (int row = 0; row < matrix.getRowDimension(); row++)
		{
			for (int col = 0; col < matrix.getColumnDimension(); col++)
			{
				Assert.assertEquals(matrix.getEntry(row, col), value, String.format("value for %d,  %d", row, col));
			}
		}
	}
	
	@Test
	public void testMax()
	{
		RealMatrix matrix = MatrixUtils.createMatrix(10, 10, 10);
		Assert.assertEquals(MatrixUtils.max(matrix), 10.0);
		
		matrix.setEntry(1,  1, 100);
		Assert.assertEquals(MatrixUtils.max(matrix), 100.0);

		MatrixUtils.set(matrix, Double.POSITIVE_INFINITY);
		Assert.assertEquals(MatrixUtils.max(matrix), Double.POSITIVE_INFINITY);
		
		MatrixUtils.set(matrix, Double.NEGATIVE_INFINITY);
		Assert.assertEquals(MatrixUtils.max(matrix), Double.NEGATIVE_INFINITY);

		matrix.setEntry(5,  3, Double.MIN_VALUE);
		Assert.assertEquals(MatrixUtils.max(matrix), Double.MIN_VALUE);
	}
	
	@Test
	public void testMultiply()
	{
		RealMatrix matrix = MatrixUtils.createMatrix(15, 15, 0);
		MatrixUtils.multiply(matrix, 2);
		TestUtilities.compareMatrix(matrix, MatrixUtils.createMatrix(15, 15, 0), 0);
		
		matrix = MatrixUtils.createMatrix(15, 15, 1);
		MatrixUtils.multiply(matrix, 0.1);
		TestUtilities.compareMatrix(matrix, MatrixUtils.createMatrix(15, 15, 0.1), 0);
	}
	
	@Test
	public void testCardinality()
	{
		RealMatrix matrix = MatrixUtils.createMatrix(15, 15, 0);
		Assert.assertEquals(MatrixUtils.cardinality(matrix), 0);
		
		matrix.setEntry(1,  1, 1.1);
		Assert.assertEquals(MatrixUtils.cardinality(matrix), 1);

		matrix.setEntry(0,  2, -33);
		Assert.assertEquals(MatrixUtils.cardinality(matrix), 2);
	}
	
	@Test
	public void testSumMatrix()
	{
		RealMatrix matrix = MatrixUtils.createMatrix(10, 10, 0);
		Assert.assertEquals(MatrixUtils.sum(matrix), 0.0);
		
		MatrixUtils.set(matrix, 1);
		Assert.assertEquals(MatrixUtils.sum(matrix), 100.0);
	}
	
	@Test
	public void testSumVector()
	{
		RealVector vector = new ArrayRealVector(10);
		Assert.assertEquals(MatrixUtils.sum(vector), 0.0, 1e-100);
		
		vector.set(0.01);
		Assert.assertEquals(MatrixUtils.sum(vector), 0.1, 1e-10);
	}
	
	@Test
	public void testNormalizeMatrix()
	{
		
		for (int i = 0; i < 100; i++)
		{
			RealMatrix matrix = TestUtilities.createRandomMatrix(50, 50);
			MatrixUtils.normalize(matrix);
			Assert.assertEquals(MatrixUtils.sum(matrix), 1.0, 1e-10, "sum to one");
			for (int row = 0; row < matrix.getRowDimension(); row++)
			{
				for (int col = 0; col < matrix.getColumnDimension(); col++)
				{
					Assert.assertTrue(matrix.getEntry(row, col) >= 0, 
							String.format("value for %d,  %d is %2f", row, col, matrix.getEntry(row, col)));
				}
			}
		}
	}
	
	@Test
	public void testNormalizeVector()
	{
		RealMatrix randomMatrix = TestUtilities.createRandomMatrix(100, 50);
		for (int i = 0; i < 100; i++)
		{
			RealVector vector = randomMatrix.getRowVector(i);
			boolean normalized = MatrixUtils.normalize(vector);
			Assert.assertEquals(normalized, true, "wasn't all 0");
			Assert.assertEquals(MatrixUtils.sum(vector), 1.0, 1e-10, "sum to one");
			Assert.assertTrue(vector.getMinValue() >= 0, "all positive");
		}
		
		int size = 360;
		RealVector allZeros = new ArrayRealVector(size, 0);
		boolean normalized = MatrixUtils.normalize(allZeros);
		Assert.assertEquals(normalized, false, "all 0");
		Assert.assertEquals(MatrixUtils.sum(allZeros), 1.0, 1e-10, "sum to one");
		Assert.assertEquals(allZeros.getMinValue(), 1.0 / size, 1e-10, "min is 1 /size");
		Assert.assertEquals(allZeros.getMaxValue(), 1.0 / size, 1e-10, "max is 1 /size");
		
	}
	
	@Test
	public void testRescaleToSpanUnit()
	{
		RealVector allZero = new ArrayRealVector(100);
		MatrixUtils.rescaleToSpanUnit(allZero);
		Assert.assertEquals(MatrixUtils.sum(allZero), 0.0, "all zero left zero");
		
		RealVector mixed = new ArrayRealVector(new double[] {0, 0, 0.5, 0.2, 0.22, 0.1, 0, 0});
		MatrixUtils.rescaleToSpanUnit(mixed);
		Assert.assertEquals(mixed.toArray(), new double[] {0, 0, 1.0, 0.4, 0.44, 0.2, 0, 0}, "doubled (max was 0.5)");
	}
	
	@Test(enabled = false)
	public void comparePerformance()
	{
		RealVector vector = new ArrayRealVector(TestUtilities.createRandomMatrix(1, 1000).getRowVector(0));
		int numSims = 100000000;
		
		long loopTime = System.currentTimeMillis();
		for (int i = 0; i < numSims; i++)
		{
			sumForLoop(vector);
		}
		loopTime = System.currentTimeMillis() - loopTime;
		
		long walkTime = System.currentTimeMillis();
		for (int i = 0; i < numSims; i++)
		{
			sumWalker(vector);
		}
		walkTime = System.currentTimeMillis() - walkTime;
		
		System.out.println("Loop time: " + loopTime);
		System.out.println("Walk time: " + walkTime);
	}
	
	private double sumForLoop(RealVector vector)
	{
		double sum = 0;
		
		for (int i = 0; i < vector.getDimension(); i++)
		{
			sum += vector.getEntry(i);
		}
		return sum;
	}
	
	private double sumWalker(RealVector vector)
	{
		double sum = 0;
		
		sum = vector.walkInOptimizedOrder(new RealVectorPreservingVisitor() 
		{
			private double sum;
			
			@Override
			public void visit(int index, double value) 
			{
				sum += value;
			}
			
			@Override
			public void start(int dimension, int start, int end) 
			{
				sum = 0;
			}
			
			@Override
			public double end() 
			{
				return sum;
			}
		});
		
		return sum;
	}

}
