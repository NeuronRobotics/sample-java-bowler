package jamaica.test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import sun.net.InetAddressCachePolicy;

import com.neuronrobotics.sdk.commands.bcs.core.PingCommand;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import com.neuronrobotics.sdk.common.BowlerDatagram;
import com.neuronrobotics.sdk.common.BowlerDatagramFactory;
import com.neuronrobotics.sdk.common.ByteList;
import com.neuronrobotics.sdk.common.MACAddress;
import com.neuronrobotics.sdk.network.UDPBowlerConnection;

public class RealTimeDevice extends BowlerAbstractDevice {
	private UDPBowlerConnection clnt;
	ByteList bytesToPacketBuffer = new ByteList();

	public RealTimeDevice() {
		try {
			clnt = new UDPBowlerConnection(
					InetAddress.getByName("192.168.1.10"));
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		setConnection(clnt);
		connect();
	}

	public void fastPushTest() {
		BowlerDatagram cmd = BowlerDatagramFactory.build(new MACAddress(),
				new PingCommand());
		try {
			getConnection().write(cmd.getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public BowlerDatagram getLastResponse() {
		// This should be how to load packets without a threading engine
		try {
			getConnection().loadPacketFromPhy(bytesToPacketBuffer);
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return getConnection().getLastSyncronousResponse();
	}

	@Override
	public void onAsyncResponse(BowlerDatagram data) {
		// TODO Auto-generated method stub

	}

}
