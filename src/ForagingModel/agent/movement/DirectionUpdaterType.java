package ForagingModel.agent.movement;

public enum DirectionUpdaterType 
{ 
	/**
	 * The direction is updated according to a Poisson process
	 */
	PoissonProcess, 
	/**
	 * The direction is updated every fixed number of time steps
	 */
	TimeStep
}

