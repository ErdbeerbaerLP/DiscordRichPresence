package de.erdbeerbaerlp.discordrpc;

import net.arikia.dev.drpc.DiscordEventHandlers;
import net.arikia.dev.drpc.DiscordRPC;
import net.arikia.dev.drpc.DiscordRichPresence;
import net.minecraft.client.Minecraft;

public class Discord {
	private static DiscordRichPresence presence = new DiscordRichPresence();

	public static long now = DRPC.gameStarted;

	private static String currentTitle;
	private static String currentSubtitle;
	private static String currentImgKey;
	private static boolean isDev = false;

	private static DiscordEventHandlers handlers;

	private static boolean initialized = false;

	/**
	 * Disables all calls from this mod allowing to set custom data from another mod
	 * (Still loads configs)
	 * 
	 * @param preventConfigLoad true to prevent loading the config file
	 */
	public static void disableModDefault(boolean preventConfigLoad) {
		DRPC.isEnabled = false;
		DRPC.preventConfigLoad = preventConfigLoad;
	}

	/**
	 * Disables all calls from this Mod allowing to set custom data from another mod
	 * (Still loads configs)
	 */
	public static void disableModDefault() {
		disableModDefault(false);
	}

	/**
	 * Starts up the discord rich presence service (call in
	 * {@link FMLConstructionEvent} or earlier!)
	 */
	public static void initDiscord() {
		if (initialized)
			return;
		DiscordRPC.discordInitialize("511106082366554122", handlers, true);
		DRPCLog.Info("Starting Discord");
		Discord.initialized = true;
	}

	/**
	 * Same as {@link #initDiscord initDiscord} but allowing you to set an custom
	 * Client ID
	 * 
	 * @param clientID <a href=
	 *                 "https://discordpy.readthedocs.io/en/rewrite/discord.html">
	 *                 See here</a>
	 */
	public static void customDiscordInit(String clientID) {
		if (initialized)
			return;
		DiscordRPC.discordInitialize(clientID, handlers, true);
		DRPCLog.Info("Starting Discord with cliend ID " + clientID);
		Discord.initialized = true;
	}

	public static void setPresence(String title, String subtitle, String iconKey, boolean useUUID) {

		presence.details = title;
		currentTitle = title;
		presence.state = subtitle;
		currentSubtitle = subtitle;
		presence.largeImageKey = iconKey;
		currentImgKey = iconKey;
		presence.startTimestamp = now;
		if (useUUID) {
			if (Minecraft.getInstance().getSession().getPlayerID().contains("210f7275c79f44f8a7a07da71c751bb9")) {
				presence.smallImageKey = "4865346365834586";
				presence.smallImageText = "The Developer of this Mod";
				isDev = true;
			}
		}
		DiscordRPC.discordUpdatePresence(presence);
	}

	/**
	 * Sets the DiscordRichPresence
	 * 
	 * @param title    The first line of the RichPresence (Below "Minecraft")
	 * @param subtitle The second line of RichPresence
	 * @param iconKey  The icon key.<br>
	 *                 <B>Default Icon Keys:</b> <br>
	 *                 world,<br>
	 *                 cube,<br>
	 *                 34565655649643693 (Black and white cube),<br>
	 *                 3454083453475893469 (Half color, Half B{@literal &}W cube)
	 */
	public static void setPresence(String title, String subtitle, String iconKey) {
		setPresence(title, subtitle, iconKey, true);
	}

	protected static void reloadPresence() {
		setPresence(ClientConfig.NAME.get(), currentSubtitle, currentImgKey);
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

	protected static boolean isPlayerDev() {
		return isDev;
	}

	protected static DiscordRichPresence getPresence() {
		return presence;
	}
}
