package ForagingModel.space;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import ForagingModel.core.GridPoint;
import ForagingModel.core.MatrixUtils;
import ForagingModel.core.ModelEnvironment;
import ForagingModel.core.NdPoint;
import ForagingModel.core.NumberGenerator;
import ForagingModel.core.Parameters;
import ForagingModel.input.CellData;
import ForagingModel.predator.PredatorManager;
import ForagingModel.schedule.SchedulePriority;
import ForagingModel.schedule.Scheduler;

public class SpaceFactory 
{
	public static LocationManager createLocationManager()
	{
		return new LocationMangerImpl(Parameters.get().getMinDimension(), Parameters.get().getMinDimension(), 
									  Parameters.get().getMaxDimensionX(), Parameters.get().getMaxDimensionY());
	}

	public static LocationManager createLocationManager(int landscapeSizeX, int landscapeSizeY)
	{
		return new LocationMangerImpl(Parameters.get().getMinDimension(), Parameters.get().getMinDimension(), landscapeSizeX, landscapeSizeY);
	}

	public static LocationManager createLocationManager(int landscapeSize)
	{
		return new LocationMangerImpl(0, 0, landscapeSize, landscapeSize);
	}

	public static ResourceAssemblage generateResource(List<CellData> resourceData, Scheduler scheduler) 
	{
		RealMatrix resources = generateResourceMatrix(resourceData);
		return createResourceAssemblage(resources, scheduler);
	}

	public static ResourceAssemblage generateTwoPatchResource(Scheduler scheduler)
	{
		return SpaceFactory.generateTwoPatchResource(Parameters.get().getLandscapeSizeX(), Parameters.get().getLandscapeSizeY(), scheduler);
	}
	
	public static ResourceAssemblage generateTwoPatchResource(int gridWidth, int gridHeight, Scheduler scheduler)
	{
		List<NdPoint> twoPatches = Arrays.asList(new NdPoint[] 
				{ new NdPoint(gridWidth / 3, gridHeight / 2), 
				  new NdPoint(gridWidth * 2 / 3, gridHeight / 2) });
		
		RealMatrix resources = generateResourceMatrix(gridWidth, gridHeight, twoPatches, gridWidth / 10, 0.5);
		MatrixUtils.normalize(resources);
		return createResourceAssemblage(resources, scheduler);
	}
	
	protected static ResourceAssemblage generateRandomResource(int gridWidth, int gridHeight, int numPatches, double radius, double steepness)
	{
		RealMatrix resources = generateResourceMatrix(gridWidth, gridHeight, numPatches, radius, steepness);
		MatrixUtils.normalize(resources);
		return createResourceAssemblage(resources, Parameters.get().getResourceRegenerationRate(), Parameters.get().getIntervalSize());
	}

	protected static ResourceAssemblage generateUniformResource(int width, int height) 
	{
		double quality = 1.0 / width / height;
		
		List<CellData> resourceQuality = new ArrayList<CellData>();
		
		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				resourceQuality.add(CellData.create(x, y, quality));
			}
		}
		RealMatrix resources = generateResourceMatrix(resourceQuality);
		return createResourceAssemblage(resources, Parameters.get().getResourceRegenerationRate(), Parameters.get().getIntervalSize());
	}
	
	protected static ResourceAssemblage createResourceAssemblage(RealMatrix resources, Scheduler scheduler)
	{
		ResourceAssemblage resourceAssemblage = new ResourceMatrix(resources, Parameters.get().getResourceRegenerationRate(), Parameters.get().getIntervalSize());
		scheduler.register(resourceAssemblage, SchedulePriority.ResourceGrow);
		return resourceAssemblage;
	}

	protected static ResourceAssemblage createResourceAssemblage(RealMatrix resources, double regenerationRate, double intervalSize)
	{
		return new ResourceMatrix(resources, regenerationRate, intervalSize);
	}
	
	protected static RealMatrix generateResourceMatrix(int gridWidth, int gridHeight, int numPatches, double radius, double steepness)
	{
		NumberGenerator generator = ModelEnvironment.getNumberGenerator();
		List<NdPoint> patchCenters = new ArrayList<NdPoint>();
		for (int i = 0; i < numPatches; i++)
		{
			patchCenters.add(new NdPoint(generator.nextDoubleFromTo(0, gridWidth),
										 generator.nextDoubleFromTo(0, gridHeight)));
		}
		return generateResourceMatrix(gridWidth, gridHeight, patchCenters, radius, steepness);
	}
	
	protected static RealMatrix generateResourceMatrix(int gridWidth, int gridHeight, List<NdPoint> patchCenters, double radius, double steepness)
	{
		List<CellData> resourceQuality = new ArrayList<CellData>();
		
		for (int x = 0; x < gridWidth; x++)
		{
			for (int y = 0; y < gridHeight; y++)
			{
				// now sum contributions from each patch
				double quality = 0;
				for (NdPoint patch : patchCenters)
				{
					quality += calculatePatchQuality(patch, radius, steepness, x, y);
				}
				resourceQuality.add(CellData.create(x, y, quality));
			}
		}
		return generateResourceMatrix(resourceQuality);
	}

	protected static RealMatrix generateResourceMatrix(List<CellData> resourceQuality)
	{
		// first calculate dimensions (no longer normalize to 1 since landscape files already are and want to use bigger ones that don't add to 1)
		int maxXIdx = 0;
		int maxYIdx = 0;
		
		for (CellData data : resourceQuality)
		{
			maxXIdx = (data.getX() > maxXIdx) ? data.getX() : maxXIdx;
			maxYIdx = (data.getY() > maxYIdx) ? data.getY() : maxYIdx;
		}
		
		// now create the resource quality for each grid point
		RealMatrix resources = new Array2DRowRealMatrix(maxXIdx + 1, maxYIdx + 1);
		for (CellData data : resourceQuality)
		{
			double value = data.getCarryingCapacity();
			resources.setEntry(data.getX(), data.getY(), value);
		}
		return resources;
	}

	protected static double calculatePatchQuality(NdPoint center, double radius, double steepness, int atX, int atY)
	{
		double dist = SpaceUtils.getDistance(center, new GridPoint(atX, atY));
		NormalDistribution standardNormal = new NormalDistribution();
		double value = 1 - standardNormal.cumulativeProbability(steepness * (dist - radius));
		return value;
	}

	public static MemoryAssemblage createMemoryAssemblage(ResourceAssemblage resources, PredatorManager predatorManager, Scheduler scheduler)
	{
		// note that this might eventually get more complicated with different decision rules
		// for now, no predators ==> create resource memory
		// and predators ==> create aggregate memory for predators and resource
		
		MemoryAssemblage memory;
		ResourceMemory resourceMemory = createResourceMemory(resources);
		
		if (Parameters.get().getPredation() && null != predatorManager)
		{
			PredatorMemory predatorMemory = createPredatorMemory(predatorManager);
			memory = createAggregateMemory(resourceMemory, predatorMemory);
		}
		else
		{
			memory = resourceMemory;
		}
		
		scheduler.register(memory, SchedulePriority.MemoryDecay);
		scheduler.register(memory);

		return memory;
	}
	
	protected static ResourceMemory createResourceMemory(ResourceAssemblage resources)
	{
		return createResourceMemory(resources, Parameters.get().getIsFullyInformed());
	}

	protected static ResourceMemory createResourceMemory(ResourceAssemblage resources, boolean fullyInformed)
	{
		Parameters params = Parameters.get();
		
		RealMatrix initialMemoryValues = resources.getInitialMemory(fullyInformed, params.getInitialValueUninformedMemory());
		ResourceMemory memory = (ResourceMemory) createResourceMemory(initialMemoryValues, resources, 
				AngularProbabilityInfo.create(),
				params.getShortLearningRate(), params.getLongLearningRate(), 
				params.getShortSpatialScale(), params.getLongSpatialScale(), 
				params.getShortDecayRate(), params.getLongDecayRate(), 
				params.getShortMemoryFactor(), params.getMemoryAlpha(), 
				params.getMemorySpatialScaleForaging(), params.getIntervalSize());
		return memory;
	}

	protected static ResourceMemory createResourceMemory(RealMatrix longMemories, ResourceAssemblage resources, AngularProbabilityInfo angProbInfo,
            double shortLearningRate, double longLearningRate, double shortSpatialScale, double longSpatialScale, 
            double shortDecayRate, double longDecayRate, double shortMemoryFactor, 
            double alpha, double memorySpatialScale, double intervalSize)
	{
		return new ResourceMemory(longMemories, resources, angProbInfo,
				shortLearningRate, longLearningRate, shortSpatialScale, longSpatialScale, 
				shortDecayRate, longDecayRate, shortMemoryFactor, alpha, memorySpatialScale, intervalSize);
	}

	protected static PredatorMemory createPredatorMemory(PredatorManager predators)
	{
		Parameters params = Parameters.get();
		RealMatrix memories = new Array2DRowRealMatrix(params.getLandscapeSizeX(), params.getLandscapeSizeY());
		PredatorMemory memory = createPredatorMemory(memories, predators, AngularProbabilityInfo.create(),
				params.getPredatorLearningRate(), params.getPredatorMemoryFactor(), 
				params.getPredatorDecayRate(), params.getPredatorEncounterRadius(), 
				params.getMemorySpatialScalePredation(), params.getIntervalSize());
		return memory;
	}
	
	protected static PredatorMemory createPredatorMemory(RealMatrix memories, PredatorManager predators,
			AngularProbabilityInfo angularProbabilityInfo, 
			double learningRate, double predatorMemoryFactor, double decayRate, 
            double encounterRadius, double memorySpatialScale, double intervalSize)
	{
		return new PredatorMemory(memories, predators, angularProbabilityInfo, 
				learningRate, predatorMemoryFactor, decayRate, 
				encounterRadius, memorySpatialScale, intervalSize);
	}
	
	protected static AggregateMemory createAggregateMemory(ResourceMemory resourceMemory, PredatorMemory predatorMemory)
	{
		return createAggregateMemory(resourceMemory, predatorMemory, Parameters.get().getFoodSafetyTradeoff());
	}

	protected static AggregateMemory createAggregateMemory(ResourceMemory resourceMemory, PredatorMemory predatorMemory, double foodSafetyTradeoff)
	{
		return new AggregateMemory(resourceMemory, predatorMemory, foodSafetyTradeoff);
	}


}
