package de.erdbeerbaerlp.discordrpc;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;


public class Message_Icon {

    private String toSend;

    public Message_Icon(final String toSend) {
        this.toSend = toSend;
    }


    public Object encode(Message_Icon a, PacketBuffer b) {
        b.writeByte(0x00);
        b.writeBytes(toSend.getBytes(StandardCharsets.UTF_8));
        return b;
    }

    public Object onMessageReceived(Message_Icon a, Supplier<Context> b) {
        DRPCEventHandler.customIco = a.toSend;
        b.get().setPacketHandled(true);
        return null;
    }

}
