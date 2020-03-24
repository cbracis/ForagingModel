package ForagingModel.agent.movement;

import java.util.Set;

import org.apache.commons.math3.util.FastMath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ForagingModel.core.Angle;
import ForagingModel.core.ModelEnvironment;
import ForagingModel.core.NdPoint;
import ForagingModel.core.NumberGenerator;
import ForagingModel.core.Velocity;
import ForagingModel.space.SpaceUtils;

public abstract class AbstractMovementProcess 
{
	private final static Logger logger = LoggerFactory.getLogger(AbstractMovementProcess.class);

	private double minDimensionX;
	private double minDimensionY;
	private double maxDimensionX;
	private double maxDimensionY;
	
	protected double speed;
	protected Velocity currentVelocity;
	protected double dt; // time step
	protected double cornerFactor;

	protected enum Bounds {None, Top, Bottom, Left, Right, TopLeft, TopRight, BottomLeft, BottomRight}

	protected AbstractMovementProcess(double minDimensionX, double minDimensionY, double maxDimensionX, double maxDimensionY, double intervalSize)
	{
		this.minDimensionX = minDimensionX;
		this.minDimensionY = minDimensionY;
		this.maxDimensionX = maxDimensionX;
		this.maxDimensionY = maxDimensionY;
		this.dt = intervalSize;
		cornerFactor = 2;
	}
	
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
	
	protected Bounds checkBounds(NdPoint currentLocation, Velocity unscaledVelocity)
	{
		return checkBounds(currentLocation, unscaledVelocity, true);
	}

	protected Bounds checkBounds(NdPoint currentLocation, Velocity unscaledVelocity, boolean includeCornerProtection)
	{
		NdPoint newLocation = unscaledVelocity.move(currentLocation, dt);
		// 1 not 0 or can get out of bounds error near corner when velocity has to be adjusted
		// i.e. only bottom boundary crossed, but near enough to right corner that that quadrant on angles also must be excluded
		double cornerOffset = unscaledVelocity.mod() * (includeCornerProtection ? cornerFactor : 1); 
		return checkBounds(newLocation, cornerOffset);
	}
	
	protected Bounds checkBounds(NdPoint newLocation, double cornerOffset)
	{
		Bounds boundaryCrossed = Bounds.None;
		
		if (newLocation.getY() < minDimensionY)	// bottom
		{
			if (newLocation.getX() < minDimensionX + cornerOffset)
			{
				boundaryCrossed = Bounds.BottomLeft;
			} 
			else if (newLocation.getX() > maxDimensionX - cornerOffset)
			{
				boundaryCrossed = Bounds.BottomRight;
			}
			else
			{
				boundaryCrossed = Bounds.Bottom;
			}
		}
		else if (newLocation.getY() > maxDimensionY)	// top
		{
			if (newLocation.getX() < minDimensionX + cornerOffset)
			{
				boundaryCrossed = Bounds.TopLeft;
			} 
			else if (newLocation.getX() > maxDimensionX - cornerOffset)
			{
				boundaryCrossed = Bounds.TopRight;
			}
			else
			{
				boundaryCrossed = Bounds.Top;
			}
		}
		else if (newLocation.getX() < minDimensionX) // left
		{
			if (newLocation.getY() < minDimensionY + cornerOffset)
			{
				boundaryCrossed = Bounds.BottomLeft;
			} 
			else if (newLocation.getY() > maxDimensionY - cornerOffset)
			{
				boundaryCrossed = Bounds.TopLeft;
			}
			else
			{
				boundaryCrossed = Bounds.Left;
			}
		}
		else if (newLocation.getX() > maxDimensionX) // right
		{
			if (newLocation.getY() < minDimensionY + cornerOffset)
			{
				boundaryCrossed = Bounds.BottomRight;
			} 
			else if (newLocation.getY() > maxDimensionY - cornerOffset)
			{
				boundaryCrossed = Bounds.TopRight;
			}
			else
			{
				boundaryCrossed = Bounds.Right;
			}
		}
		return boundaryCrossed;
	}
	
	protected Velocity reflectOffBoundary(Velocity newVelocity, Bounds outsideBounds) 
	{
		Velocity reflectedVelocity;
		
		switch (outsideBounds)
		{
		case Top:
		case Bottom:
			reflectedVelocity = Velocity.create(newVelocity.x(), -newVelocity.y());	// flip y
			break;
		case Left:
		case Right:
			reflectedVelocity = Velocity.create(-newVelocity.x(), newVelocity.y());	// flip x
			break;
		// make sure signs are correct in corners
		case TopLeft:
			reflectedVelocity = Velocity.create(Math.abs(newVelocity.x()), -Math.abs(newVelocity.y()));	// +x, -y
			break;
		case TopRight:
			reflectedVelocity = Velocity.create(-Math.abs(newVelocity.x()), -Math.abs(newVelocity.y())); // -x, -y
			break;
		case BottomLeft:
			reflectedVelocity = Velocity.create(Math.abs(newVelocity.x()), Math.abs(newVelocity.y()));	// +x, +y
			break;
		case BottomRight:
			reflectedVelocity = Velocity.create(-Math.abs(newVelocity.x()), Math.abs(newVelocity.y()));	// -x, +y
			break;
		default:
			throw new IllegalArgumentException("Value of Bounds doesn't match any cases: " + outsideBounds);
		}
		return reflectedVelocity;
	}
	
	protected AngleBounds getNewAngleBounds(Bounds outsideBounds)
	{
		double angleMin;
		double angleMax;
		
		switch (outsideBounds)
		{
		case Top:
			angleMin = Math.PI;
			angleMax = 2 * Math.PI;
			break;
		case TopLeft:
			angleMin = 3 * Math.PI / 2;
			angleMax = 2 * Math.PI;
			break;
		case Left:
			angleMin = 3 * Math.PI / 2;
			angleMax = Math.PI / 2;
			break;
		case BottomLeft:
			angleMin = 0;
			angleMax = Math.PI / 2;
			break;
		case Bottom:
			angleMin = 0;
			angleMax = Math.PI;
			break;
		case BottomRight:
			angleMin = Math.PI / 2;
			angleMax = Math.PI;
			break;
		case Right:
			angleMin = Math.PI / 2;
			angleMax = 3 * Math.PI / 2;
			break;
		case TopRight:
			angleMin = Math.PI;
			angleMax = 3 * Math.PI / 2;
			break;
		case None:
			angleMin = 0;
			angleMax = 2 * Math.PI;
			break;
		default:
			throw new IllegalArgumentException("Value of Bounds doesn't match any cases: " + outsideBounds);
		}
		return new AngleBounds(angleMin, angleMax);
	}
	
	protected Angle getClosestBoundaryAngleAway(NdPoint location, Bounds outsideBounds)
	{
		Angle angle;
		
		switch (outsideBounds)
		{
		case Top:
			angle = new Angle(3 * Math.PI / 2);
			break;
		case TopLeft:
			angle = new Angle(7 * Math.PI / 4);
			break;
		case Left:
			angle = new Angle(0);
			break;
		case BottomLeft:
			angle = new Angle(Math.PI / 4);
			break;
		case Bottom:
			angle = new Angle(Math.PI / 2);
			break;
		case BottomRight:
			angle = new Angle(3 * Math.PI / 4);
			break;
		case Right:
			angle = new Angle(Math.PI);
			break;
		case TopRight:
			angle = new Angle(5 * Math.PI / 4);
			break;
		case None:
			angle = null;
			break;
		default:
			throw new IllegalArgumentException("Value of Bounds doesn't match any cases: " + outsideBounds);
		}
		return angle;
	}
	
	protected Angle getNewRandomAngle(Bounds outsideBounds)
	{
		AngleBounds bounds = getNewAngleBounds(outsideBounds);
		NumberGenerator generator = ModelEnvironment.getNumberGenerator();
		
		Angle angle;
		if (bounds.overlapsZero())
		{
			// min angle is below zero (greater) and max is above zero
			double magnitudeBelowZero = (2 * Math.PI - bounds.minAngle);
			double shiftedAngle = generator.nextDoubleFromTo(0, bounds.maxAngle + magnitudeBelowZero);
			angle = new Angle( shiftedAngle - magnitudeBelowZero );

		}
		else
		{
			angle = new Angle( generator.nextDoubleFromTo(bounds.minAngle, 
														  bounds.maxAngle ));
		}
		return angle;
	}
	
	protected Angle getClosestAngle(Angle outOfBounds, Bounds outsideBounds)
	{
		AngleBounds bounds = getNewAngleBounds(outsideBounds);
		Angle diffWithMin = Angle.absDifference(outOfBounds, new Angle(bounds.minAngle));
		Angle diffWithMax = Angle.absDifference(outOfBounds, new Angle(bounds.maxAngle));
		
		double closest = (diffWithMin.get() < diffWithMax.get()) ? bounds.minAngle : bounds.maxAngle;
		
		return new Angle(closest);
	}
	
	protected Angle getAngleAway(NdPoint currentLocation, Set<NdPoint> predators)
	{
		Angle away = null;
		
		Velocity vector = Velocity.create(0, 0);
		if (predators.size() > 0)
		{
			for (NdPoint predator : predators)
			{
				// combine vectors, all weighted by distance
				vector = vector.plus(Velocity.create(predator, 
													 currentLocation, 
						  							 1.0 / SpaceUtils.getDistance(predator, currentLocation)));
			}
			
			away = new Angle(vector.arg());
		}
		return away;
	}
	
	protected Angle getAngleAway(NdPoint currentLocation, Set<NdPoint> predators, Bounds outsideBounds)
	{
		// previous escape direction crossed boundary, so add "predator" to direct away from boundary and recalculate escape angle
		double minPredDistance = Double.MAX_VALUE;
		for (NdPoint predator : predators)
		{
			minPredDistance = FastMath.min(minPredDistance, SpaceUtils.getDistance(predator, currentLocation));
		}
		Angle angleAwayBoundary = getClosestBoundaryAngleAway(currentLocation, outsideBounds);
		Velocity awayBoundary = Velocity.createPolar(minPredDistance, angleAwayBoundary);
		
		predators.add(awayBoundary.move(currentLocation));
		return getAngleAway(currentLocation, predators);
	}

	
	protected Angle getReverseAngle(NdPoint currentLocation, Velocity previousVelocity, double speed)
	{
		Angle reverseAngle = new Angle(previousVelocity.arg() + Math.PI);
		
		Bounds outsideBounds = checkBounds(currentLocation, Velocity.createPolar(speed, reverseAngle), false);
		if (outsideBounds != Bounds.None)
		{
			reverseAngle = getClosestAngle(reverseAngle, outsideBounds);
			logger.trace("Warning {}: previous vel {} and loc {}, but reverse outside bounds {}", 
					ModelEnvironment.getSimulationIndex(), previousVelocity.toString(), currentLocation.toString(), outsideBounds.toString());
		}
		
		return reverseAngle;
	}
	
	public class AngleBounds
	{
		protected double minAngle;
		protected double maxAngle;
		
		protected AngleBounds(double minAngle, double maxAngle)
		{
			this.minAngle = minAngle;
			this.maxAngle = maxAngle;
		}
		
		public boolean overlapsZero()
		{
			return (minAngle > maxAngle);
		}
		
		public double getMin()
		{
			return minAngle;
		}
		
		public double getMax()
		{
			return maxAngle;
		}
	}

}
