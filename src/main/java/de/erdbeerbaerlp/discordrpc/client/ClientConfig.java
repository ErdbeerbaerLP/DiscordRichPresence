package de.erdbeerbaerlp.discordrpc.client;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlComment;
import com.moandjiezana.toml.TomlIgnore;
import com.moandjiezana.toml.TomlWriter;

import java.io.File;
import java.io.IOException;

public class ClientConfig {

    @TomlIgnore
    private static final File configFile = new File("./config/DiscordRichPresence.toml");
    @TomlIgnore
    private static ClientConfig INSTANCE;

    static {

        INSTANCE = new ClientConfig();
    }

    @TomlComment("First line of Rich Presence")
    public String name = "Forge 1.19";
    @TomlComment("Placeholders:\n%ip%  Server IP")
    public String serverMessage = "Playing on %ip%";
    @TomlComment("No placeholders supported, Text that shows when you are in the main menu")
    public String menuText = "In Main Menu";
    @TomlComment("Placeholders:\n%coords% (X:??? Y:??? Z:???)\n%world% World name")
    public String worldMessage = "Playing in %world% (%coords%)";
    @TomlComment("Custom client id, see https://github.com/ErdbeerbaerLP/DiscordRichPresence/wiki/Set-up-custom-Icons-(for-Modpacks) for more info")
    public String clientID = "511106082366554122";
    @TomlComment("Do you want to use custom Hypixel integration (show what game you are playing and such)?")
    public boolean hypixelIntegration = true;
    @TomlComment("Do you want servers to send you a customized rich presence text?\nAlso toggles hardcoded custom icons and text of not fully integrated servers like mineplex")
    public boolean customIntegration = true;
    @TomlComment("Disables config GUI\nRequires config file editing to enable again")
    public boolean configGUIDisabled = false;
    @TomlComment("Setting this to true disables name changing through GUI")
    public boolean preventClientNameChange = false;

    public static ClientConfig instance() {
        return INSTANCE;
    }

    public void loadConfig() throws IOException, IllegalStateException {
        if (!configFile.exists()) {
            INSTANCE = new ClientConfig();
            INSTANCE.saveConfig();
            return;
        }
        INSTANCE = new Toml().read(configFile).to(ClientConfig.class);
        INSTANCE.saveConfig(); //Re-write the config so new values get added after updates
    }

    public void saveConfig() throws IOException {
        if (!configFile.exists()) {
            if (!configFile.getParentFile().exists()) configFile.getParentFile().mkdirs();
            configFile.createNewFile();
        }
        final TomlWriter w = new TomlWriter.Builder()
                .indentValuesBy(2)
                .indentTablesBy(4)
                .padArrayDelimitersBy(2)
                .build();
        w.write(this, configFile);
    }

}
