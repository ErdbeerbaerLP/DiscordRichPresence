package de.erdbeerbaerlp.discordrpc;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

import java.util.function.Supplier;


public class Message_Icon
{
	
	private String toSend;
	
	public Message_Icon(String toSend) {
		this.toSend = toSend;
	}



	public Object encode(Message_Icon a, PacketBuffer b) {
		b.writeInt(0);
		b.writeString(toSend);
		return b;
	}
	
	public Object onMessageReceived(Message_Icon a, Supplier<Context> b) {
		DRPCEventHandler.customIco = a.toSend;
		return null;
	}

}
