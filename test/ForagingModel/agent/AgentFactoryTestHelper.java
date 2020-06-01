package ForagingModel.agent;

import ForagingModel.agent.Agent.Sex;
import ForagingModel.agent.movement.MovementBehavior;
import ForagingModel.space.LocationManager;
import ForagingModel.space.ResourceAssemblage;

public class AgentFactoryTestHelper 
{
	public static Forager createForager(LocationManager space, Sex sex,
			ResourceAssemblage resource, MovementBehavior movement, Recorder recorder,
			double averageConsumption, double consumptionRate, double consumptionSpatialScale)
	{
		return AgentFactory.createForager(space, sex, resource, movement, recorder, 
				averageConsumption, consumptionRate, consumptionSpatialScale);
	}
	
}
