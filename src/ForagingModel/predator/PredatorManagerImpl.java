package ForagingModel.predator;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ForagingModel.core.NdPoint;
import ForagingModel.core.Parameters;
import ForagingModel.space.SpaceUtils;

public class PredatorManagerImpl implements PredatorManager, Serializable
{
	private static final long serialVersionUID = 6777614874394584636L;

	private List<Predator> predators;
	private int currentInterval;
	
	
	protected PredatorManagerImpl(List<Predator> predators)
	{
		this.predators = predators;
		initialize();
	}

	@Override
	public Set<NdPoint> getActivePredators() 
	{
		Set<NdPoint> activePredators = new HashSet<NdPoint>();

		// dynamically adjust for border
		// note: reading directly from parameters instead of constructor since PredatorManager cached
		int emptyBorderSize = Parameters.get().getEmptyBorderSize();

		for (Predator predator : predators)
		{
			if (predator.isActive(currentInterval))
			{
				NdPoint adjustedPredLoc = new NdPoint(predator.getLocation().getX() + emptyBorderSize, predator.getLocation().getY() + emptyBorderSize);
				activePredators.add(adjustedPredLoc);
			}
		}
		return activePredators;
	}

	@Override
	public Set<NdPoint> getActivePredators(NdPoint consumerLocation, double encounterRadius) 
	{
		Set<NdPoint> predatorLocs = new HashSet<NdPoint>();
		
		// dynamically adjust for border
		// note: reading directly from parameters instead of constructor since PredatorManager cached
		int emptyBorderSize = Parameters.get().getEmptyBorderSize();
		
		for (Predator predator : predators)
		{
			NdPoint adjustedPredLoc = new NdPoint(predator.getLocation().getX() + emptyBorderSize, predator.getLocation().getY() + emptyBorderSize);
			if (SpaceUtils.getDistance(adjustedPredLoc, consumerLocation) < encounterRadius
				&& predator.isActive(currentInterval))
			{
				predatorLocs.add(adjustedPredLoc);
			}
		}
		
		return predatorLocs;
	}
	
	@Override
	public List<Predator> getAllPredators()
	{
		// for reporting
		return predators;
	}
	
	protected int getTotalNumberPredators()
	{
		return predators.size();
	}

	@Override
	public void notifyInterval(int currentInterval) 
	{
		this.currentInterval = currentInterval;
	}
	
	private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException 
	{
		in.defaultReadObject();
		initialize();
	}
	
	private void initialize()
	{
		currentInterval = 0;
	}

	@Override
	public void notifyTimeStep(int currentTimeStep) 
	{
		// do nothing
	}
}
