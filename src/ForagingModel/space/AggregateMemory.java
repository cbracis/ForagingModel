package ForagingModel.space;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import ForagingModel.core.GridPoint;
import ForagingModel.core.MatrixUtils;
import ForagingModel.core.NdPoint;

public class AggregateMemory extends AbstractMemory implements MemoryAssemblage 
{
	private ResourceMemory resourceMemory;
	private PredatorMemory predatorMemory;
	
	private double foodSafetyTradeoff;
	
	private int currentInterval; // used for debugging
	
	protected AggregateMemory(ResourceMemory resourceMemory, PredatorMemory predatorMemory, double foodSafetyTradeoff)
	{
		super(resourceMemory.angProbInfo);
		this.resourceMemory = resourceMemory;
		this.predatorMemory = predatorMemory;
		
		// set resource and predator to use same probability cache
		this.resourceMemory.probabilityCache = this.probabilityCache;
		this.predatorMemory.probabilityCache = this.probabilityCache;
		
		this.foodSafetyTradeoff = foodSafetyTradeoff;
	}
	
	@Override
	public void learn(NdPoint consumerLocation) 
	{
		resourceMemory.learn(consumerLocation);
		predatorMemory.learn(consumerLocation);
	}

	@Override
	protected RealMatrix getProbabilities(NdPoint currentLocation) 
	{
		// very simplistic, add matrices together and then figure it out;
		
		RealMatrix probabilities = resourceMemory.getMemory();
		probabilities = probabilities.subtract(predatorMemory.getMemory());
		GridPoint currentPoint = SpaceUtils.getGridPoint(currentLocation);
		 
		for (int row = 0; row < probabilities.getRowDimension(); row++)
		{
			for(int column = 0; column < probabilities.getColumnDimension(); column++)
			{
				double dist = SpaceUtils.getDistance(currentLocation, new GridPoint(row, column));
				// TODO: need to scale by alpha and memory spatial scale??
				double prob = Math.exp(probabilities.getEntry(row, column) * Math.exp(-dist));
				if ( currentPoint.getX() == row && currentPoint.getY() == column )
				{
					// make current grid square 0, otherwise it will have high prob due to being close and direction of center is arbitrary
					// note that this is 1 not 0 since R code sets to 0 before multiplying by alpha and exp(0)=1
					prob = 1;
				}
				if (prob > MAX_PROBABILITY_VALUE) // e.g. infinity
				{
					// if memory is large (i.e. for example very concentrated patch) and nearby, the exponential can overflow and return infinity
					prob = MAX_PROBABILITY_VALUE;
				}
				probabilities.setEntry(row, column, prob);
			}
		}
		
		// can be set to null to avoid choosing a destination
		probabilities = normalizeMatrix(probabilities);
		
		return probabilities;

	}

	@Override
	protected RealVector getAngularProbabilities(NdPoint currentLocation) 
	{
		RealVector resourceProbs = resourceMemory.getAngularProbabilities(currentLocation);
		RealVector predatorSafety = predatorMemory.getPredatorSafety(currentLocation);

		// food-safety tradeoff: add probability to resource so that there are more direction options
		// currently as percentage of uniform distribution, so t = 1 would add an equal amount of probability uniformly distributed
		int size = resourceProbs.getDimension();
		double tradeoff = foodSafetyTradeoff / size;
		resourceProbs.mapAddToSelf(tradeoff);
		MatrixUtils.normalize(resourceProbs);
		
		// add probabilities (other options, multiply seems problematic)  // this was for both being probability distributions
		// multiply resource prob by predator safety which ranges [0,1] but doesn't sum to 1
		RealVector aggregateProbs = MatrixUtils.multiply(resourceProbs, predatorSafety);
		
		if (!normalizeVector(aggregateProbs))
		{
			// all uniform, so base direction off predator safety, need to normalize first
			aggregateProbs = predatorSafety.copy();
			normalizeVector(aggregateProbs);
		}
		
		probabilityCache.updateAggregate(aggregateProbs);
//		System.out.printf("i = %d, f = %.2e, p = %.2e\n", currentInterval, probabilityCache.foragingSum(), probabilityCache.predatorSum());
		
		return aggregateProbs;
	}
	

	@Override
	public double[][] reportCurrentState(State state) 
	{
		double[][] data;
		switch (state)
		{
		case Resource:
			data = resourceMemory.reportCurrentState(state);
			break;
		case Predators:
			data = predatorMemory.reportCurrentState(state);
			break;
		case Scent:
		default:
			data = null;
			break;
		}
		return data;
	}

	@Override
	public void execute(int currentInterval, int priority) 
	{
		resourceMemory.decay();
		predatorMemory.decay();
		
		this.currentInterval = currentInterval;
	}

	@Override
	public void notifyTimeStep(int currentTimeStep) 
	{
		// do nothing
	}

	// TODO: better way than making this public?
	
	public MemoryAssemblage getResourceMemory()
	{
		return resourceMemory;
	}
	
	public MemoryAssemblage getPredatorMemory()
	{
		return predatorMemory;
	}

}
