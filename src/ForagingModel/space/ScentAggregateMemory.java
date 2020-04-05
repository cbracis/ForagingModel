package ForagingModel.space;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import ForagingModel.core.MatrixUtils;
import ForagingModel.core.NdPoint;

/**
 * This class implements memory based on a resource memory and a scent map. That is, 
 * forager attracted to resources and repulsed by conspecifics.
 * It is modeled closely on AggregateMemory.
 */
public class ScentAggregateMemory extends AbstractMemory implements
		MemoryAssemblage 
{
	private ResourceMemory resourceMemory;
	private ScentHistory scentHistory;
	
	protected ScentAggregateMemory(ResourceMemory resourceMemory, ScentHistory scentHistory)
	{
		super(resourceMemory.angProbInfo);
		this.resourceMemory = resourceMemory;
		this.scentHistory = scentHistory;
		
		// set resource and scent to use same probability cache
		this.resourceMemory.probabilityCache = this.probabilityCache;
		this.scentHistory.probabilityCache = this.probabilityCache;
	}


	@Override
	public void execute(int currentInterval, int priority) 
	{
		resourceMemory.decay();
		scentHistory.decay();
	}

	@Override
	public void learn(NdPoint consumerLocation) 
	{
		resourceMemory.learn(consumerLocation);
		// scent updating handled by TODO
	}

	@Override
	public double[][] reportCurrentState() 
	{
		// for now just report resource, need to adjust by scent??
		RealMatrix probabilities = resourceMemory.getMemory();
		return probabilities.getData();
	}

	@Override
	protected RealMatrix getProbabilities(NdPoint currentLocation) 
	{
		// this is for unused MemoryDestinationMovement, if ever want to use that
		// again, this needs to do something smarter to take scent into account
		
		RealMatrix probabilities = resourceMemory.getProbabilities(currentLocation);		
		return probabilities;
	}

	@Override
	protected RealVector getAngularProbabilities(NdPoint currentLocation) 
	{
		RealVector resourceProbs = resourceMemory.getAngularProbabilities(currentLocation);
		RealVector conspecificSafety = scentHistory.getConspecificSafety(currentLocation);

		// multiply resource prob by conspecific safety which ranges [0,1] but doesn't sum to 1
		RealVector aggregateProbs = MatrixUtils.multiply(resourceProbs, conspecificSafety);
		
		if (!normalizeVector(aggregateProbs))
		{
			// all uniform, so base direction off conspecific safety, need to normalize first
			aggregateProbs = conspecificSafety.copy();
			normalizeVector(aggregateProbs);
		}
		
		probabilityCache.updateAggregate(aggregateProbs, 0);
		
		return aggregateProbs;
	}

}
