package test;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.neuronrobotics.sdk.bowlercam.server.ImageFileProvider;
import com.neuronrobotics.sdk.bowlercam.server.ImageServer;

public class ImageServerTester {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		ImageFileProvider provider = new ImageFileProvider(new File("sample_JPEG.jpg"));
		new ImageServer(8081,provider);
		
		System.out.println("Starting server...");
	}

}
