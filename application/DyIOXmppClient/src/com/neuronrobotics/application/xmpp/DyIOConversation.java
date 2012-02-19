package com.neuronrobotics.application.xmpp;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

import com.neuronrobotics.sdk.dyio.DyIORegestry;


public class DyIOConversation implements IConversation, MessageListener {

	public void processMessage(Chat chat, Message message) {
		Message msg = new Message(message.getFrom(), Message.Type.chat);
	    if(message.getType().equals(Message.Type.chat) && message.getBody() != null) {
	        System.out.println("Received: " + message.getBody()+" from: "+message.getFrom());
	        try {
	        	msg.setBody(onMessage(message.getBody()));
	        	System.out.println("Sending: "+msg.getBody());
	            chat.sendMessage(msg);
	        } catch (XMPPException ex) {
	            ex.printStackTrace();
	            System.out.println("Failed to send message");
	        }
	    } else {
	        System.out.println("I got a message I didn't understand\n\n"+message.getType());
	    }
	}

	@Override
	public String onMessage(String input) {
		if(input.toLowerCase().contains("ping")){
			return "ping: \n"+DyIORegestry.get().ping();
		}else{
			return "unknown command";
		}
	}

}
