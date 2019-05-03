package ForagingModel.agent.movement;

import ForagingModel.core.Angle;
import ForagingModel.core.DirectionProbabalistic;
import ForagingModel.core.NdPoint;
import ForagingModel.core.Velocity;
import ForagingModel.space.MemoryAssemblage;

public class DirectionalOUProcess extends OUProcess 
{
	private MemoryAssemblage memory;
	private DirectionProbabalistic currentDirection;

	protected DirectionalOUProcess(double speed, double tau, MemoryAssemblage memory, 
			Velocity initialVelocity, double minDimensionX, double minDimensionY, double maxDimensionX, double maxDimensionY,
			double intervalSize) 
	{
		super(speed, tau, initialVelocity, minDimensionX, minDimensionY, maxDimensionX, maxDimensionY, intervalSize);
		this.memory = memory;
		currentDirection = null;
		this.beta = 0;

	}
	
	@Override
	public Velocity getNextVelocity(NdPoint currentLocation, boolean stateChange, Velocity previousVelocity) 
	{
		// TODO: not handling stateChange, what to do to reset direction??
		
		// try to not allow going in direction of predator by adjusting tau (but this will slow speed too??)
		double origianlTau = tau;
		Velocity velocity = null;

		currentDirection = memory.getDirectionProbabalistic(currentLocation);

		for (double t = tau; t > 0; t -= 0.1)
		{
			tau = t;
	
			mu = Velocity.createPolar(speed, currentDirection.angle());
			velocity = super.getNextVelocity(currentLocation, stateChange, previousVelocity);
			
			if (currentDirection.angleIsSafe(new Angle(velocity.arg())))
			{
				// angle is ok for predators
				break;
			}
		}
		
		tau = origianlTau;
		return velocity;
	}

}
