package ForagingModel.agent.movement;

public enum MovementProcessType 
{ 
	/**
	 * All movement is straight, even for multiple states (speeds may differ)
	 */
	Straight, 
	/**
	 * All movement uses an Orstein-Ulenbeck process, even for multiple states (speeds may differ)
	 */
	OU,
	/**
	 * Only valid for multiple states, feeding uses an OU process while searching uses straight movement
	 */
	Mixed,
	/**
	 * A correlated discrete random-walk, optionally with the direction being selected probabilistically from memory rather than randomly
	 */
	Correlated,
	/**
	 * A correlated continuous process, similar to the OU process without the white noise
	 */
	ContinuousCorrelated
	
}
