package ForagingModel.agent.movement;

import java.util.Set;

import ForagingModel.core.Angle;
import ForagingModel.core.ModelEnvironment;
import ForagingModel.core.NdPoint;
import ForagingModel.core.NumberGenerator;
import ForagingModel.core.Velocity;

public class CorrelatedProcess extends AbstractMovementProcess implements MovementProcess 
{
	protected NumberGenerator generator = ModelEnvironment.getNumberGenerator();

	protected double persistence;
	protected Angle angle;
	
	protected CorrelatedProcess(double speed, double persistence, Velocity initialVelocity, 
			double minDimensionX, double minDimensionY, double maxDimensionX, double maxDimensionY, double intervalSize)
	{
		super(minDimensionX, minDimensionY, maxDimensionX, maxDimensionY, intervalSize);
		this.speed = speed;
		this.persistence = persistence;
		this.angle = new Angle(initialVelocity.arg());
	}


	@Override
	public Velocity getNextVelocity(NdPoint currentLocation, boolean stateChange, Velocity previousVelocity) 
	{
		Angle nextAngle = getNextAngle(currentLocation);
		// start fresh with new angle if there is a state change between feeding and searching
		angle = stateChange ? nextAngle: Angle.combine(angle, nextAngle, persistence);
		Velocity velocity = Velocity.createPolar(speed, angle);
		
		Bounds outsideBounds = checkBounds(currentLocation, velocity);
		if (outsideBounds != Bounds.None)
		{
			angle = getNewAngle(currentLocation, outsideBounds);
			velocity = Velocity.createPolar(speed, angle);
		}
		
		velocity = velocity.scaleBy(dt);
		return velocity;

	}
	
	@Override
	public Velocity getEscapeVelocity(NdPoint currentLocation,	Set<NdPoint> predators) 
	{		
		Velocity velocity = super.getEscapeVelocity(currentLocation, predators);
		angle = new Angle(velocity.arg());
		return velocity;
	}
	
	@Override
	public Velocity getReverseVelocity(NdPoint currentLocation, Velocity previousVelocity)
	{
		angle = getReverseAngle(currentLocation, previousVelocity, speed);
		Velocity velocity = Velocity.createPolar(speed, angle);
		velocity = velocity.scaleBy(dt);
		return velocity;
	}


	protected Angle getNextAngle(NdPoint currentLocation)
	{
		return new Angle(generator.nextDoubleFromTo(0, 2.0 * Math.PI));
	}
	
	protected Angle getNewAngle(NdPoint currentLocation, Bounds outsideBounds)
	{
		Angle newAngle = getNewRandomAngle(outsideBounds);
		assert(checkBounds(Velocity.createPolar(speed, newAngle).move(currentLocation, dt), 0) == Bounds.None);
		return newAngle;

	}
	
}
