package ForagingModel.predator;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import ForagingModel.core.NdPoint;
import ForagingModel.core.Parameters;
import ForagingModel.core.Parameters.Parameter;
import ForagingModel.core.TestUtilities;

public class PredatorManagerTest 
{
	@AfterMethod
	public void resetParameters()
	{
		Parameters.resetToDefaults();
	}
	
	@Test
	public void testGetActive()
	{
		int duration = 10;
		Predator pred1 = new Predator(3, duration, new NdPoint(2.3, 13.6));
		Predator pred2 = new Predator(5, duration, new NdPoint(4.3, -4.2));
		Predator pred3 = new Predator(13, duration, new NdPoint(23, 9.8));
		
		List<Predator> predators = new ArrayList<Predator>(Arrays.asList(new Predator[] {pred1, pred2, pred3 }));
		PredatorManager manager = PredatorFactory.createPredatorManager(predators);

		// set interval via execute before call
		manager.notifyInterval(1);
		Assert.assertEquals(manager.getActivePredators().size(), 0);

		manager.notifyInterval(3);
		Assert.assertEquals(manager.getActivePredators(), new HashSet<NdPoint>(Arrays.asList(pred1.getLocation())));

		manager.notifyInterval(5);
		Assert.assertEquals(manager.getActivePredators(), new HashSet<NdPoint>(Arrays.asList( pred1.getLocation(), pred2.getLocation() )));

		manager.notifyInterval(10);
		Assert.assertEquals(manager.getActivePredators(), new HashSet<NdPoint>(Arrays.asList( pred1.getLocation(), pred2.getLocation() )));

		manager.notifyInterval(13);
		Assert.assertEquals(manager.getActivePredators(), new HashSet<NdPoint>(Arrays.asList( pred2.getLocation(), pred3.getLocation() )));

		manager.notifyInterval(20);
		Assert.assertEquals(manager.getActivePredators(), new HashSet<NdPoint>(Arrays.asList( pred3.getLocation() )));

		manager.notifyInterval(30);
		Assert.assertEquals(manager.getActivePredators().size(), 0);

	}
	
	@Test
	public void testGetActiveRadius()
	{
		int duration = 10;
		Predator pred1 = new Predator(3, duration, new NdPoint(1, 1));
		Predator pred2 = new Predator(5, duration, new NdPoint(10, 10));
		Predator pred3 = new Predator(13, duration, new NdPoint(15, 15));
		
		List<Predator> predators = new ArrayList<Predator>(Arrays.asList(new Predator[] {pred1, pred2, pred3 }));
		PredatorManager manager = PredatorFactory.createPredatorManager(predators);

		manager.notifyInterval(1);
		Assert.assertEquals(manager.getActivePredators(new NdPoint(1.1, 1.1), 1).toArray(), 
				new NdPoint[] {});

		manager.notifyInterval(3);
		Assert.assertEquals(manager.getActivePredators(new NdPoint(1.1, 1.1), 1), 
				new HashSet<NdPoint>(Arrays.asList( pred1.getLocation() )));

		manager.notifyInterval(3);
		Assert.assertEquals(manager.getActivePredators(new NdPoint(10, 10), 1).size(), 0);
		
		manager.notifyInterval(5);
		Assert.assertEquals(manager.getActivePredators(new NdPoint(5, 5), 10), 
				new HashSet<NdPoint>(Arrays.asList( pred1.getLocation(), pred2.getLocation() )));

		manager.notifyInterval(5);
		Assert.assertEquals(manager.getActivePredators(new NdPoint(8, 8), 5), 
				new HashSet<NdPoint>(Arrays.asList( pred2.getLocation() )));

		manager.notifyInterval(13);
		Assert.assertEquals(manager.getActivePredators(new NdPoint(8, 8), 20), 
				new HashSet<NdPoint>(Arrays.asList( pred2.getLocation(), pred3.getLocation() )));

		manager.notifyInterval(13);
		Assert.assertEquals(manager.getActivePredators(new NdPoint(8, 8), 5), 
				new HashSet<NdPoint>(Arrays.asList( pred2.getLocation() )));
	}
	
	public void testGetActiveEmptyBorders() throws ParseException
	{
		double border = 5;
		TestUtilities.setParameter(Parameter.EmptyBorderSize, border);
		
		int duration = 100;
		NdPoint pred1Loc = new NdPoint(0.1, 0.1);
		NdPoint pred2Loc = new NdPoint(3, 25);

		List<Predator> predators = new ArrayList<Predator>(Arrays.asList(new Predator[] {new Predator(1, duration, pred1Loc), 
				new Predator(5, duration, pred2Loc) }));
		PredatorManager manager = PredatorFactory.createPredatorManager(predators);
		
		manager.notifyInterval(10);
		Assert.assertEquals(manager.getActivePredators(new NdPoint(7, 7), 5), 
				new HashSet<NdPoint>(Arrays.asList( new NdPoint(pred2Loc.getX() + border, pred2Loc.getY() + border))));

				Assert.assertEquals(manager.getActivePredators(), 
						new HashSet<NdPoint>(Arrays.asList( new NdPoint(pred1Loc.getX() + border, pred1Loc.getY() + border),
								new NdPoint(pred2Loc.getX() + border, pred2Loc.getY() + border))));

	}

}
