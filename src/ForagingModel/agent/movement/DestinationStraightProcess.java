package ForagingModel.agent.movement;

import ForagingModel.core.ModelEnvironment;
import ForagingModel.core.Velocity;
import ForagingModel.space.SpaceUtils;
import ForagingModel.core.NdPoint;

public class DestinationStraightProcess extends StraightProcess implements DestinationProcess 
{
	private NdPoint destination;
	private double arrivalRadius;
 
	protected DestinationStraightProcess(double speed, double arrivalRadius, 
			Velocity initialVelocity, double minDimensionX, double minDimensionY, double maxDimensionX, double maxDimensionY, 
			double intervalSize) 
	{
		super(speed, initialVelocity, minDimensionX, minDimensionY, maxDimensionX, maxDimensionY, intervalSize);
		this.arrivalRadius = arrivalRadius;
	}
	
	public void setDestination(NdPoint destination, NdPoint currentLocation) 
	{
		this.destination = destination;
		if (destination != null)
		{
			currentVelocity = Velocity.create(currentLocation, destination, speed);
		}
		else
		{
			Velocity newVelocity = Velocity.createPolar(speed, 
														ModelEnvironment.getNumberGenerator().nextDoubleFromTo(0, 2*Math.PI));
			NdPoint newLocation = newVelocity.move(currentLocation, dt);
			Bounds outsideBounds = checkBounds(newLocation, speed);
			
			if (outsideBounds != Bounds.None)
			{
				newVelocity = getNewVelocity(currentLocation, outsideBounds);
			}	
			
			currentVelocity = newVelocity;
		}
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
