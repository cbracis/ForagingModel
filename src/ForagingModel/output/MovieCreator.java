package ForagingModel.output;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ForagingModel.core.ForagingModelException;
import ForagingModel.core.ModelEnvironment;
import ForagingModel.core.Parameters;
import ForagingModel.schedule.Schedulable;

public class MovieCreator implements Schedulable 
{
	private final static Logger logger = LoggerFactory.getLogger(MovieCreator.class);

	private File outputDir;
	private File fileList;
	private File movieFile;
	
	protected MovieCreator()
	{
		outputDir = new File(Parameters.get().getR_OutputPath());
		fileList = FileUtils.getFile(outputDir, "moviefilenames.dat");
		movieFile = FileUtils.getFile(outputDir, "movie" + ModelEnvironment.getSimulationIndex() + ".gif");
	}
	
	private void createMovie() 
	{
		writeFilesList();
		if (movieFile.exists())
		{
			File bak = new File(FilenameUtils.removeExtension(movieFile.getAbsolutePath()) + ".bak");
			movieFile.renameTo(bak);
		}

//		#! /bin/sh
//		# Pass two arguments to script
//		# 1: file listing all files to animate
//		# 2: resulting gif
//
//		convert -delay 5 @$1 $2
//		gifsicle --colors 256 -b -O2 $2

		
		Map<String, File> map = new HashMap<String, File>();
		map.put("fileList", fileList);
		map.put("movieFile", movieFile);
		CommandLine convertCmd = new CommandLine("convert");
		convertCmd.addArgument("-delay", false);
		convertCmd.addArgument("3", false);
		convertCmd.addArgument("@${fileList}");
		convertCmd.addArgument("${movieFile}");
		convertCmd.setSubstitutionMap(map);
		DefaultExecutor executor = new DefaultExecutor();
		ExecuteWatchdog watchdog = new ExecuteWatchdog(5 * 60 * 1000); // wait up to 5 min
		executor.setWatchdog(watchdog);
		try 
		{
			int exitValue = executor.execute(convertCmd);
			if (executor.isFailure(exitValue) && watchdog.killedProcess()) 
			{
			    logger.warn("Timeout: Failed to convert pdf's to gif.");
			}
			// stop doing gifsicle part, not working for some reason now
//			else
//			{
//				CommandLine gifsicleCmd = new CommandLine("gifsicle");
//				gifsicleCmd.addArgument("--colors");
//				gifsicleCmd.addArgument("256");
//				gifsicleCmd.addArgument("-b");
//				gifsicleCmd.addArgument("-O2");
//				gifsicleCmd.addArgument("${movieFile}");
//				gifsicleCmd.setSubstitutionMap(map);
//				exitValue = executor.execute(gifsicleCmd);
//				if (executor.isFailure(exitValue) && watchdog.killedProcess()) 
//				{
//				    logger.warn("Timeout: Failed to gifsicle.");
//				}
//
//			}
		} 
		catch (ExecuteException e) 
		{
			logger.warn("Failed to create gif.", e);
		} 
		catch (IOException e) 
		{
			logger.warn("IOException: Failed to create gif.", e);
		}
		
	}
	
	private void writeFilesList()
	{
		if (fileList.exists())
		{
			fileList.delete();
		}
		List<File> files = new ArrayList<File>(FileUtils.listFiles(outputDir, new WildcardFileFilter("plot*.pdf"), null));
		
		
		FileWriter writer = null;
		try
		{
			writer = new FileWriter(fileList, false);
			IOUtils.writeLines(files, IOUtils.LINE_SEPARATOR, writer);
		} catch (IOException e) 
		{
			throw new ForagingModelException("Error writing file list to create output movie", e);
		}
		finally
		{
			IOUtils.closeQuietly(writer);
		}
	}

	public void execute(int currentInterval, int priority)  
	{
		createMovie();
	}


}
