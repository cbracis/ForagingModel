package ForagingModel.agent.movement;

public enum MovementType 
{ 
	/**
	 * The forager switches between feeding and searching and uses memory to determine where to go when searching
	 * Specifically, a *destination* is probabilistically selected based on the memory map
	 */
	MemoryDestination, 
	/**
	 * The forager switches between feeding and searching and uses memory to determine where to go when searching
	 * Specifically, a *direction* is probabilistically selected based on the memory map
	 */
	MemoryDirectional, 
	/**
	 * The forager switches between feeding and searching using only local cues of current conditions
	 */
	Kinesis, 
	/**
	 * The forager always moves the same way (e.g. straight or OU)
	 */
	SingleState 
}
