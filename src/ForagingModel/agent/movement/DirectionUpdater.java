package ForagingModel.agent.movement;

import ForagingModel.schedule.Notifiable;

public interface DirectionUpdater extends Notifiable 
{
	boolean updateDirection();
}
