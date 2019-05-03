package ForagingModel.space;

import java.text.ParseException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ForagingModel.core.GridPoint;
import ForagingModel.core.NdPoint;
import ForagingModel.core.Parameters;
import ForagingModel.predator.PredatorManager;

public class PredatorMemoryTest 
{
	@AfterMethod
	public void resetParameters()
	{
		Parameters.resetToDefaults();
	}

	@Test
	public void testLearning()
	{
		NdPoint pred = new NdPoint(2.5, 3.5);
		GridPoint predPt = SpaceUtils.getGridPoint(pred);
		int gridSize = 10;
		PredatorManager predatorManager = Mockito.mock(PredatorManager.class);
		Mockito.when(predatorManager.getActivePredators(Mockito.any(NdPoint.class), Mockito.anyDouble()))
			.thenReturn(new HashSet<NdPoint>(Arrays.asList(new NdPoint[]{ pred })));
		RealMatrix memories = new Array2DRowRealMatrix(gridSize, gridSize);
		
		PredatorMemory predMemory = SpaceFactory.createPredatorMemory(memories, predatorManager, 
				AngularProbabilityInfo.create(), 1.0, 1.0, 1.0, 1.0, 1.0, 1.0);
		
		predMemory.learn(new NdPoint(1, 1));

		// System.out.println(predMemory.getMemory().toString());
		// max should be at pred location and decline away
		// note only checking along straight line out from pred location
		RealMatrix predMemValues = predMemory.getMemory();
		double currentMax = predMemValues.getEntry(predPt.getX(), predPt.getY());
		int x = predPt.getX();
		int y = predPt.getY();
		for (int i = 1; i < gridSize; i++)
		{
			double left = (x + i < gridSize) ? predMemValues.getEntry(x + i, y) : 0;
			double right = (x - i >= 0) ? predMemValues.getEntry(x - i, y) : 0;
			double top = (y + i < gridSize) ? predMemValues.getEntry(x, y + i) : 0;
			double bottom = (y - i >= 0) ? predMemValues.getEntry(x, y - i) : 0;

			Assert.assertTrue(currentMax > left || left == 0, 
					String.format("left %.2f less than %.2f for i=%d", left, currentMax, i));
			Assert.assertTrue(currentMax > right || right == 0,
					String.format("right %.2f less than %.2f for i=%d", right, currentMax, i));
			Assert.assertTrue(currentMax > top || top == 0,
					String.format("top %.2f less than %.2f for i=%d", top, currentMax, i));
			Assert.assertTrue(currentMax > bottom || bottom == 0,
					String.format("bottom %.2f less than %.2f for i=%d", bottom, currentMax, i));

			currentMax = Math.max(Math.max(left,  right), Math.max(top,  bottom));
		}
	}
	
	@Test
	public void testLearningMultiplePredators()
	{
		NdPoint pred1 = new NdPoint(5.2, 5.3);
		GridPoint predPt1 = SpaceUtils.getGridPoint(pred1);
		NdPoint pred2 = new NdPoint(1.1, 9.7);
		GridPoint predPt2 = SpaceUtils.getGridPoint(pred2);
		int gridSize = 10;
		PredatorManager predatorManager = Mockito.mock(PredatorManager.class);
		Mockito.when(predatorManager.getActivePredators(Mockito.any(NdPoint.class), Mockito.anyDouble()))
			.thenReturn(new HashSet<NdPoint>(Arrays.asList(new NdPoint[]{ pred1, pred2 })));
		RealMatrix memories = new Array2DRowRealMatrix(gridSize, gridSize);
		
		double encounterRadius = 0.5;
		PredatorMemory predMemory = SpaceFactory.createPredatorMemory(memories, predatorManager, 
				AngularProbabilityInfo.create(), 1.0, 1.0, 1.0, encounterRadius, 1.0, 1.0);
		
		predMemory.learn(new NdPoint(1, 1));

//		System.out.println(predMemory.getMemory().toString());
		
		RealMatrix predMemValues = predMemory.getMemory();
		double predVal1 = predMemValues.getEntry(predPt1.getX(), predPt1.getY());
		double predVal2 = predMemValues.getEntry(predPt2.getX(), predPt2.getY());

		// which is greater depends on if distance set to 0 within encounter radius
//		Assert.assertTrue(predVal1 > predVal2, "First predator closer to grid square center");
		Assert.assertEquals(predVal1, predVal2, 1e-10, "Predator values similar");
		
		for (int row = 0; row < gridSize; row ++)
		{
			for (int col = 0; col < gridSize; col++)
			{
				GridPoint point = new GridPoint(row, col);
				if (!point.equals(predPt1) && !point.equals(predPt2))
				{
					Assert.assertTrue(predMemValues.getEntry(row, col) < predVal2, 
							String.format("Point %s less than predator locations",  point.toString()));
				}
			}
		}
	}
	
	@Test
	public void testGetPredatorSafety()
	{
		int gridSize = 10;
		Parameters.get().setLandscapeSizeX(gridSize);
		Parameters.get().setLandscapeSizeY(gridSize);
		RealMatrix memories = new Array2DRowRealMatrix(gridSize, gridSize);
		memories.setEntry(3, 6, 0.5); // so along 4th angle of 8 has predator

		double encounterRadius = 0.5; // so nearby grid squares don't learn max
		PredatorManager predatorManager = Mockito.mock(PredatorManager.class);
		PredatorMemory predMemory = SpaceFactory.createPredatorMemory(memories, predatorManager, 
				AngularProbabilityInfo.create(10, 0.5, 8), 1.0, 1.0, 1.0, encounterRadius, 1.0, 1.0);
		
		RealVector probs = predMemory.getPredatorSafety(new NdPoint(5,5));
		
// previous, verify pdf away from predator
		//only 1 predator along 1 angle, so angle opposite (the last) should have all probability
//		Assert.assertEquals(probs.getEntry(7), 1.0, "move away from predator");
//		for (int i = 0; i < probs.getDimension() - 1; i++)

		//only 1 predator along one angle, so rest should be safe (=1)
		Assert.assertTrue(probs.getEntry(3) < 1.0, "move away from predator (not safe)");
		for (int i = 0; i < probs.getDimension(); i++)
		{
			if (i == 3) i++;
			Assert.assertEquals(probs.getEntry(i), 1.0, "all other directions safe (1), " + i);
		}

	}
	
	@Test
	public void aggregateValues() throws ParseException
	{
		// mock predator at top of landscape
		int gridSize = 50;
		Parameters.get().setLandscapeSizeX(gridSize);
		Parameters.get().setLandscapeSizeY(gridSize);
		NdPoint predLoc = new NdPoint(25.5, 46.5); 
		RealMatrix memories = new Array2DRowRealMatrix(gridSize, gridSize);
		PredatorManager predatorManager = Mockito.mock(PredatorManager.class);
		Mockito.when(predatorManager.getActivePredators(Mockito.any(NdPoint.class), Mockito.anyDouble())).thenReturn(new HashSet<NdPoint>(Arrays.asList(new NdPoint[] {predLoc})));

		
		AngularProbabilityInfo angInfo = AngularProbabilityInfo.create();
		double encounterRadius = 5;
		
		// predator is directly above forager, which angle is 90 deg?
		List<Double> angles = angInfo.getAngles();
		int directionAboveIdx = angles.indexOf(Math.PI / 2);
		
		// learn with one encounter, TODO learn more encounters
		// learn location just inside encounter radius of 5
		NdPoint learnLoc = new NdPoint(25.5, 42);

		double[] memValues = new double[] {1, 5, 10, 15, 20, 30};
		
		for (int m = 0; m < memValues.length; m++)
		{
			double memorySpatialScale = memValues[m];
			PredatorMemory predMemory = SpaceFactory.createPredatorMemory(memories, predatorManager, 
					angInfo, 10, 1.0, 1.0, encounterRadius, memorySpatialScale, 1.0);
			predMemory.learn(learnLoc);
			System.out.println("\n memorySpatialScale = " + memorySpatialScale);

			// now test prob. in direction of predator as well as to side for varying distances.
			for (int i = 0; i < 20; i ++)
			{
				double y = i * 2 + 2;
	//			NdPoint left = new NdPoint(predLoc.getX() - 2, y);
				NdPoint center = new NdPoint(predLoc.getX(), y);
	//			NdPoint right = new NdPoint(predLoc.getX() + 2, y);
				
	//			double leftValue = predMemory.getAngularProbabilities(left).getEntry(directionAboveIdx);
				double centerValue = predMemory.getPredatorSafety(center).getEntry(directionAboveIdx);
	//			double rightValue = predMemory.getAngularProbabilities(right).getEntry(directionAboveIdx);
				
	//			System.out.println("left  ," + left.getX() + "," + left.getY() + "," + leftValue);
				System.out.println("center, " + SpaceUtils.getDistance(predLoc, center) + ", " + centerValue);
	//			System.out.println("right ," + right.getX() + "," + right.getY() + "," + rightValue);
			}
		}
	
	}
	
	@Test(enabled = false, dataProvider = "learningRates")
	public void reportLearningForDifferentLearningRates(double learningRate)
	{
		NdPoint pred = new NdPoint(2.5, 3.5);
		GridPoint predPt = SpaceUtils.getGridPoint(pred);
		int gridSize = 10;
		PredatorManager predatorManager = Mockito.mock(PredatorManager.class);
		Mockito.when(predatorManager.getActivePredators(Mockito.any(NdPoint.class), Mockito.anyDouble()))
			.thenReturn(new HashSet<NdPoint>(Arrays.asList(new NdPoint[]{ pred })));
		RealMatrix memories = new Array2DRowRealMatrix(gridSize, gridSize);
		
		PredatorMemory predMemory = SpaceFactory.createPredatorMemory(memories, predatorManager, 
				AngularProbabilityInfo.create(), learningRate, 1.0, 1.0, 1.0, 1.0, 1.0);
		
		predMemory.learn(new NdPoint(1, 1));
		RealMatrix memory = predMemory.getMemory();
		// report memory at predator's location (should be same for all points in encounter radius)
		System.out.println("learning rate = " + learningRate + ", memory = " + memory.getEntry(predPt.getX(), predPt.getY()));
		
		// currently learning rate > 1 means memory value can also be > 1
		// encounters diminish with increased learning rates, though improvements are marginal greater than 20

	}
	
	@DataProvider
	public Object[][] learningRates() 
	{
		return new Object[][] {
				new Object[] { 1 },
				new Object[] { 2 },
				new Object[] { 5 },
				new Object[] { 10 },
				new Object[] { 20 },
				new Object[] { 50 },
				new Object[] { 100 },
				new Object[] { 1000 }
		};
	}


}
