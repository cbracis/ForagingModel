package ForagingModel.core;

import org.apache.commons.math3.linear.RealVector;

public class DirectionProbabilityInfo 
{
	private DirectionProbabalistic direction;
	private RealVector foragingProbabilities;
	private RealVector predatorSafety;
	private RealVector aggregateProbabilities;
	
	private double foragingSum;
	private double predatorSum;
	private double aggregateSum;
	
	public DirectionProbabilityInfo(DirectionProbabalistic direction, RealVector foragingProbabilities, RealVector predatorSafety, RealVector aggregateProbabilities,
			double foragingSum, double predatorSum, double aggregateSum)
	{
		this.direction = direction;
		this.foragingProbabilities = foragingProbabilities;
		this.predatorSafety = predatorSafety;
		this.aggregateProbabilities = aggregateProbabilities;
		
		this.foragingSum = foragingSum;
		this.predatorSum = predatorSum;
		this.aggregateSum = aggregateSum;
	}
	
	public DirectionProbabilityInfo() {	}
	
	
	public void updateDirection(DirectionProbabalistic direction)
	{
		this.direction = direction;
	}
	
	public void updateForaging(RealVector foragingProbabilities, double foragingSum)
	{
		this.foragingProbabilities = foragingProbabilities;
		this.foragingSum = foragingSum;
	}
	
	public void updatePredator(RealVector predatorSafety, double predatorSum)
	{
		this.predatorSafety = predatorSafety;
		this.predatorSum = predatorSum;
	}
	
	public void updateAggregate(RealVector aggregateProbabilities, double aggregateSum)
	{
		this.aggregateProbabilities = aggregateProbabilities;
		this.aggregateSum = aggregateSum;
	}
	
	public boolean isNull()
	{
		// true if all 3 probabilities are null
		return foragingProbabilities == null && predatorSafety == null && aggregateProbabilities == null;
	}

	
	public Angle angle()
	{
		return (direction == null) ? null : direction.angle();
	}
	
	public double getEMD()
	{
		return (direction == null) ? null : direction.scaledEarthMoversDistance();
	}
	
	public RealVector foragingProbabilities()
	{
		return foragingProbabilities;
	}
	
	public RealVector predatorSafety()
	{
		return predatorSafety;
	}
	
	public RealVector aggregateProbabilities()
	{
		return aggregateProbabilities;
	}
	
	public double foragingSum()
	{
		return foragingSum;
	}
	
	public double predatorSum()
	{
		return predatorSum;
	}

	public double aggregateSum()
	{
		return aggregateSum;
	}

	public void clear() 
	{
		this.direction = null;
		this.foragingProbabilities = null;
		this.predatorSafety = null;
		this.aggregateProbabilities = null;
		
		this.foragingSum = 0;
		this.predatorSum = 0;
		this.aggregateSum = 0;
	}


}
