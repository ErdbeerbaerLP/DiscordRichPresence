package de.erdbeerbaerlp.discordrpc;

import net.arikia.dev.drpc.DiscordEventHandlers;
import net.arikia.dev.drpc.DiscordRPC;
import net.arikia.dev.drpc.DiscordRPC.DiscordReply;
import net.arikia.dev.drpc.callbacks.ReadyCallback;
import net.arikia.dev.drpc.DiscordRichPresence;
import net.arikia.dev.drpc.DiscordUser;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;

public class Discord {
	private static DiscordRichPresence presence = new DiscordRichPresence();

	public static long now = ModClass.gameStarted;

	private static String currentTitle;
	private static String currentSubtitle;
	private static String currentImgKey;
	private static boolean joinRequestEnabled = false;

	private static DiscordEventHandlers handlers = new DiscordEventHandlers.Builder().setReadyEventHandler(new ReadyCallback() {
		
		@Override
		public void apply(DiscordUser user) {
			System.out.println("READY, Logged in as "+user.username);
		}
	}).setDisconnectedEventHandler((error,message)->{
		System.out.println(error);
		System.out.println(message);
	}).setJoinRequestEventHandler((usr)->{
		System.out.println(usr.username+" Wants to join! (Accepting as test)");
		new Thread() {
			public void run() {
				
				try {
					sleep(1000);
				} catch (InterruptedException e) {
				}
				DiscordRPC.discordRespond(usr.userId, DiscordReply.YES);
			}
		}.start();
		
	}).setJoinGameEventHandler((secret)->{
		System.out.println(secret);
	}).setDisconnectedEventHandler((error, msg)->{
		System.out.println("DISCONNECTED! "+error+" "+msg);
	}).build();
	
	
	private static Thread dcUpdateThread = new Thread() {
		{
			setName("Discord Update Thread");
		}
		public void run() {
			while(true) {
			DiscordRPC.discordRunCallbacks();
//			System.out.println("Callback");
			try {
				sleep(500);
			} catch (InterruptedException e) {}
		}}
	};
	
	private static boolean initialized = false;
	
	/**
	 * Disables all calls from this mod allowing to set custom data from another mod
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
		if(initialized) return;
		DiscordRPC.discordInitialize("511106082366554122", handlers, true);
		DRPCLog.Info("Starting Discord");
		Discord.initialized  = true;
		if(!dcUpdateThread.isAlive()) dcUpdateThread.start();
	}
	/**
	 * Same as {@link #initDiscord initDiscord} but allowing you to set an custom Client ID
	 * @param clientID <a href="https://discordpy.readthedocs.io/en/rewrite/discord.html"> See here</a>
	 */
	public static void customDiscordInit(String clientID) {
		if(initialized) return;
		DiscordRPC.discordInitialize(clientID, handlers, true);
		DRPCLog.Info("Starting Discord with cliend ID "+clientID);
		Discord.initialized  = true;
		if(!dcUpdateThread.isAlive()) dcUpdateThread.start();
	}
	
	public static void setPresence(String title, String subtitle, String iconKey){
		presence.details = title;
		currentTitle = title;
		presence.state = subtitle;
		currentSubtitle = subtitle;
		presence.largeImageKey = iconKey;
		currentImgKey = iconKey;
		presence.startTimestamp = now;
		
		if(Minecraft.getMinecraft().getCurrentServerData() != null &&Minecraft.getMinecraft().getCurrentServerData().serverIP != "localhost") presence.joinSecret = genSecret();
		DiscordRPC.discordUpdatePresence(presence);
	}

	private static String genSecret() {
		return "drpc-test";
	}
	protected static void reloadPresence() {
		setPresence(RPCconfig.NAME, currentSubtitle, currentImgKey);
	}

	protected static String getTitle(){
		return currentTitle;
	}
	protected static String getSubtitle(){
		return currentSubtitle;
	}
	protected static String getImgKey(){
		return currentImgKey;
	}
	protected static DiscordRichPresence getPresence(){
		return presence;
	}
	protected static void shutdown() {
		dcUpdateThread.interrupt();
		DiscordRPC.discordShutdown();
		initialized = false;
	}
	public static void enableJoinRequest() {
		joinRequestEnabled = true;
	}
	public static void disableJoinRequest() {
		joinRequestEnabled = false;
	}
}
