
package ForagingModel.space;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ForagingModel.agent.Agent;
import ForagingModel.agent.movement.MovementBehavior;
import ForagingModel.core.ForagingModelException;
import ForagingModel.core.ModelEnvironment;
import ForagingModel.core.NdPoint;
import ForagingModel.core.Velocity;

public class LocationMangerImpl implements LocationManager 
{
	private double minDimensionX;
	private double minDimensionY;
	private double maxDimensionX;
	private double maxDimensionY;
	
	private Map<Agent, NdPoint> agents;
	private Map<Agent,NdPoint> destinations;
	

	protected LocationMangerImpl(double minDimensionX, double minDimensionY, double maxDimensionX, double maxDimensionY)
	{
		this.minDimensionX = minDimensionX;
		this.minDimensionY = minDimensionY;
		this.maxDimensionX = maxDimensionX;
		this.maxDimensionY = maxDimensionY;
		agents = new HashMap<Agent, NdPoint>();
		destinations = new HashMap<Agent, NdPoint>();
	}
	
	public void moveByVector(Agent agent, Velocity velocity) 
	{
		NdPoint newLocation = velocity.move(getLocation(agent));
		moveTo(agent, newLocation);
	}

	public void moveTo(Agent agent, NdPoint point) 
	{
		checkBounds(point);
		agents.put(agent, point);
	}

	public void updateDestination(Agent agent, NdPoint point) 
	{
		// destination can be null
		if (point != null)
		{
			checkBounds(point);
		}
		
		// TODO: throwing away initial destination during constructor, better way to handle this?
		if (agent!= null)
		{
			destinations.put(agent, point);
		}
	}

	public void updateDestination(MovementBehavior movement, NdPoint point) 
	{
		Agent agent = ModelEnvironment.getMovementMapper().getAgent(movement);
		updateDestination(agent, point);
	}

	public NdPoint getLocation(Agent agent) 
	{
		return agents.get(agent);
	}

	public NdPoint getDestination(Agent agent) 
	{
		// can be null for agents not using memory or currently w/o destination
		return destinations.get(agent);
	}

	public List<Agent> getAgents() 
	{
		return new ArrayList<Agent>(agents.keySet());
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> getAgents(Class<T> type) 
	{
		ArrayList<T> agentTypes = new ArrayList<T>();
		for (Agent agent : agents.keySet())
		{
			if (type.isInstance(agent))
			{
				agentTypes.add((T)agent);
			}
		}
		return agentTypes;
	}
	
	@Override
	public <T> List<NdPoint> getAgentLocations(Class<T> type) 
	{
		ArrayList<NdPoint> locs = new ArrayList<NdPoint>();
		
		for (Agent agent : agents.keySet())
		{
			if (type.isInstance(agent))
			{
				locs.add(agents.get(agent));
			}
		}
		return locs;
	}
	
	protected void checkBounds(NdPoint newLocation)
	{
		if ( newLocation.getX() < minDimensionX ||
			 newLocation.getX() > maxDimensionX ||
			 newLocation.getY() < minDimensionY ||
			 newLocation.getY() > maxDimensionY )
		{
			throw new ForagingModelException("New location outside of bounds of the landscape: " + newLocation);
		}
	}
}
