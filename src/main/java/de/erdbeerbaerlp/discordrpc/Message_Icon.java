package de.erdbeerbaerlp.discordrpc;

import java.nio.charset.StandardCharsets;

import org.apache.logging.log4j.Level;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;


public class Message_Icon implements IMessage {
	  // A default constructor is always required
	  public Message_Icon(){}

	  private String toSend;
	  public Message_Icon(String toSend) {
	    this.toSend = toSend;
	  }

	  @Override public void toBytes(ByteBuf buf) {
	    // Writes the int into the buf
		  buf.writeInt(0);
		  ByteBufUtils.writeUTF8String(buf, toSend);
	  }

	  @Override public void fromBytes(ByteBuf buf) {
	    // Reads the int back from the buf. Note that if you have multiple values, you must read in the same order you wrote.
		  toSend = buf.toString(StandardCharsets.UTF_8).trim();
	  }
	  public static class ICOReceiveHandler implements IMessageHandler<Message_Icon, IMessage> {
		  // Do note that the default constructor is required, but implicitly defined in this case

		  @Override
		  public IMessage onMessage(Message_Icon message, MessageContext ctx) {
		    //Log.Info("Packet received! "+message.toSend);
		    DRPCEventHandler.customIco = message.toSend;
		    return null;
		  }
		}
	}