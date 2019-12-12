package de.erdbeerbaerlp.discordrpc;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

import java.util.function.Supplier;


public class Message_Message
{
	private String toSend;
	
	public Message_Message(String toSend) {
		this.toSend = toSend;
	}
	
	
	public Object encode(Message_Message a, PacketBuffer b) {
		b.writeInt(0);
		b.writeString(toSend);
		return b;
	}



	public Object onMessageReceived(Message_Message a, Supplier<Context> b) {
		DRPCLog.Info("Message-packet received: \""+a.toSend+"\"! Applying message...");
	    DRPCEventHandler.serverCustomMessage = toSend;
	    return null;
	}
}
