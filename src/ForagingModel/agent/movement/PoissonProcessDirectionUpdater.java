package ForagingModel.agent.movement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ForagingModel.core.NumberGenerator;

public class PoissonProcessDirectionUpdater extends AbstractDirectionUpdater implements DirectionUpdater 
{
	private final static Logger logger = LoggerFactory.getLogger(PoissonProcessDirectionUpdater.class);

	private double meanIntervalUpdate; 
	private NumberGenerator generator;
	
	protected PoissonProcessDirectionUpdater(double meanIntervalUpdate, double intervalSize, NumberGenerator generator)
	{
		super(intervalSize);
		this.meanIntervalUpdate = meanIntervalUpdate;
		this.generator = generator;
	}

	protected double getNextUpdateTime(double currentTime)
	{
		return currentTime + generator.nextExponential(meanIntervalUpdate);
	}

}