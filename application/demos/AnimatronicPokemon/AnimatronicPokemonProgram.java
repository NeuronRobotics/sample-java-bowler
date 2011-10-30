import java.util.Random;

import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.dyio.DyIO;
import com.neuronrobotics.sdk.dyio.peripherals.ServoChannel;
import com.neuronrobotics.sdk.ui.ConnectionDialog;
/**
 * 
 * @author acamilo
 */

public class AnimatronicPokemonProgram {
	public static void main(String[] args) {
		//Log.enableDebugPrint(true);
		// Grab a DyIO
		DyIO dyio=new DyIO();
		if (!ConnectionDialog.getBowlerDevice(dyio)){
			System.exit(1);
		}
		try{
			new AnimatronicPokemonProgram(dyio);
		} catch(Exception e){
			e.printStackTrace();
			System.exit(1);
		}
	}
	public AnimatronicPokemonProgram(DyIO dyio) throws InterruptedException{
		System.out.println("I Has A DyIO");
		/*
		SmeargleHead = new ServoChannel (dyio.getChannel(22));
		SmeargleArm = new ServoChannel (dyio.getChannel(21));
		*/
		

		Bulbasaur starter = new Bulbasaur(dyio);
		new Thread(starter).start();
		
		Smeargle somerandompokemon = new Smeargle(dyio);
		new Thread(somerandompokemon).start();
		
		while(true){
			System.out.println("Battery Voltage:\t"+dyio.getBatteryVoltage(true));
			
			Thread.sleep(10000);
		}
		
	}
}

class Bulbasaur implements Runnable {
	// Constants
	private static final int BulbasaurHeadMax = 0;
	private static final int BulbasaurHeadMid = 37;
	private static final int BulbasaurHeadMin = 140;

	private DyIO myDyIO;
	private ServoChannel BulbasaurHead;
	private Random generator;
	
	public Bulbasaur(DyIO dyio){
		myDyIO = dyio;
		generator = new Random();
		BulbasaurHead = new ServoChannel (dyio.getChannel(23));
	}
	
    @Override
	public void run() {
		try {
		while(true){
			int action = generator.nextInt(4);
			BulbasaurHead.SetPosition(BulbasaurHeadMin, action);
			Thread.sleep(generator.nextInt(10)*1000);
			BulbasaurHead.SetPosition(BulbasaurHeadMax, action);
			Thread.sleep(generator.nextInt(10)*1000);

		}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}

class Smeargle implements Runnable {
	// Constants
	private static final int SmeargleHeadMax = 30;
	private static final int SmeargleHeadCenter = 135;
	private static final int SmeargleHeadMin = 200;
	
	private static final int SmeargleArmMin= 110;
	private static final int SmeargleArmMax= 32;

	private DyIO myDyIO;
	private ServoChannel SmeargleHead;
	private ServoChannel SmeargleArm;
	private Random generator;
	
	public Smeargle(DyIO dyio){
		myDyIO = dyio;
		generator = new Random();
		SmeargleHead = new ServoChannel (dyio.getChannel(22));
		SmeargleArm = new ServoChannel (dyio.getChannel(21));
	}
	
	public void waveArm() throws InterruptedException{
		SmeargleArm.SetPosition(SmeargleArmMin, .7);
		Thread.sleep(700);
		SmeargleArm.SetPosition(SmeargleArmMax, .7);
		Thread.sleep(700);
	}
	
    @Override
	public void run() {
		try {
		while(true){
			int action = generator.nextInt(4);
			int time = generator.nextInt(10);
			if(action==1){
				SmeargleHead.SetPosition(SmeargleHeadMin, time);
				Thread.sleep(time*1000);
			} else if(action==2){
				SmeargleHead.SetPosition(SmeargleHeadMax,time);
				Thread.sleep(time*1000);
			} else if(action==3){
				waveArm();
				Thread.sleep(time*1000);
			}
			
			else {
				SmeargleHead.SetPosition(SmeargleHeadCenter, time);
				Thread.sleep(time*1000);
			}
			
		}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
