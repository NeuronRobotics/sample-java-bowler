package test;


import java.io.DataInputStream;
import java.io.DataOutputStream;

import java.net.Socket;

public class CameraTest {

	/**
	 * @param args
	 */
	public static void main(String[] args)throws Exception {
		String host = "localhost";
		int port = 8080;
		byte[] b = new byte[4096];
	  String sentence = "GET / HTTP/1.1\n" +
	  		"Host: "+host+":"+port+"\n\r";
	  String modifiedSentence;
	  Socket clientSocket = new Socket(host, port);
	  DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
	  DataInputStream inFromServer = new DataInputStream(clientSocket.getInputStream());
	  System.out.println("Sending: \n"+sentence);
	  outToServer.writeBytes(sentence);
	  System.out.println("Waiting for response..");
	  while(inFromServer.available()==0) {
		  
	  }
	  System.out.println("Reading..");
	  inFromServer.read(b);
	  modifiedSentence = new String(b);
	  System.out.println("FROM SERVER: \n" + modifiedSentence);
	  clientSocket.close();
	}

}
