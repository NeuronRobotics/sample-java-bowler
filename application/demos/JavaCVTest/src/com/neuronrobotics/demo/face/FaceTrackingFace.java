package com.neuronrobotics.demo.face;

import com.neuronrobotics.sdk.dyio.DyIO;
import com.neuronrobotics.sdk.ui.ConnectionDialog;
import com.neuronrobotics.video.OSUtil;

public class FaceTrackingFace {
	public FaceTrackingFace() throws Exception{
		DyIO.disableFWCheck();
		DyIO dyio = new DyIO();
		if(!ConnectionDialog.getBowlerDevice(dyio))
			return;
		dyio.enableBrownOutDetect(false);
		Head head = new Head(dyio);
		
		
		boolean UseHighSpeedImaging = false;
		if(OSUtil.isLinux()){
			UseHighSpeedImaging = true;
		}
		FaceDectector f;
//		if(UseHighSpeedImaging)
//			f=new FaceDectector("/dev/video0",320,240);
//		else
			f=new FaceDectector(0);
		long avgIndex=1;
		double avegTotal = 0;
		while (f.isVisible()){
			long start = System.currentTimeMillis();
			head.setNewFaces(f.updateFaces(106,80));
			double sec = (double)(System.currentTimeMillis()-start)/1000.0;
			avegTotal+=(1/sec);
			//System.out.println("FPS: "+(int)(1/sec)+" average: "+(avegTotal/avgIndex));
			avgIndex++;
		}
		head.close();
		f.stop();
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try{
			new FaceTrackingFace();
		}catch (Exception ex){
			ex.printStackTrace();
		}
		System.exit(0);
	}

}
