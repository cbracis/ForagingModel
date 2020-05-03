package ForagingModel.space;

import java.util.Set;
import java.util.concurrent.Callable;

import ForagingModel.core.NdPoint;

public class ScentHistoryUpdater implements Callable<Void> 
{
	private ScentHistory scentHistoryToUpdate;
	private Set<NdPoint> conspecificLocations;
	
	private ScentHistoryUpdater(ScentHistory scentHistoryToUpdate, Set<NdPoint> conspecificLocations)
	{
		this.scentHistoryToUpdate = scentHistoryToUpdate;
		this.conspecificLocations = conspecificLocations;
	}
	
	protected static ScentHistoryUpdater create(ScentHistory scentHistoryToUpdate, Set<NdPoint> conspecificLocations)
	{
		return new ScentHistoryUpdater(scentHistoryToUpdate, conspecificLocations);
	}

	@Override
	public Void call() throws Exception 
	{
		scentHistoryToUpdate.depositScent(conspecificLocations);
		return null;
	}

}

