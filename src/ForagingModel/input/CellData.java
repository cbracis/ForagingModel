package ForagingModel.input;


public class CellData 
{
	private int x;
	private int y;
	private double carryingCapacity;
	
	private CellData(int x, int y, double carryingCapacity)
	{
		this.x = x;
		this.y = y;
		this.carryingCapacity = carryingCapacity;
	}
	
	public static CellData create(int x, int y, double carryingCapacity)
	{
		return new CellData(x, y, carryingCapacity);
	}
	
	public int getX() 
	{
		return x;
	}

	public int getY() 
	{
		return y;
	}

	public double getCarryingCapacity() 
	{
		return carryingCapacity;
	}

	@Override
	public String toString() 
	{
		return String.format("Cell[x=%d, y=%d, K=%f]",
				x, y, carryingCapacity);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CellData other = (CellData) obj;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		if (Double.doubleToLongBits(carryingCapacity) != Double.doubleToLongBits(other.carryingCapacity))
			return false;
		return true;
	}

}
