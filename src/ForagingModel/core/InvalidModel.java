package ForagingModel.core;

public class InvalidModel implements Model 
{
	private String reason;
	
	protected InvalidModel(String reason)
	{
		this.reason = reason;
	}
	
	@Override
	public void run() 
	{
		System.out.println("Not running model: " + reason);
	}

}
