package ForagingModel.input;

public class InputFactory 
{
	public static ResourceLandscapeReader createResourceLandscapeReader()
	{
		return new ResourceLandscapeReader();
	}
	
	public static StartPointReader createStartPointReader()
	{
		return new StartPointReader();
	}
}
