package ForagingModel.agent.movement;

import java.util.HashSet;
import java.util.Set;

import ForagingModel.agent.Recorder;
import ForagingModel.core.NdPoint;
import ForagingModel.core.Velocity;
import ForagingModel.predator.PredatorManager;
import ForagingModel.space.SpaceUtils;

public class KineticMovement implements MovementBehavior
{
	private MovementProcess searching;
	private MovementProcess feeding;
	private BehaviorSwitchingRule switchingRule;
	private BehaviorState state;
	private Recorder recorder;
	private PredatorManager predators;
	private double predatorEncounterRadius;
	private BehaviorState previousState;
	private Velocity previousVelocity;
	private Set<NdPoint> previousPredators;

	protected KineticMovement(MovementProcess searching, MovementProcess feeding, 
			BehaviorSwitchingRule switchingRule, Recorder recorder,
			PredatorManager predators, double predatorEncounterRadius) 
	{
		this.searching = searching;
		this.feeding = feeding;
		this.switchingRule = switchingRule;
		this.state = BehaviorState.Searching; // start out searching, but doesn't really matter which
		this.recorder = recorder;
		this.predators = predators;
		this.predatorEncounterRadius = predatorEncounterRadius;
		previousState = BehaviorState.Searching;
		previousVelocity = null;
		previousPredators = new HashSet<NdPoint>();
	}

	@Override
	public Velocity getNextVelocity(NdPoint currentLocation, double currentConsumptionRate) 
	{
		state = switchingRule.decideBehavior(currentConsumptionRate);
		boolean stateChanged = !(state == previousState);
		
		// if encountered predator, switch to searching
		Set<NdPoint> encounters = predators.getActivePredators(currentLocation, predatorEncounterRadius);
//		Set<NdPoint> encountersIncPrevious = new HashSet<NdPoint>(encounters);
	//	encountersIncPrevious.addAll(previousPredators);
		
		Velocity velocity;
		if (encounters.size() > 0) // predators
		{
			state = BehaviorState.Escape;
			
			// take evasive action if encountered predators
			velocity = searching.getEscapeVelocity(currentLocation, encounters);
			
			// disable reversing steps for now to simplify model
			// if it needs to be added back, make sure to add to SingleState too

//			// check if this velocity is still in the encounter radius for the predators
//			boolean stillEncounteringPredators = isStillEncounteringPredators(velocity.move(currentLocation), encounters); // change here
//
//			// if so, back up instead, if this isn't first step
//			if (stillEncounteringPredators && previousVelocity != null)
//			{
//				Velocity reverseVelocity = searching.getReverseVelocity(currentLocation, previousVelocity);
//				
//				// but check first that this isn't also still encountering predators, which could be the case if a 
//				// predator just appeared and any more will still be inside encounter radius
//				if (!isStillEncounteringPredators(reverseVelocity.move(currentLocation), encounters)) // change here
//				{
//					velocity = reverseVelocity;
//				}
//			}
			
			// now set previous predators for next time
			previousPredators = encounters;
		}
		else // no predator encounters
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
			
			// now set previous predators for next time
			previousPredators = new HashSet<NdPoint>();
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
	public boolean encounteredPredator() 
	{
		// previousPredators is set to current encounter set at end of getNextVelocity()
		return previousPredators.size() > 0;
	}

	private boolean isStillEncounteringPredators(NdPoint location, Set<NdPoint> encounters)
	{
		boolean stillEncounteringPredators = false;
		
		for (NdPoint predatorLoc : encounters)
		{
			if (SpaceUtils.getDistance(location, predatorLoc) < predatorEncounterRadius)
			{
				stillEncounteringPredators = true;
			}
		}
		return stillEncounteringPredators;
	}

}
