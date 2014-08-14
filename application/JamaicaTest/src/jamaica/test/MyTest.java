package jamaica.test;

import javax.realtime.AbsoluteTime;
import javax.realtime.Clock;
import javax.realtime.PriorityScheduler;
import javax.realtime.PriorityParameters;
import javax.realtime.PeriodicParameters;
import javax.realtime.RelativeTime;
import javax.realtime.RealtimeThread;

import com.neuronrobotics.sdk.common.BowlerAbstractConnection;
//import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.network.UDPBowlerConnection;

public class MyTest {

	public static void main(String[] args) {
		final Clock clock = Clock.getRealtimeClock();
		RelativeTime resolution = clock.getResolution();
		long ms = clock.getTime().getMilliseconds();
		int ns = clock.getTime().getNanoseconds();
		double usResolution = (double) ((resolution.getMilliseconds() * 1000000) + resolution
				.getNanoseconds()) / 1000.0;
		System.out.println("Got 'ms: " + ms + "' - 'ns: " + ns
				+ "'  resoulution = " + usResolution);
		if (ms < 0 || ns < 0 || ns > 999999 || usResolution > 1) {
			System.out
					.println("Absolute time values are incorrect. Please set the system clock!");
			System.out.println("Got 'ms: " + ms + "' - 'ns: " + ns
					+ "'  resoulution = " + usResolution);
			System.exit(1);
		}

		/* priority for new thread: mininum+10 */
		int priority = PriorityScheduler.instance().getMaxPriority();
		PriorityParameters priortyParameters = new PriorityParameters(priority);
		final long periodTime = Long.parseLong(args[0]);
		final long bound = Long.parseLong(args[1]);
		final long size = Long.parseLong(args[2]);
		/* period: 1ms */
		RelativeTime period = new RelativeTime(periodTime /* ms */, 0 /* ns */);

		/* release parameters for periodic thread: */
		PeriodicParameters periodicParameters = new PeriodicParameters(null,period, null, null, null, null);
		BowlerAbstractConnection.setUseThreadedStack(false);
		final RealTimeDevice device = new RealTimeDevice();
		//Log.enableInfoPrint();
		/* create periodic thread: */
		RealtimeThread realtimeThread = new RealtimeThread(priortyParameters,periodicParameters) {
			private AbsoluteTime time = new AbsoluteTime();
			private AbsoluteTime inital = new AbsoluteTime();

			public void run() {
				long start, last = clock.getTime().getMilliseconds();
				start = last;

				long data[] = new long[(int) size];
				clock.getTime(inital);
				System.out.println("Starting");
				for (int n = 0; n < size; n++) {

					waitForNextPeriod();
					device.fastPushTest();
					clock.getTime(time);
					last = start;
					start = ((time.getNanoseconds() - inital.getNanoseconds() + ((time
							.getMilliseconds() - inital.getMilliseconds()) * 1000000))) / 1000;
					data[n] = (start - last);

				}
				System.out.println("Done");
				int fail = 0;
				for (int n = 1; n < size; n++) {
					long ms = data[n];
					if (ms > (periodTime * 1000) + bound
							|| (ms < (periodTime * 1000) - bound)) {
						System.out.println(n + " Hello "
								+ (periodTime * 1000 * 100) / ms);
						fail++;
					}

				}
				System.out.println("Failed " + fail + " times");
			}
		};
		/* start periodic thread: */
		realtimeThread.start();
	}

}
