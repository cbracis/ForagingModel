package ForagingModel.agent.movement;

import java.util.HashSet;
import java.util.Set;

import ForagingModel.agent.Recorder;
import ForagingModel.core.NdPoint;
import ForagingModel.core.Velocity;
import ForagingModel.predator.PredatorEncounterBehavior;
import ForagingModel.predator.PredatorManager;

public class SingleStateMovement implements MovementBehavior 
{
	private MovementProcess movement;
	private Recorder recorder;
	private boolean predationEnabled;
	private PredatorManager predators;
	private double predatorEncounterRadius;
	private PredatorEncounterBehavior predatorEncounterBehavior;
	private boolean escapedPredator;
	private BehaviorState state;

	protected SingleStateMovement(MovementProcess movement, Recorder recorder,
			PredatorManager predators, 
			double predatorEncounterRadius, PredatorEncounterBehavior predatorEncounterBehavior)
	{
		this.movement = movement;
		this.recorder = recorder;
		this.predators = predators;
		this.predatorEncounterRadius = predatorEncounterRadius;
		this.predatorEncounterBehavior = predatorEncounterBehavior;
		
		predationEnabled = (predators == null) ? false : true;
		escapedPredator = false;
	}
	
	@Override
	public Velocity getNextVelocity(NdPoint currentLocation, double currentConsumptionRate) 
	{
		Set<NdPoint> encounters = predationEnabled ?
				predators.getActivePredators(currentLocation, predatorEncounterRadius) 
				: new HashSet<NdPoint>(); // empty set

		Velocity velocity;

		if ( encounters.size() > 0 //predators
			& predatorEncounterBehavior.equals(PredatorEncounterBehavior.Escape) )
		{
			// take evasive action if encountered predators
			velocity = movement.getEscapeVelocity(currentLocation, encounters);
			state = BehaviorState.Escape;
			escapedPredator = true;
		}
		else
		{
			// null since previous velocity only matters for state change
			velocity = movement.getNextVelocity(currentLocation, false, null);
			escapedPredator = false;
			state = BehaviorState.SingleState;
		}
		
		recorder.recordPredatorEncounters(encounters); //encounters here before moving
		recorder.recordVelocity(velocity);
		recorder.recordState(getState());
		
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
