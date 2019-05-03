package ForagingModel.predator;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ForagingModel.core.ForagingModelException;
import ForagingModel.core.ModelEnvironment;
import ForagingModel.core.ModelEnvironment.PredatorParamKey;
import ForagingModel.core.NdPoint;
import ForagingModel.core.NumberGenerator;
import ForagingModel.core.Parameters;
import ForagingModel.space.ResourceAssemblage;
import ForagingModel.space.SpaceUtils;

public class PredatorGenerator 
{
	private final static Logger logger = LoggerFactory.getLogger(PredatorGenerator.class);
	
	private NumberGenerator generator;
	private ResourceAssemblage resources; 
	private String resourceId;
	private int predatorDuration;
	private double predatorRandomness;
	private int totalPredationPressure;
	private boolean introducedScenario;
	private int maxIntervals;
	private double minDimX;
	private double minDimY;
	private double maxDimX;
	private double maxDimY;
	
	
	protected PredatorGenerator(ResourceAssemblage unboarderedResources, String resourceId,
			int predatorDuration, double predatorRandomness, int totalPredationPressure)
	{
		this.resources = unboarderedResources;
		this.resourceId = resourceId;
		this.predatorDuration = predatorDuration; // assume in units of intervals, not time step?
		this.predatorRandomness = predatorRandomness;
		this.totalPredationPressure = totalPredationPressure;
		generator = ModelEnvironment.getNumberGenerator();
		
		Parameters params = Parameters.get();
		maxIntervals = params.getNumIntervals() - predatorDuration; 
		if (maxIntervals < 0)
		{
			throw new ForagingModelException(String.format("Number of intervals %d too short for predator duration %d", params.getNumIntervals(), predatorDuration));
		}
		minDimX = params.getMinDimension();
		minDimY = params.getMinDimension();
		maxDimX = params.getMaxDimensionX();
		maxDimY = params.getMaxDimensionY();
		introducedScenario = Parameters.get().getPredatorIntroduction();

	}
	
	public PredatorManager generatePredators()
	{
		PredatorManager predManager = null;
		PredatorParamKey key = new PredatorParamKey(resourceId, predatorDuration, maxIntervals, predatorRandomness, totalPredationPressure);
		
		if (ModelEnvironment.getPredatorCache().containsKey(key))
		{
			// get from cache
			predManager = ModelEnvironment.getPredatorCache().get(key);
		}
		else
		{
			if (totalPredationPressure > 0)
			{
				logger.info("Predators not in cache: " + key.toString());
			}
			int numPredators = totalPredationPressure / predatorDuration;
			List<Predator> predators = new ArrayList<Predator>(numPredators);
			
			for (int i = 0; i < numPredators; i++)
			{
				if (introducedScenario)
				{
					// hard code predators to second half and upper right
					predators.add(createPredator(Parameters.get().getNumIntervals() / 2, maxDimX * 0.5, maxDimY * 0.5, maxDimX, maxDimY));
				}
				else
				{
					predators.add(createPredator());
				}
			}
			
			predManager = PredatorFactory.createPredatorManager(predators);
			ModelEnvironment.getPredatorCache().put(key, predManager);
		}
		return predManager;
	}
	
	protected Predator createPredator()
	{
		return createPredator(0, minDimX, minDimY, maxDimX, maxDimY);
	}
	
	protected Predator createPredator(int minTime, double minBoxX, double minBoxY, double maxBoxX, double maxBoxY)
	{
		int start = (maxIntervals == minTime) ? minTime : (int)generator.nextIntFromTo(minTime, maxIntervals);
		
		// algorithm for generating inhomogeneous poisson process from rpoispp in spatstat
//        X <- runifpoispp(lmax, win)
//        if (X$n == 0) 
//            return(X)
//        prob <- lambda[X]/lmax
//        u <- runif(X$n)
//        retain <- (u <= prob)
//        X <- X[retain, ]
//        return(X)

		NdPoint loc = null;
		// max dim is max continuous location, not max grid point idx
		double qualityMax = introducedScenario ? resources.getMaxQuality((int)minBoxX, (int)minBoxY, (int)maxBoxX - 1, (int)maxBoxY - 1) : resources.getMaxQuality(); 
		
		while (loc == null)
		{
			loc = new NdPoint(generator.nextDoubleFromTo(minBoxX, maxBoxX), generator.nextDoubleFromTo(minBoxY, maxBoxY));
			double randomValue = generator.nextDouble();
			double quality = predatorRandomness * resources.getAverageQuality() 
					+ (1 - predatorRandomness) * resources.getIntrinsicQuality(SpaceUtils.getGridPoint(loc));
			double probability = quality / qualityMax;

			if (randomValue > probability) // retain if randomValue <= probability
			{
				loc = null;
			}
		}
		
		return new Predator(start, predatorDuration, loc);
	}
	

}
