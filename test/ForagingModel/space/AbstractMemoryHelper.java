package ForagingModel.space;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import ForagingModel.core.NdPoint;

public class AbstractMemoryHelper extends AbstractMemory 
{
	protected AbstractMemoryHelper(AngularProbabilityInfo info)
	{
		super(info);
	}

	@Override
	protected RealMatrix getProbabilities(NdPoint currentLocation) 
	{
		return null;
	}

	@Override
	protected RealVector getAngularProbabilities(NdPoint currentLocation) 
	{
		// uniform based on number of angles
		int numAngles = angProbInfo.getAngles().size();
		return new ArrayRealVector(numAngles, 1.0 / (double)numAngles);
	}

	@Override
	public void learn(NdPoint consumerLocation) 
	{
	}

	@Override
	public double[][] reportCurrentState(State state) 
	{
		return null;
	}

	@Override
	public void execute(int currentInterval, int priority) 
	{
	}

}
