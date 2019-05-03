package ForagingModel.output;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.mockito.Mockito;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REngine;
import org.testng.Assert;
import org.testng.annotations.Test;

import ForagingModel.agent.Forager;
import ForagingModel.core.NdPoint;
import ForagingModel.core.Parameters;
import ForagingModel.core.TestUtilities;
import ForagingModel.predator.PredatorManager;
import ForagingModel.space.LocationManager;
import ForagingModel.space.MemoryAssemblage;
import ForagingModel.space.ResourceAssemblage;

public class RVisualizerTest 
{
	// Make these tests not enabled by default since they will overwrite actual output
	@Test(enabled = false)
	public void testPlotIterationNoMemeory()
	{
		ResourceAssemblage resource = Mockito.mock(ResourceAssemblage.class);
		LocationManager locManager = Mockito.mock(LocationManager.class);
		RVisualizer visualizer = new RVisualizer(resource, locManager, 0);
		
		Mockito.when(resource.reportCurrentState()).thenReturn(new double[][] { {0, 0, 1, 2}, {0, 0.5, 0.5, 0.5}, {0, 1, 1, 1}, {0, 2, 2, 0.5} });
		Mockito.when(resource.getMaxQuality()).thenReturn(3.0);
		
		Mockito.when(locManager.getAgentLocations(Forager.class)).thenReturn(Arrays.asList( new NdPoint(1,1) ));
		
		visualizer.plotIteration(0);

		Mockito.when(locManager.getAgentLocations(Forager.class)).thenReturn(Arrays.asList( new NdPoint(2,2) ));

		visualizer.plotIteration(1);
	}

	@Test(enabled = false)
	public void testPlotIteration()
	{
		ResourceAssemblage resource = Mockito.mock(ResourceAssemblage.class);
		MemoryAssemblage memory = Mockito.mock(MemoryAssemblage.class);
		LocationManager locManager = Mockito.mock(LocationManager.class);
		RVisualizer visualizer = new RVisualizer(resource, locManager, 0);
		
		Mockito.when(resource.reportCurrentState()).thenReturn(new double[][] { {0, 0, 1, 2}, {0, 0.5, 0.5, 0.5}, {0, 1, 1, 1}, {0, 2, 2, 0.5} });
		Mockito.when(resource.getMaxQuality()).thenReturn(3.0);

		// TODO inject memory
		Mockito.when(memory.reportCurrentState()).thenReturn(new double[][] { {0, 0, 0.5, 2}, {-1, -1, 0.5, 0.5}, {0, 2, 2, -1}, {0, 1, 1, 0} });

		Mockito.when(locManager.getAgentLocations(Forager.class)).thenReturn(Arrays.asList( new NdPoint(1,1) ));
		
		visualizer.plotIteration(2);

		Mockito.when(locManager.getAgentLocations(Forager.class)).thenReturn(Arrays.asList( new NdPoint(2,2) ));

		visualizer.plotIteration(3);
	}

	@Test
	public void testSimpleRFunction() throws Exception
	{
		String rFilePath = Parameters.get().getRFilePath();
		
		REngine re = null;
		try
		{
			re = REngine.engineForClass("org.rosuda.REngine.JRI.JRIEngine");
	
		    re.parseAndEval("source(\"" + rFilePath + "\")");
		    
	        re.assign("iteration", new int[] { 1 });
	        
	        REXP result = re.parseAndEval("returnNumber(iteration)");
	        
	        Assert.assertEquals(result.asInteger(), 1, "Got back what put in");
	        
		}
		finally
		{
        	if (re != null)
        	{
        		re.close();
        	}
		}
	}
	
	@Test
	public void testCreatePredatorMatrix()
	{
		ResourceAssemblage resource = Mockito.mock(ResourceAssemblage.class);
		LocationManager locManager = Mockito.mock(LocationManager.class);
		PredatorManager predManager = Mockito.mock(PredatorManager.class);
		NdPoint foragerLoc = new NdPoint(1, 13);

		Mockito.when(locManager.getAgents(Forager.class)).thenReturn(
				new ArrayList<Forager>(Arrays.asList(new Forager[] { Mockito.mock(Forager.class) })));
		Mockito.when(predManager.getActivePredators()).thenReturn(new HashSet<NdPoint>());

		RVisualizer visualizer = new RVisualizer(resource, locManager,predManager, 0);
		
		Assert.assertEquals(visualizer.createPredatorMatrix(foragerLoc).length, 0, "empty array");
		
		NdPoint point1 = new NdPoint(0.2, 13.8); // encountered
		NdPoint point2 = new NdPoint(-5.6, 33.1); // not encountered
		Set<NdPoint> predators = new HashSet<NdPoint>(Arrays.asList(new NdPoint[]
				{ point1, point2 }));

		Mockito.when(predManager.getActivePredators()).thenReturn(predators);
		TestUtilities.compareMatrix(new Array2DRowRealMatrix(visualizer.createPredatorMatrix(foragerLoc)), 
				new Array2DRowRealMatrix(new double[][]{ {point2.getX(), point2.getY(), 0}, {point1.getX(), point1.getY(), 1} }), // 2 1 happens to be order of set
				1e-10);

	}
}
