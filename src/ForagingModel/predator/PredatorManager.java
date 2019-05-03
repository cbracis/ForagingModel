package ForagingModel.predator;

import java.util.List;
import java.util.Set;

import ForagingModel.core.NdPoint;
import ForagingModel.schedule.Notifiable;

public interface PredatorManager extends Notifiable
{
	Set<NdPoint> getActivePredators();

	Set<NdPoint> getActivePredators(NdPoint consumerLocation, double encounterRadius);
	
	List<Predator> getAllPredators();
}
