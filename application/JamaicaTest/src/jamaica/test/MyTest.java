package jamaica.test;

import javax.realtime.PeriodicParameters;
import javax.realtime.PriorityParameters;
import javax.realtime.PriorityScheduler;
import javax.realtime.RealtimeThread;
import javax.realtime.RelativeTime;

public class MyTest {

	public static void main(String[] args) {
		/* priority for new thread: min+10 */
		int pri =
		PriorityScheduler.instance().getMinPriority() + 20;
		System.out.println("Priority = "+pri);
		PriorityParameters prip = new PriorityParameters(pri);
		/* period: 20ms */
		final double ms=10;
		RelativeTime period =
		new RelativeTime((long) ms /* ms */, 0 /* ns */);
		/* release parameters for periodic thread: */
		PeriodicParameters perp =
		new PeriodicParameters(null, period, null, null, null, null);
		/* create periodic thread: */
		RealtimeThread rt = new RealtimeThread(prip, perp)
		{
			public void run()
			{
				int n=1;
				long start =System.nanoTime();
				int num = 1000;
				long times[] = new long[num]; 
				boolean running=true;
				while ( waitForNextPeriod() && running)
				{
					
					times[n]=System.nanoTime();
					n++;
					if(n==num){

						running=false;
					}else{
						//System.out.println("Hello "+n);
					}
				}
				if(running){
					System.out.println("Failed");
					System.exit(0);
				}
				System.out.println("Done");
				for(int i=1;i<num;i++){
					double interval = (double)(times[i]-(times[i-1]))/1000000.0;
					if(interval>ms*1.01 || interval <ms*.99)
						System.out.println("Hello "+i+" time = "+interval+" ms");
				}
			}
		};
		/* start periodic thread: */
		rt.start();
		System.out.println("Started test.");
	}

}
