package de.erdbeerbaerlp.discordrpc;

import java.util.function.Supplier;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class Message_Message {
	private String toSend;

	public Message_Message(String toSend) {
		// TODO Auto-generated constructor stub
		this.toSend = toSend;
	}

	public Object encode(Message_Message a, PacketBuffer b) {
		// TODO Auto-generated method stub
		b.writeInt(0);
		b.writeString(toSend);
		return b;
	}

	public Object onMessageReceived(Message_Message a, Supplier<Context> b) {
		DRPCLog.Info("Message-packet received: \"" + a.toSend + "\"! Applying message...");
		DRPCEventHandler.serverCustomMessage = toSend;
		return null;
	}
}
