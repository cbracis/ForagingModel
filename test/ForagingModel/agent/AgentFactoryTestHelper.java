package ForagingModel.agent;

import ForagingModel.agent.movement.MovementBehavior;
import ForagingModel.space.LocationManager;
import ForagingModel.space.ResourceAssemblage;

public class AgentFactoryTestHelper 
{
	public static Forager createForager(LocationManager space, 
			ResourceAssemblage resource, MovementBehavior movement, Recorder recorder,
			double averageConsumption, double consumptionRate, double consumptionSpatialScale)
	{
		return AgentFactory.createForager(space, resource, movement, recorder, 
				averageConsumption, consumptionRate, consumptionSpatialScale);
	}
	
}
