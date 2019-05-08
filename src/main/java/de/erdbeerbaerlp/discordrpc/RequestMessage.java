package de.erdbeerbaerlp.discordrpc;

import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;


public class RequestMessage implements IMessage {
	  // A default constructor is always required
	  public RequestMessage(){}

	  private String toSend;
	  public RequestMessage(String toSend) {
	    this.toSend = toSend;
	  }

	  @Override public void toBytes(ByteBuf buf) {
	    // Writes the int into the buf
		 // buf.writeInt(0);
		  ByteBufUtils.writeUTF8String(buf, toSend);
	  }

	  @Override public void fromBytes(ByteBuf buf) {
	    // Reads the int back from the buf. Note that if you have multiple values, you must read in the same order you wrote.
		  toSend = buf.toString(StandardCharsets.UTF_8).trim();
	  }
	  public static class CommunicationMessageHandler implements IMessageHandler<RequestMessage, IMessage> {
		  // Do note that the default constructor is required, but implicitly defined in this case

		  @Override
		  public IMessage onMessage(RequestMessage message, MessageContext ctx) {
		    DRPCLog.Info("Packet received! "+message.toSend);
		    if(message.toSend.equals("DRPC-Message-Request") && !ModClass.isClient) {
					System.out.println("Client requested Discord RichPresence Message... Trying to send it");
					ModClass.MSG.sendTo(new Message_Message(ServerConfig.SERVER_MESSAGE), ctx.getServerHandler().player);
					ModClass.ICO.sendTo(new Message_Icon(ServerConfig.SERVER_ICON), ctx.getServerHandler().player);
		    }
		    return null;
		  }
		}
	}