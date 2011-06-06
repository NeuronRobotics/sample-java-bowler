package scare;

import java.io.File;
import java.io.IOException;

import com.neuronrobotics.sdk.dyio.peripherals.ServoChannel;

public class Slimey extends Thread {
	File dir1 = new File (".");
	ServoChannel eyes, mouth;
	float loopTime = (float) 1;
	private boolean muteState=false;
	public Slimey(ServoChannel e,ServoChannel m){
		eyes=e;
		mouth=m;
	}
	public void run(){
		while(true){
			eye();
			int j = (int)(Math.random()*110)+80;
			//System.out.println("Setting Mouth to: "+j);
			mouth.SetPosition(j,loopTime );
			if( !muteState){
				String command;// = "play "+dir1.getCanonicalPath()+"/src/sounds/pig.wav";
				try {
					switch((int)(Math.random()*6)){
					case 1:
						command = "play "+dir1.getCanonicalPath()+"/src/sounds/bquiek1.wav";
						break;
					case 2:
						command = "play "+dir1.getCanonicalPath()+"/src/sounds/cartoon170.wav";
						break;
					case 3:
						command = "play "+dir1.getCanonicalPath()+"/src/sounds/ferret.wav";
						break;
					case 4:
						command = "play "+dir1.getCanonicalPath()+"/src/sounds/purrsqueek.wav";
						break;
					case 5:
						command = "play "+dir1.getCanonicalPath()+"/src/sounds/RACCOON.WAV";
						break;
					case 6:
						command = "play "+dir1.getCanonicalPath()+"/src/sounds/squeek.wav";
						break;
					default:
						command = "play "+dir1.getCanonicalPath()+"/src/sounds/squeek2.wav";
					}
					//System.out.println("Running: " + command);
					Runtime.getRuntime().exec("aumix -v60");
					Runtime.getRuntime().exec(command);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			//eye();
			eye();
		}
	}
	public void mute(boolean state){
		muteState = state;
	}
	private void eye(){
		try {Thread.sleep((long) (loopTime*1000));} catch (InterruptedException x) {}
		int e = (int)(Math.random()*54)+161;
		//eyes.getChannel().getDevice().SetPrintModes(true, true);
		if(!eyes.SetPosition(e,loopTime)){
			System.out.println("Failed to set eyes");
		}
		//eyes.getChannel().getDevice().SetPrintModes(false, false);
	}
}
