package ForagingModel.agent.movement;

import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.DualHashBidiMap;

import ForagingModel.agent.Agent;

public class MovementMapper 
{
	private BidiMap movementToAgent;
	private BidiMap agentToMovement;
	
	protected MovementMapper()
	{
		movementToAgent = new DualHashBidiMap();
		agentToMovement = movementToAgent.inverseBidiMap();
	}
	
	public static MovementMapper create() 
	{
		return new MovementMapper();
	}
	
	public void reset()
	{
		movementToAgent.clear();
	}
	
	public void register(MovementBehavior movement, Agent agent)
	{
		movementToAgent.put(movement, agent);
	}
	
	public Agent getAgent(MovementBehavior movement)
	{
		return (Agent) movementToAgent.get(movement);
	}
	
	public MovementBehavior getMovement(Agent agent)
	{
		return (MovementBehavior) agentToMovement.get(agent);
	}

}
