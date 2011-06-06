package scare;

import java.io.File;
import java.io.IOException;

import com.neuronrobotics.sdk.dyio.DyIO;
import com.neuronrobotics.sdk.dyio.DyIOChannelMode;
import com.neuronrobotics.sdk.dyio.DyIOCommunicationException;
import com.neuronrobotics.sdk.dyio.peripherals.AnalogInputChannel;
import com.neuronrobotics.sdk.dyio.peripherals.DigitalOutputChannel;
import com.neuronrobotics.sdk.dyio.peripherals.IAnalogInputListener;
import com.neuronrobotics.sdk.dyio.peripherals.ServoChannel;
import com.neuronrobotics.sdk.ui.ConnectionDialog;

public class ScareApplication implements IAnalogInputListener{
	DyIO d=new DyIO();
	DigitalOutputChannel hand ;
	DigitalOutputChannel neck ;
	DigitalOutputChannel jaw ;
	DigitalOutputChannel arm;
	Slimey slime;
	RollingAverage rangeAvg = new RollingAverage(1);
	int threshhold=75;
	public ScareApplication() throws InterruptedException{
		File dir1 = new File (".");
		if(!ConnectionDialog.getBowlerDevice(d)){
			throw new RuntimeException();
		}
		System.out.println("Starting...");
		hand = new DigitalOutputChannel(d.getChannel(0));
		neck = new DigitalOutputChannel(d.getChannel(1));
		jaw = new DigitalOutputChannel(d.getChannel(2));
		arm = new DigitalOutputChannel(d.getChannel(3));
		ServoChannel   slimyJaw = new ServoChannel  (d.getChannel(5));
		ServoChannel   slimyEyes = new ServoChannel  (d.getChannel(4));
		slime=new Slimey(slimyEyes,slimyJaw);
		slime.start();
		
		AnalogInputChannel range = new AnalogInputChannel(d.getChannel(12),true);
		range.addAnalogInputListener(this);
		while(true){
			if(rangeAvg.get()<threshhold){
				slime.mute(true);
				try{
					range.getChannel().setMode(DyIOChannelMode.ANALOG_IN, false);
				}catch (DyIOCommunicationException ex){
					down();
				}
				System.out.println("SCARE!");
				try {
					String command;// = "play "+dir1.getCanonicalPath()+"/src/sounds/pig.wav";
					switch((int)(Math.random()*3)){
					case 1:
						command = "play "+dir1.getCanonicalPath()+"/src/sounds/pig.wav";
						break;
					case 2:
						command = "play "+dir1.getCanonicalPath()+"/src/sounds/MONSTER_Echo.WAV";
						break;
					default:
						command = "play "+dir1.getCanonicalPath()+"/src/sounds/slimer.wav";	
					}
					//System.out.println("Running: " + command);
					Runtime.getRuntime().exec("aumix -v100");
					Runtime.getRuntime().exec(command);
				} catch (IOException e) {}
				Thread.sleep(200);
				neck.setHigh(true);
				arm.setHigh(true);
				for(int i=0;i<10;i++){
					hand.setHigh((i%2)==0);
					jaw.setHigh((i%2)==0);
					Thread.sleep(100);
				}
				down();
				System.out.println("Waiting for threshold to go down..");
				try{
					range.getChannel().setMode(DyIOChannelMode.ANALOG_IN, true);
				}catch (DyIOCommunicationException ex){
					range.getChannel().setMode(DyIOChannelMode.ANALOG_IN, true);
					down();
				}
				while(rangeAvg.get()<threshhold){
					Thread.sleep(100);
				}
				System.out.println("Resetting...");
				slime.mute(false);
			}else{
				down();
				try{
					range.getChannel().setMode(DyIOChannelMode.ANALOG_IN, true);
				}catch (DyIOCommunicationException ex){
					range.getChannel().setMode(DyIOChannelMode.ANALOG_IN, true);
					down();
				}
				Thread.sleep(50);
				try{
					range.getChannel().setMode(DyIOChannelMode.ANALOG_IN, false);
				}catch (DyIOCommunicationException ex){
					down();
				}
				Thread.sleep(50);
				System.out.println("Range = "+rangeAvg.get());
			}
		}
	}
	private void down(){
		if(neck.getChannel().getMode() !=DyIOChannelMode.DIGITAL_OUT )
			neck.getChannel().setMode(DyIOChannelMode.DIGITAL_OUT);
		if(arm.getChannel().getMode() !=DyIOChannelMode.DIGITAL_OUT )
			arm.getChannel().setMode(DyIOChannelMode.DIGITAL_OUT);
		if(jaw.getChannel().getMode() !=DyIOChannelMode.DIGITAL_OUT )
			jaw.getChannel().setMode(DyIOChannelMode.DIGITAL_OUT);
		if(hand.getChannel().getMode() !=DyIOChannelMode.DIGITAL_OUT )
			hand.getChannel().setMode(DyIOChannelMode.DIGITAL_OUT);
		hand.setHigh(false);
		neck.setHigh(false);
		jaw.setHigh(false);
		arm.setHigh(false);
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			new ScareApplication();
		}catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

	}

	@Override
	public void onAnalogValueChange(AnalogInputChannel chan, double value) {
		//System.out.println("Got :"+value);
		rangeAvg.add(value);
	}
}
