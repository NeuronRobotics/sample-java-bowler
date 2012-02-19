package com.neuronrobotics.application.xmpp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.jivesoftware.smack.XMPPException;

import com.neuronrobotics.application.xmpp.GoogleChat.GoogleChatEngine;
import com.neuronrobotics.sdk.common.BowlerAbstractConnection;
import com.neuronrobotics.sdk.dyio.DyIO;
import com.neuronrobotics.sdk.dyio.DyIORegestry;
import com.neuronrobotics.sdk.ui.ConnectionDialog;

public class DyIOChatBot implements IConversationFactory{

	/**
	 * @param args
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) {
		if(args.length<1){
			System.err.println("You must specify a login credentials file");
		}
		FileInputStream f = null;
		try {
			f= new FileInputStream(new File(args[0]));
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
			System.err.println(args[0]+" is not a valid file.\nYou must specify a login credentials file");
			System.exit(1);
		}
        System.out.println("Starting IM client");
        BowlerAbstractConnection c = ConnectionDialog.promptConnection();
        if(c==null){
        	System.out.println("No DyIO to talk to");
        	System.exit(2);
        }
        DyIO.disableFWCheck();
        DyIORegestry.setConnection(c);
        GoogleChatEngine eng = null;
		try {
			eng = new GoogleChatEngine(new DyIOConversationFactory(),f);
			//eng.startChat("mad.hephaestus@gmail.com");
		} catch (XMPPException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.exit(1);
		}
        
//		try {
//			eng.getChats().get(0).sendMessage("Test From Artillect");
//		} catch (XMPPException e1) {
//			e1.printStackTrace();
//		}       
		System.out.println("Press enter to disconnect");
        
        try {
            System.in.read();
        } catch (IOException ex) {
            //ex.printStackTrace();
        }
        DyIORegestry.disconnect();
		eng.disconnect();
		System.out.println("Disconnect ok!");
		System.exit(0);
	}

	@Override
	public IConversation getConversation() {
		// TODO Auto-generated method stub
		return null;
	}

}
