package de.erdbeerbaerlp.discordrpc;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent.Context;

import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;


public class RequestMessage
{
	private String toSend;
	
	public RequestMessage(String toSend) {
		this.toSend = toSend;
	}
	
	
	public PacketBuffer encode(RequestMessage a, PacketBuffer b) {
		b.writeByte(0x00);
		b.writeBytes(toSend.getBytes(StandardCharsets.UTF_8));
		return b;
	}



	public Object onMessageReceived(RequestMessage a, Supplier<Context> b) {
	    if(a.toSend.equals("DRPC-Message-Request") && !DRPC.isClient) {
				System.out.println("Client requested Discord RichPresence Message... Trying to send it");
				DRPC.MSG.sendTo(new Message_Message(ServerConfig.SERVER_MESSAGE.get()), b.get().getSender().connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
				DRPC.ICON.sendTo(new Message_Icon(ServerConfig.SERVER_ICON.get()), b.get().getSender().connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
			    }
	    return null;
	}

}
