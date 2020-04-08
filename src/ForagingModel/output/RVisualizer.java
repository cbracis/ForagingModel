package ForagingModel.output;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.math3.linear.RealVector;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngine;
import org.rosuda.REngine.REngineException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ForagingModel.agent.Forager;
import ForagingModel.agent.movement.BehaviorState;
import ForagingModel.agent.movement.MemoryMovementBehavior;
import ForagingModel.agent.movement.MovementBehavior;
import ForagingModel.core.DirectionProbabilityInfo;
import ForagingModel.core.ModelEnvironment;
import ForagingModel.core.NdPoint;
import ForagingModel.core.Parameters;
import ForagingModel.predator.PredatorManager;
import ForagingModel.space.LocationManager;
import ForagingModel.space.MemoryAssemblage;
import ForagingModel.space.MemoryAssemblage.State;
import ForagingModel.space.ResourceAssemblage;
import ForagingModel.space.SpaceUtils;

public class RVisualizer implements SimulationVisualizer 
{
	private final static Logger logger = LoggerFactory.getLogger(RVisualizer.class);

	private ResourceAssemblage resources;
	private MemoryAssemblage memory;
	private LocationManager locationManager;
	private PredatorManager predatorManager;
	private double predatorEncounterRadius;
	private Forager forager;
	private List<Forager> conspecifics;
	private boolean plotMemory;
	private DirectionProbabilityInfo probabilityInfo;
	private double maxResource;
	private double[] xyLim;
	int numIterationsToSkip; 
	
	protected RVisualizer(ResourceAssemblage resources, LocationManager locationManager,
			int numIterationsToSkip)
	{
			this(resources, locationManager, null, numIterationsToSkip);
	}

	protected RVisualizer(ResourceAssemblage resources, LocationManager locationManager, 
			PredatorManager predatorManager, int numIterationsToSkip) 
	{
		this.resources = resources;
		this.locationManager = locationManager;
		this.predatorManager = predatorManager;
		this.numIterationsToSkip = numIterationsToSkip;
		
		maxResource = resources.getMaxQuality();
		xyLim = new double[]{ Parameters.get().getMinDimension(), Parameters.get().getMinDimension(), 
							  Parameters.get().getMaxDimensionX(), Parameters.get().getMaxDimensionY() };
		predatorEncounterRadius = Parameters.get().getPredatorEncounterRadius();
		getForager();
		cleanFiles();
	}

	public void plotIteration(int iteration) 
	{
		double[][] resourceValues = resources.reportCurrentState();
		double[][] foragerTrack = createLocationMatrix(Arrays.asList(forager));
		double[][] conspecificTracks = createLocationMatrix(conspecifics);
		int[] states = createStateArray(forager.getReporter().getStateHistory());
		double[][] predatorLocations = createPredatorMatrix(getCurrentForagerLocation());
		String rFilePath = Parameters.get().getRFilePath();
		
//		System.out.println(Arrays.toString(resourceValues[0]));
//		System.out.println(Arrays.deepToString(conspecificTracks));
//		System.out.println(Arrays.toString(states));
////		System.out.println(Arrays.toString(predatorLocations[0]));
//		System.out.println(foragerLocations.toString());
		
		REngine re = null;
		try 
		{
			re = REngine.engineForClass("org.rosuda.REngine.JRI.JRIEngine");
			
	        re.parseAndEval("source(\"" + rFilePath + "\")");
	        
	        re.assign("resourceValues", org.rosuda.REngine.REXP.createDoubleMatrix(resourceValues));
	        re.assign("foragerTrack", org.rosuda.REngine.REXP.createDoubleMatrix(foragerTrack));
	        re.assign("conspecificTracks", org.rosuda.REngine.REXP.createDoubleMatrix(conspecificTracks));
	        re.assign("states", states);
	        re.assign("predatorLocations", org.rosuda.REngine.REXP.createDoubleMatrix(predatorLocations));
	        re.assign("maxResource", new double[] { maxResource });
	        if (plotMemory)
	        {
	    		double[][] memoryValues = memory.reportCurrentState(State.Resource);
	        	re.assign("memoryValues", org.rosuda.REngine.REXP.createDoubleMatrix(memoryValues));
	        	double[] destination = SpaceUtils.toArray(locationManager.getDestination(forager));
	        	re.assign("destination", destination);
	        	int[] memoryUsage = getMemoryUsage();
	        	re.assign("memoryUsage", memoryUsage);
	        	
	        	if (!probabilityInfo.isNull())
	        	{
	        		double[] foragingProbs = getProbabilitiesAsArray(probabilityInfo.foragingProbabilities());
	        	   	re.assign("foragingProbs", foragingProbs);
	        		double[] predatorProbs = getProbabilitiesAsArray(probabilityInfo.predatorSafety());
	        	   	re.assign("predatorProbs", predatorProbs);
	        		double[] aggregateProbs = getProbabilitiesAsArray(probabilityInfo.aggregateProbabilities());
	        	   	re.assign("aggregateProbs", aggregateProbs);	 
	        		double[] angle = new double[] { probabilityInfo.angle().get() };
	        	   	re.assign("angle", angle);	       	     
	        		double[] emd = new double[] { probabilityInfo.getEMD() };
	        	   	re.assign("emd", emd);	       	     
	        	}
	        }
	        re.assign("xyLim", xyLim);
	        re.assign("iteration", new int[] { iteration });
	        
	        if (plotMemory)
	        {
	        	re.parseAndEval("plotResourceAndMemoryAndProbability(resourceValues, memoryValues, foragingProbs, predatorProbs, aggregateProbs, foragerTrack, conspecificTracks, states, memoryUsage, destination, angle, emd, predatorLocations, maxResource, xyLim, iteration)");	
//	            System.out.println(result.asString());

	        }
	        else
	        {	        
	        	re.parseAndEval("plotResource(resourceValues, foragerTrack, conspecificTracks, states, predatorLocations, maxResource, xyLim, iteration)");
//	            System.out.println(result.asString());
	        }
		} catch (ClassNotFoundException e) 
		{
			throw new RCommunicationException("Problem calling R through JRI", e);
		} catch (NoSuchMethodException e) 
		{
			throw new RCommunicationException("Problem calling R through JRI", e);
		} catch (IllegalAccessException e) 
		{
			throw new RCommunicationException("Problem calling R through JRI", e);
		} catch (InvocationTargetException e) 
		{
			throw new RCommunicationException("Problem calling R through JRI", e);
		} catch (REngineException e) 
		{
			throw new RCommunicationException("Problem calling R through JRI", e);
		} catch (REXPMismatchException e) 
		{
			throw new RCommunicationException("Problem calling R through JRI", e);
		}

		finally
		{
			if (re != null)
        	{
        		re.close();
        	}
		}
	}
	

	private int[] getMemoryUsage() 
	{
		// true = 1, false = 0
		List<Boolean> memoryUseageList = forager.getReporter().getMemoryUsageHistory();
		int[] memoryUseage = new int[memoryUseageList.size()];
		
		for (int i = 0; i < memoryUseageList.size(); i++)
		{
			if (memoryUseageList.get(i) == true)
			{
				memoryUseage[i] = 2; // using memory = 2
			} else
			{
				memoryUseage[i] = 1; // not using memory = 1
			}
		}
		return memoryUseage;
	}
	
	private double[][] createLocationMatrix(List<Forager> foragers)
	{
		double[][] locationMatrix = null;
		
		if (null != foragers && foragers.size() > 0)
		{
			// need to get size of first forager's track to size array
			int trackLength = foragers.get(0).getReporter().getLocationHistory().size();
			
			// row is each location, columns are id, x and y
			locationMatrix = new double[foragers.size() * trackLength][3];
			int row = 0;
	
			for (Forager forager : foragers)
			{
				int id = forager.getId();
				List<NdPoint> locations = forager.getReporter().getLocationHistory();
			
				for (int i = 0; i < locations.size(); i++)
				{
					NdPoint location = locations.get(i);
					locationMatrix[row][0] = id;
					locationMatrix[row][1] = location.getX();
					locationMatrix[row][2] = location.getY();
					row++;
				}
			}
		}
		
		return locationMatrix;
	}
	
	private NdPoint getCurrentForagerLocation()
	{
		 List<NdPoint> foragerLocations = forager.getReporter().getLocationHistory();
		 return foragerLocations.get(foragerLocations.size() - 1); // last one
	}
	
	private int[] createStateArray(List<BehaviorState> stateHistory) 
	{
		int[] states = new int[stateHistory.size()];
		
		for (int i = 0; i < stateHistory.size(); i++)
		{
			states[i] = stateHistory.get(i).value();
		}
		return states;
	}

	protected double[][] createPredatorMatrix(NdPoint currentLocation) 
	{
		double[][] predatorMatrix = null; //new double[][] {{0, 0, 1}};
		
		if (null != predatorManager) // only if predation is enabled
		{
			List<NdPoint> currentPredators = new ArrayList<NdPoint>(predatorManager.getActivePredators());
			
			// row is each predator, columns are x, y, and encountered (0,1)
			predatorMatrix = new double[currentPredators.size()][3];
			
			for (int i = 0; i < currentPredators.size(); i++)
			{
				NdPoint predLocation = currentPredators.get(i);
				predatorMatrix[i][0] = predLocation.getX();
				predatorMatrix[i][1] = predLocation.getY();
				predatorMatrix[i][2] = SpaceUtils.getDistance(predLocation, currentLocation) < predatorEncounterRadius ? 1 : 0;
			}
		}
		
		return predatorMatrix;
	}
	
	private double[] getProbabilitiesAsArray(RealVector probabilities)
	{
		double[] array;
		
		if (probabilities == null)
		{
			array = new double[] {};
		}
		else
		{
			array = probabilities.toArray();
		}
		return array;
	}

	private void getForager() 
	{
		// for just plot 1 forager,even if there are multiple
		// this could be improved
		List<Forager> foragers = locationManager.getAgents(Forager.class);
		if (foragers.size() >= 1)
		{
			forager = foragers.get(0);
			conspecifics = new ArrayList<Forager>(foragers);
			conspecifics.remove(forager);
			
			// get memory
			MovementBehavior behavior = ModelEnvironment.getMovementMapper().getMovement(forager);
			if (behavior != null && behavior instanceof MemoryMovementBehavior)
			{
				memory = ((MemoryMovementBehavior) behavior).getMemory();
				probabilityInfo = memory.reportCurrentProbabilities(); 
			}
			else
			{
				memory = null;
			}
			plotMemory = (memory == null) ? false : true;
		}
		if (foragers.size() > 1)
		{
			logger.warn("Warning: only forager id={} plotted.", forager.getId());

		}
	}

	private void cleanFiles() 
	{
		File output = new File(Parameters.get().getR_OutputPath());
		
		if (!output.exists())
		{
			output.mkdir();
		}
		else
		{
			// just delete the plot*.pdf files to avoid deleting movies, etc.
			Iterator<File> plotIt = FileUtils.iterateFiles(output, new WildcardFileFilter("plot*.pdf"), null); // null -> no subdirs
			while (plotIt.hasNext())
			{
				plotIt.next().delete();
			}
		}
	}

	public void execute(int currentInterval, int priority) 
	{
		if (currentInterval >= numIterationsToSkip)
		{
			plotIteration(currentInterval);
		}
	}

}
