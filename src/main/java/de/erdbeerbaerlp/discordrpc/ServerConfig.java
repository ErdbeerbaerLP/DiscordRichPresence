package de.erdbeerbaerlp.discordrpc;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
public class ServerConfig {

	public static final ForgeConfigSpec CONFIG_SPEC;
	public static final ServerConfig CONFIG;
	
	public static ConfigValue<String> SERVER_MESSAGE;
	public static ConfigValue<String> SERVER_ICON;
	
	static
	{
		Pair<ServerConfig,ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ServerConfig::new);
		System.out.println("Loading serverside config file...");
		CONFIG = specPair.getLeft();
		CONFIG_SPEC = specPair.getRight();
	}
	
	ServerConfig(ForgeConfigSpec.Builder builder)
	{
		builder.comment("Server RichPresence config").push("RichPresence");
		SERVER_MESSAGE = builder.comment("The second line you have in the rich presence.\n Placeholders are:\n%players% - Amount of all players\n%otherpl% - Amount of players -1 (except you)").define("Message", "Playing on a random Server with %otherpl% other players");
		SERVER_ICON = builder.comment("The Icon-Key. Use 'world' or 'cube' if you don't have an special one.").define("IconKey", "world");
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
