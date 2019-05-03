package ForagingModel.schedule;

public interface Notifiable 
{
	void notifyInterval(int currentInterval);

	void notifyTimeStep(int currentTimeStep);

}
