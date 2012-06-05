package com.neuronrobotics.sdk.bowlercam.server;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

public class ImageURLProvider implements IImageProvider {

	private URL imageSource;
	private BufferedImage bi=null;
	
	public ImageURLProvider(URL im){
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
