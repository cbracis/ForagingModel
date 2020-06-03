package ForagingModel.space;

import java.util.List;
import java.util.Set;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.util.FastMath;

import ForagingModel.core.GridPoint;
import ForagingModel.core.MatrixUtils;
import ForagingModel.core.NdPoint;
import ForagingModel.predator.PredatorManager;

public class PredatorMemory extends AbstractMemory implements MemoryAssemblage 
{
	private RealMatrix memories;
	private PredatorManager predatorManager;

	private double learningRate;
	private double decayRate;
	private double encounterRadius;
	private double memorySpatialScale; 
	private double predatorMemoryFactor; // what to multiply each angular value by so that threat is ~1 near predator and ~0 far away
	private double intervalSize;
	
	private int rows; 
	private int columns;
	private RealVector distanceFactor;
	
	private final double MAX_PREDATOR_VALUE = 1.0;

	protected PredatorMemory(RealMatrix memories, PredatorManager predators,
			AngularProbabilityInfo angularProbabilityInfo,
			double learningRate, double predatorMemoryFactor, double decayRate, 
            double encounterRadius, double memorySpatialScale, double intervalSize)
	{
		super(angularProbabilityInfo);
		this.memories = memories;
		this.predatorManager = predators;
		
		this.learningRate = learningRate;
		this.decayRate = decayRate;
		this.encounterRadius = encounterRadius;
		this.memorySpatialScale = memorySpatialScale;
		this.intervalSize = intervalSize;
		
		// TODO: for now just use inverse of spacing (basically undoing the multiplication of delta x in approximating integration)
		// but should make parameter
		this.predatorMemoryFactor = predatorMemoryFactor; //20.0 (balance divide by gamma.Z); //1.0 / this.angProbInfo.getSpacing() / 9.0;
		
		rows =  memories.getRowDimension();
		columns = memories.getColumnDimension();
		
		distanceFactor = getExponentialDistanceFactor(memorySpatialScale);//, encounterRadius);

	}

	
	@Override
	public void learn(NdPoint consumerLocation) 
	{
		Set<NdPoint> predators = predatorManager.getActivePredators(consumerLocation, encounterRadius);
		
		for (int row = 0; row < rows; row++)
		{
			for(int column = 0; column < columns; column++)
			{
				GridPoint location = new GridPoint(row, column);
				
				for (NdPoint predatorLocation : predators)
				{
					double distance = SpaceUtils.getDistance(predatorLocation, location);
					learn(row, column, distance);
				}
			}
		}

	}
	
	protected void learn(int row, int column, double distance) 
	{
		double memory = memories.getEntry(row, column);
		
		// old learn amount, normal kernel (but adjusted so distance = 0 inside encounter radius)
//		if (distance < encounterRadius) {distance = 0;}
//		double learnAmount = learningRate * Math.exp(-distance * distance / spatialScale) / (2 * Math.PI * spatialScale) * (MAX_PREDATOR_VALUE - memory) * intervalSize;

		// new learn amount, top hat kernel (normalize by pi r^2)
		double learnAmount = 0;
		if (distance < encounterRadius)
		{
			learnAmount = learningRate * (MAX_PREDATOR_VALUE - memory) / (Math.PI * encounterRadius * encounterRadius) * intervalSize;
		}
		
		memories.setEntry(row, column, memory + learnAmount);
	}
	
	@Override
	public double[][] reportCurrentState(State state) 
	{
		double[][] data;
		switch (state)
		{
		case Predators:
			data = memories.getData();
			break;
		case Resource:
		case Scent:
		default:
			data = null;
			break;
		}
		return data;
	}
	
	protected RealMatrix getMemory()
	{
		return memories;
	}


	@Override
	public void execute(int currentInterval, int priority) 
	{
		decay();
	}
	
	protected RealMatrix getProbabilities(NdPoint currentLocation)
	{
		// really won't have predators without forage, 
		// but implementing as a way to play with effect of just predators
		
		// for now, just take 1 - values and weight by distance

		RealMatrix probabilities = memories.scalarMultiply(-1).scalarAdd(1);
		GridPoint currentPoint = SpaceUtils.getGridPoint(currentLocation);
		 
		for (int row = 0; row < rows; row++)
		{
			for(int column = 0; column < columns; column++)
			{
				double dist = SpaceUtils.getDistance(currentLocation, new GridPoint(row, column));
				// TODO: need to scale by alpha??
				double prob = Math.exp(probabilities.getEntry(row, column) * Math.exp(-dist / memorySpatialScale) / memorySpatialScale);
				if ( currentPoint.getX() == row && currentPoint.getY() == column )
				{
					// make current grid square 0, otherwise it will have high prob due to being close and direction of center is arbitrary
					// note that this is 1 not 0 since R code sets to 0 before multiplying by alpha and exp(0)=1
					prob = 1;
				}
				if (prob > MAX_PROBABILITY_VALUE) // e.g. infinity
				{
					// if memory is large (i.e. for example very concentrated patch) and nearby, the exponential can overflow and return infinity
					prob = MAX_PROBABILITY_VALUE;
				}
				probabilities.setEntry(row, column, prob);
			}
		}
		
		// can be set to null to avoid choosing a destination
		probabilities = normalizeMatrix(probabilities);
		
		return probabilities;
	}
	
	
	// This returns probabilities by NORMALIZING safety so they add to one, called by feeding behavior to avoid predators while feeding
	// TODO: use a smaller memory spatial scale for this?? right now just based on existing predator safety
	@Override
	protected RealVector getAngularProbabilities(NdPoint currentLocation) 
	{
		// TODO this call updates probability cache, copying or else don't plot well but inefficient
		RealVector probs = getPredatorSafety(currentLocation).copy();
		MatrixUtils.normalize(probs);
		return probs;
	}
	
	// This is called by AggregateMemory to get the safety values to multiply resource memory
	protected RealVector getPredatorSafety(NdPoint currentLocation) 
	{
		// TODO: lots of duplicated code from ResourceMemory, what to do??
		List<Double> angles = angProbInfo.getAngles();
		RealVector probs = new ArrayRealVector(angles.size());
		RealVector probsUntruncated = new ArrayRealVector(angles.size());
		double maxValue = 0;
		
		for (int angleIdx = 0; angleIdx < angles.size(); angleIdx++)
		{
			List<GridPoint> samplePoints = angProbInfo.getSamplePoints(currentLocation, angles.get(angleIdx));
			RealVector sampleValues = new ArrayRealVector(samplePoints.size());
			
			// value at location z = P(z) * exp(-distance(z, loc) * gamma.Z)
			
			// now multiply by memory value by distance factor (exponential part)
			for (int pointIdx = 0; pointIdx < samplePoints.size(); pointIdx++)
			{
				GridPoint point = samplePoints.get(pointIdx);
				sampleValues.setEntry(pointIdx, 
						memories.getEntry(point.getX(), point.getY()) * distanceFactor.getEntry(pointIdx) * predatorMemoryFactor);
			}
			
			double value = MatrixUtils.sum(sampleValues); 
			maxValue = FastMath.max(maxValue, value);
			
			probsUntruncated.setEntry(angleIdx, value);
			probs.setEntry(angleIdx, FastMath.min(value, MAX_PREDATOR_VALUE)); // TODO: or explicitly max at 1
		}
		
		// if all sum to one, replace with untruncated values divided by max so go in less bad direction
		// this is important for smaller memory spatial scale values
		// Note: disabling this because harder to document and smaller memory spatial scales don't perform well anyway
//		if (sum >= MAX_PREDATOR_VALUE * probs.getDimension())
//		{
//			probs = probsUntruncated.mapDivideToSelf(maxValue);
//		}
		
		// this gives safety = 1 - threat (no longer a probability distribution)
		probs.mapMultiplyToSelf(-1).mapAddToSelf(1);
		probabilityCache.updatePredator(probs);
		
		return probs;
	}


	protected void decay()
	{
		// new = old - old * decayRate * intervalSize
		double factor = 1.0 - decayRate * intervalSize;
		for (int row = 0; row < rows; row++)
		{
			for (int column = 0; column < columns; column++)
			{
				memories.setEntry(row, column, memories.getEntry(row, column) * factor );
			}
		}
	}

}
