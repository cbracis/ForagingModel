package ForagingModel.space;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ForagingModel.agent.Agent;
import ForagingModel.core.ForagingModelException;
import ForagingModel.core.NdPoint;
import ForagingModel.schedule.Schedulable;
import ForagingModel.schedule.SchedulePriority;

public class ScentManager implements Schedulable 
{
	private LocationManager locManager;
	private Map<Agent,ScentHistory> scentHistories;
	private ExecutorService executor;
	
	private ScentHistory scentHistoryToAdd;
	
	protected ScentManager(LocationManager locationManager, int numThreads)
	{
		this.locManager = locationManager;
		this.scentHistories = new HashMap<Agent, ScentHistory>();
		this.scentHistoryToAdd = null;
		
        this.executor = (ExecutorService) Executors.newFixedThreadPool(numThreads);

	}

	@Override
	public void execute(int currentInterval, int priority) 
	{
		switch (SchedulePriority.fromValue(priority))
		{
		case ForagerDepositScent:
			depositScent();
			break;
		case Shutdown:
			shutdown();
			break;
		default:
			throw new UnsupportedOperationException();
		}
	}
	
	private void depositScent()
	{
		//here need to call all ScentHistories to update
		// assumption that getting all agents from scentHistories or locationManager is same...
		Set<Agent> keys = scentHistories.keySet();
		List<Agent> agents = locManager.getAgents();
		
		// here is the list of tasks to give the thread pool
		List<Callable<Void>> taskList = new ArrayList<Callable<Void>>();
		
		// first we're going to iterate the 
		for (Agent key : keys)
		{
			ScentHistory scentsToUpdate = scentHistories.get(key);
			Set<NdPoint> conspecifics = new HashSet<NdPoint>();
			
			for (Agent agent : agents)
			{
				// forager doesn't avoid own scent
				if (agent != key)
				{
					conspecifics.add(locManager.getLocation(agent));
				}
			}
			
			taskList.add(ScentHistoryUpdater.create(scentsToUpdate, conspecifics));
			//scentsToUpdate.depositScent(conspecifics);			
		}
		try 
		{
            executor.invokeAll(taskList);
        } catch (InterruptedException e) 
        {
            throw new ForagingModelException("Threading problems, oh no :|", e);
        }

	}
	
	private void shutdown()
	{
		executor.shutdown();
	}
	
	/**
	 * Adds a ScentHistory to be managed. Important: this must be followed by a call
	 * to add the corresponding Agent.
	 * @param scentHistory ScentHistory to be managed
	 */
	public void add(ScentHistory scentHistory)
	{
		if (scentHistoryToAdd != null)
		{ 
			throw new ForagingModelException("Unadded ScentHistory abandoned.");
		}
		scentHistoryToAdd = scentHistory;
	}
	
	/**
	 * Adds the Agent who corresponds to the ScentHistory just added. This means that the 
	 * scents for all conspecifics to this Agent will be added to the ScentHistory.
	 * @param agent Agent corresponding to the just-added ScentHistory
	 */
	public void add(Agent agent)
	{
		if (scentHistoryToAdd == null)
		{ 
			throw new ForagingModelException("No current ScentHistory to add.");
		}
		
		scentHistories.put(agent, scentHistoryToAdd);
		scentHistoryToAdd = null;	
	}
	
}
