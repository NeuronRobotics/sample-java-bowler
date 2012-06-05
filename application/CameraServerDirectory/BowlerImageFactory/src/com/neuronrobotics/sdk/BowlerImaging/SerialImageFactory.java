package com.neuronrobotics.sdk.BowlerImaging;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

public class SerialImageFactory {
	public static byte[] ImageToArray(BufferedImage img,String type){
		if(img==null){
			return new byte[0];
		}
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			ImageIO.write(img, type ,bos);
			bos.flush();
			bos.close();
		} catch (IOException e) {
			//impossible
		}
		return bos.toByteArray();
	}
	
	public static byte[] JPGImageToArray(BufferedImage img){
		return ImageToArray(img,"jpg");
	}
	public static byte[] PNGImageToArray(BufferedImage img){
		return ImageToArray(img,"png");
	}
	public static byte[] BMPImageToArray(BufferedImage img){
		return ImageToArray(img,"bmp");
	}
	public static BufferedImage ByteArrayToImage(byte [] array) throws IOException{
		BufferedImage image = null;
		image = ImageIO.read(new ByteArrayInputStream(array));
		return image;
	}

}
