package ForagingModel.core;


public class CoreFactory 
{
	protected static ParameterManager createParameterManager(String propertiesFile)
	{
		return new ParameterManager(propertiesFile);
	}
}
