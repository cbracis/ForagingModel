package ForagingModel.core;

import org.apache.commons.math3.util.FastMath;

import ForagingModel.core.NdPoint;

public class Velocity 
{
	double x;
	double y;
	
	public static Velocity createPolar(double mod, double arg)
	{
		return new Velocity(mod * FastMath.cos(arg), mod * FastMath.sin(arg));
	}
	
	public static Velocity createPolar(double mod, Angle arg)
	{
		return Velocity.createPolar(mod, arg.get());
	}

	public static Velocity create(double x, double y)
	{
		return new Velocity(x, y);
	}

	public static Velocity create(NdPoint velocity)
	{
		return new Velocity(velocity.getX(), velocity.getY());
	}
	
	public static Velocity create(NdPoint from, NdPoint to, double speed)
	{
		double angle = Velocity.create(to.getX() - from.getX(), to.getY() - from.getY()).arg();
		return Velocity.createPolar(speed, angle);
	}
	
	private Velocity(double x, double y)
	{
		this.x = x;
		this.y = y;
	}
	
	public double x()
	{
		return x;
	}
	
	public double y()
	{
		return y;
	}

	public double mod()
	{
		return Math.sqrt(x * x + y * y);
	}
	
	public double arg()
	{
		return Math.atan2(y, x);
	}
	
	public NdPoint move(NdPoint point)
	{
		return move(point, 1);
	}
	
	public NdPoint move(NdPoint point, double intervalSize)
	{
		return new NdPoint(point.getX() + this.x * intervalSize, point.getY() + this.y * intervalSize);
	}
	
	@Override
	public String toString()
	{
		return String.format( "(%1f, %2f)", this.x, this.y );
	}

	@Override
	public int hashCode() {
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
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Velocity other = (Velocity) obj;
		if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
			return false;
		if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
			return false;
		return true;
	}

	public Velocity scaleBy(double dt) 
	{
		return Velocity.create(x * dt, y* dt);
	}

	public Velocity minus(Velocity velocity) 
	{
		return Velocity.create(this.x - velocity.x, this.y - velocity.y);
	}

	public Velocity plus(Velocity velocity) 
	{
		return Velocity.create(this.x + velocity.x, this.y + velocity.y);
	}

}
