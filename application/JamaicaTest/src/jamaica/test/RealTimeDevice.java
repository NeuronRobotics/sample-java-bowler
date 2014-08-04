package jamaica.test;

import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import com.neuronrobotics.sdk.common.BowlerDatagram;
import com.neuronrobotics.sdk.network.UDPBowlerConnection;

public class RealTimeDevice extends BowlerAbstractDevice{
	private UDPBowlerConnection clnt;
	public RealTimeDevice(){
		clnt=new UDPBowlerConnection();
		setConnection(clnt);
		connect();
	}

	@Override
	public void onAsyncResponse(BowlerDatagram data) {
		// TODO Auto-generated method stub
		
	}

}
