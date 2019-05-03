package ForagingModel.core;

import org.apache.commons.math3.linear.RealVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ForagingModel.space.AngularProbabilityInfo;

public class DirectionProbabalistic 
{
	private final static Logger logger = LoggerFactory.getLogger(DirectionProbabalistic.class);

	private Angle angle;
	private double probability;
	private RealVector safety;
	private AngularProbabilityInfo angInfo;
	private double earthMoversDistance;
	
	private final double MAX_EMD = 0.2500000000000001; // empirically determined maximum earth mover's distance 
										 // between uniform and discrete pmf with all weight in one bin
	
	
	public DirectionProbabalistic(Angle angle, double probability, RealVector safety, AngularProbabilityInfo angInfo, double earthMoversDistance)
	{
		this.angle = angle;
		this.probability = probability;
		this.safety = safety;
		this.angInfo = angInfo;
		this.earthMoversDistance = earthMoversDistance;
		if (earthMoversDistance > MAX_EMD)
		{
			this.earthMoversDistance = MAX_EMD; 
			logger.warn("Earth mover's distance of {} greater than max {}", earthMoversDistance, MAX_EMD);
		}
	}
	
	public Angle angle()
	{
		return angle;
	}
	
	public double probability()
	{
		return probability;
	}
	
	public double scaledEarthMoversDistance()
	{
		// since min = 0, this returns value in [0,1]
		return earthMoversDistance / MAX_EMD;
	}
	
	public boolean angleIsSafe(Angle angle)
	{ 
		boolean isSafe = true;
		if (null != safety)
		{
			int index = angInfo.getIndexOf(angle);
			double safetyValue = safety.getEntry(index);
			isSafe = (safetyValue > Double.MIN_VALUE); 
		}
		return isSafe;
	}

	@Override
	public String toString() {
		return "[angle=" + angle + ", EMD=" + earthMoversDistance + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((angle == null) ? 0 : angle.hashCode());
		long temp;
		temp = Double.doubleToLongBits(earthMoversDistance);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DirectionProbabalistic other = (DirectionProbabalistic) obj;
		if (angle == null) {
			if (other.angle != null)
				return false;
		} else if (!angle.equals(other.angle))
			return false;
		if (Double.doubleToLongBits(earthMoversDistance) != Double
				.doubleToLongBits(other.earthMoversDistance))
			return false;
		return true;
	}
}
