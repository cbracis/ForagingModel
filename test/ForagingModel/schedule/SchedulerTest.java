package ForagingModel.schedule;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.mockito.InOrder;
import org.mockito.Mockito;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

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

		Mockito.verify(schedulable1_1, Mockito.times(expectedIntervals)).execute(Mockito.anyInt());
		Mockito.verify(schedulable1_2, Mockito.times(expectedIntervals)).execute(Mockito.anyInt());
		Mockito.verify(schedulable2_1, Mockito.times(expectedIntervals)).execute(Mockito.anyInt());
		Mockito.verify(endSchedulable1_1, Mockito.times(1)).execute(Mockito.anyInt());
		Mockito.verify(endSchedulable2_1, Mockito.times(1)).execute(Mockito.anyInt());
	}

	@Test
	public void callsSchedulablesInPriorityOrderAndThenByRegisteredOrder()
	{
		int numSteps = 5;
		double intervalSize = 1;

		Schedulable schedulable1_1 = Mockito.mock(Schedulable.class);
		Schedulable schedulable1_2 = Mockito.mock(Schedulable.class);
		Schedulable schedulable2_1 = Mockito.mock(Schedulable.class);
		Schedulable schedulable10_1 = Mockito.mock(Schedulable.class);
		Schedulable schedulable10_2 = Mockito.mock(Schedulable.class);
		Schedulable schedulable10_3 = Mockito.mock(Schedulable.class);
		Schedulable schedulable11_1 = Mockito.mock(Schedulable.class);
		Schedulable schedulable20_1 = Mockito.mock(Schedulable.class);
		Schedulable schedulable20_2 = Mockito.mock(Schedulable.class);

		Scheduler scheduler = new SchedulerImpl();
		scheduler.register(schedulable20_1, 20);
		scheduler.register(schedulable2_1, 2);
		scheduler.register(schedulable10_1, 10);
		scheduler.register(schedulable1_1, 1);
		scheduler.register(schedulable1_2, 1);
		scheduler.register(schedulable11_1, 11);
		scheduler.register(schedulable10_2, 10);
		scheduler.register(schedulable20_2, 20);
		scheduler.register(schedulable10_3, 10);

		scheduler.run(intervalSize, numSteps);

		InOrder inOrder = Mockito.inOrder(	schedulable1_1, schedulable1_2,
											schedulable2_1, schedulable10_1,
											schedulable10_2, schedulable10_3,
											schedulable11_1, schedulable20_1,
											schedulable20_2);

		for (int stepNum = 0; stepNum < numSteps; stepNum++)
		{
			inOrder.verify(schedulable1_1, Mockito.times(1)).execute(stepNum);
			inOrder.verify(schedulable1_2, Mockito.times(1)).execute(stepNum);
			inOrder.verify(schedulable2_1, Mockito.times(1)).execute(stepNum);
			inOrder.verify(schedulable10_1, Mockito.times(1)).execute(stepNum);
			inOrder.verify(schedulable10_2, Mockito.times(1)).execute(stepNum);
			inOrder.verify(schedulable10_3, Mockito.times(1)).execute(stepNum);
			inOrder.verify(schedulable11_1, Mockito.times(1)).execute(stepNum);
			inOrder.verify(schedulable20_1, Mockito.times(1)).execute(stepNum);
			inOrder.verify(schedulable20_2, Mockito.times(1)).execute(stepNum);
		}
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
		Mockito.verify(schedulableFirstRun, Mockito.times(numSteps)).execute(Mockito.anyInt());
		Mockito.reset(schedulableFirstRun);

		scheduler.register(schedulableSecondRun, 1);
		scheduler.run(intervalSize, numSteps);
		Mockito.verify(schedulableFirstRun, Mockito.times(0)).execute(Mockito.anyInt());
		Mockito.verify(schedulableSecondRun, Mockito.times(numSteps)).execute(Mockito.anyInt());
	}

	@Test
	public void abortRunCausesNothingToGetExecuted()
	{
		Schedulable schedulable = Mockito.mock(Schedulable.class);

		Scheduler scheduler = new SchedulerImpl();
		scheduler.register(schedulable, 1);

		scheduler.abortRun();

		scheduler.run(1.0, 5);

		Mockito.verify(schedulable, Mockito.times(0)).execute(Mockito.anyInt());
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

}
