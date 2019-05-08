package de.erdbeerbaerlp.discordrpc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.Loader;

public class ServerConfig {

	private static Configuration config = null;
	
	protected static final String CATEGORY_PRESENCE = "RichPresence";
	
	protected static String SERVER_MESSAGE;
	protected static String SERVER_ICON;
	
	protected static void preInit(){
		DRPCLog.Info("Loading serverside config file...");
		File configFile = new File(Loader.instance().getConfigDir(), "DiscordRPC_Server.cfg");
		config = new Configuration(configFile);
		syncFromFiles();
		
	}
	
	protected static Configuration getConfig(){
		return config;
	}
	
	protected static void syncFromFiles(){
		syncConfig(true, true);
	}
	
	
	protected static void syncFromFields(){
		syncConfig(false, false);
	}
	
	private static void syncConfig(boolean loadFromConfigFile, boolean readFieldsFromConfig){
		if(loadFromConfigFile)
			config.load();
		
		Property propMSG = config.get(CATEGORY_PRESENCE, "Message", "Playing on a random Server with %otherpl% other players");
		propMSG.setComment("The second line you have in the rich presence.\n Placeholders are:\n%players% - Amount of all players\n%otherpl% - Amount of players -1 (except you)");
		
		Property propIcon = config.get(CATEGORY_PRESENCE, "IconKey", "world");
		propIcon.setComment("The Icon-Key. Use 'world' or 'cube' if you don´t have an special one.");
		
		
		
		List<String> propOrderPresence = new ArrayList<String>();
		propOrderPresence.add(propMSG.getName());
		propOrderPresence.add(propIcon.getName());
		config.setCategoryPropertyOrder(CATEGORY_PRESENCE, propOrderPresence);
		
		
		if(readFieldsFromConfig){
			SERVER_ICON = propIcon.getString();
			SERVER_MESSAGE = propMSG.getString();
		}
		
		propIcon.set(SERVER_ICON);
		propMSG.set(SERVER_MESSAGE);
		if(config.hasChanged())
			config.save();
	}

	
}
