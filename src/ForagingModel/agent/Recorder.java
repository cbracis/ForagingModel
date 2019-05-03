package ForagingModel.agent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ForagingModel.agent.movement.BehaviorState;
import ForagingModel.core.ForagingModelException;
import ForagingModel.core.NdPoint;
import ForagingModel.core.RepeatedStepsException;
import ForagingModel.core.Velocity;
import ForagingModel.predator.Predator;
import ForagingModel.predator.PredatorManager;
import ForagingModel.space.SpaceUtils;

public class Recorder implements Reporter 
{
	private final static Logger logger = LoggerFactory.getLogger(Recorder.class);

	private final int MAX_REPEATED_STEPS = 10;
	private final int MAX_REPEATED_STEPS_LARGE = 20;
	private final int MAX_REPEATED_STEPS_ESCAPE = 5;
	private final double LOCATION_COMPARISON_TOLERANCE = 0.05;
	private final double LOCATION_COMPARISON_TOLERANCE_LARGE = 0.1;
	
	private DescriptiveStatistics consumption;
	private ArrayList<NdPoint> locations;
	private ArrayList<Velocity> velocities;
	private ArrayList<Velocity> mus;
	private ArrayList<BehaviorState> states;
	private ArrayList<Boolean> memoryUsage;
	private ArrayList<Integer> predatorEncounters;
	private ArrayList<Integer> predatorReEncounters;
	private ArrayList<Integer> qualityBins;
	private Set<NdPoint> encounteredPredators;
	private double distanceTraveled;
	private int repeatedSteps;
	private int repeatedStepsLarge;
	private int repeatedStepsEscape;
	
	private int numBurnInIterations;
	private int currentIteration;
	private int numQualityBins;
	
	private long startTime;
	private double intervalSize;
	
	private PredatorManager predators;

	protected Recorder(int numIterations, int numBurnInIterations, int numQualityBins, double intervalSize,
			PredatorManager predators)
	{
		this(numIterations, numBurnInIterations, numQualityBins, intervalSize);
		this.predators = predators;
	}
	
	protected Recorder(int numIterations, int numBurnInIterations, int numQualityBins, double intervalSize)
	{
		int recordedIterations = numIterations - numBurnInIterations;
		consumption = new DescriptiveStatistics(); 
		locations = new ArrayList<NdPoint>(recordedIterations);
		velocities = new ArrayList<Velocity>(recordedIterations);
		mus = new ArrayList<Velocity>(recordedIterations);
		states = new ArrayList<BehaviorState>(recordedIterations);
		memoryUsage = new ArrayList<Boolean>(recordedIterations);
		predatorEncounters = new ArrayList<Integer>(recordedIterations);
		predatorReEncounters = new ArrayList<Integer>(recordedIterations);
		qualityBins = new ArrayList<Integer>(recordedIterations);
		encounteredPredators = new HashSet<NdPoint>();
		distanceTraveled = 0;
		repeatedSteps = 0;
		repeatedStepsLarge = 0;
		repeatedStepsEscape = 0;
		this.numBurnInIterations = numBurnInIterations;
		currentIteration = 0; // index (0-based) of current iteration recording
		this.numQualityBins = numQualityBins;
		
		this.intervalSize = intervalSize;
		
		startTime = System.currentTimeMillis();
	}
	
	public void recordIteration()
	{
		// move to next iteration
		currentIteration++;
	}
	
	public void recordConsumption(double consumptionAmount)
	{
		if (currentIteration >= numBurnInIterations)
		{
			consumption.addValue(consumptionAmount);
		}
	}
	
	public void recordLocation(NdPoint location)
	{
		if (currentIteration >= numBurnInIterations)
		{
			if (locations.size() > 0)
			{
				double distance = SpaceUtils.getDistance(locations.get(locations.size() - 1), location);
				distanceTraveled += distance;
			}
			if (locations.size() > 1)
			{
				int lastEscapeBehavior = states.lastIndexOf(BehaviorState.Escape);
				if (location.equals(locations.get(locations.size() - 2), LOCATION_COMPARISON_TOLERANCE))
				{
					repeatedSteps++;
					logger.trace("Repeated {} steps interval {}", repeatedSteps, currentIteration);
					
					if (repeatedSteps > MAX_REPEATED_STEPS 
						&& lastEscapeBehavior > -1 
						&& states.size() - lastEscapeBehavior <= MAX_REPEATED_STEPS ) // verify at least one of repeated steps due to escape behavior
					{
						throw new RepeatedStepsException("Exceeded " + MAX_REPEATED_STEPS + " at interval " + currentIteration);
					}
				}
				else
				{
					repeatedSteps = 0;
				}
				
				// also look for larger patterns
				if (location.equals(locations.get(locations.size() - 2), LOCATION_COMPARISON_TOLERANCE_LARGE))
				{
					repeatedStepsLarge++;
					logger.trace("Repeated {} LARGE steps interval {}", repeatedStepsLarge, currentIteration);
					
					if (repeatedStepsLarge > MAX_REPEATED_STEPS_LARGE
						&& lastEscapeBehavior > -1 
						&& states.size() - lastEscapeBehavior <= MAX_REPEATED_STEPS_LARGE ) // verify at least one of repeated steps due to escape behavior
					{
						throw new RepeatedStepsException("Large: Exceeded " + MAX_REPEATED_STEPS_LARGE + " at interval " + currentIteration);
					}
				}
				else
				{
					repeatedStepsLarge = 0;
				}

			}
			locations.add(location);
		}
	}
	
	public void recordVelocity(Velocity velocity)
	{
		if (currentIteration >= numBurnInIterations)
		{
			velocities.add(velocity);
		}
	}
	
	public void recordMu(Velocity mu)
	{
		if (currentIteration >= numBurnInIterations)
		{
			mus.add(mu);
		}		
	}
	
	public void recordState(BehaviorState state)
	{
		if (currentIteration >= numBurnInIterations)
		{
			states.add(state);
		}
		
		if (state == BehaviorState.Escape)
		{
			repeatedStepsEscape++;
			logger.trace("Repeated {} ESCAPE steps interval {}", repeatedStepsEscape, currentIteration);
			
			if (repeatedStepsEscape > MAX_REPEATED_STEPS_ESCAPE)
			{
				throw new RepeatedStepsException("Escape: Exceeded " + MAX_REPEATED_STEPS_ESCAPE + " at interval " + currentIteration);
			}
		}
		else
		{
			repeatedStepsEscape = 0;
		}
	}
	
	public void recordUseMemory(boolean useMemory)
	{
		if (currentIteration >= numBurnInIterations)
		{
			memoryUsage.add(useMemory);
		}
	}
	
	public void recordPredatorEncounters(Set<NdPoint> activePredators) 
	{
		if (currentIteration >= numBurnInIterations)
		{
			predatorEncounters.add(activePredators.size());
			
			int repreatEncounters = 0;
			for (NdPoint predator : activePredators)
			{
				if (encounteredPredators.contains(predator))
				{
					repreatEncounters++;
				} else
				{
					encounteredPredators.add(predator);
				}
			}
			
			predatorReEncounters.add(repreatEncounters);
		}
	}
	
	public void recordQualityBin(int bin) 
	{
		qualityBins.add(bin);
	}


	/* (non-Javadoc)
	 * @see ForagingModel.agent.Recorder#getTotalConsumption()
	 */
	@Override
	public double getTotalConsumption() 
	{
		return consumption.getSum();
	}
	
	/* (non-Javadoc)
	 * @see ForagingModel.agent.Recorder#getSdConsumption()
	 */
	@Override
	public double getSdConsumption() 
	{
		// Note that Descriptive.sampleStandardDeviation includes a correction factor 
		// since the standard deviation calculated as the sqrt of the variance underestimates the unbiased standard deviation
		
		// note there is also a population variance (non-bias corrected, in that it uses n instead of n-1 in denominator)
		return Math.sqrt(consumption.getVariance());
		// TODO: this will depend on the intervalSize...
	}
	
	/* (non-Javadoc)
	 * @see ForagingModel.agent.Recorder#getDistanceTraveled()
	 */
	@Override
	public double getDistanceTraveled()
	{
		return distanceTraveled;
	}
	
	/* (non-Javadoc)
	 * @see ForagingModel.agent.Recorder#getSinuosity()
	 */
	@Override
	public double getSinuosity()
	{
		// this is how Watkins & Rose calculated it, but see other measures too
		double sinuosity = 0;
		if (locations.size() >= 2)
		{
			double straightLineDist = SpaceUtils.getDistance(locations.get(0), locations.get(locations.size() - 1));
			sinuosity = distanceTraveled / straightLineDist;
		}
		return sinuosity;
	}
	
	/* (non-Javadoc)
	 * @see ForagingModel.agent.Recorder#getAverageSpeed()
	 */
	@Override
	public double getAverageSpeed()
	{
		double avgSpeed = 0;
		if (velocities.size() > 0)
		{
			double speedSum = 0;
			for (Velocity velocity : velocities)
			{
				speedSum += velocity.mod();
			}
			avgSpeed = speedSum / velocities.size() / intervalSize;
		}
		return avgSpeed;
	}
	
	/* (non-Javadoc)
	 * @see ForagingModel.agent.Recorder#getAverageSpeedSearch()
	 */
	@Override
	public double getAverageSpeedSearch()
	{
		double avgSpeed = 0;
		double timeSearch = 0;
		if (velocities.size() > 0 && velocities.size() == states.size())
		{
			double speedSum = 0;
			for (int i = 0; i < velocities.size(); i++)
			{
				if (states.get(i) == BehaviorState.Searching)
				{
					speedSum += velocities.get(i).mod();
					timeSearch++;
				}
			}
			if (timeSearch > 0)
			{
				avgSpeed = speedSum / timeSearch / intervalSize;
			}
		}
		return avgSpeed;
	}
	
	/* (non-Javadoc)
	 * @see ForagingModel.agent.Recorder#getAverageSpeedFeeding()
	 */
	@Override
	public double getAverageSpeedFeeding()
	{
		double avgSpeed = 0;
		double timeFeeding = 0;
		if (velocities.size() > 0 && velocities.size() == states.size())
		{
			double speedSum = 0;
			for (int i = 0; i < velocities.size(); i++)
			{
				if (states.get(i) == BehaviorState.Feeding)
				{
					speedSum += velocities.get(i).mod();
					timeFeeding++;
				}
			}
			if (timeFeeding > 0)
			{
				avgSpeed = speedSum / timeFeeding / intervalSize;
			}
		}
		return avgSpeed;
	}


	
	/* (non-Javadoc)
	 * @see ForagingModel.agent.Recorder#getMeanSinTurningAngle()
	 */
	@Override
	public double getMeanSinTurningAngle()
	{
		double meanSin = 0;
		if (velocities.size() >= 2)
		{
			double sinSum = 0;
			Velocity previous = velocities.get(0);
			for (int i = 1; i < velocities.size(); i++)
			{
				// turning angle is difference of current and previous direction
				sinSum += Math.sin(velocities.get(i).arg() - previous.arg());
				previous = velocities.get(i);
			}
			// -1 since we start with the second velocity (i.e. differences between velocities)
			meanSin = sinSum / (velocities.size() - 1); // TODO: how to scale by intervalSize??
		}
		return meanSin;
	}

	/* (non-Javadoc)
	 * @see ForagingModel.agent.Recorder#getMeanCosTurningAngle()
	 */
	@Override
	public double getMeanCosTurningAngle()
	{
		double meanCos = 0;
		if (velocities.size() >= 2)
		{
			double cosSum = 0;
			Velocity previous = velocities.get(1);
			for (int i = 1; i < velocities.size(); i++)
			{
				// turning angle is difference of current and previous direction
				cosSum += Math.cos(velocities.get(i).arg() - previous.arg());
				previous = velocities.get(i);
			}
			meanCos = cosSum / velocities.size(); // TODO: how to scale by intervalSize?
		}
		return meanCos;
	}
	
	/* (non-Javadoc)
	 * @see ForagingModel.agent.Recorder#getTimeSearching()
	 */
	@Override
	public double getTimeSearching()
	{
		// note: only kinetic and memory movements record state
		
		int intervalsSearching = 0;
		for (BehaviorState state : states)
		{
			if (state == BehaviorState.Searching)
			{
				intervalsSearching++;
			}
		}
		
		double timeSearching = intervalsSearching / intervalSize;
		
		return timeSearching;
	}
	
	/* (non-Javadoc)
	 * @see ForagingModel.agent.Recorder#getSearchMemoryUsage()
	 */
	@Override
	public double getSearchMemoryUsage()
	{
		// Kinesis records state but not memory usage
		int length = Math.min(states.size(), memoryUsage.size());
		int intervalsSearching = 0;
		int intervalsMemory = 0;
		
		for (int i = 0; i < length; i++)
		{
			if (states.get(i) == BehaviorState.Searching)
			{
				intervalsSearching++;
				
				if(memoryUsage.get(i))
				{
					intervalsMemory++;
				}
			}
		}
		double memorySearchPercentage = (intervalsSearching == 0) ? 0 : (double) intervalsMemory / (double) intervalsSearching;
		return memorySearchPercentage;
	}
	
	/* (non-Javadoc)
	 * @see ForagingModel.agent.Recorder#getPredatorEncounters()
	 */
	@Override
	public int getPredatorEncounters()
	{
		int totalEncounters = 0;
		
		for (int i = 0; i < predatorEncounters.size(); i++)
		{
			totalEncounters += predatorEncounters.get(i);
		}
		return totalEncounters;
	}
	
	public int getPredatorReEncounters()
	{
		int reEncounters = 0;
		
		for (int i = 0; i < predatorReEncounters.size(); i++)
		{
			reEncounters += predatorReEncounters.get(i);
		}
		return reEncounters;
	}
	
	public List<Predator> getAllPredators()
	{
		List<Predator> allPredators = (predators == null) ? new ArrayList<Predator>() : predators.getAllPredators();
		return allPredators;
	}

	
	public int[] getQualityBins()
	{
		int[] bins = new int[numQualityBins];
		
		for (int i = 0; i < qualityBins.size(); i++)
		{
			int bin = qualityBins.get(i);
			bins[bin] = bins[bin] + 1;
		}
		
		return bins;
	}

	@Override
	public long getExecutionTime() 
	{
		return System.currentTimeMillis() - startTime;
	}
	
	@Override
	public List<NdPoint> getLocationHistory() 
	{
		return locations;
	}

	@Override
	public List<BehaviorState> getStateHistory() 
	{
		return states;
	}

	@Override
	public List<Double> getConsumptionHistory() 
	{
		return new ArrayList<Double>(Arrays.asList(ArrayUtils.toObject(consumption.getValues())));
	}

	@Override
	public List<Boolean> getMemoryUsageHistory() 
	{
		return memoryUsage;
	}
	
	@Override
	public List<Velocity> getMuHistory()
	{
		return mus;
	}

	/* (non-Javadoc)
	 * @see ForagingModel.agent.Recorder#getSummaryMetic()
	 */
	@Override
	public double getSummaryMetic(SummaryMetric metric)
	{
		double value;
		
		switch(metric)
		{
		case TotalConsumption:
			value = getTotalConsumption();
			break;
		case StdDevConsumption:
			value = getSdConsumption();
			break; 
		case DistanceTraveled:
			value = getDistanceTraveled();
			break; 
		case Sinuosity:
			value = getSinuosity();
			break; 
		case AverageSpeed:
			value = getAverageSpeed();
			break; 
		case AverageSpeedSearch:
			value = getAverageSpeedSearch();
			break; 
		case AverageSpeedFeeding:
			value = getAverageSpeedFeeding();
			break; 
		case MeanSinTurningAngle:
			value = getMeanSinTurningAngle();
			break; 
		case MeanCosTurningAngle:
			value = getMeanCosTurningAngle();
			break;
		case TimeSearching:
			value = getTimeSearching();
			break;
		case MemorySearchPercentage:
			value = getSearchMemoryUsage();
			break;
		case TotalPredatorEncounters:
			value = getPredatorEncounters();
			break;
		case PredatorReEncounters:
			value = getPredatorReEncounters();
			break;
		case ExecutionTime:
			value = getExecutionTime();
			break;
		default:
			throw new ForagingModelException(metric + " not implemented.");
		}
		return value;
	}
}
