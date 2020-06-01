package de.erdbeerbaerlp.customdiscordrpc;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;


public class Customdiscordrpc extends JavaPlugin {
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
        Bukkit.getPluginManager().registerEvent(PlayerJoinEvent.class, new Listener() {
        }, EventPriority.NORMAL, (listener, event) -> {
            PlayerJoinEvent ev = (PlayerJoinEvent) event;
            new Thread(() -> {
                // Do this multiple times to make sure the client receives it, even when client login is slow
                for (int i = 0; i < 10; i++) {
                    sendPackets(ev.getPlayer());
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }, this);
        Bukkit.getPluginManager().registerEvent(PlayerChangedWorldEvent.class, new Listener() {
        }, EventPriority.NORMAL, (listener, event) -> {
            PlayerChangedWorldEvent ev = (PlayerChangedWorldEvent) event;
            sendPackets(ev.getPlayer());
        }, this);
    }

    public void sendMessage(Player p, String PACKET_DATA, String protocol) {
        byte[] message = PACKET_DATA.getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[message.length + 1];
        result[0] = 1;
        System.arraycopy(message, 0, result, 1, message.length);
        p.sendPluginMessage(this, protocol, result);
    }

    public void sendPackets(Player player) {
        this.sendMessage(player, this.discordMsg, "discordrpc:discord-msg");
        this.sendMessage(player, this.discordIconKey, "discordrpc:discord-icon");
    }

}
