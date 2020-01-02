package de.erdbeerbaerlp.discordrpc;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.Loader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Config Class for Discord RichPresence
 */
public class RPCconfig {

    protected static final String CATEGORY_PRESENCE = "RichPresence";
    /**
     * First line of Rich Presence
     */
    public static String NAME;
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
    protected static Configuration config;
    protected static boolean CONFIG_GUI_ENABLED;
    protected static boolean DEV_COMMANDS;
    protected static String CLIENT_ID;

    static boolean ENABLE_HYPIXEL_INTEGRATION;
    static boolean ENABLE_HIVEMC_INTEGRATION;
    static boolean ENABLE_CUSTOM_INTEGRATION;


    protected static void loadConfigFromFile() {
        File configFile = new File(Loader.instance().getConfigDir(), "DiscordRPC.cfg");
        config = new Configuration(configFile);
        syncConfig(true, true);
    }

    private static Configuration getConfig() {
        return config;
    }

    protected static void reloadConfig() {
        syncConfig(false, true);
        Discord.reloadPresence();
    }

    protected static void saveChanges() {
        syncConfig(false, false);
        Discord.reloadPresence();
    }

    private static void syncConfig(boolean loadFromConfigFile, boolean readFieldsFromConfig) {
        if (loadFromConfigFile)
            config.load();

        Property propertyName = config.get(CATEGORY_PRESENCE, "Client Name", "Minecraft 1.12");
        propertyName.setComment("First line of Rich Presence");

        Property propertyMultiplayer = config.get(CATEGORY_PRESENCE, "Server Text", "Playing on %ip% (%online% + / %max% players)");
        propertyMultiplayer.setComment("Placeholders:\n%ip%  Server IP\n%coords% - Coordinates (X:??? Y:??? Z:???)\n%online% - Online players\n%max% - Server´s maximum amount of players (unless bungeecord!)\n%otherpl% - Amount of players -1 (except you)\n%dimensionName% - The name of the dimension\n%dimensionID% - The ID of the current dimension\n%biome% - The current Biome");
        propertyMultiplayer.setRequiresWorldRestart(true);

        Property propertyInMenu = config.get(CATEGORY_PRESENCE, "MainMenu", "In Main Menu");
        propertyInMenu.setComment("No placeholders supported, Text that shows when you are in the main menu");

        Property propertySingleplayer = config.get(CATEGORY_PRESENCE, "Singleplayer Text", "Playing in %world% (%coords%)");
        propertySingleplayer.setComment("Placeholders:\n%coords% (X:??? Y:??? Z:???)\n%world% World name\n%dimensionName% - The name of the dimension\n%dimensionID% - The ID of the current dimension\n%biome% - The current Biome");
        propertySingleplayer.setRequiresWorldRestart(true);

        Property propDisableConfigGui = config.get(CATEGORY_PRESENCE, "Disable-Config-GUI", false);
        propDisableConfigGui.setComment("Disables config GUI\nRequires config file editing to enable again");

        Property propCliID = config.get(CATEGORY_PRESENCE, "Client ID", "511106082366554122");
        propCliID.setComment("Client ID allowing custom icons to show up");

        Property propEnableDevelopmentCommands = config.get(CATEGORY_PRESENCE, "DevCommands", false);
        propEnableDevelopmentCommands.setComment("Do you want to use development commands?");


        Property propEnableHypixel = config.get(CATEGORY_PRESENCE, "Hypixel-Integration", true);
        propEnableHypixel.setComment("Do you want to use custom Hypixel integration (show what game you are playing and such)?");

        Property propEnableHive = config.get(CATEGORY_PRESENCE, "Hivemc-Integration", true);
        propEnableHive.setComment("Do you want to use custom Hivemc integration (show what game you are playing)?");

        Property propEnableCustomMSG = config.get(CATEGORY_PRESENCE, "Custom-Messages-From-Server", true);
        propEnableCustomMSG.setComment("Do you want servers to send you a customized rich presence text?\nAlso toggles hardcoded custom icons and text of not fully integrated servers like mineplex");


        List<String> order = new ArrayList<>();
        order.add(propertyName.getName());
        order.add(propertyInMenu.getName());
        order.add(propertySingleplayer.getName());
        order.add(propertyMultiplayer.getName());
        order.add(propEnableHypixel.getName());
        order.add(propEnableHive.getName());
        order.add(propEnableCustomMSG.getName());
        order.add(propDisableConfigGui.getName());
        order.add(propCliID.getName());
        order.add(propEnableDevelopmentCommands.getName());
        config.setCategoryPropertyOrder(CATEGORY_PRESENCE, order);


        if (readFieldsFromConfig) {
            NAME = propertyName.getString();
            SERVER_MESSAGE = propertyMultiplayer.getString();
            WORLD_MESSAGE = propertySingleplayer.getString();
            MAIN_MENU_TEXT = propertyInMenu.getString();
            CONFIG_GUI_ENABLED = !propDisableConfigGui.getBoolean();
            CLIENT_ID = propCliID.getString();
            DEV_COMMANDS = propEnableDevelopmentCommands.getBoolean();
            ENABLE_CUSTOM_INTEGRATION = propEnableCustomMSG.getBoolean();
            ENABLE_HYPIXEL_INTEGRATION = propEnableHypixel.getBoolean();
            ENABLE_HIVEMC_INTEGRATION = propEnableHive.getBoolean();
        }

        propertyName.set(NAME);
        propertyName.setDefaultValue(NAME);
        propertyName.setShowInGui(CONFIG_GUI_ENABLED);
        propertySingleplayer.set(WORLD_MESSAGE);
        propertyMultiplayer.set(SERVER_MESSAGE);
        propEnableCustomMSG.set(ENABLE_CUSTOM_INTEGRATION);
        propEnableHive.set(ENABLE_HIVEMC_INTEGRATION);
        propEnableHypixel.set(ENABLE_HYPIXEL_INTEGRATION);
        propertyInMenu.set(MAIN_MENU_TEXT);
        propEnableDevelopmentCommands.set(DEV_COMMANDS);
        propDisableConfigGui.set(!CONFIG_GUI_ENABLED);
        propCliID.set(CLIENT_ID);
        if (config.hasChanged())
            config.save();
    }


}
