package ForagingModel.space;

import ForagingModel.core.GridPoint;
import ForagingModel.core.NdPoint;
import ForagingModel.core.Parameters;

public class SpaceUtils 
{
	public static double getDistance(NdPoint point1, GridPoint point2)
	{
		// grid points give the coordinate of the lower left corner, but compute distance to grid square center by adding 0.5
		return Math.sqrt( Math.pow(point1.getX() - point2.getX() - 0.5, 2) + Math.pow(point1.getY() - point2.getY() - 0.5, 2) );
	}

	public static double getDistance(NdPoint point1, NdPoint point2)
	{
		return Math.sqrt( Math.pow(point1.getX() - point2.getX(), 2) + Math.pow(point1.getY() - point2.getY(), 2) );
	}

	public static GridPoint getGridPoint(NdPoint point)
	{
		// just need to floor (remove decimal) to get corresponding grid square
		return new GridPoint((int) point.getX(), (int) point.getY());
	}
	
	public static double[] toArray(NdPoint point)
	{
		return (point == null) ? null : new double[] { point.getX(), point.getY() };
	}
	
	public static boolean inBounds(NdPoint location, double minDimensionX, double minDimensionY, double maxDimensionX, double maxDimensionY)
	{
		boolean inBounds = true;
		
		if (location.getY() < minDimensionY	// bottom
			|| location.getY() > maxDimensionY	// top
			|| location.getX() < minDimensionX // left
			|| location.getX() > maxDimensionX) // right
		{
			inBounds = false;
		}
		return inBounds;
	}

	public static boolean inBounds(GridPoint location, double minDimensionX, double minDimensionY, double maxDimensionX, double maxDimensionY)
	{
		boolean inBounds = true;
		
		// for grid points, ok to equal min but must be strictly less than max
		
		if (location.getY() < minDimensionY	// bottom
			|| location.getY() >= maxDimensionY	// top
			|| location.getX() < minDimensionX // left
			|| location.getX() >= maxDimensionX) // right
		{
			inBounds = false;
		}
		return inBounds;
	}
	
	public static boolean inBounds(NdPoint location)
	{
		Parameters params = Parameters.get();
		return inBounds(location, params.getMinDimension(), params.getMinDimension(), params.getMaxDimensionX(), params.getMaxDimensionY());
	}
	
	public static boolean inBounds(GridPoint location)
	{
		Parameters params = Parameters.get();
		return inBounds(location, params.getMinDimension(), params.getMinDimension(), params.getMaxDimensionX(), params.getMaxDimensionY());
	}


}
