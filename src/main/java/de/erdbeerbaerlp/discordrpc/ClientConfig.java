package de.erdbeerbaerlp.discordrpc;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
public class ClientConfig {


	public static final ForgeConfigSpec CONFIG_SPEC;

	public static final ClientConfig CONFIG;

	public static ConfigValue<String> NAME;
	public static ConfigValue<String> SERVER_MESSAGE;
	public static BooleanValue DEV_COMMANDS;
	public static ConfigValue<String> MENU_TEXT;
	public static ConfigValue<String> WORLD_MESSAGE;
	public static BooleanValue PREVENT_CLIENT_NAME_CHANGE;
	public static ConfigValue<String> CLIENT_ID;


	static {
		Pair<ClientConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ClientConfig::new);
		System.out.println("Loading clientside config file...");
		CONFIG = specPair.getLeft();
		CONFIG_SPEC = specPair.getRight();
		
	}

	ClientConfig(ForgeConfigSpec.Builder builder) {
		builder.comment("Discord RichPresence Config File").push("Rich-Presence");
		NAME = builder.comment("First line of Rich Presence").define("Client-Name", "Forge 1.16");
		SERVER_MESSAGE = builder.comment("Placeholders:\n%ip%  Server IP").define("Server-Text", "Playing on %ip%");
		MENU_TEXT = builder.comment("No placeholders supported, Text that shows when you are in the main menu").define("Main-Menu", "In Main Menu");
		WORLD_MESSAGE = builder.comment("Placeholders:\n%coords% (X:??? Y:??? Z:???)\n%world% World name").define("Singleplayer-Text", "Playing in %world% (%coords%)");
		PREVENT_CLIENT_NAME_CHANGE = builder.comment("Setting this to true disables name changing through GUI").define("Disable-Name-changing", false);
		DEV_COMMANDS = builder.comment("Do you want to use development commands?").define("Dev-Commands", false);
		CLIENT_ID = builder.comment("Custom client id, see https://github.com/ErdbeerbaerLP/DiscordRichPresence/wiki/Set-up-custom-Icons-(for-Modpacks) for more info").define("client-id", "511106082366554122");
		builder.pop();
	}
    @SubscribeEvent
    public static void onLoad(final ModConfig.Loading configEvent) {
        LogManager.getLogger().info("Loaded drpc config file {}", configEvent.getConfig().getFileName());
    }

    @SubscribeEvent
	public static void onFileChange(final ModConfig.Reloading configEvent) {
		System.out.println("DRPC config just got changed on the file system!");
	}
}
