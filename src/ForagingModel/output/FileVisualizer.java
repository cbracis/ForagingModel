package ForagingModel.output;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.math3.linear.RealVector;

import ForagingModel.agent.Forager;
import ForagingModel.agent.movement.MemoryMovementBehavior;
import ForagingModel.agent.movement.MovementBehavior;
import ForagingModel.core.DirectionProbabilityInfo;
import ForagingModel.core.ModelEnvironment;
import ForagingModel.space.LocationManager;
import ForagingModel.space.MemoryAssemblage;

public class FileVisualizer implements SimulationVisualizer 
{
	private LocationManager locationManager;
	private File probsFolder;
	private int numIterationsToSkip; 
	private Map<Integer, DirectionProbabilityInfo> foragerProbs;

	protected FileVisualizer(LocationManager locationManager, File resultsFile, int numIterationsToSkip) 
	{
		this.locationManager = locationManager;
		this.numIterationsToSkip = numIterationsToSkip;
		
		probsFolder = new File(FilenameUtils.removeExtension(resultsFile.getAbsolutePath()));
		probsFolder.mkdirs();

		processForagers();
	}

	@Override
	public void plotIteration(int iteration) 
	{
		Set<Integer> ids = foragerProbs.keySet();
		
		for (int id : ids)
		{
			DirectionProbabilityInfo probInfo = foragerProbs.get(id);
			List<String[]> columns = new ArrayList<String[]>();			
			List<String> header = new ArrayList<String>();
			
			if (null != probInfo.foragingProbabilities())
			{
				header.add("resource");
				columns.add(realRectorToStringArray(probInfo.foragingProbabilities()));
			}
			if (null != probInfo.attractiveScentProbabilities())
			{
				header.add("attractive_scent");
				columns.add(realRectorToStringArray(probInfo.attractiveScentProbabilities()));
			}
			if (null != probInfo.repulsiveScentValues())
			{
				header.add("repulsive_scent");
				columns.add(realRectorToStringArray(probInfo.repulsiveScentValues()));
			}
			if (null != probInfo.predatorSafety())
			{
				header.add("predator");
				columns.add(realRectorToStringArray(probInfo.predatorSafety()));
			}
			if (null != probInfo.aggregateProbabilities())
			{
				header.add("aggregate");
				columns.add(realRectorToStringArray(probInfo.aggregateProbabilities()));
			}

			if (header.size() > 0)
			{
				File probFile = new File(probsFolder, "Probs_sim=" + ModelEnvironment.getSimulationIndex() + 
						"_id=" + id + "_iter=" + iteration + ".csv");
				FileUtils.writeToFile(header.toArray(new String[0]), columns, probFile);
			}
		}
	}

	@Override
	public void execute(int currentInterval, int priority) 
	{
		if (currentInterval >= numIterationsToSkip)
		{
			plotIteration(currentInterval);
		}
	}

	private void processForagers() 
	{
		foragerProbs = new HashMap<Integer, DirectionProbabilityInfo>();
		List<Forager> foragers = locationManager.getAgents(Forager.class);
		
		for (Forager forager : foragers)
		{
			// get memory
			MovementBehavior behavior = ModelEnvironment.getMovementMapper().getMovement(forager);
			if (behavior != null && behavior instanceof MemoryMovementBehavior)
			{
				MemoryAssemblage memory = ((MemoryMovementBehavior) behavior).getMemory();
				DirectionProbabilityInfo probabilityInfo = memory.reportCurrentProbabilities(); 
				foragerProbs.put(forager.getId(), probabilityInfo);
			}
		}
	}
	
	private String[] realRectorToStringArray(RealVector vector)
	{
		return Arrays.toString(vector.toArray()).replace("[", "").replace("]", "").split(",");
	}
}
