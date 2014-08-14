package jamaica.test;

import java.io.IOException;

import com.neuronrobotics.sdk.commands.bcs.core.PingCommand;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import com.neuronrobotics.sdk.common.BowlerDatagram;
import com.neuronrobotics.sdk.common.BowlerDatagramFactory;
import com.neuronrobotics.sdk.common.MACAddress;
import com.neuronrobotics.sdk.network.UDPBowlerConnection;

public class RealTimeDevice extends BowlerAbstractDevice{
	private UDPBowlerConnection clnt;
	public RealTimeDevice(){
		clnt=new UDPBowlerConnection();
		setConnection(clnt);
		connect();
	}
	
	public void fastPushTest(){
		BowlerDatagram cmd= BowlerDatagramFactory.build(new MACAddress(), new PingCommand());
		try {
			getConnection().write(cmd.getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onAsyncResponse(BowlerDatagram data) {
		// TODO Auto-generated method stub
		
	}

}
