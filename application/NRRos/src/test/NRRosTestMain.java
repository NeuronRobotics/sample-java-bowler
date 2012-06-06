package test;

import com.neuronrobotics.sdk.addons.ros.RosDyIONode;
import com.neuronrobotics.sdk.common.BowlerAbstractConnection;
import com.neuronrobotics.sdk.dyio.DyIO;
import com.neuronrobotics.sdk.ui.ConnectionDialog;

public class NRRosTestMain {
	
	public NRRosTestMain(){
		BowlerAbstractConnection c = ConnectionDialog.promptConnection();
		if(c==null)
			System.exit(1);
		System.out.println("Starting ROS node test");
		DyIO dyio = new DyIO(c);
		dyio.connect();
		RosDyIONode node = new RosDyIONode(dyio);
		node.start();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new NRRosTestMain();
	}

}
