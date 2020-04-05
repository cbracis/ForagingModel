package ForagingModel.core;

import java.io.File;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import ForagingModel.agent.movement.DirectionUpdaterType;
import ForagingModel.agent.movement.MovementProcessType;
import ForagingModel.agent.movement.MovementType;
import ForagingModel.input.InputFactory;
import ForagingModel.input.StartPointReader;
import ForagingModel.output.RVisualizer;
import ForagingModel.predator.PredatorEncounterBehavior;

public class Parameters
{
	// private static Logger logger = Logger.getLogger( Parameters.class.getName() );
	
	public enum Parameter { ResourceRegenerationRate, // beta.R
							ConsumerConsumptionRate, // beta.C
							ConsumerConsumptionSpatialScale, // gamma.C
							LandscapeSizeX, // 50
							LandscapeSizeY, // 50
							ForagerSpeedSearch, // nu[1]
							ForagerSpeedFeeding, // nu[2]
							ForagerTauSearch, // tau[1]
							ForagerTauFeeding, // tau[2]
							ForagerAnglePersistanceSearch, 
							ForagerAnglePersistanceFeeding,
							ForagerNumber,
							IntervalSize, // dt
							AvgConsumptionIsFullyInformed, // learn avg consumption or calculate from landscape
							AvgConsumptionLearningRate, // learning rate for avg consumption if learned
							AvgConsumptionStartValue, // starting value if learning
							ConsumeDuringSearch,
							ShortLearningRate, //beta.S 
							LongLearningRate, // beta.L
							ShortSpatialScale, //gamma.S
							LongSpatialScale, // gamma.S
							ShortDecayRate, // phi.S
							LongDecayRate, // phi.L
							ShortMemoryFactor, // beta.M
							MemoryAlpha, // alpha
							MemorySpatialScaleForaging, // gamma.Z
							MemorySpatialScalePredation, // gamma.Z for predators
							MovementProcess, // straight or OU
							MovementType, // memory, kinesis, or single state
							MemoryIsFullyInformed, // informed
							MemoryValueUninformed, // default vale if uninformed
							MemoryArrivalRadius, // radius
							DirectionUpdaterTimeStep, //lambda, rate of Poisson process or num time steps
							DirectionUpdaterType, // Poisson process or time step
							ResultsFileName, 
							SaveTracks,
							NumSteps, // nSteps 
							BurnInSteps,
							RepeatSimulation,
							ResourceLandscapeFileName,
							EmptyBorderSize,
							StartPointsType,
							StartPointsFileName,
							RandomSeed,
							CreateRandomSeed,
							VisualizeSimulation, 
							VisualizeProbabilities,
							PredatorDuration, 
							PredatorRandomness, // 1 = random, 0 = by resources
							TotalPredationPressure, 
							PredatorEncounterRadius,
							PredatorLearningRate,
							PredatorDecayRate,
							PredatorMemoryFactor,
							PredatorIntroduction, // for now hard code when on, eventually will need more parameters?
							PredatorEncounterBehavior,
							FoodSafetyTradeoff,
							ScentTracking,
							ScentDepositionRate,
							ScentDepositionSpatialScale,
							ScentDecayRate,
							ScentResponseSpatialScale,
							ScentResponseFactor}
	
	public enum ParameterType { Integer, Long, Boolean, Double, UnitInterval, String, MovementProcess, MovementType, DirectionUpdaterType, StartPointsType, PredatorEncounterBehavior };
	
	private static Parameters parameters;
	
	private Map<Parameter, Object> values;
	private Map<Parameter, ParameterType> types;

	private List<NdPoint> startPoints;
	private int startPointIndex;
	
	private final String OUTPUT_DIR = "output";
	private final String R_FILE = "Visualizer.R";
	private final String R_OUTPUT_DIR = OUTPUT_DIR; // also in Visualizer.R file, same as output but could change
	private final String PREDATOR_CACHE_FILE = "PredatorCache.ser";
	
	private Parameters() 
	{ 
		values = new HashMap<Parameter, Object>();
		types = new HashMap<Parameter, ParameterType>();
		
		// Default values which will be overwritten if a properties file is specified
		
		values.put( Parameter.ResourceRegenerationRate, 0.01 );
		types.put( Parameter.ResourceRegenerationRate, ParameterType.UnitInterval );

		values.put( Parameter.ConsumerConsumptionRate, 1.0 );
		types.put( Parameter.ConsumerConsumptionRate, ParameterType.Double );

		values.put( Parameter.ConsumerConsumptionSpatialScale, 1.0 );
		types.put( Parameter.ConsumerConsumptionSpatialScale, ParameterType.Double );

		values.put( Parameter.LandscapeSizeX, 50 );
		types.put( Parameter.LandscapeSizeX, ParameterType.Integer );

		values.put( Parameter.LandscapeSizeY, 50 );
		types.put( Parameter.LandscapeSizeY, ParameterType.Integer );

		values.put( Parameter.ForagerNumber, 1 );
		types.put( Parameter.ForagerNumber, ParameterType.Integer );

		values.put( Parameter.ForagerSpeedSearch, 2.0 );
		types.put( Parameter.ForagerSpeedSearch, ParameterType.Double );

		values.put( Parameter.ForagerSpeedFeeding, 0.5 );
		types.put( Parameter.ForagerSpeedFeeding, ParameterType.Double );

		values.put( Parameter.ForagerTauSearch, 4.0 );
		types.put( Parameter.ForagerTauSearch, ParameterType.Double );

		values.put( Parameter.ForagerTauFeeding, 2.0 );
		types.put( Parameter.ForagerTauFeeding, ParameterType.Double );

		values.put( Parameter.ForagerAnglePersistanceSearch, 0.8 );
		types.put( Parameter.ForagerAnglePersistanceSearch, ParameterType.Double ); // to allow for -1 to mean MemoryCorrelation

		values.put( Parameter.ForagerAnglePersistanceFeeding, 0.2 );
		types.put( Parameter.ForagerAnglePersistanceFeeding, ParameterType.UnitInterval );

		values.put( Parameter.IntervalSize, 1.0 );
		types.put( Parameter.IntervalSize, ParameterType.Double );
		
		values.put( Parameter.AvgConsumptionIsFullyInformed, true );
		types.put( Parameter.AvgConsumptionIsFullyInformed, ParameterType.Boolean );

		values.put( Parameter.AvgConsumptionLearningRate, 0.9 );
		types.put( Parameter.AvgConsumptionLearningRate, ParameterType.Double );

		values.put( Parameter.AvgConsumptionStartValue, 0.0 );
		types.put( Parameter.AvgConsumptionStartValue, ParameterType.Double );

		values.put( Parameter.ConsumeDuringSearch, true );
		types.put( Parameter.ConsumeDuringSearch, ParameterType.Boolean );

		values.put( Parameter.ShortLearningRate, 1.0 );
		types.put( Parameter.ShortLearningRate, ParameterType.Double );
		
		values.put( Parameter.LongLearningRate, 1.0 );
		types.put( Parameter.LongLearningRate, ParameterType.Double );

		values.put( Parameter.ShortSpatialScale, 1.0 );
		types.put( Parameter.ShortSpatialScale, ParameterType.Double );

		values.put( Parameter.LongSpatialScale, 1.0 );
		types.put( Parameter.LongSpatialScale, ParameterType.Double );
		
		values.put( Parameter.ShortDecayRate, 0.0010 );
		types.put( Parameter.ShortDecayRate, ParameterType.Double );

		values.put( Parameter.LongDecayRate, 0.0001 );
		types.put( Parameter.LongDecayRate, ParameterType.Double );

		values.put( Parameter.ShortMemoryFactor, 2.0 );
		types.put( Parameter.ShortMemoryFactor, ParameterType.Double );

		values.put( Parameter.MemoryAlpha, 2500.0 ); // set by size of grid?
		types.put( Parameter.MemoryAlpha, ParameterType.Double );

		values.put( Parameter.MemorySpatialScaleForaging, 5.0 );
		types.put( Parameter.MemorySpatialScaleForaging, ParameterType.Double );

		values.put( Parameter.MemorySpatialScalePredation, 1.0 );
		types.put( Parameter.MemorySpatialScalePredation, ParameterType.Double );

		values.put( Parameter.MovementProcess, MovementProcessType.OU );
		types.put( Parameter.MovementProcess, ParameterType.MovementProcess );

		values.put( Parameter.MovementType, MovementType.MemoryDestination );
		types.put( Parameter.MovementType, ParameterType.MovementType );

		values.put( Parameter.MemoryIsFullyInformed, true );
		types.put( Parameter.MemoryIsFullyInformed, ParameterType.Boolean );

		values.put( Parameter.MemoryValueUninformed, 0.0 );
		types.put( Parameter.MemoryValueUninformed, ParameterType.Double );
		
		values.put( Parameter.MemoryArrivalRadius, 2.5 );
		types.put( Parameter.MemoryArrivalRadius, ParameterType.Double );

		values.put( Parameter.DirectionUpdaterTimeStep, 1.0 );
		types.put( Parameter.DirectionUpdaterTimeStep, ParameterType.Double );

		values.put( Parameter.DirectionUpdaterType, DirectionUpdaterType.PoissonProcess );
		types.put( Parameter.DirectionUpdaterType, ParameterType.DirectionUpdaterType );

		values.put( Parameter.ResultsFileName, "ForagerResults.csv" );
		types.put( Parameter.ResultsFileName, ParameterType.String );

		values.put( Parameter.SaveTracks, false );
		types.put( Parameter.SaveTracks, ParameterType.Boolean );

		values.put( Parameter.NumSteps, 5000 );
		types.put( Parameter.NumSteps, ParameterType.Integer );

		values.put( Parameter.BurnInSteps, 0 );
		types.put( Parameter.BurnInSteps, ParameterType.Integer );

		values.put( Parameter.RepeatSimulation, 1 );
		types.put( Parameter.RepeatSimulation, ParameterType.Integer );

		values.put( Parameter.ResourceLandscapeFileName, "" );
		types.put( Parameter.ResourceLandscapeFileName, ParameterType.String );

		values.put( Parameter.EmptyBorderSize, 0 );
		types.put( Parameter.EmptyBorderSize, ParameterType.Integer );

		values.put( Parameter.StartPointsType, StartPointsType.Center );
		types.put( Parameter.StartPointsType, ParameterType.StartPointsType );

		values.put( Parameter.StartPointsFileName, "" );
		types.put( Parameter.StartPointsFileName, ParameterType.String );
		
		values.put(Parameter.RandomSeed, 999);
		types.put(Parameter.RandomSeed, ParameterType.Integer);

		values.put(Parameter.CreateRandomSeed, true);
		types.put(Parameter.CreateRandomSeed, ParameterType.Boolean);

		values.put(Parameter.VisualizeSimulation, false);
		types.put(Parameter.VisualizeSimulation, ParameterType.Boolean);

		values.put(Parameter.VisualizeProbabilities, false);
		types.put(Parameter.VisualizeProbabilities, ParameterType.Boolean);

		values.put(Parameter.PredatorDuration, 500);
		types.put(Parameter.PredatorDuration, ParameterType.Integer);

		values.put(Parameter.PredatorRandomness, 0.5);
		types.put(Parameter.PredatorRandomness, ParameterType.UnitInterval);

		values.put(Parameter.TotalPredationPressure, 0);
		types.put(Parameter.TotalPredationPressure, ParameterType.Integer);

		values.put( Parameter.PredatorEncounterRadius, 5.0 );
		types.put( Parameter.PredatorEncounterRadius, ParameterType.Double );

		values.put( Parameter.PredatorLearningRate, 1.0 );
		types.put( Parameter.PredatorLearningRate, ParameterType.Double );

		values.put( Parameter.PredatorDecayRate, 0.01 );
		types.put( Parameter.PredatorDecayRate, ParameterType.UnitInterval );

		values.put( Parameter.PredatorMemoryFactor, 1.0 );
		types.put( Parameter.PredatorMemoryFactor, ParameterType.Double );

		values.put( Parameter.PredatorIntroduction, false );
		types.put( Parameter.PredatorIntroduction, ParameterType.Boolean );

		values.put( Parameter.PredatorEncounterBehavior, PredatorEncounterBehavior.Escape );
		types.put( Parameter.PredatorEncounterBehavior, ParameterType.PredatorEncounterBehavior );

		values.put( Parameter.FoodSafetyTradeoff, 0.0 );
		types.put( Parameter.FoodSafetyTradeoff, ParameterType.Double );
		
		values.put( Parameter.ScentTracking, false );
		types.put( Parameter.ScentTracking, ParameterType.Boolean );

		values.put( Parameter.ScentDepositionRate, 10.0 );
		types.put( Parameter.ScentDepositionRate, ParameterType.Double );

		values.put( Parameter.ScentDepositionSpatialScale, 10.0 );
		types.put( Parameter.ScentDepositionSpatialScale, ParameterType.Double );

		values.put( Parameter.ScentDecayRate, 0.01 );
		types.put( Parameter.ScentDecayRate, ParameterType.UnitInterval );

		values.put( Parameter.ScentResponseSpatialScale, 5.0 );
		types.put( Parameter.ScentResponseSpatialScale, ParameterType.Double );

		values.put( Parameter.ScentResponseFactor, 1.0 );
		types.put( Parameter.ScentResponseFactor, ParameterType.Double );

	}
	
	protected void init()
	{
		initStartPoints();
	}
	
	public static synchronized void resetToDefaults()
	{
		parameters = new Parameters();
	}
	
	public static synchronized Parameters get() 
	{
		if (parameters == null) 
		{
			parameters = new Parameters();;
		}
		return parameters;
	}
	
	public String toString()
	{
		// these are the params that could have invalid combinations
		return String.format("Parameters mt: %s mp: %s sd: %.4f ld %.4f ps %.1f\n", 
							 getMovementType(), getMovementProcess(), getShortDecayRate(), getLongDecayRate(), getForagerAnglePersistanceSearch()); 
	}

	public Object clone() throws CloneNotSupportedException 
	{
		throw new CloneNotSupportedException();
	}
	
	public void set( Parameter parameter, String value ) throws ParseException
	{
		ParameterType type = types.get( parameter );
		Object parsedValue;
		
		switch ( type )
		{
		case Integer:
			parsedValue = Integer.parseInt( value );
			break;
		case Long:
			parsedValue = Long.parseLong( value );
			break;
		case Boolean:
			parsedValue = Boolean.parseBoolean( value );
			break;
		case Double:
			parsedValue = Double.parseDouble( value );
			break;
		case UnitInterval:
			parsedValue = Double.parseDouble( value );
			if ( (Double)parsedValue < 0.0 || (Double)parsedValue > 1.0)
			{
				throw new ParseException("Parameters of type UnitInterval must be in [0, 1]", 0);
			}
			break;
		case String:
			parsedValue = value;
			break;
		case MovementProcess:
			parsedValue = MovementProcessType.valueOf( value );
			break;
		case MovementType:
			parsedValue = MovementType.valueOf( value );
			break;
		case DirectionUpdaterType:
			parsedValue = DirectionUpdaterType.valueOf( value );
			break;
		case StartPointsType:
			parsedValue = StartPointsType.valueOf( value );
			break;
		case PredatorEncounterBehavior:
			parsedValue = PredatorEncounterBehavior.valueOf( value );
			break;
		default:
			throw new ForagingModelException( "Parameter not found: " + parameter );
		}
		
		values.put( parameter, parsedValue );
	}
	

	protected void set( Parameter parameter, double value )
	{
		if ( types.get( parameter ) == ParameterType.Double )
		{
			values.put( parameter, value );
		}
		else
		{
			throw new ForagingModelException( parameter + "'s type is not double." );
		}
	}

	protected void set( Parameter parameter, int value )
	{
		if ( types.get( parameter ) == ParameterType.Integer )
		{
			values.put( parameter, value );
		}
		else
		{
			throw new ForagingModelException( parameter + "'s type is not int." );
		}
	}
	
	// for reporting
	public String get(Parameter parameter)
	{
		Object value = values.get(parameter);
		return (value == null) ? "null" : value.toString();
	}

	public double getResourceRegenerationRate()
	{
		return (Double) values.get(Parameter.ResourceRegenerationRate);
	}

	public double getConsumptionRate() 
	{
		return (Double) values.get(Parameter.ConsumerConsumptionRate);
	}

	public double getConsumptionSpatialScale() 
	{
		return (Double) values.get(Parameter.ConsumerConsumptionSpatialScale);
	}

	public int getLandscapeSizeX() 
	{
		return (Integer) values.get(Parameter.LandscapeSizeX);
	}
	
	public int getLandscapeSizeY() 
	{
		return (Integer) values.get(Parameter.LandscapeSizeY);
	}
	
	public double getIntervalSize() 
	{
		return (Double) values.get(Parameter.IntervalSize);
	}
	
	public int getForagerNumber()
	{
		return (Integer) values.get(Parameter.ForagerNumber);
	}


	public double getForagerSpeedSearch()
	{
		return (Double) values.get(Parameter.ForagerSpeedSearch);
	}

	public double getForagerSpeedFeeding()
	{
		return (Double) values.get(Parameter.ForagerSpeedFeeding);
	}

	public double getForagerTauSearch()
	{
		return (Double) values.get(Parameter.ForagerTauSearch);
	}

	public double getForagerTauFeeding()
	{
		return (Double) values.get(Parameter.ForagerTauFeeding);
	}
	
	public double getForagerAnglePersistanceSearch()
	{
		// do validation here since not done at set, ParameterManager checks against negative values for Kinesis to skip invalid combos
		double angle = (Double) values.get(Parameter.ForagerAnglePersistanceSearch);
		if (angle > 1)
		{
			throw new ForagingModelException("Parameter ForagerAnglePersistanceSearch must be <= 1 for memory");
		}
		return angle;
	}

	public double getForagerAnglePersistanceFeeding()
	{
		return (Double) values.get(Parameter.ForagerAnglePersistanceFeeding);
	}

	public boolean getAvgConsumptionIsFullyInformed()
	{
		return (Boolean) values.get(Parameter.AvgConsumptionIsFullyInformed);
	}

	public double getAvgConsumptionLearningRate()
	{
		return (Double) values.get(Parameter.AvgConsumptionLearningRate);
	}

	public double getAvgConsumptionStartValue()
	{
		return (Double) values.get(Parameter.AvgConsumptionStartValue);
	}

	public boolean getConsumeDuringSearch()
	{
		return (Boolean) values.get(Parameter.ConsumeDuringSearch);
	}

	public double getShortLearningRate()
	{
		return (Double) values.get(Parameter.ShortLearningRate);
	}

	public double getLongLearningRate()
	{
		return (Double) values.get(Parameter.LongLearningRate);
	}

	public double getShortSpatialScale()
	{
		return (Double) values.get(Parameter.ShortSpatialScale);
	}

	public double getLongSpatialScale()
	{
		return (Double) values.get(Parameter.LongSpatialScale);
	}

	public double getShortDecayRate()
	{
		return (Double) values.get(Parameter.ShortDecayRate);
	}

	public double getLongDecayRate()
	{
		return (Double) values.get(Parameter.LongDecayRate);
	}

	public double getShortMemoryFactor()
	{
		return (Double) values.get(Parameter.ShortMemoryFactor);
	}
	
	public double getMemoryAlpha()
	{
		return (Double) values.get(Parameter.MemoryAlpha);
	}

	public double getMemorySpatialScaleForaging()
	{
		return (Double) values.get(Parameter.MemorySpatialScaleForaging);
	}

	public double getMemorySpatialScalePredation()
	{
		return (Double) values.get(Parameter.MemorySpatialScalePredation);
	}

	public MovementProcessType getMovementProcess()
	{
		return MovementProcessType.valueOf(values.get(Parameter.MovementProcess).toString());
	}

	public MovementType getMovementType()
	{
		return MovementType.valueOf(values.get(Parameter.MovementType).toString());
	}

	public boolean getIsFullyInformed() 
	{
		return (Boolean) values.get(Parameter.MemoryIsFullyInformed);
	}
	
	public double getInitialValueUninformedMemory() 
	{
		return (Double) values.get(Parameter.MemoryValueUninformed);
	}
	
	public double getMemoryArrivalRadius()
	{
		return (Double) values.get(Parameter.MemoryArrivalRadius);
	}

	public double getDirectionUpdaterTimeStep()
	{
		return (Double) values.get(Parameter.DirectionUpdaterTimeStep);
	}

	public DirectionUpdaterType getDirectionUpdaterType()
	{
		return DirectionUpdaterType.valueOf(values.get(Parameter.DirectionUpdaterType).toString());
	}


	public boolean getIsMemorySetCorrelation() 
	{
		// -1 for ForagerAnglePersistanceSearch means memory sets correlation (formerly MemoryCorrelation) 
		// for ease of reporting (memory correlation replaces persistence value) and to avoid duplicate simulations
		boolean usesMemory = (getMovementType() == MovementType.MemoryDirectional) || (getMovementType() == MovementType.MemoryDestination);
		return usesMemory && ((Double) values.get(Parameter.ForagerAnglePersistanceSearch)) < 0;
	}

	public File getResultsFile() 
	{
		String fileName = (String) values.get(Parameter.ResultsFileName);
		File results = FileUtils.getFile(getOutputPath(), fileName);;
		return results;
	}
	
	public boolean getSaveTracks()
	{
		return (Boolean) values.get(Parameter.SaveTracks);
	}

	
	public File getResourceLandscapeFile() 
	{
		String fileName = (String) values.get(Parameter.ResourceLandscapeFileName);
		File results = null;
		
		// return null if landscape file not specified
		if (fileName != null && !StringUtils.isEmpty(fileName))
		{
			try 
			{
				results = new File( ClassLoader.getSystemResource(fileName).toURI());
			} catch (URISyntaxException e) 
			{
				throw new IllegalArgumentException("Cannot get file object for resource file " + fileName, e);
			}
		}
		return results;
	}

	public int getEmptyBorderSize()
	{
		return (Integer) values.get(Parameter.EmptyBorderSize);
	}

	public StartPointsType getStartPointsType()
	{
		return StartPointsType.valueOf(values.get(Parameter.StartPointsType).toString());
	}

	public File getStartPointsFile() 
	{
		String fileName = (String) values.get(Parameter.StartPointsFileName);
		File results = new File(fileName);
		return results;
	}
	
	public int getNumSteps()
	{
		return (Integer) values.get(Parameter.NumSteps);
	}

	public int getBurnInSteps()
	{
		return (Integer) values.get(Parameter.BurnInSteps);
	}

	public int getRepeatSimulation()
	{
		return (Integer) values.get(Parameter.RepeatSimulation);
	}

	public int getRandomSeed()
	{
		return (Integer) values.get(Parameter.RandomSeed);
	}

	public boolean getCreateRandomSeed()
	{
		return (Boolean) values.get(Parameter.CreateRandomSeed);
	}

	public boolean getVisualizeSimulation()
	{
		return (Boolean) values.get(Parameter.VisualizeSimulation);
	}

	public boolean getVisualizeProbabilities()
	{
		return (Boolean) values.get(Parameter.VisualizeProbabilities);
	}

	public int getPredatorDuration() 
	{
		return (Integer) values.get(Parameter.PredatorDuration);
	}

	public double getPredatorRandomness() 
	{
		return (Double) values.get(Parameter.PredatorRandomness);
	}

	public int getTotalPredationPressure() 
	{
		return (Integer) values.get(Parameter.TotalPredationPressure);
	}
	
	public double getPredatorEncounterRadius() 
	{
		return (Double) values.get(Parameter.PredatorEncounterRadius);
	}

	public double getPredatorLearningRate() 
	{
		return (Double) values.get(Parameter.PredatorLearningRate);
	}

	public double getPredatorDecayRate() 
	{
		return (Double) values.get(Parameter.PredatorDecayRate);
	}

	public double getPredatorMemoryFactor() 
	{
		return (Double) values.get(Parameter.PredatorMemoryFactor);
	}

	public boolean getPredatorIntroduction() 
	{
		return (Boolean) values.get(Parameter.PredatorIntroduction);
	}
	
	public PredatorEncounterBehavior getPredatorEncounterBehavior() 
	{
		return (PredatorEncounterBehavior) values.get(Parameter.PredatorEncounterBehavior);
	}

	public double getFoodSafetyTradeoff() 
	{
		return (Double) values.get(Parameter.FoodSafetyTradeoff);
	}
	
	public boolean getScentTracking() 
	{
		return (Boolean) values.get(Parameter.ScentTracking);
	}

	public double getScentDepositionRate() 
	{
		return (Double) values.get(Parameter.ScentDepositionRate);
	}

	public double getScentDepositionSpatialScale() 
	{
		return (Double) values.get(Parameter.ScentDepositionSpatialScale);
	}

	public double getScentDecayRate() 
	{
		return (Double) values.get(Parameter.ScentDecayRate);
	}

	public double getScentResponseSpatialScale() 
	{
		return (Double) values.get(Parameter.ScentResponseSpatialScale);
	}

	public double getScentResponseFactor() 
	{
		return (Double) values.get(Parameter.ScentResponseFactor);
	}

	// non-specified parameters
	// --------------------------------------------------------------------------
	
	// the landscape file name
	public String getResourceId()
	{
		File landscape = getResourceLandscapeFile();
		String id = (landscape == null) ? "default" : landscape.getName();
		return id;
	}
	
	public boolean getPredation()
	{
		// total pressure = 0 ==> false
		return 0 != getTotalPredationPressure();
	}
	
	public double getMinDimension()
	{
		return 0;
	}
	
	public double getMaxDimensionX()
	{
		// this is the max continuous location
		// the max grid point is at landscapeSize - 1
		return getLandscapeSizeX();
	}

	public double getMaxDimensionY()
	{
		// this is the max continuous location
		// the max grid point is at landscapeSize - 1
		return getLandscapeSizeY();
	}

	/**
	 * The number of actual iterations the model runs for, depending on the step number and size
	 * @return number of intervals in simulation
	 */
	public int getNumIntervals()
	{
		return (int) ((double) getNumSteps() / getIntervalSize());
	}
	
	/**
	 * The number of actual iterations the model needs to burn in for, depending on the step number and size
	 * @return number of burn in intervals in simulation
	 */
	public int getNumBurnInIntervals()
	{
		return (int) ((double) getBurnInSteps() / getIntervalSize());
	}

	/**
	 * Returns the current starting location. If a start point file and index is specified, the corresponding point is returned.
	 * If a file is not specified, a new random location will be returned each time this method is called.
	 * @return the starting location
	 */
	public NdPoint getStartingLocation()
	{
		NdPoint startingLocation = null;
		
		switch (getStartPointsType())
		{
		case Center:
			double startX;
			double startY;
			
			if (getPredatorIntroduction())
			{
				// start in center of upper right quadrant
				startX = (getMaxDimensionX() - getMinDimension()) * 0.75;
				startY = (getMaxDimensionY() - getMinDimension()) * 0.75;
			}
			else
			{
				startX = (getMaxDimensionX() - getMinDimension()) / 2;
				startY = (getMaxDimensionY() - getMinDimension()) / 2;
			}			
			startingLocation = new NdPoint(startX, startY);			
			break;
			
		case Random:
			NumberGenerator generator = ModelEnvironment.getNumberGenerator();
			startingLocation = new NdPoint(generator.nextDoubleFromTo(0, getLandscapeSizeX()), generator.nextDoubleFromTo(0, getLandscapeSizeY()));
			break;
			
		case FromFile:
			startingLocation = getNextStartPointFromFile();
			break;
			
		default:
			throw new ForagingModelException("Unhandled StartPointsType " + getStartPointsType());
		}
	
		return startingLocation;
	}
	
	private NdPoint getNextStartPointFromFile()
	{
		if (null == startPoints)
		{
			throw new ForagingModelException("Requested start point from file but file not specified");
		}
		if (startPointIndex >= startPoints.size())
		{
			throw new ForagingModelException(String.format("Requested start point %d, but only %d start points read from file", startPointIndex, startPoints.size()));
		}

		NdPoint point = startPoints.get(startPointIndex);
		startPointIndex++;
		
		if (startPointIndex >= startPoints.size())
		{
			// automatically wrap around and re-use start points
			// in the case that there are the same number of start points as foragers, 
			// the same start points will thereby be used for the next simulation
			// in the case that the number of start points is the same as the number of simulations,
			// (e.g. for one forager) this can be used to for the set of start points to use
			startPointIndex = 0;
		}
		
		return point;
	}
	
	public String getRFilePath()
	{
		// note that this doesn't work
		// ClassLoader.getSystemResource( R_FILE ).getPath();
		// because Parameters is in a different package than the R_FILE? 
		return RVisualizer.class.getResource( R_FILE ).getPath();
	}

	public String getR_OutputPath()
	{
		// just return the directory, which is a child of the project dir 
		return R_OUTPUT_DIR;
	}

	public String getOutputPath()
	{
		// output dir, is child of the project dir 
		return OUTPUT_DIR;
	}
	
	public File getPredatorCacheFile()
	{
		return new File(PREDATOR_CACHE_FILE);
	}

	// setters
	
	public void setLandscapeSizeX(int size)
	{
		parameters.set(Parameter.LandscapeSizeX, size);
	}

	public void setLandscapeSizeY(int size)
	{
		parameters.set(Parameter.LandscapeSizeY, size);
	}

	// other
	
	/**
	 * Checks for invalid parameter combinations. This can happen in batch mode for example, when iterating through all combinations
	 * of multiple parameter values.
	 * @return true if parameters are valid, false otherwise
	 */
	public boolean areParametersValid()
	{
		boolean valid = true;
		
		if (getMovementProcess() == MovementProcessType.Mixed && getMovementType() == MovementType.SingleState) 
		{ valid = false; }
		
		// -1 for persistence means use memory correlation
		if (getForagerAnglePersistanceSearch() < 0 & (getMovementType() == MovementType.SingleState || getMovementType() == MovementType.Kinesis) )
		{ valid = false; }

		
		// short decay must be strictly less than long decay unless both are 1 (no memory)
		if ( (getShortDecayRate() < getLongDecayRate()) ||
			 (getShortDecayRate() == getLongDecayRate() && getShortDecayRate() != 1.0)) 
		{ valid = false; }
		
		// predators and scent not currently both supported
		if (getPredation() && getScentTracking())
		{ valid = false; }

		return valid;
	}
	
	private void initStartPoints()
	{
		StartPointReader spReader = InputFactory.createStartPointReader();
		startPoints = spReader.readStartPointsFile(getStartPointsFile());
		startPointIndex = 0;
	}

}
