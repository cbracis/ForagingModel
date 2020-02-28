package ForagingModel.schedule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.mockito.InOrder;
import org.mockito.Mockito;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.Assert;

public class SchedulerTest
{
	@DataProvider(name = "run.arg.provider")
	public Iterator<Object[]> provideRunArgData()
	{
		List<Object[]> data = new ArrayList<Object[]>();

		data.add(new Object[] { 1.0, 2000, 2000 });
		data.add(new Object[] { 2.0, 2000, 1000 });
		data.add(new Object[] { 0.5, 2000, 4000 });
		data.add(new Object[] { 0.1, 1000, 10000 });

		return data.iterator();
	}

	@Test(dataProvider = "run.arg.provider")
	public void callsSchedulablesTheCorrectNumberOfTimesWithRegisteredAtEndHappeningLast(	double intervalSize,
																							int numSteps,
																							int expectedIntervals)
	{
		Schedulable schedulable1_1 = Mockito.mock(Schedulable.class);
		Schedulable schedulable1_2 = Mockito.mock(Schedulable.class);
		Schedulable schedulable2_1 = Mockito.mock(Schedulable.class);
		Schedulable endSchedulable1_1 = Mockito.mock(Schedulable.class);
		Schedulable endSchedulable2_1 = Mockito.mock(Schedulable.class);

		Scheduler scheduler = new SchedulerImpl();
		scheduler.register(schedulable2_1, 2);
		scheduler.register(schedulable1_1, 1);
		scheduler.register(schedulable1_2, 1);
		scheduler.registerAtEnd(endSchedulable2_1, 2);
		scheduler.registerAtEnd(endSchedulable1_1, 1);

		scheduler.run(intervalSize, numSteps);

		Mockito.verify(schedulable1_1, Mockito.times(expectedIntervals)).execute(Mockito.anyInt(), Mockito.anyInt());
		Mockito.verify(schedulable1_2, Mockito.times(expectedIntervals)).execute(Mockito.anyInt(), Mockito.anyInt());
		Mockito.verify(schedulable2_1, Mockito.times(expectedIntervals)).execute(Mockito.anyInt(), Mockito.anyInt());
		Mockito.verify(endSchedulable1_1, Mockito.times(1)).execute(Mockito.anyInt(), Mockito.anyInt());
		Mockito.verify(endSchedulable2_1, Mockito.times(1)).execute(Mockito.anyInt(), Mockito.anyInt());
	}

	@Test
	public void callsSchedulablesInPriorityOrder()
	{
		int numSteps = 5;
		double intervalSize = 1;

		Schedulable schedulable1 = Mockito.mock(Schedulable.class);
		Schedulable schedulable2 = Mockito.mock(Schedulable.class);
		Schedulable schedulable10 = Mockito.mock(Schedulable.class);
		Schedulable schedulable11 = Mockito.mock(Schedulable.class);
		Schedulable schedulable20 = Mockito.mock(Schedulable.class);
		
		Scheduler scheduler = new SchedulerImpl();
		scheduler.register(schedulable20, 20);
		scheduler.register(schedulable2, 2);
		scheduler.register(schedulable10, 10);
		scheduler.register(schedulable1, 1);
		scheduler.register(schedulable11, 11);

		scheduler.run(intervalSize, numSteps);
		
		InOrder inOrder = Mockito.inOrder(schedulable1, schedulable2,
										  schedulable10, schedulable11,
										  schedulable20 );
		
		for (int stepNum = 0; stepNum < numSteps; stepNum++)
		{
			inOrder.verify(schedulable1, Mockito.times(1)).execute(Mockito.eq(stepNum), Mockito.anyInt());
			inOrder.verify(schedulable2, Mockito.times(1)).execute(Mockito.eq(stepNum), Mockito.anyInt());
			inOrder.verify(schedulable10, Mockito.times(1)).execute(Mockito.eq(stepNum), Mockito.anyInt());
			inOrder.verify(schedulable11, Mockito.times(1)).execute(Mockito.eq(stepNum), Mockito.anyInt());
			inOrder.verify(schedulable20, Mockito.times(1)).execute(Mockito.eq(stepNum), Mockito.anyInt());
		}
	}
	
	@Test
	public void callsSchedulablesRandomizedWithinInPriorityOrder()
	{
		int numSteps = 5;
		double intervalSize = 1;
		
		Recorder recorder = new Recorder();

		Schedulable schedulable10_1 = new RecordingSchedulable(1, recorder);
		Schedulable schedulable10_2 = new RecordingSchedulable(2, recorder);
		Schedulable schedulable10_3 = new RecordingSchedulable(3, recorder);
		
		Scheduler scheduler = new SchedulerImpl();
		scheduler.register(schedulable10_1, 10);
		scheduler.register(schedulable10_2, 10);
		scheduler.register(schedulable10_3, 10);

		scheduler.run(intervalSize, numSteps);
		
		List<Integer> intervals = recorder.reportRecordedIntervals();
		List<Integer> ids = recorder.reportRecordedIds();
		
		List<Integer> orderedIntervals = new ArrayList<Integer>(intervals.size());
		orderedIntervals.addAll(intervals);
		Collections.sort(orderedIntervals);
		Assert.assertEquals(intervals, orderedIntervals, "intervals called in order");
	
		//now need to test that ids are not always called in the same order
		// compare subsets of length 3 (the num of schedulables) for each of the 5 intervals
		List<Integer> interval_1 = ids.subList(0, 3);
		int numSame = 0;
		for (int i = 3; i < ids.size(); i+=3)
		{
			if (interval_1.equals(ids.subList(i, i+3)))
			{
				numSame++;
			}
		}
		Assert.assertTrue(numSame < 4, "Schedulables not always called in same order.");
	}
	
	@Test
	public void resetsRegisteredSchedulablesAfterRun()
	{
		int numSteps = 5;
		double intervalSize = 1;

		Schedulable schedulableFirstRun = Mockito.mock(Schedulable.class);
		Schedulable schedulableSecondRun = Mockito.mock(Schedulable.class);

		Scheduler scheduler = new SchedulerImpl();

		scheduler.register(schedulableFirstRun, 1);
		scheduler.run(intervalSize, numSteps);
		Mockito.verify(schedulableFirstRun, Mockito.times(numSteps)).execute(Mockito.anyInt(), Mockito.anyInt());
		Mockito.reset(schedulableFirstRun);

		scheduler.register(schedulableSecondRun, 1);
		scheduler.run(intervalSize, numSteps);
		Mockito.verify(schedulableFirstRun, Mockito.times(0)).execute(Mockito.anyInt(), Mockito.anyInt());
		Mockito.verify(schedulableSecondRun, Mockito.times(numSteps)).execute(Mockito.anyInt(), Mockito.anyInt());
	}

	@Test
	public void abortRunCausesNothingToGetExecuted()
	{
		Schedulable schedulable = Mockito.mock(Schedulable.class);

		Scheduler scheduler = new SchedulerImpl();
		scheduler.register(schedulable, 1);

		scheduler.abortRun();

		scheduler.run(1.0, 5);

		Mockito.verify(schedulable, Mockito.times(0)).execute(Mockito.anyInt(), Mockito.anyInt());
	}
	
	@Test(dataProvider = "run.arg.provider")
	public void notifiesIntervalsAndTimeSteps( double intervalSize,
											   int numSteps,
											   int expectedIntervals)
	{
	
		Notifiable notifiable = Mockito.mock(Notifiable.class);
		Scheduler scheduler = new SchedulerImpl();
		scheduler.register(notifiable);
		
		scheduler.run(intervalSize, numSteps);

		int expectedSteps = Math.min(numSteps, expectedIntervals); // will never have more steps notified than intervals (can have 2 steps per interval)
		Mockito.verify(notifiable, Mockito.times(expectedSteps)).notifyTimeStep(Mockito.anyInt());
		Mockito.verify(notifiable, Mockito.times(expectedIntervals)).notifyInterval(Mockito.anyInt());
	}
	
	private class RecordingSchedulable implements Schedulable
	{
		private int id;
		private Recorder recorder;
		
		private RecordingSchedulable(int id, Recorder recorder)
		{
			this.id = id;
			this.recorder = recorder;
		}

		@Override
		public void execute(int currentInterval, int priority) 
		{
			recorder.record(currentInterval, id);
		}	
	}
	
	private class Recorder
	{
		private List<Integer> intervals;
		private List<Integer> ids;
		
		private Recorder() 
		{
			intervals = new ArrayList<Integer>();
			ids = new ArrayList<Integer>();
		}
		
		public void record(int interval, int schedulableId)
		{
			intervals.add(interval);
			ids.add(schedulableId);
		}
		
		public List<Integer> reportRecordedIntervals()
		{
			return intervals;
		}
		
		public List<Integer> reportRecordedIds()
		{
			return ids;
		}
	}

}
