package ForagingModel.space;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import ForagingModel.core.MatrixUtils;
import ForagingModel.core.NdPoint;

public class ScentSexHistory extends AbstractMemory implements MemoryAssemblage 
{
	private ScentHistory scentHistory;
	private ScentHistory femaleHistory;
	
	protected ScentSexHistory(ScentHistory scentHistory, ScentHistory femaleHistory)
	{
		super(scentHistory.angProbInfo);
		this.scentHistory = scentHistory;
		this.femaleHistory = femaleHistory;
		
		// set scent to use this probability cache
		this.scentHistory.probabilityCache = this.probabilityCache;
	}

	@Override
	public void execute(int currentInterval, int priority) 
	{
		scentHistory.decay();
		// female ScentHistory is shared and thus decayed on its own
	}

	@Override
	public void learn(NdPoint consumerLocation) 
	{
		// scent updating handled by ScentManager
	}

	@Override
	public double[][] reportCurrentState(State state) 
	{
		double[][] data;
		switch (state)
		{
		case Scent:
			data = scentHistory.reportCurrentState(state);
			break;
		case Resource:
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
		// this is only for unused MemoryDestinationMovement
		return null;
	}

	@Override
	protected RealVector getAngularProbabilities(NdPoint currentLocation) 
	{
		RealVector conspecificSafety = scentHistory.getConspecificSafety(currentLocation);
		RealVector femalesProbs = femaleHistory.getScentAttractionProbabilities(currentLocation);

		// resource and scent already use same probCache, but need to save femaleProbs
		probabilityCache.updateAttractiveScent(femalesProbs);
		

		// multiply female prob by conspecific safety which ranges [0,1] but doesn't sum to 1
		RealVector aggregateProbs = MatrixUtils.multiply(femalesProbs, conspecificSafety);
		
		if (!normalizeVector(aggregateProbs))
		{
			// all uniform, so base direction off conspecific safety, need to normalize first
			aggregateProbs = conspecificSafety.copy();
			normalizeVector(aggregateProbs);
		}
		
		probabilityCache.updateAggregate(aggregateProbs);
		
		return aggregateProbs;
	}

}
