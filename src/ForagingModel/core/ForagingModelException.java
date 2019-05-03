package ForagingModel.core;

/**
 * An unexpected exception in ForagingModel.
 *
 */
public class ForagingModelException extends RuntimeException 
{

	private static final long serialVersionUID = 229219356133024301L;

	public ForagingModelException() 
	{
		super(prefix());
	}
	
	public ForagingModelException( String message ) 
	{
		super( prefix() + message );
	}
	
	public ForagingModelException( String message, Exception nestedException )
	{
		super( prefix() + message, nestedException);
	}
	
	private static String prefix()
	{
		// information to reproduce simulation if there are exceptions
		return "S" + ModelEnvironment.getSimulationIndex() + " R" + Parameters.get().getRandomSeed() + ":";
	}

}
