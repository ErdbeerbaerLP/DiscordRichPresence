package de.erdbeerbaerlp.discordrpc;

import de.erdbeerbaerlp.discordrpc.client.DRPCEventHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;


public class Message_Icon {

    private final String toSend;

    public Message_Icon(final String toSend) {
        this.toSend = toSend;
    }


    public Object encode(Message_Icon a, FriendlyByteBuf b) {
        b.writeByte(0x00);
        b.writeBytes(toSend.getBytes(StandardCharsets.UTF_8));
        return b;
    }

    public Object onMessageReceived(Message_Icon a, Supplier<NetworkEvent.Context> b) {
        DRPCEventHandler.customIco = a.toSend;
        b.get().setPacketHandled(true);
        return null;
    }

}
