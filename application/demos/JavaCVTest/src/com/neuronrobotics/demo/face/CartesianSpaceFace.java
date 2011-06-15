package com.neuronrobotics.demo.face;

import com.googlecode.javacv.cpp.opencv_core.CvRect;

public class CartesianSpaceFace {
	private static final double xViewingAngle = Math.atan2((7.75/2), 10.5);
	private static final double yViewingAngle = Math.atan2((5.1/2), 9.5);
	private static final double zFudgeFactor  = (72/9.0);
	
	private double currentX=0,currentY=0,currentZ=0,prevX=0,prevY=0,prevZ=0;
	
	public CartesianSpaceFace(CvRect f,int pixX,int pixY){
		double[] ret = convertToCartesian(f,pixX, pixY);
		currentX = ret[0];
		currentY = ret[1];
		currentZ = ret[2];
		prevX = ret[0];
		prevY = ret[1];
		prevZ = ret[2];
		//System.out.println("New face x: "+currentX+", y: "+currentY+", z: "+currentZ);
	}
	public double [] getFaceLocation(){
		double[] ret = {currentX,currentY,currentZ};
		return ret;
	}
	public void updateFace(CvRect f,int pixX,int pixY){
		double[] ret = convertToCartesian(f,pixX, pixY);
		prevX = currentX ;
		prevY = currentY ;
		prevZ = currentZ ;
		currentX = ret[0];
		currentY = ret[1];
		currentZ = ret[2];
	}
	public double calcDifferenceVectorLegnth(CvRect f,int pixX,int pixY){
		double[] ret = convertToCartesian(f,pixX, pixY);
		double diffX = currentX - ret[0];
		double diffY = currentY - ret[1];
		double diffZ = currentZ - ret[2];
		double hyp = Math.sqrt((diffX*diffX)+(diffY*diffY));
		double abs = Math.sqrt((hyp*hyp)+(diffZ*diffZ));
		return abs;
	}
	private double[] convertToCartesian(CvRect f,int pixX,int pixY){
		double centerX = pixX/2;
		double centerY = pixY/2;
		
		double x = f.x(), y = f.y(), w = f.width(), h = f.height(); 
		double fx = (x+(w/2)), fy = ( y+(h/2));
		
		double[] ret = new double[3];
		
		double screenSize = Math.sqrt((pixX*pixX)+(pixY*pixY));
		ret[2] = zFudgeFactor/(Math.sqrt((f.height()*f.height())+(f.width()*f.width()))/screenSize) ;
		
		double xang = (xViewingAngle *(centerX - fx))/ centerX ;
		double yang = (yViewingAngle *(centerY - fy))/ centerY;
		ret[0] =  ret[2] * Math.tan(xang);
		ret[1] =  ret[2] * Math.tan(yang); 
		return ret;
	}
}
