package de.erdbeerbaerlp.discordrpc;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;


public class Message_Message {
    private String toSend;

    public Message_Message(String toSend) {
        this.toSend = toSend;
    }


    public Object encode(Message_Message a, PacketBuffer b) {
        b.writeByte(0x00);
        b.writeBytes(toSend.getBytes(StandardCharsets.UTF_8));
        return b;
    }


    public Object onMessageReceived(Message_Message a, Supplier<Context> b) {
        DRPCEventHandler.serverCustomMessage = toSend;
        b.get().setPacketHandled(true);
        return null;
    }
}
