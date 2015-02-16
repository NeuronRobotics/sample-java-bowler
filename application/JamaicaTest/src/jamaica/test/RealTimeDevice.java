package jamaica.test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import sun.net.InetAddressCachePolicy;

import com.neuronrobotics.sdk.commands.bcs.core.PingCommand;
import com.neuronrobotics.sdk.common.BowlerAbstractCommand;
import com.neuronrobotics.sdk.common.BowlerAbstractConnection;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import com.neuronrobotics.sdk.common.BowlerDatagram;
import com.neuronrobotics.sdk.common.BowlerDatagramFactory;
import com.neuronrobotics.sdk.common.BowlerMethod;
import com.neuronrobotics.sdk.common.ByteList;
import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.common.MACAddress;
import com.neuronrobotics.sdk.network.UDPBowlerConnection;
import com.neuronrobotics.sdk.util.ThreadUtil;

public class RealTimeDevice extends BowlerAbstractDevice {
	private UDPBowlerConnection clnt;
	ByteList bytesToPacketBuffer = new ByteList();
	
	private class LoaderThread extends Thread{
		private boolean run=true;
		BowlerDatagram gd;
		public void run(){
			while(isRun()){
				ThreadUtil.wait(10);
				gd = getLastResponse();
				if(gd != null){
					System.out.println("Loading from coms "+gd);
				}
				
			}
		}
		public boolean isRun() {
			return run;
		}
		public void setRun(boolean run) {
			this.run = run;
		}
	};

	public RealTimeDevice() {
		// after loading the RPC list with the normal threaded stack, stop the stack thread
		BowlerAbstractConnection.setUseThreadedStack(false);

		Log.enableDebugPrint();
		try {
			clnt = new UDPBowlerConnection(
					InetAddress.getByName("192.168.1.10"));
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		setConnection(clnt);
		connect();
		
		LoaderThread loader = new LoaderThread();
		loader.start();
		ThreadUtil.wait(200);
		
		loadRpcList();
		
		loader.setRun(false);
		loader=null;
	}
	
	

	public void fastPushTest() {
		BowlerAbstractCommand command = getConnection().getCommand("hsmri.*", 
																	BowlerMethod.POST, 
																	"sync", 
																	new Object[]{new int[20]});
		if(command == null)
			throw new RuntimeException("Command failed to parse "+getConnection());
		BowlerDatagram cmd = BowlerDatagramFactory.build(new MACAddress(),
				command);
		
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
