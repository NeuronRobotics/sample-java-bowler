package com.neuronrobotics.sdk.BowlerImaging;

import ij.IJ;
import ij.ImagePlus;
//import ij.ParticleAnalyzer;
import ij.WindowManager;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.filter.ParticleAnalyzer;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.util.ArrayList;

import com.neuronrobotics.sdk.bowlercam.device.ItemMarker;

import jjil.algorithm.Gray8Rgb;
import jjil.algorithm.RgbVecThresh;
import jjil.core.Error;
import jjil.core.Gray8Image;
import jjil.core.RgbImage;
import jjil.core.RgbVal;
import jjil.j2se.RgbImageJ2se;

@SuppressWarnings("unused")
public class ImageProcessingFactory implements Measurements{
	public static BufferedImage threshhold(BufferedImage im,Color target, int threshold,boolean within){
		RgbImage tmp = RgbImageJ2se.toRgbImage(im);
		RgbVecThresh thresh = new  RgbVecThresh(target.getRGB(),0,threshold,within);
		try {
			thresh.push(tmp);
			while(thresh.isEmpty());
			
			Gray8Image g8 =(Gray8Image) thresh.getFront();
			BufferedImage bimage = new BufferedImage(im.getWidth(), im.getHeight(),BufferedImage.TYPE_BYTE_GRAY);
			Graphics2D g = bimage.createGraphics();
			
			Gray8Rgb convert = new Gray8Rgb();
			convert.push(g8);
			tmp = (RgbImage)convert.getFront();
			g.drawImage(RgbImageJ2se.toBufferedImage(tmp), 0, 0, null);
			
			//g.drawImage(RgbImageJ2se.toImage(g8), 0, 0, null);
			return bimage;
			//return RgbImageJ2se.toBufferedImage(tmp);
		} catch (Error e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		return null;
		//return threshhold(new ImagePlus("",im),target, threshold,within).getBufferedImage();
	}
	public static ArrayList<ItemMarker> getBlobsOfColor(BufferedImage im, int minBlobMass, int maxBlobMass, int matchVal,Color target, int threshold,boolean within){
		ImagePlus imp = threshhold(new ImagePlus("",im),target, threshold,within);
		return getBlobs(imp, minBlobMass, maxBlobMass, matchVal);
	}
	public static ImagePlus threshhold(ImagePlus im,Color target, int threshold,boolean within){
		int nR=target.getRed();
		int nG=target.getGreen();
		int nB= target.getBlue();
	    int nThresh = threshold;
	    boolean bWithin=within;
		int [] nData =(int []) im.getProcessor().getPixels();
		byte[] bData = new byte[im.getWidth()*im.getHeight()];
		for (int i=0; i<im.getWidth()*im.getHeight(); i++) {
	        int nRCurr = RgbVal.getR(nData[i]) - Byte.MIN_VALUE;
	        int nGCurr = RgbVal.getG(nData[i]) - Byte.MIN_VALUE;
	        int nBCurr = RgbVal.getB(nData[i]) - Byte.MIN_VALUE;	
	        boolean rok = (((nRCurr)<(nR+nThresh)) && ((nRCurr)>(nR-nThresh)));
	        boolean gok = (((nGCurr)<(nG+nThresh)) && ((nGCurr)>(nG-nThresh)));
	        boolean bok = (((nBCurr)<(nB+nThresh)) && ((nBCurr)>(nB-nThresh)));
	        boolean isTarget = rok && gok && bok;
	        if(isTarget){
	      	  bData[i] = bWithin?Byte.MIN_VALUE:Byte.MAX_VALUE;
	        }else
	      	  bData[i] = bWithin?Byte.MAX_VALUE:Byte.MIN_VALUE;
	    }
		ByteProcessor bp = new ByteProcessor(im.getWidth(),
				im.getHeight(),
				bData,
				getGrayScaleColorModel());
		bp.setThreshold(0, 128,ImageProcessor.BLACK_AND_WHITE_LUT);
		return  new ImagePlus("",bp);
	}
	
	public static ArrayList<ItemMarker> getBlobs(ImagePlus imp, int minBlobMass, int maxBlobMass, int matchVal){
		ArrayList<ItemMarker> mark = new  ArrayList<ItemMarker>();
		int options = ParticleAnalyzer.INCLUDE_HOLES |ParticleAnalyzer.CLEAR_WORKSHEET  ;
		int measurements=AREA|CENTROID|MEAN|CIRCULARITY;
		ResultsTable rt = ResultsTable.getResultsTable();
		ParticleAnalyzer p = new ParticleAnalyzer(options, measurements,rt, minBlobMass, maxBlobMass);
		//p.setup("", imp);
		p.analyze(imp);
		int n = rt.getCounter();		
        for (int i=0; i<n; i++){
        	int radius = (int)(Math.sqrt(rt.getValue("Area",i)/Math.PI));
        	if(radius > 0){
        		ItemMarker imk=new ItemMarker((int)rt.getValue("X",i),
						(int)rt.getValue("Y",i),
						radius);
        		mark.add(imk);
        		//System.out.println(imk);
        	}
        	
        }
		return mark;
	}
	
	public static ArrayList<ItemMarker> getBlobs(BufferedImage im, int minBlobMass, int maxBlobMass, int matchVal){
		ArrayList<ItemMarker> mark = new  ArrayList<ItemMarker>();
		ByteProcessor bp= new ByteProcessor(toGrayScale(im));
		bp.setThreshold(0, 128,ImageProcessor.BLACK_AND_WHITE_LUT);
		ImagePlus imp = new ImagePlus("",bp);
        return getBlobs(imp, minBlobMass, maxBlobMass, matchVal);
	}
	public static BufferedImage toGrayScale(BufferedImage in){
		BufferedImage bi = new BufferedImage(in.getWidth(), in.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		Graphics g = bi.createGraphics();
		g.drawImage(in, 0, 0, null);
		return bi;
	}

	public static BufferedImage drawMarks(BufferedImage im, ArrayList<ItemMarker> marks,Color color) {
		BufferedImage bimage = new BufferedImage(im.getWidth(), im.getHeight(),BufferedImage.TYPE_INT_RGB);
		Graphics2D g =  bimage.createGraphics();
		g.drawImage(im, 0, 0, im.getWidth(), im.getHeight(), null);
		g.setColor(color);
		for (ItemMarker image : marks){
			//System.out.println("Mark: "+im);
			g.drawOval(image.getX(),image.getY(), image.getRadius(), image.getRadius());
		}
		return bimage;
	}
	
	public static Color getColorAtLocation(BufferedImage im, int x, int y) {
		if(x<0 || y<0 || x > im.getWidth() || y> im.getHeight())
			return Color.black;
		return new Color(im.getRGB(x, y));
	}
	
	private static IndexColorModel getGrayScaleColorModel(){
		int mapSize = 256;
		byte[] reds = new byte[mapSize ];
		byte[] greens = new byte[mapSize];
		byte[] blues = new byte[mapSize];
		for (int i=0; i<mapSize; i++) {
			reds[i]=(byte) i;
			greens[i]=(byte) i;
			blues[i]=(byte) i;
		}
		return new IndexColorModel(8,mapSize,reds,greens,blues);
	}
	public static BufferedImage resize(BufferedImage image, int width, int height) {
		if(image == null)
			return null;
		BufferedImage resizedImage = new BufferedImage(width, height,image.getType());
		Graphics2D g = resizedImage.createGraphics();
		g.drawImage(image, 0, 0, width, height, null);
		g.dispose();
		return resizedImage;
	}

	public static BufferedImage resize(BufferedImage image, double scale) {
		if(image == null)
			return null;
		if (scale<.01)
			scale = .01;
		double width =  ((double)image.getWidth())*scale;
		double hight = ((double)image.getHeight())*scale;
		return resize(image,(int)width,(int)hight) ;
	} 
	public static BufferedImage copy(BufferedImage in) {
		return resize(in,1);
	}
	
}
