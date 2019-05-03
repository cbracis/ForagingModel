package ForagingModel.agent.movement;

import org.mockito.Mockito;

import ForagingModel.agent.Recorder;
import ForagingModel.agent.movement.AbstractMovementProcess.AngleBounds;
import ForagingModel.core.Velocity;
import ForagingModel.predator.PredatorManager;

/**
 * Provides access to protected factory methods for testing purposes.
 *
 */
public class MovementFactoryTestHelper 
{
	public static MovementBehavior createStraightMovement(double speed, Velocity initialVelocity, double minDimension, double maxDimension, double intervalSize)
	{
		return MovementFactory.createSingleStateMovement(
				MovementFactory.createStraightProcess(speed, initialVelocity, minDimension, minDimension, maxDimension, maxDimension, intervalSize),
				Mockito.mock(Recorder.class), Mockito.mock(PredatorManager.class)
		);
	}

	public static AngleBounds createAngleBounds(double min, double max)
	{
		AbstractMovementProcessHelper helper = new AbstractMovementProcessHelper();
		return helper.new AngleBounds(min, max);
	}

}
