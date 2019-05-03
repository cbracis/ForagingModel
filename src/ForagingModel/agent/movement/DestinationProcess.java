package ForagingModel.agent.movement;

import ForagingModel.core.NdPoint;

public interface DestinationProcess extends MovementProcess
{
	public void setDestination(NdPoint destination, NdPoint currentLocation);
	
	public NdPoint getDestination();

	public boolean hasArrived(NdPoint currentLocation);
}
