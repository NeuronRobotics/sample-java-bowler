package com.neuronrobotics.sdk.bowlercam.server;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import com.neuronrobotics.sdk.BowlerImaging.ImageProcessingFactory;
import com.neuronrobotics.sdk.bowlercam.device.ItemMarker;


public class ImageProcessorProvider implements IImageProvider{
	
	private  IImageProvider imageSource;
	private BufferedImage bi=null;
	private ThrehholdProcessor tp = new ThrehholdProcessor();
	private ArrayList<ItemMarker> markers = new ArrayList<ItemMarker>();
	private boolean drawMarks=true;
	private int minBlobMass=5;
	private int maxBlobMass=100000;
	private int matchVal=255;
	
	public ImageProcessorProvider(IImageProvider im){
		imageSource=im;
	}
	public void enableThreshhold(Color target, int threshold,boolean within){
		tp = new ThrehholdProcessor(target, threshold, within);
	}
	public ArrayList<ItemMarker> getBlobs(){
		getImage();
		return markers;
	}
	public void enableDrawMarks(boolean draw){
		drawMarks = draw;
	}
	public void enableBlobDetect(int min,int max){
		tp.enabled = true;
		minBlobMass=min;
		maxBlobMass=max;
	}
	@Override
	public BufferedImage getImage() {
		try{
			bi=imageSource.getImage();
			if(tp.isEnabled()){
				bi = ImageProcessingFactory.threshhold(bi, tp.getTarget(), tp.getTreshhold(),tp.isWithin());
				markers = ImageProcessingFactory.getBlobs(bi, minBlobMass, maxBlobMass, matchVal);
				if(drawMarks){
					bi = ImageProcessingFactory.drawMarks(bi, markers, Color.green);
				}
			}
			
		}catch(Exception e){}
		
		return bi;
	}
	@Override
	public String toString(){
		String s="Processor settings: ";
		s+=" Color = "+tp.getTarget();
		s+=" Threshhold = "+tp.getTreshhold();
		s+=" Within = "+tp.isWithin();
		s+=" min = "+minBlobMass;
		s+=" max = "+maxBlobMass;
		s+=" match = "+matchVal;
		
		return s;
	}
}
