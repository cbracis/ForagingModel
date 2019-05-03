package ForagingModel.core;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

/**
 * Provides values from various probability distributions. 
 * 
 * All the distributions from Apache Commons math3.
 */
public interface NumberGenerator
{

	/**
	 * This retrieves the next double in the specified range from the default uniform stream.
	 *
	 * @param from the start of the range (exclusive)
	 * @param to   the end of the range (exclusive)
	 * @return the next double from the default uniform stream
	 */
	public double nextDoubleFromTo(double from, double to);

	/**
	 * This retrieves the next double from the default uniform stream. T
	 *
	 * @return the next double from the default uniform stream
	 */
	public double nextDouble();

	/**
	 * This retrieves the next integer in the specified range from the default uniform stream.
	 *
	 * @param from the start of the range (inclusive)
	 * @param to   the end of the range (inclusive)
	 * @return the next int from the default uniform stream
	 */
	public long nextIntFromTo(int from, int to);

	/**
	 * This retrieves the next integer in the specified range from the default uniform stream.
	 *
	 * @param from the start of the range (inclusive)
	 * @param to   the end of the range (inclusive)
	 * @return the next int from the default uniform stream
	 */
	/**
	 * The retrieves an integer corresponding to an index and weighted by the specified probabilities
	 * @param probabilities a vector of probabilities summing to one
	 * @return an integer between 0 and length - 1 of the probabilities vector
	 */
	public long nextInt(RealVector probabilities);

	/**
	 * The retrieves an integer corresponding to a two-dimensional index and weighted by the specified probabilities
	 * @param probabilities a 2D matrix of probabilites summing to one
	 * @return an array of length 2 specifying the row and column 
	 */
	public GridPoint nextInt(RealMatrix probabilities);
	
	/**
	 * Retrieves the next double from a normal distribution with the specified parameters
	 * random number generator.
	 *
	 * @param mean
	 * @param standardDeviation
	 * @return double from that normal distribution
	 */
	public double nextNormal(double mean, double standardDeviation);

	/**
	 * Retrieves the next double from a normal distribution with mean = 0 and standard deviation = 1
	 * random number generator.
	 *
	 * @return double from standard normal distribution
	 */
	public double nextStandardNormal();
	
	/**
	 * Retrieves the next double from an exponential distribution with the specified parameter
	 * random number generator.
	 *
	 * @param mean the mean of the distribution, where mean = 1 / lambda, f(x) = lambda exp(-lambda x)
	 * @return double from that exponential distribution
	 */
	public double nextExponential(double mean);

}