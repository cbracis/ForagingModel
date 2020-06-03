package ForagingModel.output;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import ForagingModel.core.ForagingModelException;
import au.com.bytecode.opencsv.CSVWriter;

public class FileUtils 
{
	protected static void writeToFile(String[] stringsToWrite, File fileToWrite, boolean append)
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
	
	protected static void writeToFile(List<String[]> stringsToWrite, File fileToWrite, boolean append)
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

	protected static void writeToFile(String[] header, List<String[]> columns, File fileToWrite)
	{
		CSVWriter csvWriter = null;
		int nCols = header.length;
		int nRows = columns.get(0).length; // trust caller that there is at least 1 column and all same length
		
		try
		{
			csvWriter = new CSVWriter(new FileWriter(fileToWrite, false), CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER);

			csvWriter.writeNext(header);
			
			for (int i = 0; i < nRows; i++)
			{
				String[] line = new String[nCols];
				for (int j = 0; j < nCols; j++)
				{
					line[j] = columns.get(j)[i];
				}
				csvWriter.writeNext(line);
			}
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
}
