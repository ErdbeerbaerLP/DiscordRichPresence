package de.erdbeerbaerlp.discordrpc;

import java.util.function.Supplier;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class RequestMessage {
	private String toSend;
	public RequestMessage(String toSend) {
		// TODO Auto-generated constructor stub
		this.toSend = toSend;
	}



	public PacketBuffer encode(RequestMessage a, PacketBuffer b) {
		// TODO Auto-generated method stub
		b.writeInt(0);
		
		b.writeString(a.toSend);
		return b;
	}



	public Object onMessageReceived(RequestMessage a, Supplier<Context> b) {
	    if(a.toSend.equals("DRPC-Message-Request") && !DRPC.isClient) {
				System.out.println("Client requested Discord RichPresence Message... Trying to send it");
//				DRPC.MSG.sendTo(new Message_Message(ServerConfig.SERVER_MESSAGE.get()), b.get().getSender());
//				DRPC.ICO.sendTo(new Message_Icon(ServerConfig.SERVER_ICON.get()),b.get().getSender());
				DRPC.MSG.sendTo(new Message_Message(ServerConfig.SERVER_MESSAGE.get()), b.get().getSender().connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
				DRPC.ICO.sendTo(new Message_Icon(ServerConfig.SERVER_ICON.get()), b.get().getSender().connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
			    }
	    return null;
	}

}
