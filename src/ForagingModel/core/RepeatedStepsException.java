package ForagingModel.core;

/**
 * Too many steps back and forth in the same place.
 *
 */
public class RepeatedStepsException extends ForagingModelException 
{

	private static final long serialVersionUID = -3887183822446606661L;

	public RepeatedStepsException() 
	{
		super();
	}
	
	public RepeatedStepsException(String message) 
	{
		super(message);
	}
	
	public RepeatedStepsException(String message, Exception nestedException)
	{
		super(message, nestedException);
	}

}
