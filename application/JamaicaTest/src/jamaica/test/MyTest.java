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
		try {
			final long periodTime = Long.parseLong(args[0]);// Period in MS
			final long bound = Long.parseLong(args[1]);// percent jitter
			final long size = Long.parseLong(args[2]);// How many iterations

			/* period: 1ms */
			RelativeTime period = new RelativeTime(periodTime /* ms */, 0 /* ns */);

			/* release parameters for periodic thread: */
			PeriodicParameters periodicParameters = new PeriodicParameters(
					null, period, null, null, null, null);
			BowlerAbstractConnection.setUseThreadedStack(false);
			final RealTimeDevice device = new RealTimeDevice();
			// Log.enableInfoPrint();
			/* create periodic thread: */
			RealtimeThread realtimeThread = new RealtimeThread(
					priortyParameters, periodicParameters) {
				private AbsoluteTime inital = new AbsoluteTime();

				public void run() {
					long start, last = clock.getTime().getMilliseconds();
					start = last;

					AbsoluteTime data[] = new AbsoluteTime[(int) size];
					for (int i = 0; i < data.length; i++) {
						data[i] = new AbsoluteTime();
					}
					clock.getTime(inital);
					System.out.println("Starting");
					for (int n = 0; n < size; n++) {

						waitForNextPeriod();
						clock.getTime(data[n]);
						device.fastPushTest();

					}
					System.out.println("Done");
					int fail = 0;
					RelativeTime difference = new RelativeTime();
					for (int n = 1; n < size; n++) {
						data[n].subtract(data[n - 1], difference);
						double timeInUs = (double) difference.getMilliseconds()
								* 1000.0 + (double) difference.getNanoseconds()
								/ 1000.0;
						double period = (double) periodTime * 1000.0;
						int percent = (int) (((period - timeInUs) / period) * 100.0);
						if (percent > bound || percent < (-bound)) {
							System.out.println("Packet #" + n + " difference="
									+ (int) timeInUs + "us " + percent + "%");
							fail++;
						}
					}
					System.out.println("Failed " + fail + " times");
					System.exit(0);
				}
			};
			/* start periodic thread: */
			realtimeThread.start();
		} catch (Exception ex) {
			System.out.println("Usage: <MS period> <percent allowable error> <number of packets>");
			System.exit(0);
		}
	}

}
