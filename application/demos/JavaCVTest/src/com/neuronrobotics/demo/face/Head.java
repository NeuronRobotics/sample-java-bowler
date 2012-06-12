package com.neuronrobotics.demo.face;

import java.util.ArrayList;
import java.util.zip.ZipException;

import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.neuronrobotics.sdk.dyio.DyIO;
import com.neuronrobotics.sdk.dyio.peripherals.ServoChannel;
import com.neuronrobotics.sdk.util.ThreadUtil;

public class Head {
	DyIO dyio;
	ServoChannel pan;
	ServoChannel tilt;
	ServoChannel jaw;
	chomp chomper;
	private static final double servoScale = 0.762711864;
	private static final int tiltZero=128;
	private static final int panZero=128;
	
	int panPos,tiltPos;

	public Head(DyIO d){
		dyio = d;
		if(!d.isAvailable())
			throw new RuntimeException("DyIO not connected!");
		pan  = new ServoChannel(dyio.getChannel(12));
		tilt = new ServoChannel(dyio.getChannel(13));
		jaw  = new ServoChannel(dyio.getChannel(14));
		move(panZero, tiltZero);
		setAngles(Math.toRadians(0), Math.toRadians(0));
		jawClose();
		say("Hello World");
	}
	public void say(String text){

	}
	public void close(){

		dyio.disconnect();
	}
	public void setNewFaces(FaceLocations loc){
		int canterx = loc.getWidth()/2;
		int cantery = loc.getHight()/2;
		ArrayList<CvRect> faces = loc.getFaces();
		if(faces.size()>0){
			CartesianSpaceFace csf = new CartesianSpaceFace(faces.get(0), loc.getWidth(), loc.getHight());
			double [] faceLocation = csf.getFaceLocation();
			double [] globalFaceLocation = new double[3];
			// move by panning first
			// rotate about z
			double zRot = getPanAngle();
			double hypLocal = hyp(faceLocation[0],faceLocation[2]);
			double panLocal = Math.atan2(faceLocation[0], faceLocation[2]);
			globalFaceLocation[0] = Math.cos(panLocal + zRot) * hypLocal;
			globalFaceLocation[2] = Math.sin(panLocal + zRot) * hypLocal;
			//System.out.println("Global Location, x: "+globalFaceLocation[0]+" z: "+globalFaceLocation[2]);
			double panUpdate = zRot-(panLocal/7);
			//next update the tilt
			double xRot = getTiltAngle();
			double tiltLocal = Math.asin(faceLocation[1]/faceLocation[2]);
			double tiltUpdate = xRot-(tiltLocal/7);
			
			//System.out.println("Current servo angel: "+Math.toDegrees(zRot)+" angle Local: "+Math.toDegrees(panLocal) +" setting to: "+ Math.toDegrees(panUpdate) );
			setAngles(panUpdate, tiltUpdate);
			if (faceLocation[2]< 15){
				System.out.println("Face too close!");
				if(chomper == null){
					chomper = new chomp();
					chomper.start();
				}
			}else{
				if(chomper != null){
					chomper.kill();
					chomper=null;
				}
			}
		}
	}
	private double hyp(double a, double b){
		return Math.sqrt((a*a)+(b*b));
	}
	private double getPanAngle(){
		return Math.toRadians((panPos-panZero)*servoScale);
	}
	private double getTiltAngle(){
		return Math.toRadians((tiltPos-tiltZero)*servoScale);
	}
	private void setAngles(double panRad, double tiltRad){
		//System.out.println("Setting pan to: "+Math.toDegrees(panRad));
		move((int)((Math.toDegrees(panRad)/servoScale)+panZero), (int)((Math.toDegrees(tiltRad)/servoScale)+tiltZero));
		//move((int)((Math.toDegrees(panRad)/servoScale)+panZero), tiltPos);
	}
	private void move(int panUpdate, int tiltUpdate){
		if(panPos != panUpdate){
			panPos=panUpdate;
			if(panPos>255)
				panPos=255;
			if(panPos<0)
				panPos=0;
			pan.SetPosition(panPos);
		}
		
		if(tiltPos!= tiltUpdate){
			tiltPos=tiltUpdate;
			if(tiltPos>136)
				tiltPos=136;
			if(tiltPos<110)
				tiltPos=110;
			tilt.SetPosition(tiltPos);
		}
	}
	private void jawClose(){
		jaw.setValue(211);
	}
	private void jawOpen(){
		jaw.setValue(125);
	}
	private class chomp extends Thread{
		private boolean nom = true;
		public void run(){
			while(nom){
				jawOpen();
				ThreadUtil.wait(150);
				jawClose();
				ThreadUtil.wait(150);
			}
			jawClose();
		}
		public void kill(){
			nom = false;
		}
	}
}
