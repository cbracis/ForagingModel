package ForagingModel.core;

import org.apache.commons.math3.linear.RealVector;

public class DirectionProbabilityInfo 
{
	private DirectionProbabalistic direction;
	private RealVector foragingProbabilities;
	private RealVector attractiveScentProbabilities;
	private RealVector repulsiveScentValues;
	private RealVector predatorSafety;
	private RealVector aggregateProbabilities;
	
		
	public DirectionProbabilityInfo() {	}
	
	
	public void updateDirection(DirectionProbabalistic direction)
	{
		this.direction = direction;
	}
	
	public void updateForaging(RealVector foragingProbabilities)
	{
		this.foragingProbabilities = foragingProbabilities;
	}
	
	public void updateAttractiveScent(RealVector attractiveScentProbabilities)
	{
		this.attractiveScentProbabilities = attractiveScentProbabilities;
	}
	
	public void updateRepulsiveScent(RealVector repulsiveScentValues)
	{
		this.repulsiveScentValues = repulsiveScentValues;
	}

	public void updatePredator(RealVector predatorSafety)
	{
		this.predatorSafety = predatorSafety;
	}

	public void updateAggregate(RealVector aggregateProbabilities)
	{
		this.aggregateProbabilities = aggregateProbabilities;
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
	
	public RealVector attractiveScentProbabilities()
	{
		return attractiveScentProbabilities;
	}

	public RealVector repulsiveScentValues()
	{
		return repulsiveScentValues;
	}

	public RealVector predatorSafety()
	{
		return predatorSafety;
	}

	public RealVector aggregateProbabilities()
	{
		return aggregateProbabilities;
	}
	

	public void clear() 
	{
		this.direction = null;
		this.foragingProbabilities = null;
		this.attractiveScentProbabilities = null;
		this.repulsiveScentValues = null;
		this.predatorSafety = null;
		this.aggregateProbabilities = null;
	}


}
