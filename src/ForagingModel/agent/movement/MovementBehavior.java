package ForagingModel.agent.movement;

import ForagingModel.core.NdPoint;
import ForagingModel.core.Velocity;

public interface MovementBehavior 
{
	Velocity getNextVelocity(NdPoint currentLocation, double currentConsumptionRate);

	BehaviorState getState();
	
	boolean encounteredPredator();
}
