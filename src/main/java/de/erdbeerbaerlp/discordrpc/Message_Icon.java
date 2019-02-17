package de.erdbeerbaerlp.discordrpc;

import java.util.function.Supplier;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class Message_Icon {

	private String toSend;

	public Message_Icon(String toSend) {
		// TODO Auto-generated constructor stub
		this.toSend = toSend;
	}

	public Object encode(Message_Icon a, PacketBuffer b) {
		// TODO Auto-generated method stub
		b.writeInt(0);
		b.writeString(toSend);
		return b;
	}

	public Object onMessageReceived(Message_Icon a, Supplier<Context> b) {
		// TODO Auto-generated method stub
		DRPCEventHandler.customIco = a.toSend;
		return null;
	}

}
