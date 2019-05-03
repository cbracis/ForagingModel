package ForagingModel.agent.movement;

import java.util.Set;

import ForagingModel.core.NdPoint;
import ForagingModel.agent.Recorder;
import ForagingModel.core.Velocity;
import ForagingModel.predator.PredatorManager;
import ForagingModel.space.LocationManager;
import ForagingModel.space.MemoryAssemblage;

public class MemoryDestinationMovement implements MovementBehavior, MemoryMovementBehavior 
{
	private DestinationProcess searching;
	private MovementProcess feeding;
	private BehaviorSwitchingRule switchingRule;
	private Recorder recorder;
	private BehaviorState previousState;
	private MemoryAssemblage memory;
	private LocationManager locationManager;
	private PredatorManager predators;
	private double predatorEncounterRadius;
	private boolean encounteredPredator;
	
	protected MemoryDestinationMovement(DestinationProcess searching, MovementProcess feeding, 
			BehaviorSwitchingRule switchingRule, Recorder recorder,
			MemoryAssemblage memory, LocationManager locationManger, PredatorManager predators, 
			NdPoint startingLocation, double predatorEncounterRadius) 
	{
		this.searching = searching;
		this.feeding = feeding;
		this.switchingRule = switchingRule;
		this.recorder = recorder;
		this.memory = memory;
		this.locationManager = locationManger;
		this.predators = predators;
		this.predatorEncounterRadius = predatorEncounterRadius;
		
		this.previousState = BehaviorState.Searching;
		this.encounteredPredator = false;
		updateDestination(startingLocation);
	}

	@Override
	public Velocity getNextVelocity(NdPoint currentLocation, double currentConsumptionRate) 
	{
		BehaviorState state = switchingRule.decideBehavior(currentConsumptionRate);
		boolean stateChanged = !(state == previousState);
		
		// check if arrived
		if (previousState.equals(BehaviorState.Searching) & searching.hasArrived(currentLocation))
		{
			state = BehaviorState.Feeding;
		}
		
		// switching from feeding to searching
		if (state.equals(BehaviorState.Searching) & previousState.equals(BehaviorState.Feeding))
		{
			updateDestination(currentLocation);
		}
		
		// if encountered predator, update state & destination for next step
		encounteredPredator = false;
		Set<NdPoint> encounters = predators.getActivePredators(currentLocation, predatorEncounterRadius);
		if (encounters.size() > 0)
		{
			state = BehaviorState.Escape;
			updateDestination(currentLocation);
			encounteredPredator = true;
		}

		
		// take evasive action if encountered predators
		Velocity velocity;
		if (encounters.size() > 0)
		{
			velocity = searching.getEscapeVelocity(currentLocation, encounters);
		}
		else
		{
			switch (state)
			{
			case Searching:
				velocity = searching.getNextVelocity(currentLocation, stateChanged, null);
				break;
			case Feeding:
				velocity = feeding.getNextVelocity(currentLocation, stateChanged, null);
				break;
			default:
				throw new IllegalArgumentException("Unexpected behavior state: " + state);	
			}
		}
		
		previousState = state;
		
		// learn at current location
		memory.learn(currentLocation);
		
		// predator encounters at current location (before moving) 
		recorder.recordPredatorEncounters(encounters);
		
		// record 
		recorder.recordVelocity(velocity);
		recorder.recordState(state);
		
		// only using memory when destination isn't null
		boolean useMemory = false;
		if (state == BehaviorState.Searching && searching.getDestination() != null)
		{
			useMemory = true;
		}
		recorder.recordUseMemory(useMemory);
		
		return velocity;
	}
	
	@Override
	public BehaviorState getState() 
	{
		return previousState;
	}

	@Override
	public MemoryAssemblage getMemory() 
	{
		return memory;
	}
	
	@Override
	public boolean encounteredPredator() 
	{
		// previousPredators is set to current encounter set at end of getNextVelocity()
		return encounteredPredator;
	}


	public NdPoint getDestination() 
	{
		return searching.getDestination();
	}

	private void updateDestination(NdPoint currentLocation)
	{
		// destination can be null if memory doesn't have any good locations to go to
		// this turns memory into kinesis
		NdPoint destination = memory.getDestinationProbabalistic(currentLocation);
		searching.setDestination(destination, currentLocation);
		locationManager.updateDestination(this, destination);
	}

}
