package ForagingModel.agent.movement;

import java.util.Set;

import ForagingModel.agent.Recorder;
import ForagingModel.core.Angle;
import ForagingModel.core.ModelEnvironment;
import ForagingModel.core.NdPoint;
import ForagingModel.core.NumberGenerator;
import ForagingModel.core.Velocity;

public class ContinuousCorrelatedProcess extends AbstractMovementProcess implements MovementProcess 
{
	protected double tau;
	protected Velocity mu;
	protected double speed;
	protected Velocity currentVelocity;
	protected DirectionUpdater directionUpdater;
	private NumberGenerator generator;
	private Recorder recorder;

	protected ContinuousCorrelatedProcess(double speed, double tau, DirectionUpdater directionUpdater, Velocity initialVelocity, 
			double minDimensionX, double minDimensionY, double maxDimensionX, double maxDimensionY, double intervalSize, Recorder recorder) 
	{
		super(minDimensionX, minDimensionY, maxDimensionX, maxDimensionY, intervalSize);
		this.tau = tau;
		this.mu = Velocity.create(0, 0); // no bias unless destination is set (DestinationOUMovement)
		this.speed = speed;
		this.currentVelocity = initialVelocity;
		this.directionUpdater = directionUpdater;
		this.recorder = recorder;
		generator = ModelEnvironment.getNumberGenerator();
	}

	@Override
	public Velocity getNextVelocity(NdPoint currentLocation, boolean stateChange, Velocity previousVelocity) 
	{
		setMu(currentLocation, false, Bounds.None);
		
		if (stateChange)
		{
			// switching between searching and feeding, keep angle of previous state but switch to speed of this one
			currentVelocity = Velocity.createPolar(currentVelocity.mod(), previousVelocity.arg());
		}
		
		// dV <- 1/tau*(mu-V) * dt 
		Velocity dV = mu.minus(currentVelocity).scaleBy(1 / tau).scaleBy(dt);
		
		Velocity newVelocity = currentVelocity.plus(dV);
		
		Bounds outsideBounds = checkBounds(currentLocation, newVelocity);
		if (outsideBounds != Bounds.None)
		{
			newVelocity = reflectOffBoundary(newVelocity, outsideBounds);
			// reset mu after bounce, for new location
			setMu(newVelocity.scaleBy(dt).move(currentLocation), true, outsideBounds);

		}

		currentVelocity = newVelocity;
		recorder.recordMu(mu);
		
		// forager should just move distance velocity * dt
		newVelocity = newVelocity.scaleBy(dt);
		return newVelocity;
	}

	// override to control direction
	protected void setMu(NdPoint currentLocation, boolean force, Bounds outsideBounds) 
	{
		if (directionUpdater.updateDirection() || force)
		{
			if (outsideBounds == Bounds.None)
			{
				mu = Velocity.createPolar(speed, generator.nextDoubleFromTo(0, 2.0 * Math.PI));
			} else
			{
				mu = Velocity.createPolar(speed, super.getNewRandomAngle(outsideBounds));
			}
		}
	}

	@Override
	public Velocity getEscapeVelocity(NdPoint currentLocation, Set<NdPoint> predators) 
	{
		Angle escapeAngle = getAngleAway(currentLocation, predators);
		
		Bounds outsideBounds = checkBounds(currentLocation, Velocity.createPolar(speed, escapeAngle));
		if (outsideBounds != Bounds.None)
		{
			escapeAngle = getAngleAway(currentLocation, predators, outsideBounds);
			outsideBounds = checkBounds(currentLocation, Velocity.createPolar(speed, escapeAngle));
			
			if (outsideBounds != Bounds.None)
			{
				escapeAngle = getClosestAngle(escapeAngle, outsideBounds);
			}
		}
		
		// straight away from predator 
		Velocity newVelocity = Velocity.createPolar(speed, escapeAngle);
		currentVelocity = newVelocity;
		recorder.recordMu(mu);
		newVelocity = newVelocity.scaleBy(dt);
		return newVelocity;
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

}
