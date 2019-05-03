package ForagingModel.input;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import ForagingModel.core.Parameters;
import ForagingModel.schedule.ScheduleFactory;
import ForagingModel.space.SpaceFactory;

public class ResourceLandscapeReaderTest 
{
	
	@Test
	public void testReadFile() throws URISyntaxException
	{
		URI filename = ClassLoader.getSystemResource("ForagingModel/input/testResource.csv").toURI();
		File inputFile = new File(filename);
		
		ResourceLandscapeReader reader = InputFactory.createResourceLandscapeReader();
		List<CellData> data = reader.readLandscapeFile(inputFile, 0);
		
		Assert.assertEquals(data.size(), 4, "Number of cells in landscape");
		Assert.assertTrue(data.contains(CellData.create(0, 0, 0.1)), "x=0, y=0, K=0.1");
		Assert.assertTrue(data.contains(CellData.create(0, 1, 0.2)), "x=0, y=1, K=0.2");
		Assert.assertTrue(data.contains(CellData.create(1, 0, 0.001)), "x=1, y=0, K=0.001");
		Assert.assertTrue(data.contains(CellData.create(1, 1, 0.699)), "x=1, y=1, K=0.699");
		
		// create resource with data to check size
		SpaceFactory.generateResource(data, ScheduleFactory.createNoOpScheduler());
		Assert.assertEquals(Parameters.get().getLandscapeSizeX(), 2, "Landscape size");
		Assert.assertEquals(Parameters.get().getLandscapeSizeY(), 2, "Landscape size");
	}
	
	@Test
	public void testReadFileWithEmptyBorder() throws URISyntaxException
	{
		URI filename = ClassLoader.getSystemResource("ForagingModel/input/testResource.csv").toURI();
		File inputFile = new File(filename);
		int borderSize = 1;
		
		ResourceLandscapeReader reader = InputFactory.createResourceLandscapeReader();
		List<CellData> data = reader.readLandscapeFile(inputFile, borderSize);
		
		Assert.assertEquals(data.size(), 16, "Number of cells in landscape");
		Assert.assertTrue(data.contains(CellData.create(0, 0, 0)), "border");
		Assert.assertTrue(data.contains(CellData.create(0, 1, 0)), "border");
		Assert.assertTrue(data.contains(CellData.create(0, 2, 0)), "border");
		Assert.assertTrue(data.contains(CellData.create(0, 3, 0)), "border");

		Assert.assertTrue(data.contains(CellData.create(1, 0, 0)), "border");
		Assert.assertTrue(data.contains(CellData.create(1, 1, 0.1)), "file x=0, y=0, K=0.1");
		Assert.assertTrue(data.contains(CellData.create(1, 2, 0.2)), "file x=0, y=1, K=0.2");
		Assert.assertTrue(data.contains(CellData.create(1, 3, 0)), "border");

		Assert.assertTrue(data.contains(CellData.create(2, 0, 0)), "border");
		Assert.assertTrue(data.contains(CellData.create(2, 1, 0.001)), "file x=1, y=0, K=0.001");
		Assert.assertTrue(data.contains(CellData.create(2, 2, 0.699)), "file x=1, y=1, K=0.699");
		Assert.assertTrue(data.contains(CellData.create(2, 3, 0)), "border");

		Assert.assertTrue(data.contains(CellData.create(3, 0, 0)), "border");
		Assert.assertTrue(data.contains(CellData.create(3, 1, 0)), "border");
		Assert.assertTrue(data.contains(CellData.create(3, 2, 0)), "border");
		Assert.assertTrue(data.contains(CellData.create(3, 3, 0)), "border");

		// create resource with data to check size
		SpaceFactory.generateResource(data, ScheduleFactory.createNoOpScheduler());
		Assert.assertEquals(Parameters.get().getLandscapeSizeX(), 4, "Landscape size");
		Assert.assertEquals(Parameters.get().getLandscapeSizeY(), 4, "Landscape size");
	}
	
	@Test
	public void testReadFileWithLargeEmptyBorder() throws URISyntaxException
	{
		URI filename = ClassLoader.getSystemResource("ForagingModel/input/testResource.csv").toURI();
		File inputFile = new File(filename);
		int borderSize = 5;
		int totalSize = 12;
		
		ResourceLandscapeReader reader = InputFactory.createResourceLandscapeReader();
		List<CellData> data = reader.readLandscapeFile(inputFile, borderSize);
		
		Assert.assertEquals(data.size(), totalSize * totalSize, "Number of cells in landscape");
		
		for (CellData cell : data)
		{
			int row = cell.getX();
			int col = cell.getY();
			Assert.assertTrue(row >= 0 && row < totalSize, "Rows in range");
			Assert.assertTrue(col >= 0 && col < totalSize, "Cols in range");
			boolean midPoint = (row == 5 || row == 6) && (col == 5 || col == 6);

			if (midPoint)
			{
				Assert.assertTrue(cell.getCarryingCapacity() != 0, "non-border non-0");
			}
			else
			{
				Assert.assertTrue(cell.getCarryingCapacity() == 0, "border");
			}
		}
		
		// create resource with data to check size
		SpaceFactory.generateResource(data, ScheduleFactory.createNoOpScheduler());
		Assert.assertEquals(Parameters.get().getLandscapeSizeX(), totalSize, "Landscape size");
		Assert.assertEquals(Parameters.get().getLandscapeSizeY(), totalSize, "Landscape size");

	}
}
