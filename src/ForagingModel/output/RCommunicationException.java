package ForagingModel.output;

import ForagingModel.core.ForagingModelException;

public class RCommunicationException extends ForagingModelException 
{
	private static final long serialVersionUID = -7211950955388385844L;

	public RCommunicationException() 
	{
		super();
	}
	
	public RCommunicationException( String message ) 
	{
		super( message );
	}
	
	public RCommunicationException( String message, Exception nestedException )
	{
		super( message, nestedException) ;
	}

}
