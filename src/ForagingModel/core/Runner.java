package ForagingModel.core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ForagingModel.core.ModelEnvironment.PredatorParamKey;
import ForagingModel.input.CellData;
import ForagingModel.input.InputFactory;
import ForagingModel.input.ResourceLandscapeReader;
import ForagingModel.predator.PredatorFactory;
import ForagingModel.predator.PredatorManager;
import ForagingModel.schedule.ScheduleFactory;
import ForagingModel.space.ResourceAssemblage;
import ForagingModel.space.SpaceFactory;

public class Runner 
{
	private final static Logger logger = LoggerFactory.getLogger(Runner.class);
	
	private Options options;
	private final String helpOpt = "help";
	private final String propertiesFileOpt = "propertiesFile";
	private final String logPropertiesFileOpt = "logPropertiesFile";
	private final String generatePredatorsOpt = "generatePredators";
	private final String reportPredatorsOpt = "reportPredators";
	private final String resumeOpt = "resume";
	private int simulationIndex;
	private int startIndex;
	private int numSkippedSimulations;
	
	private Runner()
	{
		options = createOptions();
		simulationIndex = 0;
		startIndex = 0;
		numSkippedSimulations = 0;
	}
	
	public void run(String[] args)
	{

		BasicConfigurator.configure();

		try 
		{
			CommandLineParser parser = new GnuParser();
		
			// parse the command line arguments
			CommandLine line = parser.parse(options, args);
			if (line.hasOption(helpOpt)) 
			{
				showHelp();
				System.exit(0);
			}
			
			String propertiesFile = null;
			if (line.hasOption(propertiesFileOpt))
			{
				propertiesFile = line.getOptionValue(propertiesFileOpt);
			}
			
			// log4j
//			if (line.hasOption(logPropertiesFileOpt))
//			{
//				// TODO: if specify log properties, tries to use for other properties too
//				propertiesFile = line.getOptionValue(logPropertiesFileOpt);
//
//				PropertyConfigurator.configure(propertiesFile);
//			}
//			else
//			{
//				BasicConfigurator.configure();
//			}
			
			if (line.hasOption(resumeOpt))
			{
				startIndex = Integer.parseInt(line.getOptionValue(resumeOpt));
				logger.info("Starting at simulation {}", startIndex);
			}

			// either generate predators or run simulations
			if (line.hasOption(generatePredatorsOpt))
			{
				generateAllPredators(propertiesFile);
			}
			else if (line.hasOption(reportPredatorsOpt))
			{
				String reportFile = line.getOptionValue(reportPredatorsOpt);
				reportPredators(reportFile);
			}
			else
			{
				runAllSimulations(propertiesFile);
			}
			
			
			
		} 
		catch (ParseException e) 
		{
			logger.error("Error parsing command line parameters", e);
		}
	}
	
	public static void main(String[] args)
	{
		new Runner().run(args);
	}
	
	private void runAllSimulations(String propertiesFile)
	{
		ParameterManager manager = CoreFactory.createParameterManager(propertiesFile);
		readPredatorCache();
		int repeatParamComboTimes = Parameters.get().getRepeatSimulation();
		
		for (@SuppressWarnings("unused") Parameters p : manager)
		{
			for (int repeatIdx = 0; repeatIdx < repeatParamComboTimes; repeatIdx++)
			{
				try
				{
					runSimulation();
				}
				catch (Exception e)
				{
					logger.error("Simulation " + simulationIndex + " skipped", e);
					numSkippedSimulations++;
				}
				finally
				{
					simulationIndex++;
				}
			}
		}
		
		if (numSkippedSimulations > 0)
		{
			logger.error("Skipped {} simulations total!!", numSkippedSimulations);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void readPredatorCache() 
	{
		File predatorCacheFile = Parameters.get().getPredatorCacheFile();
		
		if (predatorCacheFile.exists())
		{
			Map<PredatorParamKey,PredatorManager> predatorCache = null;
			
		   FileInputStream fileIn = null;
		   ObjectInputStream in = null;
			try
			{
				   fileIn = new FileInputStream(predatorCacheFile);
				   in = new ObjectInputStream(fileIn);
				   
				   predatorCache = (Map<PredatorParamKey,PredatorManager>) in.readObject();
				   ModelEnvironment.setPredatorCache(predatorCache);

				   in.close();
			}
			catch (IOException e)
			{
				logger.error("Error reading predator cache", e);
			}
			catch (ClassNotFoundException e)
			{
				logger.error("Error reading predator cache", e);
			}
			catch (SecurityException e)
			{
				logger.error("Error reading predator cache", e);
			}
			finally
			{
				try
				{
					if (in != null)
					{
						   in.close();
					}
					if (fileIn != null)
					{
						   fileIn.close();
					}
				}
				catch (IOException ignored) {}
			}
		}
	}

	private void generateAllPredators(String propertiesFile)
	{
		ParameterManager manager = CoreFactory.createParameterManager(propertiesFile);
		
		for (@SuppressWarnings("unused") Parameters p : manager)
		{
			try
			{
				generatePredators();
			}
			catch (Exception e)
			{
				logger.error("Error generating predators", e);
			}
		}
		
		// serialize
		Map<PredatorParamKey,PredatorManager> predatorCache = ModelEnvironment.getPredatorCache();
		
		try
		{
			FileOutputStream fileOut = new FileOutputStream(Parameters.get().getPredatorCacheFile());
			ObjectOutputStream outStream = new ObjectOutputStream(fileOut);
			outStream.writeObject(predatorCache);
			outStream.close();
			fileOut.close();
		}
		catch(IOException e)
		{
			logger.error("Error writing predator cache", e);
		}

	}
	
	private void runSimulation()
	{
		if (simulationIndex >= startIndex)
		{
			int maxRetries = 20;
			int retries = 0;
			for (; retries < maxRetries; retries++)
			{
				try
				{
					ModelEnvironment.setSimulationIndex(simulationIndex);
					ModelEnvironment.resetGenerator(); // create/load random seed per simulation
					ModelBuilder builder = new ModelBuilder();
					Model model = builder.build();
					model.run();
					break; // finished sim successfully
				}
				catch (RepeatedStepsException e)
				{
					logger.warn("Repeated steps in simulation {}, retries {}", simulationIndex, retries);
					logger.warn("  " + e.getMessage());
					if (!Parameters.get().getCreateRandomSeed())
					{
						logger.info("Not retrying since not generating random seed");
						break;
					}
				}
			}
			
			if (retries == maxRetries)
			{
				logger.error("Skipped simulation {} after exceeded retries for repeated steps", simulationIndex);
				numSkippedSimulations++;
			}
		}
	}
	
	private void generatePredators()
	{
		ResourceLandscapeReader reader = InputFactory.createResourceLandscapeReader();
		// generate predators with no border, then adjust later in PredatorManager
		List<CellData> resourceData = reader.readLandscapeFile(Parameters.get().getResourceLandscapeFile(), 0);
		ResourceAssemblage resources = SpaceFactory.generateResource(resourceData, ScheduleFactory.createNoOpScheduler());
		PredatorFactory.createPredatorManager(resources, ScheduleFactory.createNoOpScheduler());
	}
	
	private void reportPredators(String fileName)
	{
		readPredatorCache();
		Map<PredatorParamKey,PredatorManager> predatorCache = ModelEnvironment.getPredatorCache();
		
		BufferedWriter writer = null;
		try 
		{
			writer = new BufferedWriter(new FileWriter(fileName, false));
			
			for (PredatorParamKey key : predatorCache.keySet())
			{
				writer.write(key.toString());
				writer.newLine();
			}

		} 
		catch (IOException e) 
		{
			logger.error("Failed to report predator cache", e);
		}
		finally
		{
			if (writer != null)
			{
				try 
				{
					writer.close();
				} catch (IOException ignored) {} 
			}
		}
	}
	
	
	@SuppressWarnings("static-access")
	private Options createOptions()
	{
		Option help = new Option(helpOpt, "print this message");
		Option propertiesFile = OptionBuilder.withArgName( "file" )
											 .hasArg()
											 .withDescription(  "the file that specifies parameter values" )
											 .create( propertiesFileOpt );
		Option logPropertiesFile = OptionBuilder.withArgName( "file" )
				 								.hasArg()
				 								.withDescription(  "the file that specifies logging" )
				 								.create( logPropertiesFileOpt );
		Option generatePredators = new Option(generatePredatorsOpt, "generate predators but don't run simulations");
		Option reportPredators = OptionBuilder.withArgName( "file" )
				 							  .hasArg()
				 							  .withDescription(  "report predators in cache" )
				 							  .create( reportPredatorsOpt );
		Option resume = OptionBuilder.withArgName( "simulationIndex" )
									 .hasArg()
									 .withDescription(  "the index to start running simulations at" )
									 .create( resumeOpt );

	
		Options options = new Options();
		options.addOption(help);
		options.addOption(propertiesFile);
		options.addOption(logPropertiesFile);
		options.addOption(generatePredators);
		options.addOption(reportPredators);
		options.addOption(resume);
		
		return options;		
	}
	
	private void showHelp()
	{
	    HelpFormatter formatter = new HelpFormatter();
	    String header = "Run the ForagingModel, optionally specifying parameter values.\n";
	    formatter.printHelp(Runner.class.getName() + " options", header, options, "");
	}
}
