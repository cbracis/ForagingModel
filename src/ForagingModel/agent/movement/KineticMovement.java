package ForagingModel.agent.movement;

import java.util.Set;

import ForagingModel.agent.Recorder;
import ForagingModel.core.NdPoint;
import ForagingModel.core.Velocity;
import ForagingModel.predator.PredatorEncounterBehavior;
import ForagingModel.predator.PredatorManager;

public class KineticMovement implements MovementBehavior
{
	private MovementProcess searching;
	private MovementProcess feeding;
	private BehaviorSwitchingRule switchingRule;
	private BehaviorState state;
	private Recorder recorder;
	private PredatorManager predators;
	private double predatorEncounterRadius;
	private PredatorEncounterBehavior predatorEncounterBehavior;
	private BehaviorState previousState;
	private Velocity previousVelocity;
	private boolean escapedPredator;

	protected KineticMovement(MovementProcess searching, MovementProcess feeding, 
			BehaviorSwitchingRule switchingRule, Recorder recorder,
			PredatorManager predators, double predatorEncounterRadius,
			PredatorEncounterBehavior predatorEncounterBehavior) 
	{
		this.searching = searching;
		this.feeding = feeding;
		this.switchingRule = switchingRule;
		this.state = BehaviorState.Searching; // start out searching, but doesn't really matter which
		this.recorder = recorder;
		this.predators = predators;
		this.predatorEncounterRadius = predatorEncounterRadius;
		this.predatorEncounterBehavior = predatorEncounterBehavior;
		previousState = BehaviorState.Searching;
		previousVelocity = null;
		escapedPredator = false;
	}

	@Override
	public Velocity getNextVelocity(NdPoint currentLocation, double currentConsumptionRate) 
	{
		state = switchingRule.decideBehavior(currentConsumptionRate);
		boolean stateChanged = !(state == previousState);
		
		// if encountered predator, switch to searching
		Set<NdPoint> encounters = predators.getActivePredators(currentLocation, predatorEncounterRadius);
		
		Velocity velocity;
		if ( encounters.size() > 0 // predators
			& predatorEncounterBehavior.equals(PredatorEncounterBehavior.Escape) )
		{
			state = BehaviorState.Escape;
			escapedPredator = true;
			
			// take evasive action 
			velocity = searching.getEscapeVelocity(currentLocation, encounters);
		}
		else // no predator encounters or no escape behavior
		{
			switch (state)
			{
			case Searching:
				velocity = searching.getNextVelocity(currentLocation, stateChanged, previousVelocity);
				break;
			case Feeding:
				velocity = feeding.getNextVelocity(currentLocation, stateChanged, previousVelocity);
				break;
			default:
				throw new IllegalArgumentException("Unexpected behavior state: " + state);	
			}
			
			escapedPredator = false;
		}
		
		// record 
		recorder.recordPredatorEncounters(encounters); //encounters here before moving
		recorder.recordVelocity(velocity);
		recorder.recordState(state);
		
		previousVelocity = velocity;
		previousState = state;

		return velocity;
	}

	@Override
	public BehaviorState getState() 
	{
		return state;
	}
	
	@Override
	public boolean escapedPredator() 
	{
		return escapedPredator;
	}

}
