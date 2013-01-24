package mainentry;

import com.neuronrobotics.sdk.tools.ImageToScad;

public class MainEntry {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Starting Image to SCAD");
		ImageToScad isc = new ImageToScad();
		if (!(args.length > 1)){
			//start GUI
			isc.startGui();
			//end of control, gui running
			System.out.println("Gui Started OK");
		}else{
			//command line mode, handled by ImageToScad object
			System.out.println("Running command line arguments");
			isc.runCmd(args);
			System.exit(0);
		}
	}

}
