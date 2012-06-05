package com.neuronrobotics.sdk.bowlercam.server;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageFileProvider implements IImageProvider {
	
	private File imageSource;
	private BufferedImage bi=null;
	
	public ImageFileProvider(File im){
		imageSource=im;
	}
	@Override
	public BufferedImage getImage() {
		try {
			bi=ImageIO.read(imageSource);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bi;
	}

}
