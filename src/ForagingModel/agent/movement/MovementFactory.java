package ForagingModel.agent.movement;

import ForagingModel.agent.Recorder;
import ForagingModel.core.ModelEnvironment;
import ForagingModel.core.NdPoint;
import ForagingModel.core.Parameters;
import ForagingModel.core.Velocity;
import ForagingModel.predator.PredatorEncounterBehavior;
import ForagingModel.predator.PredatorManager;
import ForagingModel.schedule.Scheduler;
import ForagingModel.space.AggregateMemory;
import ForagingModel.space.LocationManager;
import ForagingModel.space.MemoryAssemblage;
import ForagingModel.space.ResourceAssemblage;
import ForagingModel.space.ScentAggregateMemory;
import ForagingModel.space.ScentHistory;
import ForagingModel.space.ScentManager;
import ForagingModel.space.ScentSexAggregateMemory;
import ForagingModel.space.SpaceFactory;

public class MovementFactory 
{
	public static MovementBehavior createMovement(ResourceAssemblage resources, 
			PredatorManager predatorManager, ScentManager scentManager, ScentHistory femalesHistory,
			double averageConsumption, LocationManager locationManager, NdPoint startingLocation, 
			Scheduler scheduler, Recorder recorder)
	{
		// based on parameter values
		Parameters params = Parameters.get();
		MovementBehavior movement = null;
		MemoryAssemblage memory = null;
		
		switch(params.getMovementType())
		{
		case MemoryDestination:
			memory = SpaceFactory.createMemoryAssemblage(resources, 
					predatorManager, scentManager, femalesHistory, scheduler);
			
			switch(params.getMovementProcess())
			{
			case Straight:
				movement = createStraightMemoryDestinationMovement(memory, averageConsumption, locationManager, predatorManager, startingLocation, recorder);
				break;
			case OU:
				movement = createOUMemoryDestinationMovement(memory, averageConsumption, locationManager, predatorManager, startingLocation, recorder);
				break;
			case Mixed:
				movement = createMixedMemoryDestinationMovement(memory, averageConsumption, locationManager, predatorManager, startingLocation, recorder);
				break;
			default:
				throw new IllegalArgumentException("Unrecognized movement process " + params.getMovementProcess());
			}
			break;
		case MemoryDirectional:
			memory = SpaceFactory.createMemoryAssemblage(resources, 
					predatorManager, scentManager, femalesHistory, scheduler);
			
			MemoryAssemblage avoidanceMemory = null;
			if (memory instanceof AggregateMemory)
			{
				avoidanceMemory = ((AggregateMemory)memory).getPredatorMemory();
			}
			else if (memory instanceof ScentAggregateMemory)
			{
				avoidanceMemory = ((ScentAggregateMemory)memory).getScentHistory();
			}
			else if (memory instanceof ScentSexAggregateMemory)
			{
				avoidanceMemory = ((ScentSexAggregateMemory)memory).getScentHistory();
			}
			
			switch(params.getMovementProcess())
			{
			case Correlated:
				movement = createCorrelatedMemoryDirectionalMovement(memory, avoidanceMemory, averageConsumption, locationManager, predatorManager, startingLocation, recorder);
				break;
			case ContinuousCorrelated:
				movement = createContinuousCorrelatedMemoryDirectionalMovement(memory, avoidanceMemory, averageConsumption, locationManager, predatorManager, startingLocation, recorder, scheduler);
				break;
			case OU:
				movement = createOUMemoryDirectionalMovement(memory, avoidanceMemory, averageConsumption, locationManager, predatorManager, startingLocation, recorder);
				break;
			case Straight:
			case Mixed:
				throw new IllegalStateException("Unsupported movement process for directional memory: " + params.getMovementProcess());
			default:
				throw new IllegalArgumentException("Unrecognized movement process " + params.getMovementProcess());
			}
			break;
		case Kinesis:
			MovementProcess searching;
			MovementProcess feeding;
			BehaviorSwitchingRule switchingRule = createBehaviorSwitchingRule(averageConsumption);

			if (params.getScentTracking())
			{
				MemoryAssemblage scent = SpaceFactory.createScentHistory(scentManager, 
						femalesHistory, scheduler);
				switch(params.getMovementProcess())
				{
				case ContinuousCorrelated:
					searching = createDirectionalContinuousCorrelatedProcess(params.getForagerSpeedSearch(), params.getForagerTauSearch(),
							scent, recorder, scheduler);
					feeding = createDirectionalContinuousCorrelatedProcess(params.getForagerSpeedFeeding(), params.getForagerTauFeeding(),
							scent, recorder, scheduler);					
					break;
				case Correlated:
				case OU:
				case Straight:
				case Mixed:
					throw new UnsupportedOperationException("Unsupported movement process for kinesis and scent: " + params.getMovementProcess());
				default:
					throw new IllegalArgumentException("Unrecognized movement process " + params.getMovementProcess());
				}

			}
			else // no scent tracking
			{
				switch(params.getMovementProcess())
				{
				case Straight:
					searching = createStraightProcess(params.getForagerSpeedSearch());
					feeding = createStraightProcess(params.getForagerSpeedFeeding());
					break;
				case OU:
					searching = createOUProcess(params.getForagerSpeedSearch(), params.getForagerTauSearch());
					feeding = createOUProcess(params.getForagerSpeedFeeding(), params.getForagerTauFeeding());
					break;
				case Mixed:
					searching = createStraightProcess(params.getForagerSpeedSearch());
					feeding = createOUProcess(params.getForagerSpeedFeeding(), params.getForagerTauFeeding());
					break;
				case Correlated:
					searching = createCorrelatedProcess(params.getForagerSpeedSearch(), params.getForagerAnglePersistanceSearch());
					feeding = createCorrelatedProcess(params.getForagerSpeedFeeding(), params.getForagerAnglePersistanceFeeding());
					break;
				case ContinuousCorrelated:
					searching = createContinuousCorrelatedProcess(params.getForagerSpeedSearch(), params.getForagerTauSearch(), recorder, scheduler);
					feeding = createContinuousCorrelatedProcess(params.getForagerSpeedFeeding(), params.getForagerTauFeeding(), recorder, scheduler);
					break;
				default:
					throw new IllegalArgumentException("Unrecognized movement process " + params.getMovementProcess());
				}
			}
			movement = createKineticMovement(searching, feeding, switchingRule, recorder, predatorManager, 
					params.getPredatorEncounterRadius(), params.getPredatorEncounterBehavior());
			break;
			
		case SingleState:
			if (params.getScentTracking())
			{
				MemoryAssemblage scent = SpaceFactory.createScentHistory(scentManager, 
						femalesHistory, scheduler);
				switch(params.getMovementProcess())
				{
				case ContinuousCorrelated:
					movement = createSingleStateMovement(
							createDirectionalContinuousCorrelatedProcess(params.getForagerSpeedSearch(), params.getForagerTauSearch(), scent, recorder, scheduler), 
							recorder, predatorManager);
					break;
				case Correlated:
				case OU:
				case Straight:
				case Mixed:
					throw new UnsupportedOperationException("Unsupported movement process for singlestate and scent: " + params.getMovementProcess());
				default:
					throw new IllegalArgumentException("Unrecognized movement process " + params.getMovementProcess());
				}
			}
			else // no scent tracking
			{
				switch(params.getMovementProcess())
				{
				case Straight:
					movement = createSingleStateMovement(createStraightProcess(params.getForagerSpeedSearch()), 
							recorder, predatorManager);
					break;
				case OU:
					movement = createSingleStateMovement(createOUProcess(params.getForagerSpeedSearch(), params.getForagerTauSearch()), 
							recorder, predatorManager);
					break;
				case Mixed:
					throw new IllegalStateException("Cannot have a mixed movement process for single state movement.");
				case Correlated:
					movement = createSingleStateMovement(createCorrelatedProcess(params.getForagerSpeedSearch(), params.getForagerAnglePersistanceSearch()), 
							recorder, predatorManager);
					break;
				case ContinuousCorrelated:
					movement = createSingleStateMovement(createContinuousCorrelatedProcess(params.getForagerSpeedSearch(), params.getForagerTauSearch(), recorder, scheduler), 
							recorder, predatorManager);
					break;
				default:
					throw new IllegalArgumentException("Unrecognized movement process " + params.getMovementProcess());
				}
			}
			break;
		default:
			throw new IllegalArgumentException("Unrecognized movement type " + params.getMovementType());
		}
		return movement;
	}
	
	public static DirectionUpdater createDirectionUpdater()
	{
		DirectionUpdater updater;
		switch(Parameters.get().getDirectionUpdaterType())
		{
		case TimeStep:
			updater = createTimeStepUpdater();
			break;
		case PoissonProcess:
			updater = createPoissonProcessUpdater();
			break;
		default:
			throw new IllegalArgumentException("Unrecognized direction updater " + Parameters.get().getDirectionUpdaterType());
		}
		return updater;

	}
	
	
	protected static MovementBehavior createSingleStateMovement(MovementProcess movement, Recorder recorder,
			PredatorManager predators)
	{
		return new SingleStateMovement(movement, recorder, predators, 
				Parameters.get().getPredatorEncounterRadius(), Parameters.get().getPredatorEncounterBehavior());
	}
	
	protected static MovementBehavior createStraightMemoryDestinationMovement(MemoryAssemblage memory, double averageConsumtion, 
			LocationManager locationManager, PredatorManager predatorManager, NdPoint startingLocation, Recorder recorder)
	{
		Parameters params = Parameters.get();
		DestinationProcess searchingBehavior = createDestinationStraightProcess(params.getForagerSpeedSearch(), params.getMemoryArrivalRadius(),
				createInitialVelocity(params.getForagerSpeedSearch()), 
				params.getMinDimension(), params.getMinDimension(), params.getMaxDimensionX(), params.getMaxDimensionY(), params.getIntervalSize());
		MovementProcess feedingBehavior = createStraightProcess(params.getForagerSpeedFeeding());
		BehaviorSwitchingRule switchingRule = createBehaviorSwitchingRule(averageConsumtion);
		return createMemoryMovement(searchingBehavior, feedingBehavior, switchingRule, recorder, memory, 
				locationManager, predatorManager, startingLocation);
	}

	protected static MovementBehavior createOUMemoryDestinationMovement(MemoryAssemblage memory, double averageConsumtion, 
			LocationManager locationManager, PredatorManager predatorManager, NdPoint startingLocation, Recorder recorder)
	{
		Parameters params = Parameters.get();
		DestinationProcess searchingBehavior = createDestinationOUProcess(params.getForagerSpeedSearch(), params.getForagerTauSearch(), 
				params.getMemoryArrivalRadius(), createInitialVelocity(params.getForagerSpeedSearch()), 
				params.getMinDimension(), params.getMinDimension(), params.getMaxDimensionX(), params.getMaxDimensionY(), params.getIntervalSize());
		MovementProcess feedingBehavior = createOUProcess(params.getForagerSpeedFeeding(), params.getForagerTauFeeding());
		BehaviorSwitchingRule switchingRule = createBehaviorSwitchingRule(averageConsumtion);
		return createMemoryMovement(searchingBehavior, feedingBehavior, switchingRule, recorder, memory, 
				locationManager, predatorManager, startingLocation);
	}

	protected static MovementBehavior createMixedMemoryDestinationMovement(MemoryAssemblage memory, double averageConsumtion, 
			LocationManager locationManager, PredatorManager predatorManager, NdPoint startingLocation, Recorder recorder)
	{
		Parameters params = Parameters.get();
		DestinationProcess searchingBehavior = createDestinationStraightProcess(params.getForagerSpeedSearch(), params.getMemoryArrivalRadius(),
				createInitialVelocity(params.getForagerSpeedSearch()), 
				params.getMinDimension(), params.getMinDimension(), params.getMaxDimensionX(), params.getMaxDimensionY(), params.getIntervalSize());
		MovementProcess feedingBehavior = createOUProcess(params.getForagerSpeedFeeding(), params.getForagerTauFeeding());
		BehaviorSwitchingRule switchingRule = createBehaviorSwitchingRule(averageConsumtion);
		return createMemoryMovement(searchingBehavior, feedingBehavior, switchingRule, recorder, memory, 
				locationManager, predatorManager, startingLocation);
	}

	protected static MovementProcess createStraightProcess(double speed)
	{		
		return createStraightProcess(speed, createInitialVelocity(speed), 
				Parameters.get().getMinDimension(), Parameters.get().getMinDimension(), 
				Parameters.get().getMaxDimensionX(), Parameters.get().getMaxDimensionY(), 
				Parameters.get().getIntervalSize());
	}
	
	protected static MovementProcess createStraightProcess(double speed, Velocity initialVelocity, 
			double minDimensionX, double minDimensionY, double maxDimensionX, double maxDimensionY, double intervalSize)
	{
		return new StraightProcess(speed, initialVelocity, minDimensionX, minDimensionY, maxDimensionX, maxDimensionY, intervalSize);
	}
	
	protected static DestinationProcess createDestinationStraightProcess(double speed, double arrivalRadius, 
			Velocity initialVelocity, double minDimensionX, double minDimensionY, double maxDimensionX, double maxDimensionY, double intervalSize)
	{
		return new DestinationStraightProcess(speed, arrivalRadius, initialVelocity, minDimensionX, minDimensionY, maxDimensionX, maxDimensionY, intervalSize);
	}
	
	protected static MovementProcess createOUProcess(double speed, double tau)
	{		
		return createOUProcess(speed, tau, createInitialVelocity(speed), 
				Parameters.get().getMinDimension(), Parameters.get().getMinDimension(), 
				Parameters.get().getMaxDimensionX(), Parameters.get().getMaxDimensionY(), 
				Parameters.get().getIntervalSize());
	}

	protected static MovementProcess createOUProcess(double speed, double tau, Velocity initialVelocity, 
			double minDimensionX, double minDimensionY, double maxDimensionX, double maxDimensionY, double intervalSize)
	{
		return new OUProcess(speed, tau, initialVelocity, minDimensionX, minDimensionY, maxDimensionX, maxDimensionY, intervalSize);
	}
	
	protected static DestinationProcess createDestinationOUProcess(double speed, double tau, double arrivalRadius, 
			Velocity initialVelocity, double minDimensionX, double minDimensionY, double maxDimensionX, double maxDimensionY, double intervalSize)
	{
		return new DestinationOUProcess(speed, tau, arrivalRadius, initialVelocity, minDimensionX, minDimensionY, maxDimensionX, maxDimensionY, intervalSize);
	}

	protected static MovementProcess createDirectionalOUProcess(double speed, double tau, MemoryAssemblage memory, 
			Velocity initialVelocity, double minDimensionX, double minDimensionY, double maxDimensionX, double maxDimensionY, double intervalSize)
	{
		return new DirectionalOUProcess(speed, tau, memory, initialVelocity, minDimensionX, minDimensionY, maxDimensionX, maxDimensionY, intervalSize);
	}

	protected static MovementBehavior createKineticMovement(MovementProcess searching, MovementProcess feeding, 
			BehaviorSwitchingRule switchingRule, Recorder recorder,
			PredatorManager predators, double predatorEncounterRadius, PredatorEncounterBehavior predatorEncounterBehavior)
	{
		return new KineticMovement(searching, feeding, switchingRule, recorder, 
				predators, predatorEncounterRadius, predatorEncounterBehavior);
	}
	
	protected static MovementBehavior createMemoryMovement(DestinationProcess searching, MovementProcess feeding, 
			BehaviorSwitchingRule switchingRule, Recorder recorder, MemoryAssemblage memory, 
			LocationManager locationManager, PredatorManager predatorManager, NdPoint startingLocation)
	{
		return new MemoryDestinationMovement(searching, feeding, switchingRule, recorder, memory, 
				locationManager, predatorManager, startingLocation, 
				Parameters.get().getPredatorEncounterRadius(), Parameters.get().getPredatorEncounterBehavior());
	}
	
	protected static BehaviorSwitchingRule createBehaviorSwitchingRule(double landscapeAverageConsumtionRate)
	{
		Parameters params = Parameters.get();
		BehaviorSwitchingRule rule;
		
		if (params.getAvgConsumptionIsFullyInformed())
		{
			rule = createMarginalValueRule(landscapeAverageConsumtionRate);
		} else
		{
			rule = createLearnedValueSwitching(params.getAvgConsumptionStartValue(), params.getAvgConsumptionLearningRate());
		}
		
		return rule;
	}
	
	protected static BehaviorSwitchingRule createMarginalValueRule(double landscapeAverageConsumtionRate)
	{
		return new MarginalValueSwitching(landscapeAverageConsumtionRate);
	}

	protected static BehaviorSwitchingRule createLearnedValueSwitching(double startAverageConsumtionRate, double learningRate)
	{
		return new LearnedValueSwitching(startAverageConsumtionRate, learningRate);
	}

	protected static Velocity createInitialVelocity(double speed)
	{
		return Velocity.createPolar(speed, ModelEnvironment.getNumberGenerator().nextDoubleFromTo(0, 2 * Math.PI));
	}

	protected static MovementProcess createCorrelatedProcess(double speed, double persistence) 
	{
		return createCorrelatedProcess(speed, persistence, createInitialVelocity(speed), 
				Parameters.get().getMinDimension(), Parameters.get().getMinDimension(), 
				Parameters.get().getMaxDimensionX(), Parameters.get().getMaxDimensionY(), 
				Parameters.get().getIntervalSize());
	}
	
	protected static MovementProcess createCorrelatedProcess(double speed, double persistence, Velocity initialVelocity, 
			double minDimensionX, double minDimensionY, double maxDimensionX, double maxDimensionY, double intervalSize)
	{
		return new CorrelatedProcess(speed, persistence, initialVelocity, minDimensionX, minDimensionY, maxDimensionX, maxDimensionY, intervalSize);
	}

	protected static MovementProcess createDirectionalCorrelatedProcess(double speed, double persistence, 
			MemoryAssemblage memory) 
	{
		return createDirectionalCorrelatedProcess(speed, persistence, memory, Parameters.get().getIsMemorySetCorrelation(), createInitialVelocity(speed), 
				Parameters.get().getMinDimension(), Parameters.get().getMinDimension(), 
				Parameters.get().getMaxDimensionX(), Parameters.get().getMaxDimensionY(), 
				Parameters.get().getIntervalSize());
	}
	
	protected static MovementProcess createDirectionalCorrelatedProcess(double speed, double persistence, 
			MemoryAssemblage memory, boolean memorySetsCorrelation,
			Velocity initialVelocity, double minDimensionX, double minDimensionY, double maxDimensionX, double maxDimensionY, double intervalSize)
	{
		return new DirectionalCorrelatedProcess(speed, persistence, memory, memorySetsCorrelation, initialVelocity, 
				minDimensionX, minDimensionY, maxDimensionX, maxDimensionY, intervalSize);
	}
	
	protected static MovementProcess createContinuousCorrelatedProcess(double speed, double tau, Recorder recorder, Scheduler scheduler) 
	{
		return createContinuousCorrelatedProcess(speed, tau, createInitialVelocity(speed), 
				Parameters.get().getMinDimension(), Parameters.get().getMinDimension(), 
				Parameters.get().getMaxDimensionX(), Parameters.get().getMaxDimensionY(), 
				Parameters.get().getIntervalSize(), recorder, scheduler);
	}
	
	protected static MovementProcess createContinuousCorrelatedProcess(double speed, double tau, Velocity initialVelocity, 
			double minDimensionX, double minDimensionY, double maxDimensionX, double maxDimensionY, double intervalSize, 
			Recorder recorder, Scheduler scheduler)
	{
		DirectionUpdater updater = createDirectionUpdater();
		ContinuousCorrelatedProcess movement = new ContinuousCorrelatedProcess(speed, tau, updater, initialVelocity, 
				minDimensionX, minDimensionY, maxDimensionX, maxDimensionY, intervalSize, recorder);
		scheduler.register(updater);
		return movement;
	}

	protected static MovementProcess createDirectionalContinuousCorrelatedProcess(double speed, double tau, 
			MemoryAssemblage memory, Recorder recorder, Scheduler scheduler) 
	{
		DirectionUpdater updater = createDirectionUpdater();
		DirectionalContinuousCorrelatedProcess movement = createDirectionalContinuousCorrelatedProcess(speed, tau, memory, 
				updater, createInitialVelocity(speed), 
				Parameters.get().getMinDimension(), Parameters.get().getMinDimension(), 
				Parameters.get().getMaxDimensionX(), Parameters.get().getMaxDimensionY(), 
				Parameters.get().getIntervalSize(),
				recorder);
		scheduler.register(movement);
		scheduler.register(updater);
		return movement;
	}
	
	protected static DirectionalContinuousCorrelatedProcess createDirectionalContinuousCorrelatedProcess(double speed, double tau, 
			MemoryAssemblage memory, DirectionUpdater updater, Velocity initialVelocity, 
			double minDimensionX, double minDimensionY, double maxDimensionX, double maxDimensionY, 
			double intervalSize, Recorder recorder)
	{
		return new DirectionalContinuousCorrelatedProcess(speed, tau, memory, updater, initialVelocity, 
				minDimensionX, minDimensionY, maxDimensionX, maxDimensionY, intervalSize, recorder);
	}
	
	protected static MovementBehavior createCorrelatedMemoryDirectionalMovement(MemoryAssemblage memorySearch, MemoryAssemblage memoryFeeding, double averageConsumtion, 
			LocationManager locationManager, PredatorManager predatorManager, NdPoint startingLocation, Recorder recorder)
	{
		Parameters params = Parameters.get();
		MovementProcess searchingBehavior = createDirectionalCorrelatedProcess(params.getForagerSpeedSearch(), params.getForagerAnglePersistanceSearch(),
				memorySearch, Parameters.get().getIsMemorySetCorrelation(), createInitialVelocity(params.getForagerSpeedSearch()), 
				params.getMinDimension(), params.getMinDimension(), params.getMaxDimensionX(), params.getMaxDimensionY(), params.getIntervalSize());
		// feed memory (aka predator memory) is null if no predators
		MovementProcess feedingBehavior;
		if (memoryFeeding == null)
		{
			feedingBehavior = createCorrelatedProcess(params.getForagerSpeedFeeding(), params.getForagerAnglePersistanceFeeding(),
					createInitialVelocity(params.getForagerSpeedFeeding()), 
					params.getMinDimension(), params.getMinDimension(), params.getMaxDimensionX(), params.getMaxDimensionY(), params.getIntervalSize());			
		}
		else
		{
			feedingBehavior = createDirectionalCorrelatedProcess(params.getForagerSpeedFeeding(), params.getForagerAnglePersistanceFeeding(),
					memoryFeeding, false, createInitialVelocity(params.getForagerSpeedFeeding()), 
					params.getMinDimension(), params.getMinDimension(), params.getMaxDimensionX(), params.getMaxDimensionY(), params.getIntervalSize());
		}
		BehaviorSwitchingRule switchingRule = createBehaviorSwitchingRule(averageConsumtion);
		MovementBehavior underlyingMovement = createKineticMovement(searchingBehavior, feedingBehavior, switchingRule, recorder,
				predatorManager, params.getPredatorEncounterRadius(), params.getPredatorEncounterBehavior());
		return new MemoryDirectionalMovement(underlyingMovement, memorySearch);
	}
	
	protected static MovementBehavior createContinuousCorrelatedMemoryDirectionalMovement(MemoryAssemblage memorySearch, MemoryAssemblage memoryFeeding, double averageConsumtion, 
			LocationManager locationManager, PredatorManager predatorManager, NdPoint startingLocation, Recorder recorder, Scheduler scheduler)
	{
		Parameters params = Parameters.get();
		MovementProcess searchingBehavior = createDirectionalContinuousCorrelatedProcess(params.getForagerSpeedSearch(), params.getForagerTauSearch(),
				memorySearch, recorder, scheduler);
		// feed memory (aka predator memory) is null if no predators
		MovementProcess feedingBehavior;
		if (memoryFeeding == null)
		{
			feedingBehavior = createContinuousCorrelatedProcess(params.getForagerSpeedFeeding(), params.getForagerTauFeeding(), recorder, scheduler);			
		}
		else
		{
			feedingBehavior = createDirectionalContinuousCorrelatedProcess(params.getForagerSpeedFeeding(), params.getForagerTauFeeding(),
					memoryFeeding, recorder, scheduler);
		}
		BehaviorSwitchingRule switchingRule = createBehaviorSwitchingRule(averageConsumtion);
		MovementBehavior underlyingMovement = createKineticMovement(searchingBehavior, feedingBehavior, switchingRule, recorder,
				predatorManager, params.getPredatorEncounterRadius(), params.getPredatorEncounterBehavior());
		return new MemoryDirectionalMovement(underlyingMovement, memorySearch);
	}

	
	protected static MovementBehavior createOUMemoryDirectionalMovement(MemoryAssemblage memorySearch, MemoryAssemblage memoryFeeding, double averageConsumtion, 
			LocationManager locationManager, PredatorManager predatorManager, NdPoint startingLocation, Recorder recorder)
	{
		Parameters params = Parameters.get();
		MovementProcess searchingBehavior = createDirectionalOUProcess(params.getForagerSpeedSearch(), params.getForagerTauSearch(), 
				memorySearch, createInitialVelocity(params.getForagerSpeedSearch()), 
				params.getMinDimension(), params.getMinDimension(), params.getMaxDimensionX(), params.getMaxDimensionY(), params.getIntervalSize());
		// feed memory (aka predator memory) is null if no predators
		MovementProcess feedingBehavior;
		if (memoryFeeding == null)
		{
			feedingBehavior = createOUProcess(params.getForagerSpeedFeeding(), params.getForagerTauFeeding());			
		}
		else
		{
			feedingBehavior = createDirectionalOUProcess(params.getForagerSpeedFeeding(), params.getForagerTauFeeding(), 
					memoryFeeding, createInitialVelocity(params.getForagerSpeedFeeding()), 
					params.getMinDimension(), params.getMinDimension(), params.getMaxDimensionX(), params.getMaxDimensionY(), params.getIntervalSize());
		}

		BehaviorSwitchingRule switchingRule = createBehaviorSwitchingRule(averageConsumtion);
		MovementBehavior underlyingMovement = createKineticMovement(searchingBehavior, feedingBehavior, switchingRule, recorder,
				predatorManager, params.getPredatorEncounterRadius(), params.getPredatorEncounterBehavior());
		return new MemoryDirectionalMovement(underlyingMovement, memorySearch);
	}
	
	protected static DirectionUpdater createTimeStepUpdater()
	{
		Parameters params = Parameters.get();
		return new TimeStepDirectionUpdater((int) params.getDirectionUpdaterTimeStep(), params.getIntervalSize());
	}

	protected static DirectionUpdater createPoissonProcessUpdater()
	{
		Parameters params = Parameters.get();
		return new PoissonProcessDirectionUpdater(params.getDirectionUpdaterTimeStep(), params.getIntervalSize(), ModelEnvironment.getNumberGenerator());
	}

}
