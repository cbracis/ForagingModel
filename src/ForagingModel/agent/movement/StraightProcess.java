package ForagingModel.agent.movement;

import ForagingModel.core.Angle;
import ForagingModel.core.ForagingModelException;
import ForagingModel.core.NdPoint;
import ForagingModel.core.Velocity;

public class StraightProcess extends AbstractMovementProcess implements MovementProcess 
{
	
	protected StraightProcess(double speed, Velocity initialVelocity, double minDimensionX, double minDimensionY, double maxDimensionX, double maxDimensionY, 
			double intervalSize)
	{
		super(minDimensionX, minDimensionY, maxDimensionX, maxDimensionY, intervalSize);
		this.speed = speed;
		this.currentVelocity = initialVelocity;
	}

	@Override
	public Velocity getNextVelocity(NdPoint currentLocation, boolean stateChange, Velocity previousVelocity) 
	{
		if (stateChange)
		{
			currentVelocity = getNewVelocity(currentLocation, Bounds.None);
		}
		
		Velocity velocity = currentVelocity;
		NdPoint newLocation = velocity.move(currentLocation, dt);
		
	
		Bounds outsideBounds = checkBounds(newLocation, speed);
		if (outsideBounds != Bounds.None)
		{
			velocity = getNewVelocity(currentLocation, outsideBounds);
			currentVelocity = velocity;
		}
		
		velocity = velocity.scaleBy(dt);
		return velocity;
	}
	
	@Override
	public Velocity getReverseVelocity(NdPoint currentLocation, Velocity previousVelocity)
	{
		Angle reverseAngle = getReverseAngle(currentLocation, previousVelocity, speed);
		Velocity newVelocity = Velocity.createPolar(speed, reverseAngle);
		currentVelocity = newVelocity;
		newVelocity = newVelocity.scaleBy(dt);
		return newVelocity;
	}

	protected Velocity getNewVelocity(NdPoint currentLocation, Bounds outsideBounds)
	{
		// doesn't try to bound exactly off edge, just get a new velocity from the starting point
		Velocity newVelocity;
		int maxTries = 100;
		int tries = 0;	

		do
		{
			newVelocity = Velocity.createPolar(speed, getNewRandomAngle(outsideBounds));
			tries++;
		}
		while (checkBounds(newVelocity.move(currentLocation, dt), speed) != Bounds.None & tries < maxTries);
		
		if (tries == maxTries)
		{
			throw new ForagingModelException("Couldn't find velocity that didn't violate bound " + outsideBounds); 
		}
		return newVelocity;
	}

}
