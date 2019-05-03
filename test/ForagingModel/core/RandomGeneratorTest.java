package ForagingModel.core;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.testng.Assert;
import org.testng.annotations.Test;

public class RandomGeneratorTest 
{
	@Test
	public void testNextIntProbabilities()
	{
		RandomGenerator generator = RandomGenerator.create();
		RealVector probabilities = new ArrayRealVector(new double[]{ 0.2, 0.5, 0.3 });
		int[] results = new int[] {0, 0, 0};
		int n = 1000;
		
		for (int i = 0; i < n; i++)
		{
			int choice = (int)generator.nextInt(probabilities);
			results[choice]++;
		}
		
		for (int i = 0; i < results.length; i++)
		{
			Assert.assertEquals(((double)results[i]) / n, probabilities.getEntry(i), 0.05, "actual results close to expected probability");
		}
	}
	
	@Test
	public void testNextIntProbabilitiesForBigVector()
	{
		RandomGenerator generator = RandomGenerator.create();
		int numProbs = 100;
		RealVector probabilities = new ArrayRealVector(numProbs);
		MatrixUtils.normalize(probabilities); 
		int[] results = new int[numProbs];
		int n = numProbs * 100000;
		
		for (int i = 0; i < n; i++)
		{
			int choice = (int)generator.nextInt(probabilities);
			results[choice]++;
		}
		
		for (int i = 0; i < results.length; i++)
		{
			Assert.assertEquals(((double)results[i]) / n, probabilities.getEntry(i), 1e-3, "actual results close to expected probability for i = " + i);
		}

	}

}
