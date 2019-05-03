package ForagingModel.agent.movement;

import java.util.Set;

import ForagingModel.core.NdPoint;
import ForagingModel.core.Velocity;

public interface MovementProcess 
{
	Velocity getNextVelocity(NdPoint currentLocation, boolean stateChange, Velocity previousVelocity);
	
	Velocity getEscapeVelocity(NdPoint currentLocation, Set<NdPoint> predators);
	
	Velocity getReverseVelocity(NdPoint currentLocation, Velocity previousVelocity);
}
