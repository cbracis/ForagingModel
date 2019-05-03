package ForagingModel.input;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ForagingModel.core.ForagingModelException;

public class ResourceLandscapeReader 
{
	protected ResourceLandscapeReader() { }
	
	public List<CellData> readLandscapeFile(File input, int emptyBorderSize)
	{
		// file can be empty string to not read data from file
		if (input == null || input.getName().isEmpty())
		{
			return null;
		}
		
		List<CellData> resourceCells = new ArrayList<CellData>();
		
		BufferedReader reader = null;
		int nFileRows = 0;
		int nFileCols = 0;
		
		try 
		{
			reader = new BufferedReader(new FileReader(input));
			String line =  reader.readLine(); // skip first line
			nFileCols = line.split(",").length;
			
			// add empty top border now that we know number of columns
			for (int row = 0; row < emptyBorderSize; row++)
			{
				for (int col = 0; col < nFileCols + 2 * emptyBorderSize; col++) // empty columns on each side
				{
					resourceCells.add(CellData.create(row, col, 0));
				}
			}
			
			while ( (line = reader.readLine()) != null )
			{
				String[] fields = line.split(",");
				
				// each line should have same number of values
				assert(fields.length == nFileCols);
				
				// left empty column
				for (int col = 0; col < emptyBorderSize; col++)
				{
					resourceCells.add(CellData.create(nFileRows + emptyBorderSize, col, 0));
				}
				
				// nRows indexes x and col indexes y
				for (int col = 0; col < fields.length; col++)
				{
					double data = Double.parseDouble(fields[col]);
					resourceCells.add(CellData.create(nFileRows + emptyBorderSize, col + emptyBorderSize, data));
				}
				
				// right empty column
				for (int col = fields.length + emptyBorderSize; col < fields.length + 2 * emptyBorderSize; col++)
				{
					resourceCells.add(CellData.create(nFileRows + emptyBorderSize, col, 0));
				}

				nFileRows++;

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
				catch (Exception ignored) { }
			}
		}
		
		// now add empty bottom border 
		for (int row = nFileRows + emptyBorderSize; row < nFileRows + 2 * emptyBorderSize; row++)
		{
			for (int col = 0; col < nFileCols + 2 * emptyBorderSize; col++) // empty columns on each side
			{
				resourceCells.add(CellData.create(row, col, 0));
			}
		}

		
		assert(nFileRows == nFileCols);
		
		return resourceCells;
	}
	
}
