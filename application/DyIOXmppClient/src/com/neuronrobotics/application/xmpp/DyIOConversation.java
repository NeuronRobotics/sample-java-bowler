package com.neuronrobotics.application.xmpp;

import java.util.EnumSet;

import javax.swing.text.html.Option;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

import com.neuronrobotics.sdk.dyio.DyIOChannelMode;
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
		String [] packet = input.split("\\ ");
		if(packet[0].toLowerCase().contains("ping")){
			return "ping: \n"+DyIORegestry.get().ping();
		}else if(packet[0].toLowerCase().contains("state")){
			return "state: \n"+DyIORegestry.get().toString();
		}else if(packet[0].toLowerCase().contains("setmode")){
			DyIOChannelMode m = DyIOChannelMode.DIGITAL_IN;
			boolean found = false;
			String options = "";
			for(DyIOChannelMode cm : EnumSet.allOf(DyIOChannelMode.class)) {
				options+=cm.toSlug()+"\n";
				if(packet[2].toLowerCase().equals(cm.toSlug())){
					m=cm;
					found = true;
				}
			}
			try{
				int port = Integer.parseInt(packet[1]);
				if(found && DyIORegestry.get().getChannel(port).canBeMode(m)){
					DyIORegestry.get().setMode(port, m);
					return "setMode "+port+" "+m.toSlug();
				}
			}catch(Exception ex){
				ex.printStackTrace();
			}
			return "error: Mode not settible on channel #"+packet[1]+" mode options are:\n"+options+"\n"+DyIORegestry.get().toString();
		}else if(packet[0].toLowerCase().contains("setvalue")){
			int port = Integer.parseInt(packet[1]);
			int value = Integer.parseInt(packet[2]);
			DyIORegestry.get().getChannel(port).setValue(value);
			return "setValue "+port+" "+value;
		}else if(packet[0].toLowerCase().contains("getvalue")){
			int port = Integer.parseInt(packet[1]);
			int value = DyIORegestry.get().getChannel(port).getValue();
			return "getValue "+port+" "+value;
		}else{
			return help();
		}
	}
	
	private String help(){
		String s="This is a REPL loop for talking to the DyIO\n" +
				"Commands use a command name, which DyIO port your connected to, and a value\n" +
				"The 3 fields are seperated by a single space charrector\n" +
				"The name is a string, and the 2 data fields are integers\n" +
				"If a field is unused, it will be displayed as 'none'\n" +
				"Commands are: \n" ;
		s+="ping \tnone \tnone :returns ping message\n";
		s+="state \tnone \tnone :returns state information\n";
		s+="setMode \t(int)channel \t(String)mode :returns the mode if successful, 'error' if not sucessful\n";
		s+="setValue \t(int)channel \t(int)value :returns the value if successful, 'error' if not sucessful\n";
		s+="getValue \t(int)channel \tnone :returns (int)value\n";
		return s;
	}

}
