package ForagingModel.predator;

import java.io.Serializable;

import ForagingModel.core.NdPoint;

public class Predator implements Serializable
{
	private static final long serialVersionUID = -4279312279075717252L;

	private int startInterval;
	private int stopInterval;
	private NdPoint location;
	
	protected Predator(int startInterval, int duration, NdPoint location)
	{
		this.startInterval = startInterval;
		this.stopInterval = startInterval + duration;
		this.location = location;
	}
	
	public static Predator create(int startInterval, int duration, NdPoint location)
	{
		return new Predator(startInterval, duration, location);
	}

	public boolean isActive(int interval) 
	{
		boolean active = (interval >= startInterval && interval < stopInterval);
			
		return active;
	}
	
	public NdPoint getLocation()
	{
		return location;
	}
	
	// these 2 report methods need to be kept in sync
	public static String[] reportColumns()
	{
		return new String[] { "startInterval", "stopInterval", "x", "y" };
	}
	
	// these 2 report methods need to be kept in sync
	public String[] report()
	{
		return new String[] { Integer.toString(startInterval), Integer.toString(stopInterval),
							  Double.toString(location.getX()), Double.toString(location.getY())};
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((location == null) ? 0 : location.hashCode());
		result = prime * result + startInterval;
		result = prime * result + stopInterval;
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
		Predator other = (Predator) obj;
		if (location == null) {
			if (other.location != null)
				return false;
		} else if (!location.equals(other.location))
			return false;
		if (startInterval != other.startInterval)
			return false;
		if (stopInterval != other.stopInterval)
			return false;
		return true;
	}

}
