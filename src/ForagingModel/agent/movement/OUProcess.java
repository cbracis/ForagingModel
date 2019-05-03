package ForagingModel.agent.movement;

import java.util.Set;

import ForagingModel.core.Angle;
import ForagingModel.core.NdPoint;
import ForagingModel.core.ModelEnvironment;
import ForagingModel.core.NumberGenerator;
import ForagingModel.core.Velocity;

public class OUProcess extends AbstractMovementProcess implements MovementProcess 
{
	protected double tau;
	protected double beta;
	protected Velocity mu;
	protected double speed;
	private Velocity currentVelocity;
	private NumberGenerator generator;
	
	protected OUProcess(double speed, double tau, Velocity initialVelocity, 
			double minDimensionX, double minDimensionY, double maxDimensionX, double maxDimensionY, double intervalSize)
	{
		super(minDimensionX, minDimensionY, maxDimensionX, maxDimensionY, intervalSize);
		this.tau = tau;
		this.beta = 2 * speed / Math.sqrt(Math.PI * tau);
		this.mu = Velocity.create(0, 0); // no bias unless destination is set (DestinationOUMovement)
		this.speed = speed;
		this.currentVelocity = initialVelocity;
		generator = ModelEnvironment.getNumberGenerator();
	}

	@Override
	public Velocity getNextVelocity(NdPoint currentLocation, boolean stateChange, Velocity previousVelocity) 
	{
		// TODO: not handling stateChange, what to do to reset direction??
		// could update to use previous velocity
		
		// dV <- 1/tau*(mu-V) * dt + beta * complex(real=rnorm(1), imaginary=rnorm(1)) * sqrt(dt)
		Velocity stochastic = Velocity.create(generator.nextStandardNormal(), generator.nextStandardNormal()).scaleBy(Math.sqrt(dt)).scaleBy(beta);
		Velocity bias =  mu.minus(currentVelocity).scaleBy(1 / tau);
		Velocity dV = bias.plus(stochastic);
		
		Velocity newVelocity = currentVelocity.plus(dV);
		
		Bounds outsideBounds = checkBounds(currentLocation, newVelocity);
		if (outsideBounds != Bounds.None)
		{
			newVelocity = reflectOffBoundary(newVelocity, outsideBounds);
		}

		currentVelocity = newVelocity;
		
		newVelocity = newVelocity.scaleBy(dt);
		return newVelocity;
	}
	
	@Override
	public Velocity getEscapeVelocity(NdPoint currentLocation,	Set<NdPoint> predators) 
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
