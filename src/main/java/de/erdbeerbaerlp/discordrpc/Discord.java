package de.erdbeerbaerlp.discordrpc;

import com.github.psnrigner.discordrpcjava.DiscordEventHandler;
import com.github.psnrigner.discordrpcjava.DiscordJoinRequest;
import com.github.psnrigner.discordrpcjava.DiscordRichPresence;
import com.github.psnrigner.discordrpcjava.DiscordRpc;
import com.github.psnrigner.discordrpcjava.ErrorCode;

import net.minecraft.client.Minecraft;

public class Discord {
	private static DiscordRichPresence presence = new DiscordRichPresence();
	private static DiscordRpc discord = new DiscordRpc();
	
	public static long now = DRPC.gameStarted;
	
	private static String currentTitle;
	private static String currentSubtitle;
	private static String currentImgKey;
	private static boolean isDev = false;
    private static DiscordEventHandler handlers = new DiscordEventHandler()
    {
        @Override
        public void ready()
        {
            System.err.println("READY");
        }

        @Override
        public void disconnected(ErrorCode errorCode, String message)
        {
            System.err.println("DISCONNECTED : " + errorCode + " " + message);
        }

        @Override
        public void errored(ErrorCode errorCode, String message)
        {
            System.err.println("ERRORED : " + errorCode + " " + message);
        }

        @Override
        public void joinGame(String joinSecret)
        {
            System.err.println("JOIN GAME : " + joinSecret);
        }

        @Override
        public void spectateGame(String spectateSecret)
        {
            System.err.println("SPECTATE GAME : " + spectateSecret);
        }

        @Override
        public void joinRequest(DiscordJoinRequest joinRequest)
        {
            System.err.println("JOIN REQUEST : " + joinRequest);
        }
    };

	private static boolean initialized = false;
	/**
	 * Disables all calls from this mod allowing to set custom data from another mod (Still loads configs)
	 * @param preventConfigLoad true to prevent loading the config file
	 */
	public static void disableModDefault(boolean preventConfigLoad) {
		DRPC.isEnabled = false;
		DRPC.preventConfigLoad = preventConfigLoad;
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
		discord.init("511106082366554122", handlers, true,null);
		DRPCLog.Info("Starting Discord");
		Discord.initialized  = true;
	}
	/**
	 * Same as {@link #initDiscord initDiscord} but allowing you to set an custom Client ID
	 * @param clientID <a href="https://discordpy.readthedocs.io/en/rewrite/discord.html"> See here</a>
	 */
	public static void customDiscordInit(String clientID) {
		if(initialized) return;
		discord.init(clientID, handlers, true, null);
		DRPCLog.Info("Starting Discord with cliend ID "+clientID);
		Discord.initialized  = true;
	}
	public static void setPresence(String title, String subtitle, String iconKey, boolean useUUID){
		
		presence.setDetails(title);
		currentTitle = title;
		presence.setState(subtitle);
		currentSubtitle = subtitle;
		presence.setLargeImageKey(iconKey);
		currentImgKey = iconKey;
		presence.setStartTimestamp(now);
		if(useUUID){
		if(Minecraft.getInstance().getSession().getPlayerID().contains("210f7275c79f44f8a7a07da71c751bb9")){
			presence.setSmallImageKey("4865346365834586");
			presence.setSmallImageText("The Developer of this Mod");
			isDev = true;
		}
		}
		discord.updatePresence(presence);
		}
	/**
	 * Sets the DiscordRichPresence
	 * @param title The first line of the RichPresence (Below "Minecraft")
	 * @param subtitle The second line of RichPresence
	 * @param iconKey The icon key.<br> <B>Default Icon Keys:</b> <br>world,<br> cube,<br> 34565655649643693 (Black and white cube),<br> 3454083453475893469 (Half color, Half B{@literal &}W cube)
	 */
	public static void setPresence(String title,String subtitle, String iconKey){
		setPresence(title, subtitle, iconKey, true);
	}
	
	protected static void reloadPresence() {
		setPresence(ClientConfig.NAME.get(), currentSubtitle, currentImgKey);
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
	protected static boolean isPlayerDev(){
		return isDev;
	}
	protected static DiscordRichPresence getPresence(){
		return presence;
	}
	protected static void shutdown() {
		discord.shutdown();
	}
}
