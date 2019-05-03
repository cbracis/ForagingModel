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
	public enum AgentType { Forager }

	public abstract AgentType getType();

}
