package ForagingModel.space;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import ForagingModel.core.MatrixUtils;
import ForagingModel.core.NdPoint;

/**
 * This class implements memory based on a resource memory and a scent map. That is, 
 * forager attracted to resources and repulsed by conspecifics. However, in this case,
 * males in particular are repulsed only by other males (the scentHistory) but attracted
 * to females (the femaleHistory).
 * It is modeled closely on AggregateMemory.
 */
public class ScentSexAggregateMemory extends AbstractMemory implements
		MemoryAssemblage 
{
	private ResourceMemory resourceMemory;
	private ScentHistory scentHistory;
	private ScentHistory femaleHistory;
	
	protected ScentSexAggregateMemory(ResourceMemory resourceMemory, ScentHistory scentHistory,
			ScentHistory femaleHistory)
	{
		super(resourceMemory.angProbInfo);
		this.resourceMemory = resourceMemory;
		this.scentHistory = scentHistory;
		this.femaleHistory = femaleHistory;
		
		// set resource and scent to use same probability cache
		this.resourceMemory.probabilityCache = this.probabilityCache;
		this.scentHistory.probabilityCache = this.probabilityCache;
	}


	@Override
	public void execute(int currentInterval, int priority) 
	{
		resourceMemory.decay();
		scentHistory.decay();
		// TODO need to decay female history but it is shared and only want to do it once!
	}

	@Override
	public void learn(NdPoint consumerLocation) 
	{
		resourceMemory.learn(consumerLocation);
		// scent updating handled by ScentManager
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
		case Scent:
			data = scentHistory.reportCurrentState(state);
			break;
		case Predators:
		default:
			data = null;
			break;
		}
		return data;
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
		RealVector femalesProbs = femaleHistory.getAngularProbabilities(currentLocation);
		
		// for now add resource and female probs then renormalize, but obs other appraoches
		// such as first adding the matrices, then calculating the probablitites
		RealVector attractiveProbs = MatrixUtils.sum(resourceProbs, femalesProbs);
		normalizeVector(attractiveProbs);

		// multiply resource prob by conspecific safety which ranges [0,1] but doesn't sum to 1
		RealVector aggregateProbs = MatrixUtils.multiply(attractiveProbs, conspecificSafety);
		
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
