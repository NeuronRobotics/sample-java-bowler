package com.neuronrobotics.sdk.tools;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import javax.imageio.ImageIO;

import com.neuronrobotics.sdk.BowlerImaging.ImageProcessingFactory;

public class ImageToScad {

	public ImageToScad() {
		// TODO Auto-generated constructor stub
	}

	public void startGui() {
		try {
			//executeConversion("test.jpg", 32, 1, .1);
			executeConversion("test2.jpg", 64, 1, .1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void runCmd(String[] args) {
		if (args.length != 4){
			System.err.println("FAILED! 4 arguments needed\n <input.jpg> <pixel width of object> <mm's per pixel> <depth scale>\nExample:\n java -jar ImageToScad input.jpg 32 1 .1");
		}
		
	}
	
	private boolean executeConversion(String inputFileName,int pixelWidth, double pixelSize, double depthScale) throws IOException{
		
		File input = new File(inputFileName);
		String outputFilename = inputFileName+".scad";
		//File outputInterm = new File(inputFileName+".interm.jpg");
		
		BufferedImage bi = ImageIO.read(input);
		int height = (int) (((double)pixelWidth * (double)bi.getHeight())/(double)bi.getWidth());
		BufferedImage scaled = ImageProcessingFactory.resize(bi, pixelWidth,height);
		BufferedImage gray = ImageProcessingFactory.toGrayScale(scaled);
		
		//ImageIO.write(gray, "jpg", outputInterm);
		  try{
			  // Create file 
			  FileWriter fstream = new FileWriter(outputFilename);
			  BufferedWriter out = new BufferedWriter(fstream);
			  //erase content
			  out.write("");
			  //Close the output stream
			  out.close();
		  }catch (Exception e){//Catch exception if any
			  System.err.println("Error: " + e.getMessage());
		  }
			  
		
		for(int x=0;x<pixelWidth;x++){
			//DXFExport Xprt;
			for(int y=0;y<height;y++){

				String line = "";
				try {
				    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outputFilename, true)));
				    out.println(line);
				    out.close();
				} catch (IOException e) {
				    //oh noes!
				}
			}
		}
		
		
		String exec = "openscad -o "+inputFileName+".stl  -D 'quality=\"production\"' "+outputFilename;
		System.out.println("Running:\n"+exec);
		Process p=Runtime.getRuntime().exec(exec); 
		try {
			p.waitFor();
			BufferedReader reader=new BufferedReader(new InputStreamReader(p.getInputStream())); 
			String line=reader.readLine(); 
			while(line!=null) 
			{ 
				System.out.println(line); 
				line=reader.readLine(); 
			} 
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		System.out.println("Complete!");
		return true;
	}
	
	

}
