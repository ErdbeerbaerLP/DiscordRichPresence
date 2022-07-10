package de.erdbeerbaerlp.discordrpc;


import de.erdbeerbaerlp.discordrpc.client.DRPCEventHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;


public class Message_Message {
    private final String toSend;

    public Message_Message(String toSend) {
        this.toSend = toSend;
    }


    public Object encode(Message_Message a, FriendlyByteBuf b) {
        b.writeByte(0x00);
        b.writeBytes(toSend.getBytes(StandardCharsets.UTF_8));
        return b;
    }


    public Object onMessageReceived(Message_Message a, Supplier<NetworkEvent.Context> b) {
        DRPCEventHandler.serverCustomMessage = toSend;
        b.get().setPacketHandled(true);
        return null;
    }
}
