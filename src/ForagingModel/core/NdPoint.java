package ForagingModel.core;

import java.io.Serializable;

import org.apache.commons.math3.util.FastMath;

public class NdPoint implements Serializable
{
	private static final long serialVersionUID = -4646162936783017548L;

	private double x;
	private double y;
	
	public NdPoint(double x, double y)
	{
		this.x = x;
		this.y = y;
	}

	public double getX()
	{
		return x;
	}
	
	public double getY()
	{
		return y;
	}

	@Override
	public String toString()
	{
		return String.format("(%.2f;%.2f)", x, y);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(x);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(y);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NdPoint other = (NdPoint) obj;
		if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
			return false;
		if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
			return false;
		return true;
	}
	
	public boolean equals(Object obj, double tolerance)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NdPoint other = (NdPoint) obj;
		if (FastMath.abs(x - other.x) > tolerance)
			return false;
		if (FastMath.abs(y - other.y) > tolerance)
			return false;
		return true;
	}

	
}
