package de.erdbeerbaerlp.customdiscordrpc;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;


public class Customdiscordrpc extends JavaPlugin implements PluginMessageListener
{
    File msgFile = new File("DiscordRPC.cfg");
    Properties config = new Properties();
    String discordMsg = "Missingno.";
    String discordIconKey = "world";
    boolean logging = false;
    
    public void onEnable() {
        try {
            if (!this.msgFile.exists()) {
                this.msgFile.createNewFile();
                System.out.println("Generating new config file!");
                this.config.setProperty("message", "Playing on a random Server with %otherpl% other players");
                this.config.setProperty("iconKey", "world");
                this.config.setProperty("logRequests", "false");
                this.config.store(new FileWriter(this.msgFile),
                                  "DiscordRPC Customizer config file\n\nPlaceholders for message:\n%players% for total players on this server\n%otherpl% for total players -1\n\niconKey: The iconKey used to show an icon.\nYou can use \"cube\" or \"world\" by default.\nContact me if you have an big server and want an iconKey for you server\n");
            }
            
            this.config.load(new FileInputStream(this.msgFile));
            this.discordMsg = this.config.getProperty("message", "Playing on a random Server with %otherpl% other players");
            this.discordIconKey = this.config.getProperty("iconKey", "world");
            this.logging = this.config.getProperty("logRequests", "false").equalsIgnoreCase("true");
        } catch (IOException var2) {
            System.err.println("ERROR READING/CREATING MESSAGE FILE...\n" + var2);
        }
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "discordrpc:discord-msg");
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "discordrpc:discord-icon");
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "discordrpc:discord-req", this);
    }
    
    public void sendMessage(Player p, String PACKET_DATA, String protocol) {
        byte[] message = PACKET_DATA.getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[message.length + 1];
        result[0] = 1;
        System.arraycopy(message, 0, result, 1, message.length);
        p.sendPluginMessage(this, protocol, result);
    }
    
    public void onPluginMessageReceived(String arg0, Player arg1, byte[] arg2) {
        String str = (new String(arg2, StandardCharsets.UTF_8)).trim();
        if (arg0.equals("discordrpc:discord-req") && str.equals("DRPC-Message-Request")) {
            if(logging) System.out.println("Client requested Discord RichPresence Message... Trying to send it");
            this.sendMessage(arg1, this.discordMsg, "discordrpc:discord-msg");
            this.sendMessage(arg1, this.discordIconKey, "discordrpc:discord-icon");
        }
        
    }
}
