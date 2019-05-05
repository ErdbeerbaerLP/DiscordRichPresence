package de.erdbeerbaerlp.discordrpc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Level;

import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Config Class for Discord RichPresence
 */
public class RPCconfig {

	protected static Configuration config;
	
	protected static final String CATEGORY_PRESENCE = "RichPresence";
	
	
	/**
	 * First line of Rich Presence
	 */
	public static String NAME;
	protected static boolean NAME_CHANGING_ALLOWED;
	/**
	 * Text displayed when in main menu
	 */
	public static String MAIN_MENU_TEXT;
	/**
	 * Text when playing on a server
	 */
	public static String SERVER_MESSAGE;
	/**
	 * Text displayed when in singleplayer
	 */
	public static String WORLD_MESSAGE;
	private static FirstLaunchWindow w = new FirstLaunchWindow();
	
	protected static boolean DEV_COMMANDS;
	
	protected static void loadConfigFromFile(){
		File configFile = new File(Loader.instance().getConfigDir(), "DiscordRPC.cfg");
		if(!configFile.exists()) {
			w.setVisible(true);
		}
		config = new Configuration(configFile);
		syncConfig(true, true);
		
	}
	
	private static Configuration getConfig(){
		return config;
	}


	protected static void reloadConfig(){
		syncConfig(false, true);
		Discord.reloadPresence();
	}
	
	private static void syncConfig(boolean loadFromConfigFile, boolean readFieldsFromConfig){
		if(loadFromConfigFile)
			config.load();
		
		Property propertyName = config.get(CATEGORY_PRESENCE, "Client Name", w.getClientName());
		propertyName.setComment("First line of Rich Presence");
		
		Property propertyMultiplayer = config.get(CATEGORY_PRESENCE, "Server Text", "Playing on %ip%");
		propertyMultiplayer.setComment("Placeholders:\n%ip%  Server IP");
		propertyMultiplayer.setRequiresWorldRestart(true);
		
		Property propertyInMenu = config.get(CATEGORY_PRESENCE, "MainMenu", "In Main Menu");
		propertyInMenu.setComment("No placeholders supported, Text that shows when you are in the main menu");
		
		Property propertySingleplayer = config.get(CATEGORY_PRESENCE, "Singleplayer Text", "Playing in %world% (%coords%)");
		propertySingleplayer.setComment("Placeholders:\n%coords% (X:??? Y:??? Z:???)\n%world% World name");
		propertySingleplayer.setRequiresWorldRestart(true);
		
		Property propDisableNameChange = config.get(CATEGORY_PRESENCE, "Disable Name changing", false);
		propDisableNameChange.setComment("Setting this to true disables name changing through GUI");
		propDisableNameChange.setShowInGui(false);
		
		Property propEnableDevelopmentCommands = config.get(CATEGORY_PRESENCE, "DevCommands", false);
		propEnableDevelopmentCommands.setComment("Do you want to use development commands?");
		
		List<String> propOrderPresence = new ArrayList<String>();
		propOrderPresence.add(propertyName.getName());
		propOrderPresence.add(propertyInMenu.getName());
		propOrderPresence.add(propertySingleplayer.getName());
		propOrderPresence.add(propertyMultiplayer.getName());
		propOrderPresence.add(propDisableNameChange.getName());
		propOrderPresence.add(propEnableDevelopmentCommands.getName());
		config.setCategoryPropertyOrder(CATEGORY_PRESENCE, propOrderPresence);
		
		
		if(readFieldsFromConfig){
			NAME = propertyName.getString();
			SERVER_MESSAGE = propertyMultiplayer.getString();
			WORLD_MESSAGE = propertySingleplayer.getString();
			MAIN_MENU_TEXT = propertyInMenu.getString();
			NAME_CHANGING_ALLOWED = !propDisableNameChange.getBoolean();
			DEV_COMMANDS = propEnableDevelopmentCommands.getBoolean();
		}
		
		propertyName.set(NAME);
		propertyName.setDefaultValue(NAME);
		propertyName.setShowInGui(NAME_CHANGING_ALLOWED);
		propertySingleplayer.set(WORLD_MESSAGE);
		propertyMultiplayer.set(SERVER_MESSAGE);
		propertyInMenu.set(MAIN_MENU_TEXT);
		propEnableDevelopmentCommands.set(DEV_COMMANDS);
		if(config.hasChanged())
			config.save();
	}

	
}
