package de.erdbeerbaerlp.discordrpc;

import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordRPC;
import club.minnced.discord.rpc.DiscordRichPresence;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;

public class Discord {
    private static final DiscordEventHandlers handlers = new DiscordEventHandlers();
    public static long now = ModClass.gameStarted;
    private static DiscordRichPresence presence = new DiscordRichPresence();
    private static String currentTitle;
    private static String currentSubtitle;
    private static String currentImgKey;
    private static boolean initialized = false;

    /**
     * Disables all calls from this mod allowing to set custom data from another mod
     *
     * @param preventConfigLoad true to prevent loading the config file
     */
    public static void disableModDefault(boolean preventConfigLoad) {
        ModClass.isEnabled = false;
        ModClass.preventConfigLoad = preventConfigLoad;
    }

    /**
     * Disables all calls from this Mod allowing to set custom data from another mod (Still loads configs)
     */
    public static void disableModDefault() {
        disableModDefault(false);
    }

    /**
     * Starts up the discord rich presence service (call in {@link FMLConstructionEvent} or earlier!)
     */
    public static void initDiscord() {
        if (initialized) return;
        DiscordRPC.INSTANCE.Discord_Initialize(RPCconfig.CLIENT_ID, handlers, true, "");
        DRPCLog.Info("Starting Discord");
        Discord.initialized = true;
    }

    /**
     * Same as {@link #initDiscord initDiscord} but allowing you to set an custom Client ID
     *
     * @param clientID <a href="https://discordpy.readthedocs.io/en/rewrite/discord.html"> See here</a>
     */
    public static void customDiscordInit(String clientID) {
        if (initialized) return;
        DiscordRPC.INSTANCE.Discord_Initialize(clientID, handlers, true, "");
        DRPCLog.Info("Starting Discord with client ID " + clientID);
        Discord.initialized = true;
    }

    public static void setPresence(String title, String subtitle, String iconKey) {
        presence.details = title;
        currentTitle = title;
        presence.state = subtitle;
        currentSubtitle = subtitle;
        presence.largeImageKey = iconKey;
        currentImgKey = iconKey;
        presence.startTimestamp = now;

        DiscordRPC.INSTANCE.Discord_UpdatePresence(presence);
    }

    private static String genSecret() {
        return "drpc-test";
    }

    protected static void reloadPresence() {
        setPresence(RPCconfig.NAME, currentSubtitle, currentImgKey);
    }

    protected static String getTitle() {
        return currentTitle;
    }

    protected static String getSubtitle() {
        return currentSubtitle;
    }

    protected static String getImgKey() {
        return currentImgKey;
    }

    protected static DiscordRichPresence getPresence() {
        return presence;
    }

    protected static void shutdown() {
        DiscordRPC.INSTANCE.Discord_Shutdown();
        initialized = false;
    }
}
