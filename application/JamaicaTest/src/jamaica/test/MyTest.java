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
		int priority = 10;
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
			
			final RealTimeDevice device = new RealTimeDevice();
			// Log.enableInfoPrint();
			/* create periodic thread: */
			RealtimeThread realtimeThread = new RealtimeThread(
					priortyParameters, periodicParameters) {

				public void run() {

					AbsoluteTime data[] = new AbsoluteTime[(int) size];
					AbsoluteTime completed[] = new AbsoluteTime[(int) size];
					for (int i = 0; i < data.length; i++) {
						data[i] = new AbsoluteTime();
						completed[i] = new AbsoluteTime();
					}
					
					System.out.println("Starting Period = "+periodTime+"ms Percent Jitter = "+bound+" for "+ size+" iterations");
					device.fastPushTest();
					for (int n = 0; n < size; n++) {

						waitForNextPeriod();
						//clock.getTime(data[n]);
						device.fastPushTest();
						//clock.getTime(completed[n]);
						if (device.getLastResponse() == null) {
							throw new RuntimeException("No response");
						}

					}
					System.out.println("Done");
					int fail = 0;
					RelativeTime difference = new RelativeTime();
					RelativeTime exec = new RelativeTime();
					double period = (double) periodTime * 1000.0;
					for (int n = 1; n < size; n++) {
						completed[n].subtract(data[n], exec);
						
						data[n].subtract(data[0], difference);
						double timeInUs = ((double) difference.getMilliseconds()
								* 1000.0) + ((double) difference.getNanoseconds()
								/ 1000.0);
						int expected = (int) (period * n);
						
						double differenceElapsed = (expected - timeInUs);
						
						
						int percent = (int) ((differenceElapsed/ period) * 100.0);
						
						if (percent > bound || percent < (-bound)) {
							System.out.println("Packet #" + n + " elapsed="
									+ difference + " , expected="+(int)periodTime*n+"ms, difference="+difference+", " + percent + " %"+" send took ="+ exec);
							fail++;
						}else{
							System.out.println("Packet #" + n + 
									" took ="+ exec);
						}
					}
					System.out.println("Failed " + fail + " times");
					System.out.println("Period = "+periodTime+"ms Percent Jitter = "+bound+" for "+ size+" iterations");
					System.exit(0);
				}
			};
			/* start periodic thread: */
			realtimeThread.start();
		} catch (Exception ex) {
			System.out
					.println("Usage: <MS period> <percent allowable error> <number of packets>");
			System.exit(0);
		}
	}

}
