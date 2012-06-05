package com.neuronrobotics.sdk.bowlercam.server;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.InetAddress;

import java.net.NetworkInterface;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.imageio.ImageIO;
import com.neuronrobotics.sdk.BowlerImaging.ImageProcessingFactory;
import com.neuronrobotics.sdk.BowlerImaging.SerialImageFactory;
import com.neuronrobotics.sdk.bowlercam.device.ItemMarker;
import com.neuronrobotics.sdk.commands.bcs.core.ReadyCommand;
import com.neuronrobotics.sdk.commands.neuronrobotics.bowlercam.BlobCommand;
import com.neuronrobotics.sdk.commands.neuronrobotics.bowlercam.ImageCommand;
import com.neuronrobotics.sdk.commands.neuronrobotics.bowlercam.ImageURLCommand;
import com.neuronrobotics.sdk.common.BowlerAbstractConnection;
import com.neuronrobotics.sdk.common.BowlerAbstractDeviceServer;
import com.neuronrobotics.sdk.common.BowlerDatagram;
import com.neuronrobotics.sdk.common.BowlerMethod;
import com.neuronrobotics.sdk.common.ByteList;
import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.network.BowlerUDPServer;

public class BowlerWebcamServer extends BowlerAbstractDeviceServer {
	private BufferedImage image=null;
	private String address="localhost";
	private static double imageScale = 1.0/8.0;
	private String webcamNms = "neuronrobotics.bowlercam.*;0.3;;";

	private ImageServer unaltered;
	private ImageServer processed;
	private ImageProcessorProvider processor=null;
	private int unalteredPort,processedPort;
	public BowlerWebcamServer(BowlerAbstractConnection connection, int unalteredPort, int processedPort) throws IOException{
		this.unalteredPort=unalteredPort;
		this.processedPort=processedPort;
		init(connection);
	}

	@SuppressWarnings("unchecked")
	private void init(BowlerAbstractConnection connection ) throws IOException {
		Log.info("Starting BowlerCam Server");
		try{
	        Enumeration e = NetworkInterface.getNetworkInterfaces();
	        while(e.hasMoreElements()){
	        	NetworkInterface ni =(NetworkInterface)  e.nextElement();
		        Enumeration e2 = ni.getInetAddresses();
		        while(e2.hasMoreElements()){
		        	InetAddress ip = (InetAddress) e2.nextElement();
		        	if(!(ip.isAnyLocalAddress() || ip.isLinkLocalAddress()||ip.isLoopbackAddress()||ip.isMulticastAddress()))
		        		setSelfAddress(ip.getHostAddress()); 
		        }
	        }
		}catch(Exception e){}
		addNamespace(webcamNms);
		setConnection(connection);
		connect();
	}
	public void setImageProvider(IImageProvider provider){
		try {
			unaltered = new ImageServer(unalteredPort,provider);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {
			processor=new ImageProcessorProvider(provider);
			//System.out.println("Starting Processor: "+processor);
			processed = new ImageServer(processedPort,processor);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setImage(BufferedImage image) {
		synchronized(this.image){
			this.image = image;
		}
	}
	public BufferedImage getImage(int chan) {
		try {
			if(chan == 0)
				image = ImageIO.read(new URL(unaltered.getURLString()));
			if(chan == 1){
				image = ImageIO.read(new URL(processed.getURLString()));
			}
		} catch (IOException e) {}
		return image;
	}
	
	private void sendImage(BufferedImage myImg, int chan) throws IOException {
		//http://discussion.forum.nokia.com/forum/showthread.php?108882-image-gt-byte-array-gt-image
		myImg = ImageProcessingFactory.resize(myImg, getImageScale());
		ByteList bytearray =new ByteList(SerialImageFactory.JPGImageToArray(myImg));
		int chunkSize=245;
		int chunks = (bytearray.size()/(chunkSize));
		//Log.info("Sending Image of in : "+chunks+" chunks");
		int chunk=0;
		while(bytearray.size()>0){
			sendAsync(new ImageCommand(chan,chunk,chunks,bytearray.popList(chunkSize)),0x37);
			chunk++;
		}
	}
	public void setSelfAddress(String address) {
		this.address = address;
	}
	public String getSelfAddress() {
		return address;
	}
	public static void setImageScale(double imageScale) {
		BowlerWebcamServer.imageScale = imageScale;
	}
	public static double getImageScale() {
		return imageScale;
	}
	
	
	@Override
	public void onSynchronusRecive(BowlerDatagram data) {
		if(data == null)
			return;
		// Connection responds to net requests, ignores all others
		String rpc = data.getRPC();
		if(rpc.contains("_img")){
			try {
				double scale =((double)ByteList.convertToInt(data.getData().getBytes(1, 4)))/1000;
				int chan = data.getData().get(0);
				sendSyncResponse(new ReadyCommand(0,0));
				setImageScale(scale);
				sendImage(getImage(chan),chan);
				return;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else if(rpc.contains("imsv")){
			try {
				int camera = data.getData().getByte(0);
				if(camera == 0)
					sendSyncResponse(new ImageURLCommand( unaltered.getURLString() ));
				if(camera == 1)
					sendSyncResponse(new ImageURLCommand( processed.getURLString() ));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else if(rpc.contains("blob")){
			if(data.getMethod() == BowlerMethod.GET){
				try {
					sendSyncResponse(new ReadyCommand(0,1));
					ArrayList<ItemMarker> markers = processor.getBlobs();
					for(ItemMarker m:markers){
						sendAsync(new BlobCommand(m.getX(),m.getY(),m.getRadius()),0x38);
					}
					sendAsync(new BlobCommand(0,0,0),0x38);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}if(data.getMethod() == BowlerMethod.POST){
				int r = data.getData().getUnsigned(0);
				int g = data.getData().getUnsigned(1);
				int b = data.getData().getUnsigned(2);
				Color c = new Color(r,g,b);
				int thresh = data.getData().getUnsigned(3);
				boolean within = data.getData().getUnsigned(4) != 0;
				int min = ByteList.convertToInt(data.getData().getBytes(5, 4), false);
				int max = ByteList.convertToInt(data.getData().getBytes(9, 4), false);
				processor.enableDrawMarks(true);
				processor.enableThreshhold(c, thresh, within);
				processor.enableBlobDetect(min,max);
				//System.out.println("Processor: "+processor);
				try {
					sendSyncResponse(new ReadyCommand(0,2));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}
}
