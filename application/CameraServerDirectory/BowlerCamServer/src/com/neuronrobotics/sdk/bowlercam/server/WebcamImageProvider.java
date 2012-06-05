package com.neuronrobotics.sdk.bowlercam.server;

import java.awt.image.BufferedImage;
import java.util.Set;

import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.video.Camera;

public class WebcamImageProvider implements IImageProvider {
	private String device;
	Camera cam;
	public WebcamImageProvider(String device){
		this.device=device;
		Set<String> devices = Camera.getAvailibleDevices();
		boolean ok=false;
		for(String dev:devices){
			if(dev.contains(device)){
				ok=true;
			}
		}
		if(!ok){
			Log.error("Device: "+device+" is not yet availible");
		}
		getImage();
	}
	@Override
	public BufferedImage getImage() {
		Set<String> devices = Camera.getAvailibleDevices();
		//System.out.println(devices);
		boolean ok=false;
		for(String dev:devices){
			if(dev.contains(device)){
				ok=true;
			}
		}
		if(ok){
			if(cam == null){
				Log.info("Device: "+device+" is now availible");
				cam = new Camera(device,320,240);
			}
			try{
				return (BufferedImage) cam.getImage();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		if(devices.size() != 0){
			Log.error("Device: "+device+" is not availible, but there exists: "+devices);
		}
		if (cam != null){
			cam.close();
			Log.error("Device: "+device+" is no longer availible");
		}
		cam=null;
		return null;
	}

}
