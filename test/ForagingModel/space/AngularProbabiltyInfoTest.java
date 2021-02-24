package ForagingModel.space;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.util.FastMath;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import au.com.bytecode.opencsv.CSVWriter;
import ForagingModel.core.Angle;
import ForagingModel.core.ForagingModelException;
import ForagingModel.core.GridPoint;
import ForagingModel.core.ModelEnvironment;
import ForagingModel.core.NdPoint;
import ForagingModel.core.NumberGenerator;
import ForagingModel.core.Parameters;
import ForagingModel.core.TestUtilities;
import ForagingModel.core.Velocity;
import ForagingModel.input.CellData;
import ForagingModel.input.InputFactory;
import ForagingModel.input.ResourceLandscapeReader;
import ForagingModel.predator.Predator;
import ForagingModel.predator.PredatorFactory;
import ForagingModel.predator.PredatorManager;
import ForagingModel.schedule.ScheduleFactory;
import ForagingModel.space.MemoryAssemblage.State;

public class AngularProbabiltyInfoTest 
{
	@BeforeClass
	public void ensureLandscape()
	{
		Parameters.resetToDefaults();
	}
	
	@Test
	public void testCreateAngles()
	{
		AngularProbabilityInfo apInfo = AngularProbabilityInfo.create(5, 1, 4);
		Assert.assertEquals(apInfo.getAngles(), 
				new ArrayList<Double>(Arrays.asList(new Double[] {0.0, Math.PI/2, Math.PI, 3 * Math.PI / 2})));
		
		
		int nAngles = 360;
		apInfo = AngularProbabilityInfo.create(5, 1, nAngles);
		List<Double> angles = apInfo.getAngles();
		Assert.assertEquals(angles.size(), nAngles, "number of angles created");
		Assert.assertEquals(angles.get(0), 0.0, "start at 0");
		Assert.assertTrue(angles.get(nAngles - 1) < 2 * Math.PI, "end before 2pi");
		double diff = angles.get(1) - angles.get(0);
		for (int i = 2; i < nAngles; i++)
		{
			Assert.assertEquals(angles.get(i) - angles.get(i - 1), diff, 1e-10, "same diff between angles");
		}
	}
	
	@Test
	public void testDistances()
	{
		AngularProbabilityInfo apInfo = AngularProbabilityInfo.create(5, 1.0, 4);
		RealVector expectedDistances = new ArrayRealVector(new double[] {1.0, 2.0, 3.0, 4.0, 5.0});
		Assert.assertEquals(apInfo.getSampleDistances(), expectedDistances);
		
		apInfo = AngularProbabilityInfo.create(1, 0.25, 4);
		expectedDistances = new ArrayRealVector(new double[] {0.25});
		Assert.assertEquals(apInfo.getSampleDistances(), expectedDistances);
		
		apInfo = AngularProbabilityInfo.create(10, 0.5, 4);
		expectedDistances = new ArrayRealVector(new double[] {0.5, 1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 4.5, 5.0});
		Assert.assertEquals(apInfo.getSampleDistances(), expectedDistances);
	}
	
	@Test(dataProvider = "angles")
	public void testGetAngleIndex(int numAngles, double angle, int expectedIndex)
	{
		AngularProbabilityInfo angInfo = AngularProbabilityInfo.create(10, 1.0, numAngles);
		Assert.assertEquals(angInfo.getIndexOf(new Angle(angle)), expectedIndex);
	}
	
	@DataProvider
	public Object[][] angles() 
	{
		return new Object[][] {
				new Object[] { 4, 0, 0 },
				new Object[] { 4, Math.PI / 2 - 0.1, 0 },
				new Object[] { 4, Math.PI / 2, 1 },
				new Object[] { 4, 2 * Math.PI - 0.0001, 3 },
				new Object[] { 360, 0, 0 },
				new Object[] { 360, Math.toRadians(1) + 0.00001, 1 }
		};
	}

	
	@Test
	public void testGetSamplePoints()
	{
		AngularProbabilityInfo apInfo = AngularProbabilityInfo.create(5, 1.0, 4);

		TestUtilities.compareList(apInfo.getSamplePoints(new NdPoint(1, 1), 0), 
				new ArrayList<GridPoint>(Arrays.asList(
						new GridPoint[] {new GridPoint(2, 1), new GridPoint(3, 1), new GridPoint(4, 1), new GridPoint(5, 1), new GridPoint(6, 1)})),
						"angle 0");
		TestUtilities.compareList(apInfo.getSamplePoints(new NdPoint(1, 1), Math.PI /2), 
				new ArrayList<GridPoint>(Arrays.asList(
						new GridPoint[] {new GridPoint(1, 2), new GridPoint(1, 3), new GridPoint(1, 4), new GridPoint(1, 5), new GridPoint(1, 6)})),
						"angle pi/2");
		TestUtilities.compareList(apInfo.getSamplePoints(new NdPoint(10, 10), Math.PI), 
				new ArrayList<GridPoint>(Arrays.asList(
						new GridPoint[] {new GridPoint(9, 10), new GridPoint(8, 10), new GridPoint(7, 10), new GridPoint(6, 10), new GridPoint(5, 10)})),
						"angle pi");
		TestUtilities.compareList(apInfo.getSamplePoints(new NdPoint(10.5, 10.5), 3 * Math.PI / 2), 
				new ArrayList<GridPoint>(Arrays.asList(
						new GridPoint[] {new GridPoint(10, 9), new GridPoint(10, 8), new GridPoint(10, 7), new GridPoint(10, 6), new GridPoint(10, 5)})),
						"angle 3pi/2");
	}
	
	@Test
	public void testGetSamplePointsCompareWithOldCode()
	{
		AngularProbabilityInfo apInfo = AngularProbabilityInfo.create();
		List<Double> angles = apInfo.getAngles();
		NdPoint location = new NdPoint(25.1, 25.2);
		RealVector distances = apInfo.getSampleDistances();
		int numPoints = apInfo.getNumPoints();
		
		for (int i = 0; i < angles.size(); i++)
		{
			double angle = angles.get(i);
			TestUtilities.compareList(apInfo.getSamplePoints(location, angle), getSamplePoints(location, angle, numPoints, distances), "angle " + angle);
		}

	}
	
	// this is the old version of get sample points to comparison test with that created too many NdPoints
	private List<GridPoint> getSamplePoints(NdPoint location, double angle, int numPoints, RealVector distances) 
	{
		List<GridPoint> points = new ArrayList<GridPoint>(numPoints);
		
		for (int i = 0; i < numPoints; i++)
		{
			Velocity vector = Velocity.createPolar(distances.getEntry(i), angle);
			GridPoint point = SpaceUtils.getGridPoint( vector.move(location) );
			if (SpaceUtils.inBounds(point))
			{
				points.add( SpaceUtils.getGridPoint( vector.move(location) ) );
			}
			else
			{
				// rest of points will be out of bounds too
				break; 
			}
		}
		
		return points;
	}
	
	// this test can be enabled to determine what are sufficient values to sample
	@Test(enabled = false)
	public void compareSamplePoints() throws URISyntaxException
	{
		// resource landscapes - 10
		// mean = -2, -1, 0, 1, 1.5, for scale = 5 and scale = 20
		List<String> landscapes = Arrays.asList("land41.csv", "land43.csv", "land45.csv", "land47.csv", "land48.csv",
												"land97.csv", "land99.csv", "land101.csv", "land103.csv", "land104.csv");
		
		// predators - 1-5 locs with 2 learning amounts
		List<NdPoint> predatorLocs = Arrays.asList(new NdPoint(1, 1), new NdPoint(15, 15), new NdPoint(40, 25), new NdPoint(20, 35), new NdPoint(26, 26));
		
		// forager locations
		// 5 systematic and 5 random
		List<NdPoint> foragerLocs = Arrays.asList(new NdPoint(25, 25), new NdPoint(25, 35), new NdPoint(25, 45), new NdPoint(35, 35), new NdPoint(45, 45),
												  new NdPoint(14.8,  22.2), new NdPoint(44.9,  23.5), new NdPoint(31.8,  3.4), new NdPoint(6.3,  47.8), new NdPoint(3.3, 14.7));
			
		// what sample points to compare, first is standard to compare rest against
		List<AngularProbabilityInfo> angProbInfos = Arrays.asList(AngularProbabilityInfo.create(5000, 0.01, 360), //first is standard to compare rest against
																	AngularProbabilityInfo.create(1000, 0.05, 360),
																	AngularProbabilityInfo.create(1000, 0.1, 360),
																	AngularProbabilityInfo.create(1000, 0.25, 360),
																	AngularProbabilityInfo.create(1000, 0.5, 360),
																	AngularProbabilityInfo.create(1000, 0.7, 360),
																	AngularProbabilityInfo.create(1000, 1.0, 360));
		
		double memorySpatialScale = 10;
		
		// comparison standard is first
		AngularProbabilityInfo standardInfo = angProbInfos.get(0);
		List<Double> resourceMse = new ArrayList<Double>();
		List<Double> predatorMse = new ArrayList<Double>();
		
		// stuff for writing out to file
		CSVWriter csvWriter = null;
		File resultsFile = new File("Anginfo.csv");
		try
		{
			csvWriter = new CSVWriter(new FileWriter(resultsFile, true), CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER);
			csvWriter.writeNext(new String[] {"memType", "landscape", "angInfo", "location", "mse"});
			
			// resource memory
			for (int i = 1; i < angProbInfos.size(); i++) // skip first
			{
				AngularProbabilityInfo info = angProbInfos.get(i);
				double totalMse = 0;
	
				for (String land : landscapes)
				{
					// read in, execute each
					URI filename = ClassLoader.getSystemResource(land).toURI();
					File inputFile = new File(filename);
					
					ResourceLandscapeReader reader = InputFactory.createResourceLandscapeReader();
					List<CellData> resourceData = reader.readLandscapeFile(inputFile, 0);
					
					ResourceAssemblage resources = SpaceFactory.generateResource(resourceData, ScheduleFactory.createNoOpScheduler());
					
					for (NdPoint foragerLoc : foragerLocs)
					{
						ResourceMemory resourceMemory = SpaceFactory.createResourceMemory(resources.getInitialMemory(true, 0), resources, standardInfo, 0, 0, 0, 0, 0, 0, 0, 0, memorySpatialScale, 0, 0);
						RealVector standardProbs = resourceMemory.getAngularProbabilities(foragerLoc);
					
						resourceMemory = SpaceFactory.createResourceMemory(resources.getInitialMemory(true, 0), resources, info, 0, 0, 0, 0, 0, 0, 0, 0, memorySpatialScale, 0, 0);
						RealVector comparisonProbs = resourceMemory.getAngularProbabilities(foragerLoc);
						double mse = computeMse(standardProbs, comparisonProbs);
						totalMse += mse;
						csvWriter.writeNext(new String[] {"resource", land, info.toString(), foragerLoc.toString(), Double.toString(mse)});
					}
	
				}
				resourceMse.add(totalMse);
			}
			
			// predator memory
			for (int i = 1; i < angProbInfos.size(); i++) // skip first
			{
				AngularProbabilityInfo info = angProbInfos.get(i);
				double totalMse = 0;
	
				List<Predator> predators = new ArrayList<Predator>();
				for (NdPoint predatorLoc : predatorLocs)
				{
					predators.add(Predator.create(0, 1000, predatorLoc));
					PredatorManager predatorMgr = PredatorFactory.createPredatorManager(predators);
					MemoryAssemblage baselinePredatorMemory = SpaceFactory.createPredatorMemory(predatorMgr);
					MemoryAssemblage baselinePredatorMoreMemory = SpaceFactory.createPredatorMemory(predatorMgr);
					for (Predator predator : predators)
					{
						baselinePredatorMemory.learn(predator.getLocation());
						
						for (int m = 0; m < 5; m++)
						{
							baselinePredatorMoreMemory.learn(predator.getLocation());
						}
					}
					
					List<MemoryAssemblage> baselinePredatorMemories = Arrays.asList(baselinePredatorMemory, baselinePredatorMoreMemory);
					
					for (NdPoint foragerLoc : foragerLocs)
					{
						for (MemoryAssemblage baseline : baselinePredatorMemories)
						{
							PredatorMemory predatorMemory = SpaceFactory.createPredatorMemory(new Array2DRowRealMatrix(baseline.reportCurrentState(State.Predators)), predatorMgr, standardInfo, 0, 0, 0, 0, memorySpatialScale, 0);
							RealVector standardProbs = predatorMemory.getAngularProbabilities(foragerLoc);
						
							predatorMemory = SpaceFactory.createPredatorMemory(new Array2DRowRealMatrix(baseline.reportCurrentState(State.Predators)), predatorMgr, info, 0, 0, 0, 0, memorySpatialScale, 0);
							RealVector comparisonProbs = predatorMemory.getAngularProbabilities(foragerLoc);
							double mse = computeMse(standardProbs, comparisonProbs);
							totalMse += mse;
							csvWriter.writeNext(new String[] {"predator", String.format("n=%d; b=%d", predators.size(), baselinePredatorMemories.indexOf(baseline)), 
															  info.toString(), foragerLoc.toString(), Double.toString(mse)});
						}
					}
				}
				predatorMse.add(totalMse);
			}
			System.out.println(resourceMse);
			System.out.println(predatorMse);
		
		} 
		catch (IOException e) 
		{
			throw new ForagingModelException("Exception writing to file: " + resultsFile.getAbsolutePath(), e);
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

	@Test(enabled = false)
	public void compareSamplePointsRandomly() throws URISyntaxException
	{

		// what sample points to compare, first is standard to compare rest against
		List<AngularProbabilityInfo> angProbInfos = Arrays.asList(AngularProbabilityInfo.create(10000, 0.01, 360), //first is standard to compare rest against
																	AngularProbabilityInfo.create(2000, 0.05, 360),
																	AngularProbabilityInfo.create(1000, 0.1, 360),
																	AngularProbabilityInfo.create(1000, 0.11, 360),
																	AngularProbabilityInfo.create(1000, 0.2, 360),
																	AngularProbabilityInfo.create(1000, 0.25, 360),
																	AngularProbabilityInfo.create(1000, 0.333, 360),
																	AngularProbabilityInfo.create(1000, 0.5, 360),
																	AngularProbabilityInfo.create(1000, 0.7, 360),
																	AngularProbabilityInfo.create(1000, 1.0, 360),
																	AngularProbabilityInfo.create(1000, 2.0, 360));
		
		double memorySpatialScale = 50;
		
		// comparison standard is first
		AngularProbabilityInfo standardInfo = angProbInfos.get(0);
		List<Double> resourceMse = new ArrayList<Double>(Collections.nCopies(angProbInfos.size(), 0.0));
		List<Double> predatorMse = new ArrayList<Double>(Collections.nCopies(angProbInfos.size(), 0.0));
		
		NumberGenerator random = ModelEnvironment.getNumberGenerator();
		double minLanscape = 0;
		double maxLandscape = 50;
		
		// stuff for writing out to file
		CSVWriter csvWriter = null;
		File resultsFile = new File("AnginfoRandom.csv");
		try
		{
			csvWriter = new CSVWriter(new FileWriter(resultsFile, true), CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER);
			csvWriter.writeNext(new String[] {"memType", "landscape", "angInfo", "location", "mse"});

			for (int iteration = 0; iteration < 1000; iteration++) // will take a loong time with 10000
			{
				// location
				NdPoint foragerLoc = new NdPoint(random.nextDoubleFromTo(minLanscape, maxLandscape), random.nextDoubleFromTo(minLanscape, maxLandscape));
				
				// resource
				long landIdx = random.nextIntFromTo(1, 2400);
				URI filename = ClassLoader.getSystemResource("land" + landIdx + ".csv").toURI();
				File inputFile = new File(filename);
				
				ResourceLandscapeReader reader = InputFactory.createResourceLandscapeReader();
				List<CellData> resourceData = reader.readLandscapeFile(inputFile, 0);
				
				ResourceAssemblage resources = SpaceFactory.generateResource(resourceData, ScheduleFactory.createNoOpScheduler());
				
				ResourceMemory resourceMemory = SpaceFactory.createResourceMemory(resources.getInitialMemory(true, 0), resources, standardInfo, 0, 0, 0, 0, 0, 0, 0, 0, memorySpatialScale, 0, 0);
				RealVector standardResourceProbs = resourceMemory.getAngularProbabilities(foragerLoc);

				// predator
				List<Predator> predators = new ArrayList<Predator>();
				long numPredators = random.nextIntFromTo(1, 10);
				long numLearning = random.nextIntFromTo(1, 5);

				for (int p = 0; p < numPredators; p++)
				{
					NdPoint predatorLoc = new NdPoint(random.nextDoubleFromTo(minLanscape, maxLandscape), random.nextDoubleFromTo(minLanscape, maxLandscape));
					predators.add(Predator.create(0, 1000, predatorLoc));
				}
				PredatorManager predatorMgr = PredatorFactory.createPredatorManager(predators);
				MemoryAssemblage baselinePredatorMemory = SpaceFactory.createPredatorMemory(predatorMgr);

				for (Predator predator : predators)
				{
					for (int l = 0; l < numLearning; l++)
					{
						baselinePredatorMemory.learn(predator.getLocation());
					}
				}

				PredatorMemory predatorMemory = SpaceFactory.createPredatorMemory(new Array2DRowRealMatrix(baselinePredatorMemory.reportCurrentState(State.Predators)), predatorMgr, standardInfo, 0, 0, 0, 0, memorySpatialScale, 0);
				RealVector standardPredatorProbs = predatorMemory.getAngularProbabilities(foragerLoc);

				// now test each spacing
				for (int i = 1; i < angProbInfos.size(); i++) // skip first
				{
					AngularProbabilityInfo info = angProbInfos.get(i);
					
					// resource	
					resourceMemory = SpaceFactory.createResourceMemory(resources.getInitialMemory(true, 0), resources, info, 0, 0, 0, 0, 0, 0, 0, 0, memorySpatialScale, 0, 0);
					RealVector comparisonProbs = resourceMemory.getAngularProbabilities(foragerLoc);
					double mse = computeMse(standardResourceProbs, comparisonProbs);
					resourceMse.set(i, resourceMse.get(i) + mse);
					csvWriter.writeNext(new String[] {"resource", "land" + landIdx, info.toString(), foragerLoc.toString(), Double.toString(mse)});
					
					// predator
					predatorMemory = SpaceFactory.createPredatorMemory(new Array2DRowRealMatrix(baselinePredatorMemory.reportCurrentState(State.Predators)), predatorMgr, info, 0, 0, 0, 0, memorySpatialScale, 0);
					comparisonProbs = predatorMemory.getAngularProbabilities(foragerLoc);
					mse = computeMse(standardPredatorProbs, comparisonProbs);
					predatorMse.set(i, predatorMse.get(i) + mse);
					csvWriter.writeNext(new String[] {"predator", String.format("n=%d; l=%d", numPredators, numLearning), 
													  info.toString(), foragerLoc.toString(), Double.toString(mse)});
				}
			}
			System.out.println(resourceMse);
			System.out.println(predatorMse);
		} 
		catch (IOException e) 
		{
			throw new ForagingModelException("Exception writing to file: " + resultsFile.getAbsolutePath(), e);
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


	private double computeMse(RealVector a, RealVector b)
	{
		double mse = 0;
		
		if (a.getDimension() != b.getDimension())
		{
			mse = -1;
		}
		else
		{
		
			for (int i = 0; i < a.getDimension(); i++)
			{
				mse += FastMath.pow(a.getEntry(i) - b.getEntry(i), 2);
			}
			mse = mse / a.getDimension();
		}
		return mse;
	}
	

}
