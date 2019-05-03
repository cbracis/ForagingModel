package ForagingModel.agent.movement;

import ForagingModel.core.Velocity;
import ForagingModel.space.SpaceUtils;
import ForagingModel.core.NdPoint;

public class DestinationOUProcess extends OUProcess implements DestinationProcess 
{
	private Velocity mu0 = Velocity.create(0, 0);
	private NdPoint destination;
	private double arrivalRadius;
	
	protected DestinationOUProcess(double speed, double tau, double arrivalRadius,
			Velocity initialVelocity, double minDimensionX, double minDimensionY, double maxDimensionX, double maxDimensionY,
			double intervalSize) 
	{
		super(speed, tau, initialVelocity, minDimensionX, minDimensionY, maxDimensionX, maxDimensionY, intervalSize);
		this.arrivalRadius = arrivalRadius;
	}
	
	@Override
	public Velocity getNextVelocity(NdPoint currentLocation, boolean stateChange, Velocity previousVelocity) 
	{
		// TODO: not handling stateChange, what to do to reset direction??
		
		// with no destination, use an undirected OU process with mu = 0
		this.mu = (destination == null) ? mu0 : Velocity.create(currentLocation, destination, speed);
		return super.getNextVelocity(currentLocation, stateChange, previousVelocity);
	}

	public void setDestination(NdPoint destination, NdPoint currentLocation) 
	{
		this.destination = destination;
	}

	public NdPoint getDestination() 
	{
		return destination;
	}

	public boolean hasArrived(NdPoint currentLocation) 
	{
		boolean hasArrived = false;
		if (destination != null && SpaceUtils.getDistance(destination, currentLocation) < arrivalRadius)
		{
			hasArrived = true;
		}
		return hasArrived;
	}
	

}
