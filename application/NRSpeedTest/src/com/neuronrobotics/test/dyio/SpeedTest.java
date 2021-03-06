package com.neuronrobotics.test.dyio;

import com.neuronrobotics.sdk.common.BowlerAbstractConnection;
import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.dyio.DyIO;
import com.neuronrobotics.sdk.dyio.DyIOChannelMode;
import com.neuronrobotics.sdk.dyio.peripherals.DigitalInputChannel;
import com.neuronrobotics.sdk.dyio.peripherals.DigitalOutputChannel;
import com.neuronrobotics.sdk.dyio.peripherals.PPMReaderChannel;
import com.neuronrobotics.sdk.dyio.peripherals.ServoChannel;
import com.neuronrobotics.sdk.serial.SerialConnection;
import com.neuronrobotics.sdk.ui.ConnectionDialog;

public class SpeedTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//Log.enableDebugPrint(true);
		DyIO.disableFWCheck();
		BowlerAbstractConnection c =null;
//		BowlerAbstractConnection c =  new SerialConnection("/dev/DyIO0")
//		BowlerAbstractConnection c =  new SerialConnection("COM65")
		if(args.length !=1)
			c = ConnectionDialog.promptConnection();
		else {
			c =  new SerialConnection(args[0]);
		}
		if(c==null) {
			System.exit(1);
		}
		System.out.println("Starting on arch: "+System.getProperty("os.arch")+" "+System.getProperty("os.name"));
		System.out.println("Starting test");
		DyIO dyio = new DyIO(c);
		//dyio.setThreadedUpstreamPackets(false);
		long start = System.currentTimeMillis();
		dyio.connect();
		System.out.println("Startup time: "+(System.currentTimeMillis()-start)+" ms");
		//dyio.enableDebug();
		for (int i=0;i<24;i++){
			dyio.getChannel(i).setAsync(false);
		}
		DigitalInputChannel dip = new DigitalInputChannel(dyio.getChannel(0));
		DigitalOutputChannel dop = new DigitalOutputChannel(dyio.getChannel(1));
//		new PPMReaderChannel(dyio.getChannel(23));
//		new ServoChannel(dyio.getChannel(11));
		
		
		double avg=0;
		
		int i;
		
		
		avg=0;
		start = System.currentTimeMillis();
		for(i=0;i<500;i++) {
			dyio.ping();
			double ms=System.currentTimeMillis()-start;
			avg +=ms;
			start = System.currentTimeMillis();
			//System.out.println("Average cycle time: "+(int)(avg/i)+"ms\t\t\t this loop was: "+ms);
		}
		System.out.println("Average cycle time for ping: "+(avg/i)+" ms");
		

		boolean high = false;
		//dyio.setCachedMode(true);
		
		avg=0;
		double best=1000;
		double worst=0;
		for(i=0;i<500;i++) {
			start = System.currentTimeMillis();
			try {
				high = !high;
				high = dip.getValue()==1;
				dop.setHigh(high);
			}catch(Exception ex) {
				ex.printStackTrace();
			}
			double ms=System.currentTimeMillis()-start;
			if (ms<best)
				best=ms;
			if(ms>worst)
				worst=ms;
			avg +=ms;
			start = System.currentTimeMillis();
			//System.out.println("Average cycle time: "+(int)(avg/i)/2+"ms\t\t\t this loop was: "+ms/2+"\t\tindex="+i);
		}
		System.out.println("Average cycle time for IO : "+(avg/(i+1))/2+" ms best="+ best/2+"ms worst="+worst/2);
		
		avg=0;
		best=1000;
		worst=0;
		dyio.setCachedMode(true);
		//Log.enableDebugPrint(true);
		for(i=0;i<500;i++) {
			start = System.currentTimeMillis();
			dyio.flushCache(0);
			double ms=System.currentTimeMillis()-start;
			if (ms<best)
				best=ms;
			if(ms>worst)
				worst=ms;
			avg +=ms;
			start = System.currentTimeMillis();
			//System.out.println("Average cycle time: "+(int)(avg/i)+"ms\t\t\t this loop was: "+ms);
		}
		System.out.println("Average cycle time for cache flush: "+(avg/(i+1))+" ms best="+ best+"ms worst="+worst);
		
		avg=0;
		best=1000;
		worst=0;
		dyio.setCachedMode(true);
		//Log.enableDebugPrint(true);
		for(i=0;i<500;i++) {
			start = System.currentTimeMillis();
			dyio.getAllChannelValues();
			double ms=System.currentTimeMillis()-start;
			if (ms<best)
				best=ms;
			if(ms>worst)
				worst=ms;
			avg +=ms;
			start = System.currentTimeMillis();
			//System.out.println("Average cycle time: "+(int)(avg/i)+"ms\t\t\t this loop was: "+ms);
		}
		System.out.println("Average cycle time for values get: "+(avg/(i+1))+" ms best="+ best+"ms worst="+worst);
		
		System.exit(0);
	}

}
