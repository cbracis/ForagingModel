package ForagingModel.core;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.util.FastMath;

public class EnsurePositive implements UnivariateFunction 
{

	@Override
	public double value(double x) 
	{
		return FastMath.max(x, 0);
	}

}
