package ForagingModel.agent.movement;

import ForagingModel.core.Angle;
import ForagingModel.core.DirectionProbabalistic;
import ForagingModel.core.NdPoint;
import ForagingModel.core.Velocity;
import ForagingModel.space.MemoryAssemblage;

public class DirectionalCorrelatedProcess extends CorrelatedProcess 
{
	private MemoryAssemblage memory;
	private boolean memorySetsCorrelation;
	private DirectionProbabalistic currentDirection;

	protected DirectionalCorrelatedProcess(double speed, double persistence, 
			MemoryAssemblage memory, boolean memorySetsCorrelation,
			Velocity initialVelocity, double minDimensionX, double minDimensionY, double maxDimensionX, double maxDimensionY,
			double intervalSize) 
	{
		super(speed, persistence, initialVelocity, minDimensionX, minDimensionY, maxDimensionX, maxDimensionY, intervalSize);
		this.memory = memory;
		this.memorySetsCorrelation = memorySetsCorrelation;
		this.currentDirection = null;
	}
	
	@Override
	public Velocity getNextVelocity(NdPoint currentLocation, boolean stateChange, Velocity previousVelocity) 
	{
		
		// call CorrelatedProcess until not going in direction that is 0 for predator
		double originalPersistance = persistence;
		Velocity velocity = null;
		
		for (double i = 0; i <= 1.0; i += 0.1)
		{
			persistence = originalPersistance - i * originalPersistance;
			velocity = super.getNextVelocity(currentLocation, stateChange, previousVelocity); // sets angle and uses persistance
			
			if (currentDirection.angleIsSafe(angle))
			{
				// angle is ok for predators
				break;
			}
		}
		
		persistence = originalPersistance;
		return velocity;
	}
	
	@Override
	protected Angle getNextAngle(NdPoint currentLocation)
	{
		currentDirection = memory.getDirectionProbabalistic(currentLocation);
		if (memorySetsCorrelation)
		{
			this.persistence = 1.0 - currentDirection.scaledEarthMoversDistance();
		}
		return currentDirection.angle();
	}
	
	@Override
	protected Angle getNewAngle(NdPoint currentLocation, Bounds outsideBounds)
	{
		AngleBounds bounds = getNewAngleBounds(outsideBounds);
		currentDirection = memory.getDirectionProbabalistic(currentLocation, bounds);
		if (memorySetsCorrelation)
		{
			this.persistence = 1.0 - currentDirection.scaledEarthMoversDistance();
		}
		return currentDirection.angle();	
	}

}
