package ForagingModel.agent.movement;

import ForagingModel.core.NdPoint;
import ForagingModel.core.Velocity;
import ForagingModel.space.MemoryAssemblage;

public class MemoryDirectionalMovement implements MovementBehavior, MemoryMovementBehavior 
{
	private MovementBehavior underlyingMovement;
	private MemoryAssemblage memory;
	
	protected MemoryDirectionalMovement(MovementBehavior movement, MemoryAssemblage memory)
	{
		this.underlyingMovement = movement;
		this.memory = memory;
	}

	@Override
	public MemoryAssemblage getMemory() 
	{
		return memory;
	}

	@Override
	public Velocity getNextVelocity(NdPoint currentLocation, double currentConsumptionRate) 
	{
		// underlying movement handles predators
		Velocity velocity = underlyingMovement.getNextVelocity(currentLocation, currentConsumptionRate);
		
		// learn at current location
		memory.learn(currentLocation);

		// state and velocity recorded in underlying behaviors
		
		return velocity;
	}

	@Override
	public BehaviorState getState() 
	{
		return underlyingMovement.getState();
	}
	
	@Override
	public boolean encounteredPredator() 
	{
		return underlyingMovement.encounteredPredator();
	}

}
