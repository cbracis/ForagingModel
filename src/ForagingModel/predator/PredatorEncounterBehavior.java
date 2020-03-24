package ForagingModel.predator;

public enum PredatorEncounterBehavior 
{
	/*
	 *  Move away orthogonally from predator(s), the default behavior
	 */
	Escape,
	/*
	 * Do nothing, continue to move normally (e.g., predators as camera traps)
	 */
	NoReaction
}
