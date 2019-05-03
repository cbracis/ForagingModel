package ForagingModel.core;

public interface RepastParameters
{
	Object getValue(String parameter);
	String getValueAsString(String parameter);
	void setValue(String parameter, Object value);
}