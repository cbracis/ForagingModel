package ForagingModel.space;

import ForagingModel.agent.movement.AbstractMovementProcess.AngleBounds;
import ForagingModel.core.DirectionProbabalistic;
import ForagingModel.core.DirectionProbabilityInfo;
import ForagingModel.core.NdPoint;
import ForagingModel.schedule.Notifiable;
import ForagingModel.schedule.Schedulable;

public interface MemoryAssemblage extends Schedulable, Notifiable
{
	public enum State 
	{
		Resource,
		Predators,
		Scent
	}
	
	public void learn(NdPoint consumerLocation);
	
	public NdPoint getDestinationProbabalistic(NdPoint currentLocation);
	
	public DirectionProbabalistic getDirectionProbabalistic(NdPoint currentLocation);

	public DirectionProbabalistic getDirectionProbabalistic(NdPoint currentLocation, AngleBounds bounds);

	public double[][] reportCurrentState(MemoryAssemblage.State state);
	
	public DirectionProbabilityInfo reportCurrentProbabilities();
}
