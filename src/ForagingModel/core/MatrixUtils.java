package ForagingModel.core;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealMatrixChangingVisitor;
import org.apache.commons.math3.linear.RealMatrixPreservingVisitor;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.util.FastMath;

public class MatrixUtils 
{
	// note: looping through the vector/matrix is much faster than using the XxxVisitor interfaces

	/**
	 * Sets all cells in the matrix to the specified value
	 * @param matrix matrix to change
	 * @param value value to set all cells to
	 */
	public static void set(RealMatrix matrix, double value)
	{
		for (int row = 0; row < matrix.getRowDimension(); row++)
		{
			for (int col = 0; col < matrix.getColumnDimension(); col++)
			{
				matrix.setEntry(row, col, value);
			}
		}
	}
	
	/**
	 * Creates a matrix of the specified dimension with all cells initialized to the same value
	 * @param rows number of rows
	 * @param columns number of columns
	 * @param initialValue value to initialize cells to
	 * @return the matrix
	 */
	public static RealMatrix createMatrix(int rows, int columns, double initialValue)
	{
		RealMatrix matrix = new Array2DRowRealMatrix(rows, columns);
		MatrixUtils.set(matrix, initialValue);
		return matrix;
	}
	
	/**
	 * Returns the maximum value in the matrix
	 * @param matrix the matrix to examine
	 * @return the maximum value
	 */
	public static double max(RealMatrix matrix)
	{
		double max = Double.NEGATIVE_INFINITY;
		
		for (int row = 0; row < matrix.getRowDimension(); row++)
		{
			for (int col = 0; col < matrix.getColumnDimension(); col++)
			{
				max = FastMath.max(max, matrix.getEntry(row, col));
			}
		}
	
		return max;
	}
	
	/**
	 * Multiplies each cell in the matrix by the specified value
	 * @param matrix the matrix to multiple
	 * @param d the value to multiply each cell by
	 */
	public static void multiply(RealMatrix matrix, final double scalar)
	{
		matrix.walkInOptimizedOrder(new RealMatrixChangingVisitor() 
		{
			@Override
			public double visit(int row, int column, double value) 
			{
				return value * scalar;
			}
			
			@Override
			public void start(int rows, int columns, int startRow, int endRow,
					int startColumn, int endColumn) {}
			
			@Override
			public double end() 
			{
				return 0;
			}
		});
	}
	
	/**
	 * Multiplies the corresponding values in a and b, resulting in a new vector of the same length
	 * @param a vector to multiply
	 * @param b vector to multiply
	 * @return new vector consisting of a[i] * b[i], or null if a and b are of different lengths
	 */
	public static RealVector multiply(RealVector a, RealVector b)
	{
		if (a.getDimension() != b.getDimension())
		{
			return null;
		}
		int size = a.getDimension();
		
		RealVector c = new ArrayRealVector(size);
		
		for (int i = 0; i < size; i++)
		{
			c.setEntry(i, a.getEntry(i) * b.getEntry(i));
		}
		
		return c;
	}
	
	/**
	 * Return the cardinality (number of non-zero cells) of the matrix
	 * @param matrix the matrix
	 * @return cardinality
	 */
	public static int cardinality(RealMatrix matrix)
	{
		int cardinality = 0;
		
		cardinality = (int) matrix.walkInOptimizedOrder(new RealMatrixPreservingVisitor() 
		{
			private int nonZeroEntries;
			
			@Override
			public void visit(int row, int column, double value) 
			{
				if (value != 0) nonZeroEntries++;
			}
			
			@Override
			public void start(int rows, int columns, int startRow, int endRow,
					int startColumn, int endColumn) 
			{
				nonZeroEntries = 0;
			}
			
			@Override
			public double end() 
			{
				return nonZeroEntries;
			}
		});
		
		return cardinality;
	}
	
	/**
	 * Sums all cell values in matrix
	 * @param matrix the matrix to sum
	 * @return sum
	 */
	public static double sum(RealMatrix matrix)
	{
		double sum = 0;
		
		sum = matrix.walkInOptimizedOrder(new RealMatrixPreservingVisitor() 
		{
			private double sum;
			
			@Override
			public void visit(int row, int column, double value) 
			{
				sum += value;
			}
			
			@Override
			public void start(int rows, int columns, int startRow, int endRow,
					int startColumn, int endColumn) 
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
	
	/**
	 * Sums all cell values in vector
	 * @param vector the vector to sum
	 * @return sum
	 */
	public static double sum(RealVector vector)
	{
		double sum = 0;
		
		for (int i = 0; i < vector.getDimension(); i++)
		{
			sum += vector.getEntry(i);
		}
		return sum;
	}
	
	/**
	 * Normalize a matrix so it sums to 1. If there are any negative values, they are first changed to 0.
	 * If the matrix is all 0, each cell is set to 1/size of the matrix.
	 * @param matrix the matrix to normalize (in place)
	 */
	public static void normalize(RealMatrix matrix)
	{
		// first change negative values to zero, then normalize
		matrix.walkInOptimizedOrder(new RealMatrixChangingVisitor() 
		{
			@Override
			public double visit(int row, int column, double value) 
			{
				return (value < 0) ? 0 : value;
			}
			
			@Override
			public void start(int rows, int columns, int startRow, int endRow,
					int startColumn, int endColumn) {}
			
			@Override
			public double end() 
			{
				return 0;
			}
		});
		
		final double sum = MatrixUtils.sum(matrix);
		int matrixSize = matrix.getRowDimension() * matrix.getColumnDimension();
		
		if (sum <= Double.MIN_VALUE * matrixSize)
		{
			MatrixUtils.set(matrix, 1.0 / matrixSize);
		}
		else 
		{
			MatrixUtils.multiply(matrix, 1.0 / sum);
		}
		
		assert(Math.abs(MatrixUtils.sum(matrix) - 1.0) < 1e-6);
	}
	
	/**
	 * Normalize a vector so it sums to 1. If there are any negative values, they are first changed to 0.
	 * If the vector is all 0, each cell is set to 1/length of the vector.
	 * @param vector the vector to normalize (in place)
	 * @return true if the vector was normalized, false if the vector was all 0 and each cell set to 1/length
	 */
	public static boolean normalize(RealVector vector)
	{
		// first change negative values to zero, then normalize
		// TODO what about small values? see AbstractMemory
		vector.mapToSelf(new EnsurePositive());
		
		double sum = MatrixUtils.sum(vector);
		boolean normalized = true;
		
		if (sum <= Double.MIN_VALUE * vector.getDimension())
		{
			vector.set(1.0 / vector.getDimension());
			normalized = false;
		}
		else 
		{
			vector.mapDivideToSelf(sum);
		}
		
		assert(!vector.isInfinite());
		assert(!vector.isNaN());
		assert(Math.abs(MatrixUtils.sum(vector) - 1.0) < 1e-6);
		
		return normalized;
	}
	
	/**
	 * Rescales vector so all values span [0,1].
	 * If the vector is all 0, it is not changed. Negative values are first changed to 0, then the vector is rescaled.
	 * @param vector the vector to rescale (in place)
	 */
	public static void rescaleToSpanUnit(RealVector vector)
	{
		// first change negative values to zero, then rescale
		vector.mapToSelf(new EnsurePositive());
		
		double max = vector.getMaxValue();
		
		if (max > Double.MIN_VALUE)
		{
			vector.mapDivideToSelf(max);
		}
		
		assert(!vector.isInfinite());
		assert(!vector.isNaN());
	}


}
