package ForagingModel.space;

import java.util.List;

import ForagingModel.agent.Agent;
import ForagingModel.agent.movement.MovementBehavior;
import ForagingModel.core.NdPoint;
import ForagingModel.core.Velocity;

public interface LocationManager 
{
	public void moveByVector(Agent agent, Velocity velocity);
	
	public void moveTo(Agent agent, NdPoint point);

	public void updateDestination(Agent agent, NdPoint point);
	
	public void updateDestination(MovementBehavior movement, NdPoint point);

	public NdPoint getLocation(Agent agent);

	public NdPoint getDestination(Agent agent);

	public List<Agent> getAgents();
	
	public <T> List<T> getAgents(Class<T> type);

	public <T> List<NdPoint> getAgentLocations(Class<T> type);
	
}
