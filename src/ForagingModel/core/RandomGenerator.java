package ForagingModel.core;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.random.RandomDataGenerator;

/**
 * An implementation of NumberGenerator that provides random values.
 */
public class RandomGenerator implements NumberGenerator
{
	private RandomDataGenerator generator;
	
	private RandomGenerator(int seed) 
	{
		// Default is Well19937c, was using MersenneTwister before with colt libraries, outdated?
		 generator = new RandomDataGenerator(new org.apache.commons.math3.random.MersenneTwister(seed));	
		 //generator.reSeed(seed);
	}
	
	protected static RandomGenerator create()
	{
		return create((int)System.currentTimeMillis());
	}
	
	protected static RandomGenerator create(int seed)
	{
		return new RandomGenerator(seed);
	}
	
	
	public double nextDoubleFromTo(double from, double to) 
	{
		return generator.nextUniform(from, to);
	}

	
	public double nextDouble() 
	{
		return generator.nextUniform(0.0, 1.0);
	}

	
	public long nextIntFromTo(int from, int to) 
	{
		return generator.nextLong(from, to);
	}

	public long nextInt(RealVector probabilities) 
	{
		assert(Math.abs(MatrixUtils.sum(probabilities) - 1.0) < 1e-10);
		double cummulativeSum = 0;
		double randomValue = nextDoubleFromTo(0, 1);
		int i = 0;
		for (; i < probabilities.getDimension(); i++)
		{
			cummulativeSum += probabilities.getEntry(i);
			if (randomValue < cummulativeSum)
			{
				break;
			}
		}
		if (i == probabilities.getDimension())
		{
			System.out.println("WARNING: potential problem picking probability");
			System.out.println(probabilities.toString());
			i = probabilities.getDimension() - 1;
		}
		return i;
	}
	
	public GridPoint nextInt(RealMatrix probabilities)
	{
		double cummulativeSum = 0;
		double randomValue = nextDoubleFromTo(0, 1);
		int rows = probabilities.getRowDimension();
		int columns = probabilities.getColumnDimension();
		
		// the row and column of the cell to return 
		int row = 0;
		int column = 0;

		for (row = 0; row < rows; row++)
		{
			for (column = 0; column < columns; column++)
			{
				cummulativeSum += probabilities.getEntry(row, column);
				if (randomValue < cummulativeSum)
				{
					break;
				}
				
			}
			// if break out of inner loop, also need to break out of outer
			if (randomValue < cummulativeSum)
			{
				break;
			}

		}
		if (row == rows || column == columns)
		{
			throw new ForagingModelException("Exceeded bounds picking probability. Probabilities sum to " 
					+ MatrixUtils.sum(probabilities) + " landscape " + Parameters.get().getResourceLandscapeFile());
		}
		
		return new GridPoint(row, column);
	}
	
	public double nextNormal(double mean, double standardDeviation) 
	{
		return generator.nextGaussian(mean, standardDeviation);
	}
	
	public double nextStandardNormal()
	{
		return nextNormal(0, 1);
	}

	public double nextExponential(double mean) 
	{
		return generator.nextExponential(mean);
	}
	
}
