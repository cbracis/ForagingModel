package ForagingModel.space;

import ForagingModel.agent.movement.AbstractMovementProcess.AngleBounds;
import ForagingModel.core.DirectionProbabalistic;
import ForagingModel.core.DirectionProbabilityInfo;
import ForagingModel.core.NdPoint;
import ForagingModel.schedule.Notifiable;
import ForagingModel.schedule.Schedulable;

public interface MemoryAssemblage extends Schedulable, Notifiable
{
	public void learn(NdPoint consumerLocation);
	
	public NdPoint getDestinationProbabalistic(NdPoint currentLocation);
	
	public DirectionProbabalistic getDirectionProbabalistic(NdPoint currentLocation);

	public DirectionProbabalistic getDirectionProbabalistic(NdPoint currentLocation, AngleBounds bounds);

	public double[][] reportCurrentState();
	
	public DirectionProbabilityInfo reportCurrentProbabilities();
}
