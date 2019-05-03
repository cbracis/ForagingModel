package ForagingModel.core;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import ForagingModel.agent.movement.MovementMapper;
import ForagingModel.core.Parameters.Parameter;
import ForagingModel.predator.PredatorManager;

public class ModelEnvironment 
{
	private static NumberGenerator generator = null;
	private static MovementMapper mapper = null;
	// this is to ensure consistency of predator location/appearance across simulations with different parameters
	private static Map<PredatorParamKey,PredatorManager> predatorCache = null;

	private static int simulationIndex = 0;
	
	public static NumberGenerator getNumberGenerator()
	{
		if (generator == null)
		{
			resetGenerator();
		}
		return generator;
	}
	
	public static MovementMapper getMovementMapper()
	{
		if (mapper == null)
		{
			mapper = MovementMapper.create();
		}
		return mapper;
	}

	public static Map<PredatorParamKey,PredatorManager> getPredatorCache()
	{
		if (predatorCache == null)
		{
			predatorCache = new HashMap<PredatorParamKey,PredatorManager>();
		}
		return predatorCache;
	}

	/**
	 * Gets the simulation index, which starts at 0 and increments for each combination of parameters.
	 * @return the simulation index
	 */
	public static int getSimulationIndex()
	{
		return simulationIndex;
	}
	
	protected static void setNumberGenerator(NumberGenerator generator)
	{
		ModelEnvironment.generator = generator;
	}
	
	protected static void resetGenerator()
	{
		// reset to new random seed, creating if necessary
		if (Parameters.get().getCreateRandomSeed())
		{
			Parameters.get().set(Parameter.RandomSeed, (int) System.currentTimeMillis()); 
		}
		generator = RandomGenerator.create(Parameters.get().getRandomSeed());
	}
	
	
	protected static void setSimulationIndex(int index)
	{
		simulationIndex = index;
	}
	
	protected static void setPredatorCache( Map<PredatorParamKey,PredatorManager> predatorCache)
	{
		ModelEnvironment.predatorCache = predatorCache;	
	}
	
	public static class PredatorParamKey implements Serializable
	{
		
		private static final long serialVersionUID = -2496962118057250076L;

		private String resourceId;
		private int predatorDuration;
		private int maxIntervals;
		private double predatorRandomness;
		private int totalPredationPressure;
		
		public PredatorParamKey(String landscape, int predatorDuration, int maxIntervals, double predatorRandomness, int totalPredationPressure)
		{
			this.resourceId = landscape;
			this.predatorDuration = predatorDuration;
			this.maxIntervals = maxIntervals;
			this.predatorRandomness = predatorRandomness;
			this.totalPredationPressure = totalPredationPressure;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + maxIntervals;
			result = prime * result + predatorDuration;
			long temp;
			temp = Double.doubleToLongBits(predatorRandomness);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			result = prime * result
					+ ((resourceId == null) ? 0 : resourceId.hashCode());
			result = prime * result + totalPredationPressure;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			PredatorParamKey other = (PredatorParamKey) obj;
			if (maxIntervals != other.maxIntervals)
				return false;
			if (predatorDuration != other.predatorDuration)
				return false;
			if (Double.doubleToLongBits(predatorRandomness) != Double
					.doubleToLongBits(other.predatorRandomness))
				return false;
			if (resourceId == null) {
				if (other.resourceId != null)
					return false;
			} else if (!resourceId.equals(other.resourceId))
				return false;
			if (totalPredationPressure != other.totalPredationPressure)
				return false;
			return true;
		}
		
		@Override
		public String toString()
		{
			return String.format("r = %s, tot = %d, d = %d, tr = %.2f", resourceId, totalPredationPressure, predatorDuration, predatorRandomness);
		}
	}

}
