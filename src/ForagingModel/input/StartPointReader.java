package ForagingModel.input;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ForagingModel.core.ForagingModelException;

import ForagingModel.core.NdPoint;

public class StartPointReader 
{
	protected StartPointReader() { }
	
	public List<NdPoint> readStartPointsFile(File input)
	{
		// file can be empty string to not read data from file
		if (input.getName().isEmpty())
		{
			return null;
		}
		
		List<NdPoint> startPoints = new ArrayList<NdPoint>();
		BufferedReader reader = null;
		
		try 
		{
			reader = new BufferedReader(new FileReader(input));
			String line =  reader.readLine(); // skip first line
			
			while ( (line = reader.readLine()) != null )
			{
				String[] fields = line.split(",");
				
				// each line should have x and y
				if (fields.length != 2)
				{
					throw new IllegalStateException("Each start point row should have x and y coordinates: " + line);
				}
				
				startPoints.add(new NdPoint(Double.parseDouble(fields[0]), Double.parseDouble(fields[1])));
			}
		}
		catch (FileNotFoundException e) 
		{
			throw new ForagingModelException("Could not open resource file " + input.toString(), e);
		} 
		catch (IOException e) 
		{
			throw new ForagingModelException("Error reading resource file " + input.toString(), e);
		}
		finally
		{
			if (reader != null)
			{
				try { reader.close(); }
				catch (IOException ignored) { }
			}
		}
		return startPoints;
	}
}
