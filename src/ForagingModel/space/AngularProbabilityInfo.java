package ForagingModel.space;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.util.FastMath;

import ForagingModel.core.Angle;
import ForagingModel.core.GridPoint;
import ForagingModel.core.NdPoint;
import ForagingModel.core.Parameters;
import ForagingModel.core.Velocity;

public class AngularProbabilityInfo 
{
	private List<Double> angles;
	private int numPoints;
	private double spacing;
	private RealVector distances;
	
	private double minDimensionX;
	private double minDimensionY;
	private double maxDimensionX;
	private double maxDimensionY;
	
	private AngularProbabilityInfo( int numPoints, double spacing, int numAngles )
	{
		// make these parameters?
		this.numPoints = numPoints;
		this.spacing = spacing;
		
		this.minDimensionX = Parameters.get().getMinDimension();
		this.minDimensionY = Parameters.get().getMinDimension();
		this.maxDimensionX = Parameters.get().getMaxDimensionX();
		this.maxDimensionY = Parameters.get().getMaxDimensionY();
		
		angles = new ArrayList<Double>(numAngles);
		for (int i = 0; i < numAngles; i++)
		{
			// from 0 to 2 pi
			angles.add( i * 2 * Math.PI / numAngles );
		}
		
		distances = new ArrayRealVector(numPoints);
		for (int i = 0; i < numPoints; i++)
		{
			// spacing is the interval separating points
			// first point is distance spacing from location, so multiply by i + 1
			distances.setEntry(i, spacing * (i + 1));
		}
	}
	
	protected static AngularProbabilityInfo create(int numPoints, double spacing, int numAngles)
	{
		return new AngularProbabilityInfo(numPoints, spacing, numAngles);
	}
	
	public static AngularProbabilityInfo create()
	{
		// make these parameters?
		return create(350, 0.333, 360); // need to span long diagonal
	}
	
	public List<Double> getAngles()
	{
		return angles;
	}
	
	public int getNumPoints()
	{
		return numPoints;
	}
	
	public double getSpacing()
	{
		return spacing;
	}

	public List<GridPoint> getSamplePoints(NdPoint location, double angle) 
	{
		List<GridPoint> points = new ArrayList<GridPoint>(numPoints);
		Velocity delta = Velocity.createPolar(distances.getEntry(0), angle);
		double deltaX = delta.x();
		double deltaY = delta.y();
		
		double currentX = location.getX();
		double currentY = location.getY();
		
		for (int i = 0; i < numPoints; i++)
		{
			currentX += deltaX;
			currentY += deltaY;
			
			GridPoint point = new GridPoint((int)currentX, (int)currentY);
			if (SpaceUtils.inBounds(point, minDimensionX, minDimensionY, maxDimensionX, maxDimensionY))
			{
				points.add( point );
			}
			else
			{
				// rest of points will be out of bounds too
				break; 
			}
		}
		
		return points;
	}
	
	public RealVector getSampleDistances()
	{
		return distances.copy();
	}
	
	public int getIndexOf(Angle angle)
	{
		int index = (int)FastMath.floor(angle.get() / 2.0 / Math.PI * angles.size());
		return index;
	}
	
	@Override
	public String toString() 
	{
		return "[angles=" + angles.size() + "; numPoints="
				+ numPoints + "; spacing=" + spacing + "]";
	}
}
