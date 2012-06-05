package test;


import com.neuronrobotics.sdk.bowlercam.server.BowlerWebcamServer;
import com.neuronrobotics.sdk.bowlercam.server.WebcamImageProvider;
import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.network.BowlerTCPServer;
import com.neuronrobotics.sdk.network.BowlerUDPServer;
import com.neuronrobotics.sdk.util.ThreadUtil;

public class BowlerCamServerTester {
	static BowlerWebcamServer tcp;
	static BowlerWebcamServer udp;
	private static String device = "/dev/video0";
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			Log.enableDebugPrint(true);
			Log.enableSystemPrint(true);
			if(args.length >0)
				device = args[0];
			tcp = new BowlerWebcamServer(new BowlerTCPServer(),1967,1968);
			udp = new BowlerWebcamServer(new BowlerUDPServer(),1969,1970);
			WebcamImageProvider cam = new WebcamImageProvider(device);
			tcp.setImageProvider(cam);
			udp.setImageProvider(cam);
			System.out.println("Starting BowlerCam Server with device: "+device);
			while(udp.isAvailable() && tcp.isAvailable() ) {
				ThreadUtil.wait(500);
				cam.getImage();
			}
			System.err.println("Server Died");
			System.exit(1);
		}catch (Exception e) {
			System.err.println("Failed out!");
			e.printStackTrace();
			System.exit(1);
		}

	}

}
