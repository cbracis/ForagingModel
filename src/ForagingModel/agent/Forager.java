/**
 * 
 */
package ForagingModel.agent;

import ForagingModel.agent.movement.BehaviorState;
import ForagingModel.agent.movement.MovementBehavior;
import ForagingModel.core.NdPoint;
import ForagingModel.core.Velocity;
import ForagingModel.space.LocationManager;
import ForagingModel.space.ResourceAssemblage;

/**
  *
 */
public class Forager extends Agent implements MovingAgent
{
	private static int currentAvailableId = 0;
	private int id;
	
	private LocationManager space;
	private ResourceAssemblage resource;
	private MovementBehavior movementBehavior;
	private Recorder recorder;
	
	private final double averageConsumption;
	private double consumptionRate;
	private double consumptionSpatialScale;
	
	private double currentConsumption = 0;
	
	protected Forager(LocationManager space, 
			ResourceAssemblage resource, MovementBehavior movementBehavior, Recorder recorder,
			double averageConsumption, double consumptionRate, double consumptionSpatialScale)
	{
		this.space = space;
		this.resource = resource;
		this.movementBehavior = movementBehavior;
		this.recorder = recorder;
		this.averageConsumption = averageConsumption;
		this.consumptionRate = consumptionRate;
		this.consumptionSpatialScale = consumptionSpatialScale;
		this.id = currentAvailableId++;
		init();
	}
	
	public void move()
	{
		Velocity movementVelocity = movementBehavior.getNextVelocity(space.getLocation(this), currentConsumption);
		space.moveByVector(this, movementVelocity);
		NdPoint location = space.getLocation(this);
		recorder.recordLocation(location);
		recorder.recordQualityBin(resource.getPercentileBin(location));
	}
	
	public void consumeResource()
	{
		if (movementBehavior.encounteredPredator())
		{
			currentConsumption = 0;
		}
		else
		{
			currentConsumption = resource.consumeResource(space.getLocation(this), consumptionRate, consumptionSpatialScale);
		}
		recorder.recordConsumption(currentConsumption);
	}
	
	public Reporter getReporter()
	{
		return recorder;
	}
	
	
	public double getCurrentConsumption()
	{
		return currentConsumption;
	}

	public double getTheoreticalAverageConsumption()
	{
		return averageConsumption;
	}

	public int getId()
	{
		return id;
	}

	public BehaviorState getState() 
	{
		// movementBehavior can be null in batch mode for param combos that don't make sense
		return (movementBehavior == null) ? BehaviorState.NotInitialized : movementBehavior.getState();
	}
	
	public String getLocation()
	{
		return space.getLocation(this).toString();
	}
	
	protected void init()
	{
		// will eventually get initial destination etc.
		// previously we consumed resource, etc. before moving, does it matter?
	}

	@Override
	public void execute(int currentInterval) 
	{
		// For now, do both move and consume here
		// With multiple foragers, these will need to be split so all foragers move before consuming
		move();
		consumeResource();
		recorder.recordIteration(); // move to next iteration to record
	}
	

	@Override
	public AgentType getType() 
	{
		return AgentType.Forager;
	}

}

