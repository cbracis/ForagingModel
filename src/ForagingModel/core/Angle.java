package ForagingModel.core;


public class Angle
{
	private double angle;
	private final double p2 = 2.0 * Math.PI;
	
	/**
	 * Upper limit on angle value (which is not actually obtained since 2pi is equivalent to 0) 
	 */
	public final static double MAX_ANGLE = 2.0 * Math.PI;
	
	public Angle(double angle)
	{
		// restrict to [0, 2pi)
		// http://911programming.wordpress.com/2013/03/15/true-modulo-operation-according-to-congruence-relation/
		this.angle = ((angle % p2) + p2) % p2;
	}
	
	public static Angle combine(Angle oldAngle, Angle newAngle, double persistence)
	{
		// http://en.wikipedia.org/wiki/Mean_of_circular_quantities
		// avg will be [-pi, pi]
		double avg = Math.atan2(persistence * Math.sin(oldAngle.angle) + (1 - persistence) * Math.sin(newAngle.angle), 
								persistence * Math.cos(oldAngle.angle) + (1 - persistence) * Math.cos(newAngle.angle));
		return new Angle(avg);
	}
	
	public static Angle combine(double oldAngle, double newAngle, double persistence)
	{
		return Angle.combine(new Angle(oldAngle), new Angle(newAngle), persistence);
	}
	
	public static Angle absDifference(Angle angle1, Angle angle2)
	{
		double diff = Math.abs(angle1.get() - angle2.get());
		if (diff > Math.PI)
		{
			// need opposite angle
			diff = 2.0 * Math.PI - diff;
		}
		return new Angle(diff);
	}

	public double get() 
	{
		return angle;
	}
	
	@Override
	public String toString() 
	{
		return String.format( "%1f", this.angle);
	}

	@Override
	public int hashCode() 
	{
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(angle);
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
		Angle other = (Angle) obj;
		if (Double.doubleToLongBits(angle) != Double
				.doubleToLongBits(other.angle))
			return false;
		return true;
	}

}
