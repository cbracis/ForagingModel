package ForagingModel.agent.movement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ForagingModel.agent.Recorder;
import ForagingModel.core.Angle;
import ForagingModel.core.DirectionProbabalistic;
import ForagingModel.core.NdPoint;
import ForagingModel.core.Velocity;
import ForagingModel.schedule.Notifiable;
import ForagingModel.space.MemoryAssemblage;

public class DirectionalContinuousCorrelatedProcess extends ContinuousCorrelatedProcess implements MovementProcess, Notifiable 
{
	private final static Logger logger = LoggerFactory.getLogger(DirectionalContinuousCorrelatedProcess.class);

	private MemoryAssemblage memory;
	private DirectionProbabalistic currentDirection;
	private boolean muSetForThisInterval; // avoids repeatedly setting mu during getNextVelocity()

	protected DirectionalContinuousCorrelatedProcess(double speed, double tau,
			MemoryAssemblage memory, DirectionUpdater directionUpdater, Velocity initialVelocity, 
			double minDimensionX, double minDimensionY, double maxDimensionX, double maxDimensionY, 
			double intervalSize, Recorder recorder) 
	{
		super(speed, tau, directionUpdater, initialVelocity, minDimensionX, minDimensionY, maxDimensionX, maxDimensionY,intervalSize, recorder);

		this.memory = memory;
		currentDirection = null;
		muSetForThisInterval = false;
	}
	
	@Override
	protected void setMu(NdPoint currentLocation, boolean force, Bounds outsideBounds) 
	{
		if (null == currentDirection)
		{
			force = true; // currentDirection not set yet, so make sure it is (i.e. first time feeding)
		}
		if ( (!muSetForThisInterval && directionUpdater.updateDirection()) || force)
		{
			if (outsideBounds == Bounds.None)
			{
				currentDirection = memory.getDirectionProbabalistic(currentLocation);
			} else
			{
				currentDirection = memory.getDirectionProbabalistic(currentLocation, super.getNewAngleBounds(outsideBounds));
			}
			mu = Velocity.createPolar(speed, currentDirection.angle());
			muSetForThisInterval = true;
//			logger.debug("Mu set to {}", mu);
		}
	}
		
	@Override
	public Velocity getNextVelocity(NdPoint currentLocation, boolean stateChange, Velocity previousVelocity) 
	{
		setMu(currentLocation, false, Bounds.None); // set mu once
		if (stateChange)
		{
// 			switching between searching and feeding, keep angle of previous state but switch to speed of this one
			currentVelocity = Velocity.createPolar(currentVelocity.mod(), previousVelocity.arg());
		}

		// try to not allow going in direction of predator by adjusting tau (but this will slow speed too??)
		double originalTau = tau;
		Velocity originalCurrentVelocity = currentVelocity;
		Velocity velocity = null;

		for (double t = tau; t > 0.9; t -= 0.1)
		{
			tau = t;
			currentVelocity = originalCurrentVelocity; // because this is updated in super.getNextVelocity(), start fresh each time
	
			velocity = super.getNextVelocity(currentLocation, false, previousVelocity); // will call setMu(), but already set, already took care of state change
			
			if (currentDirection.angleIsSafe(new Angle(velocity.arg())))
			{
				// angle is ok for predators
				break;
			}
		}
		
		tau = originalTau;
		return velocity;
	}


	@Override
	public void notifyInterval(int currentInterval) 
	{
		muSetForThisInterval = false;
	}

	@Override
	public void notifyTimeStep(int currentTimeStep) 
	{
		// do nothing
	}


}
