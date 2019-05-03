package ForagingModel.output;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import au.com.bytecode.opencsv.CSVWriter;
import ForagingModel.agent.Agent;
import ForagingModel.agent.Forager;
import ForagingModel.agent.Reporter;
import ForagingModel.agent.Reporter.SummaryMetric;
import ForagingModel.agent.movement.BehaviorState;
import ForagingModel.core.ForagingModelException;
import ForagingModel.core.ModelEnvironment;
import ForagingModel.core.NdPoint;
import ForagingModel.core.Parameters;
import ForagingModel.core.Parameters.Parameter;
import ForagingModel.core.Velocity;
import ForagingModel.predator.Predator;

public class SimulationFileWriter implements SimulationReporter 
{
	public final static String TOTAL_CONSUMPTION_FILE_APPENDAGE = "-TotalConsumption.txt";
	
	private File resultsFile;
//	private File totalConsumptionFile;
	private File tracksFolder;
	private List<? extends Agent> agents;
	private final List<Parameter> paramsToReport;
	private final List<SummaryMetric> metricsToReport;
	private int numQualityBins;

	private int numColumns;
	
	protected SimulationFileWriter(File results, List<? extends Agent> agents, int numQualityBins)
	{
		this.resultsFile = results;
//		totalConsumptionFile = new File(FilenameUtils.removeExtension(results.getAbsolutePath()) + TOTAL_CONSUMPTION_FILE_APPENDAGE);
		
		if (Parameters.get().getSaveTracks())
		{
			tracksFolder = new File(FilenameUtils.removeExtension(results.getAbsolutePath()));
			tracksFolder.mkdirs();
		}
		
		this.numQualityBins = numQualityBins;
		
		this.agents = agents;
		int numForagers = 0;
		for (Agent agent : agents)
		{
			if (agent instanceof Forager)
			{
				numForagers++;
			}
		}
		
		paramsToReport = new ArrayList<Parameter>(Arrays.asList(Parameter.values()));
		Collection<Parameter> paramsNOTreport = new HashSet<Parameter>(Arrays.asList(
				// not interesting
				Parameter.LandscapeSizeX,
				Parameter.LandscapeSizeY,
				Parameter.ResultsFileName,
				Parameter.SaveTracks,
				Parameter.StartPointsFileName,
				Parameter.VisualizeSimulation,
				Parameter.VisualizeProbabilities,
				// old memory destination version of model
				Parameter.MemoryAlpha,
				Parameter.MemoryArrivalRadius,
				// correlated movement - persistence
				Parameter.ForagerAnglePersistanceFeeding,
				Parameter.ForagerAnglePersistanceSearch,
				// not changing, so default value is used, but add back in if needed
//				Parameter.ConsumerConsumptionRate,
//				Parameter.ConsumerConsumptionSpatialScale,
//				Parameter.ShortLearningRate,
//				Parameter.LongLearningRate,
				Parameter.ShortSpatialScale,
				Parameter.LongSpatialScale,
//				Parameter.ShortMemoryFactor, actually still changing...
				Parameter.StartPointsIndex
				// predator
//				Parameter.TotalPredationPressure,
//				Parameter.PredatorDecayRate,
//				Parameter.PredatorDuration,
//				Parameter.PredatorEncounterRadius,
//				Parameter.PredatorLearningRate,
//				Parameter.PredatorRandomness,
//				Parameter.PredatorSpatialScale
				));
		
		for (Iterator<Parameter> it = paramsToReport.iterator(); it.hasNext(); )
		{
	        if (paramsNOTreport.contains(it.next()))
	        {
	            it.remove();
	        }
		}
		
		metricsToReport = Arrays.asList(
				SummaryMetric.TotalConsumption, 
				SummaryMetric.StdDevConsumption, 
				SummaryMetric.DistanceTraveled, 
				SummaryMetric.Sinuosity, 
				SummaryMetric.AverageSpeed, 
				SummaryMetric.AverageSpeedSearch,
				SummaryMetric.AverageSpeedFeeding,
				SummaryMetric.MeanSinTurningAngle, 
				SummaryMetric.MeanCosTurningAngle,
				SummaryMetric.TimeSearching,
				SummaryMetric.MemorySearchPercentage,
				SummaryMetric.TotalPredatorEncounters,
				SummaryMetric.PredatorReEncounters,
				SummaryMetric.ExecutionTime);
		numColumns = paramsToReport.size() + numForagers * metricsToReport.size() + numForagers * numQualityBins + 1; // + 1 for sim index
		
		init();
	}
	
	public void reportSummaryResults() 
	{
		Parameters params = Parameters.get();
		String[] results = new String[numColumns];
		int i = 0;
		
		// sim index
		results[i] = Integer.toString(ModelEnvironment.getSimulationIndex());
		i++;
		
		// parameters
		for (Parameter param : paramsToReport)
		{
			results[i] = params.get(param);
			i++;
		}
		// foragers
		for (Agent agent : agents)
		{
			if (agent instanceof Forager)
			{
				Forager forager = (Forager) agent;
				Reporter reporter = forager.getReporter();
				for (SummaryMetric metric : metricsToReport)
				{
					results[i] = Double.toString(reporter.getSummaryMetic(metric));
					i++;
				}

				int[] qualityBins = reporter.getQualityBins();
				for (int b = 0; b < qualityBins.length; b++)
				{
					results[i] = Double.toString(qualityBins[b]);
					i++;
				}

				// for optimization
//				writeToFile(new String[] { Double.toString(reporter.getTotalConsumption()) }, totalConsumptionFile, true);
			}
		}
		
		writeToFile(results, resultsFile, true);
		
		if (Parameters.get().getSaveTracks())
		{
			reportTracks();
			reportPredators();
		}
	}
	
	public void reportTracks() 
	{
		File tracksFile = new File(tracksFolder, "Tracks" + ModelEnvironment.getSimulationIndex() + ".csv");
		
		List<String[]> results = new ArrayList<String[]>();
		results.add(new String[] { "id", "x", "y", "behavior", "consumption", "mu" });
		
		// foragers
		for (Agent agent : agents)
		{
			if (agent instanceof Forager)
			{
				Forager forager = (Forager) agent;
				int id = forager.getId();
				Reporter reporter = forager.getReporter();
				List<NdPoint> track = reporter.getLocationHistory();
				List<BehaviorState> states = reporter.getStateHistory();
				List<Double> consumption = reporter.getConsumptionHistory();
				List<Velocity> mus = reporter.getMuHistory();
				
				for (int i = 0; i < track.size(); i++) // assume track, states, and consumption are same length
				{
					// forager id, x, y (could add more like state, consumption, etc)
					String[] line = new String[] {
							Integer.toString(id), 
							Double.toString(track.get(i).getX()), 
							Double.toString(track.get(i).getY()),
							states.get(i).toString(),
							Double.toString(consumption.get(i)),
							Double.toString(mus.get(i).arg())}; // mu might not exist for other movement process
					results.add(line);
				}
			}
		}
		
		writeToFile(results, tracksFile, false);
	}

	public void reportPredators() 
	{
		File encountersFile = new File(tracksFolder, "Predators" + ModelEnvironment.getSimulationIndex() + ".csv");
		
		List<String[]> results = new ArrayList<String[]>();
		results.add(Predator.reportColumns());
		
		// foragers
		for (Agent agent : agents)
		{
			if (agent instanceof Forager)
			{
				Forager forager = (Forager) agent;
				Reporter reporter = forager.getReporter();
				List<Predator> predators = reporter.getAllPredators();
				
				for (Predator predator : predators)
				{
					results.add(predator.report());
				}
				// single set of predators, so just report for the first if there are multiple foragers
				break; 
			}
		}
		
		writeToFile(results, encountersFile, false);
	}

	private void writeToFile(String[] stringsToWrite, File fileToWrite, boolean append)
	{
		CSVWriter csvWriter = null;
		try
		{
			csvWriter = new CSVWriter(new FileWriter(fileToWrite, append), CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER);

			csvWriter.writeNext(stringsToWrite);
		} 
		catch (IOException e) 
		{
			throw new ForagingModelException("Exception writing to file: " + fileToWrite.getAbsolutePath(), e);
		}
		finally
		{
			if (csvWriter != null)
			{
				try 
				{
					csvWriter.close();
				} catch (IOException ignored) {}
			}
		}
		
	}
	
	private void writeToFile(List<String[]> stringsToWrite, File fileToWrite, boolean append)
	{
		CSVWriter csvWriter = null;
		try
		{
			csvWriter = new CSVWriter(new FileWriter(fileToWrite, append), CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER);

			csvWriter.writeAll(stringsToWrite);
		} 
		catch (IOException e) 
		{
			throw new ForagingModelException("Exception writing to file: " + fileToWrite.getAbsolutePath(), e);
		}
		finally
		{
			if (csvWriter != null)
			{
				try 
				{
					csvWriter.close();
				} catch (IOException ignored) {}
			}
		}
		
	}

	
	private void init()
	{
		if (!resultsFile.exists())
		{
			String[] header = new String[numColumns];
			int i = 0;
			
			header[0] = "SimulationIndex";
			i++;
			
			for (Parameter param : paramsToReport)
			{
				header[i] = param.name();
				i++;
			}
			for (Agent agent : agents)
			{
				if (agent instanceof Forager)
				{
					Forager forager = (Forager) agent;
					for (SummaryMetric metric : metricsToReport)
					{
						header[i] = String.format("%s%d", metric.name(), forager.getId());
						i++;
					}
					
					for (int b = 0; b < numQualityBins; b++)
					{
						header[i] = String.format("%dBin%d", forager.getId(), b);
						i++;
					}

				}
			}
			
			writeToFile(header, resultsFile, false);
		}
	}

	@Override
	public void execute(int currentInterval) 
	{
		reportSummaryResults();
	}

}
