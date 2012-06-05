package com.neuronrobotics.sdk.bowlercam.server;

import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.Enumeration;

import com.neuronrobotics.sdk.BowlerImaging.SerialImageFactory;

public class ImageServer {
	private ServerSocket tcpSock = null;
	private TCPConnectionManager tcp = null;
	private Serve serve = null;
	private DataInputStream dis=null;
	private DataOutputStream dos=null;
	BufferedImage image = null;
	private int port=0;
	private String header = "HTTP/1.0 200 OK\r\n"+
							//"Connection: close\r\n"+
							//"Server: BowlerCamServer\r\n"+
							"Content-type: image/jpeg\r\n"+
							"\r\n";
	private  IImageProvider provider;
	private String address="http://localhost";
	public ImageServer(int port, IImageProvider provider) throws IOException{	
		this.provider = provider;
		setTCPSocket(port);
		tcp = new TCPConnectionManager();
		tcp.start();
		serve = new Serve();
		serve.start();
		
	}
	public void kill(){
		if(tcp != null){
			tcp.kill();
			tcp=null;
		}
		if(serve != null){
			serve.kill();
			serve = null;
		}
		try{
			if(dos != null){
				dos.flush();
				dos.close();
				dos=null;
			}if(dis != null){
				dis.close();
				dis=null;
			}	
			if(tcpSock != null){
				tcpSock.close();
				tcpSock = null;
			}
		}catch(Exception e){

		}
	}
	private void setTCPSocket(int port) throws IOException{
		if(tcpSock != null){
			if(dos != null){
				dos.flush();
				dos.close();
				dos=null;
			}if(dis != null){
				dis.close();
				dis=null;
			}	
		}
		if(port != this.port){
			this.port = port;
			if(tcpSock != null){
				tcpSock.close();
				tcpSock = null;
			}
		}
		if(tcpSock == null){
			ServerSocket serverSocket = new ServerSocket(this.port);
			while(!serverSocket.isBound());
			tcpSock = serverSocket;
		}

	}
	private class TCPConnectionManager extends Thread {
		private Socket connectionSocket=null;
		boolean run = true;
		public void run(){
			//System.out.println("Connection listener starting");
			while(run){
				try {
					connectionSocket = tcpSock.accept();
					setDataIns(new DataInputStream(connectionSocket.getInputStream()));
					setDataOuts(new DataOutputStream(connectionSocket.getOutputStream()));
				} catch (Exception e1) {

				}
			}
			try {
				connectionSocket.close();
			} catch (IOException e) {
			}
			//System.out.println("Connection listener ending");
		}
		public void kill(){
			run = false;
		}
	}
	private class Serve extends Thread{
		private String incoming;
		private byte [] b = new byte[1024];
		boolean run = true;
		public void run(){
			//System.out.println("Server starting");
			while(run){
				if(dis != null && dos != null) {
					try {
						if(dis.available()>0) {
							dis.read(b);
							incoming = new String(b);
							if(incoming.contains("GET /")) {
								//System.out.println("Sending Image:\r\n"+incoming);
								while(dis.available()>0)
									dis.read(b);
								sendImage(dos);
							}else {
								//System.out.println("Fail:\r\n"+incoming);
							}
						}
					} catch (IOException e) {
					}
				}
				try {
					Thread.sleep(50);			
				} catch (InterruptedException e) {
				}
			}
			//System.out.println("Server ending");
		}

		public void kill(){
			run = false;
		}
	}
	private void setDataIns(DataInputStream dataInputStream) {
		dis=dataInputStream;	
	}
	private void setDataOuts(DataOutputStream dataOutputStream) {
		dos = dataOutputStream;
	}
	private void sendImage(DataOutputStream dos) {
		try {
			image = provider.getImage();
			if(image == null) {
				System.out.println("Image is null!");
				return;
			}
			dos.writeBytes(header);
			dos.flush();
			dos.write(serialize(image));
			dos.flush();
			//System.out.print(".");
			setTCPSocket(port);
		} catch (IOException e) {
		}
	}
	
	private byte [] serialize(BufferedImage im){
		return SerialImageFactory.JPGImageToArray(im);
	}
	@SuppressWarnings("unchecked")
	public URL getURL(){
		
		URL url=null;
		try{
	        Enumeration e = NetworkInterface.getNetworkInterfaces();
	        while(e.hasMoreElements()){
	        	NetworkInterface ni =(NetworkInterface)  e.nextElement();
		        Enumeration e2 = ni.getInetAddresses();
		        while(e2.hasMoreElements()){
		        	InetAddress ip = (InetAddress) e2.nextElement();
		        	if(!(ip.isAnyLocalAddress() || ip.isLinkLocalAddress()||ip.isLoopbackAddress()||ip.isMulticastAddress()))
		        		address="http://"+ip.getHostAddress()+":"+port+"/"; 
		        }
	        }
	       url = new URL(address);
		}catch(Exception e){}
		return url;
	}
	public String getURLString(){
		getURL();
		return address;
	}
}
