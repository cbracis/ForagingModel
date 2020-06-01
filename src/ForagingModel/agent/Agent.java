/**
 * 
 */
package ForagingModel.agent;

import ForagingModel.schedule.Schedulable;

/**
  *
 */
public abstract class Agent implements Schedulable
{
	public enum AgentType { Forager, SexForager }
	
	public enum Sex { Female, Male, Unknown }

	public abstract AgentType getType();

}
