package com.neuronrobotics.sdk.addons.waldo;

import com.neuronrobotics.sdk.dyio.DyIO;
import com.neuronrobotics.sdk.dyio.peripherals.AnalogInputChannel;
import com.neuronrobotics.sdk.dyio.peripherals.IAnalogInputListener;
import com.neuronrobotics.sdk.dyio.peripherals.ServoChannel;
import com.neuronrobotics.sdk.serial.SerialConnection;
import com.neuronrobotics.sdk.util.ThreadUtil;

public class WaldoTest implements IAnalogInputListener {
	
	AnalogInputChannel [] inputs = new AnalogInputChannel [6]; 
	ServoChannel [] outputs = new ServoChannel [6];
	int [] values = new int[6];
	int [] inputOffsets = new int [] {106,125, 223, 88, 148, 234};
	int [] outputOffsets = new int [] {189, 90, 187, 112, 121, 171};
	public WaldoTest(){
		DyIO.disableFWCheck();
		DyIO master = new DyIO(new SerialConnection("/dev/DyIO0"));
		master.connect();
		DyIO slave = new DyIO(new SerialConnection("/dev/DyIO1"));
		slave.connect();
		
		for(int i=0;i<6;i++){
			inputs[i]=new AnalogInputChannel(master.getChannel(10+i));
			inputs[i].addAnalogInputListener(this);
			outputs[i]=new ServoChannel(slave.getChannel(11-i));
			values[i]= outputs[i].getValue();
		}
		slave.setCachedMode(true);
		while(master.isAvailable() && slave.isAvailable()){
			ThreadUtil.wait(200);
			//do something
			System.out.print("\nInput [ "); 
			for(int i=0;i<6;i++){
				int tmp=values[i]-inputOffsets[i]+outputOffsets[i];
				System.out.print(values[i]+" ("+tmp+"),");
				if(tmp>255)
					tmp=255;
				if(tmp<0)
					tmp=0;
					
				outputs[i].SetPosition(tmp);
			}
			System.out.print("]\n");
			slave.flushCache(.2);
		}
		System.exit(0);
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try{
			new WaldoTest();
		}catch (Exception ex){
			ex.printStackTrace();
			System.exit(0);
		}
	}
	@Override
	public void onAnalogValueChange(AnalogInputChannel chan, double value) {
		for (int i=0;i<6;i++){
			if(inputs[i] == chan){
				values[i]= (int) (value*.25);
				if(i==2 || i==0 || i == 3)
					values[i]=255-values[i];
			}
		}
	}

}
