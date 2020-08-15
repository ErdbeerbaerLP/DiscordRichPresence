package de.erdbeerbaerlp.discordrpc;

import de.erdbeerbaerlp.guilib.components.*;
import de.erdbeerbaerlp.guilib.gui.ExtendedScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;


public class ConfigGui extends ExtendedScreen {

    // "Global" components
    private Label title;
    private Button cancel, save, backToMenu;
    private Image discordLogo;

    //Page 0 Components
    private Button general;
    private Button developers;
    private Button serverIntegrations;
    private Button bugReport;

    //Page 1 Components
    private TextField gameName, singleplayerText, multiplayerText;
    private Label gameNameLabel, singleplayerLabel, multiplayerLabel;

    //Page 2 Components
    private CheckBox logToChat, disableConfigMenu;

    //Page 3 Components
    private CheckBox hypixel, hive, customMsg;

    public ConfigGui(Screen parentScreen) {
        super(parentScreen);
    }

    @Override
    public void buildGui() {
        title = new Label("Discord RPC Config", 0, 5);
        discordLogo = new Image(0, 0, 200, 68, false);
        discordLogo.loadImage("https://discordapp.com/assets/fc0b01fe10a0b8c602fb0106d8189d9b.png");
        cancel = new Button(0, 0, 65, "Cancel", Button.DefaultButtonIcons.DELETE);
        save = new Button(0, 0, 65, "Save", Button.DefaultButtonIcons.SAVE);
        backToMenu = new Button(0, 0, 134, "Back", Button.DefaultButtonIcons.ARROW_LEFT);

        general = new Button(0, 0, 120, "General Settings", new ResourceLocation(DRPC.MODID, "textures/gui/buttonicons/discord.png"));
        developers = new Button(0, 0, 146, "Developer Settings", Button.DefaultButtonIcons.FILE);
        serverIntegrations = new Button(0, 0, 270, "Server Integration Settings", new ResourceLocation(DRPC.MODID, "textures/gui/buttonicons/integration.png"));
        bugReport = new Button(0, 0, 99, "Report an Bug", new ResourceLocation(DRPC.MODID, "textures/gui/buttonicons/bug.png"));

        gameNameLabel = new Label("Game Name", 0, 0);
        singleplayerLabel = new Label("Singleplayer Message", 0, 0);
        multiplayerLabel = new Label("Multiplayer Message", 0, 0);
        gameName = new TextField(0, 0, 200);
        singleplayerText = new TextField(0, 0, 200);
        multiplayerText = new TextField(0, 0, 200);

        logToChat = new CheckBox(0, 0, "Enable Log in chat", ClientConfig.LOGTOCHAT.get());
        disableConfigMenu = new CheckBox(0, 0, "Disable Config Menu", ClientConfig.CONFIG_GUI_DISABLED.get());

        hypixel = new CheckBox(0, 0, "Enable Hypixel Integration", ClientConfig.ENABLE_HYPIXEL_INTEGRATION.get());
        hive = new CheckBox(0, 0, "Enable HiveMC Integration", ClientConfig.ENABLE_HIVEMC_INTEGRATION.get());
        customMsg = new CheckBox(0, 0, "Enable Custom Server Integration", ClientConfig.ENABLE_CUSTOM_INTEGRATION.get());


        backToMenu.setTooltips("Back to main config menu");

        general.setTooltips("Some basic settings");
        gameName.setTooltips("The first line of your Rich Presence", "Default: Minecraft 1.12");
        singleplayerText.setTooltips("The second line of the Rich Presence when in singleplayer", "", "PLACEHOLDERS: ", "%coords% - Coordinates (X:??? Y:??? Z:???)", "%world% World name", "%dimensionName% - The name of the dimension", "%dimensionID% - The ID of the current dimension", "%biome% - The current Biome");
        multiplayerText.setTooltips("The default second line of the Rich Presence when in multiplayer", "", "PLACEHOLDERS: ", "%ip%  Server IP", "%online% - Online players", "%coords% - Coordinates (X:??? Y:??? Z:???)", "%max% - Server´s maximum amount of players (unless bungeecord!)", "%otherpl% - Amount of players -1 (except you)", "%dimensionName% - The name of the dimension", "%dimensionID% - The ID of the current dimension", "%biome% - The current Biome");

        developers.setTooltips("Some config entries for developers / modpack creators");
        disableConfigMenu.setTooltips("WARNING:", "This disables the config menu!", "Only use when you really want to disable it!");
        logToChat.setTooltips("Do you want to print log of this mod to ingame chat?");

        serverIntegrations.setTooltips("Configurate how some servers will show up");
        hypixel.setTooltips("When enabled, this mod will show details about your current game if available");
        hive.setTooltips("When enabled, the discord rich presence will show your current game using HiveMCs API");
        customMsg.setTooltips("When enabled, every server can define custom messages using this mod or an spigot plugin", "Also disables hardcoded custom icons and text of not fully integrated servers like mineplex");


        general.assignToPage(0);
        developers.assignToPage(0);
        serverIntegrations.assignToPage(0);
        discordLogo.assignToPage(0);
        save.assignToPage(0);
        cancel.assignToPage(0);
        bugReport.assignToPage(0);

        gameName.assignToPage(1);
        gameNameLabel.assignToPage(1);
        singleplayerText.assignToPage(1);
        singleplayerLabel.assignToPage(1);
        multiplayerText.assignToPage(1);
        multiplayerLabel.assignToPage(1);


        logToChat.assignToPage(2);
        disableConfigMenu.assignToPage(2);


        hypixel.assignToPage(3);
        hive.assignToPage(3);
        customMsg.assignToPage(3);


        save.setClickListener(() -> {
            ClientConfig.LOGTOCHAT.set(logToChat.isChecked());
            ClientConfig.NAME.set(gameName.getText());
            ClientConfig.ENABLE_HIVEMC_INTEGRATION.set(hive.isChecked());
            ClientConfig.ENABLE_HYPIXEL_INTEGRATION.set(hypixel.isChecked());
            ClientConfig.ENABLE_CUSTOM_INTEGRATION.set(customMsg.isChecked());
            ClientConfig.WORLD_MESSAGE.set(singleplayerText.getText());
            ClientConfig.SERVER_MESSAGE.set(multiplayerText.getText());
            ClientConfig.CONFIG_GUI_DISABLED.set(disableConfigMenu.isChecked());
            close();
        });

        bugReport.setClickListener(() -> openURL("https://github.com/ErdbeerbaerLP/DiscordRichPresence/issues", (a) -> {
        }));
        cancel.setClickListener(this::close);
        general.setClickListener(() -> this.setPage(1));
        developers.setClickListener(() -> this.setPage(2));
        serverIntegrations.setClickListener(() -> this.setPage(3));
        backToMenu.setClickListener(() -> this.setPage(0));

        title.setCentered();
        backToMenu.setVisible(false);
        gameName.setText(ClientConfig.NAME.get());
        singleplayerText.setText(ClientConfig.WORLD_MESSAGE.get());
        multiplayerText.setText(ClientConfig.SERVER_MESSAGE.get());


        addAllComponents(
                title,
                discordLogo,
                cancel,
                bugReport,
                save,
                general,
                developers,
                serverIntegrations,
                backToMenu,
                gameName,
                gameNameLabel,
                singleplayerLabel,
                singleplayerText,
                multiplayerLabel,
                multiplayerText,
                logToChat,
                disableConfigMenu,
                hypixel,
                hive,
                customMsg);

    }

    @Override
    public void updateGui() {
        singleplayerText.setEnabled(Minecraft.getInstance().world == null);
        multiplayerText.setEnabled(Minecraft.getInstance().world == null);
        title.setX(width / 2);
        title.setY(10);
        discordLogo.setPosition(width / 2 - discordLogo.getWidth() / 2, 20);
        save.setX(width / 2 - 65);
        save.setY(height - 40);
        cancel.setY(save.getY());
        cancel.setX(save.getX() + 69);
        backToMenu.setY(save.getY());
        backToMenu.setX(save.getX());
        backToMenu.setVisible(getCurrentPage() > 0);

        general.setX(width / 2 - general.getWidth());
        general.setY(discordLogo.getY() + discordLogo.getHeight() + 30);
        developers.setX(general.getX() + general.getWidth() + 4);
        developers.setY(general.getY());
        serverIntegrations.setX(general.getX());
        serverIntegrations.setY(general.getY() + general.getHeight() + 5);
        bugReport.setPosition(cancel.getX() + cancel.getWidth() + 10, cancel.getY());

        gameName.setX(width / 8);
        gameName.setY(height / 5);
        gameNameLabel.setX(gameName.getX());
        gameNameLabel.setY(gameName.getY() - 12);
        singleplayerText.setPosition(gameName.getX(), gameName.getY() + 55);
        singleplayerLabel.setX(singleplayerText.getX());
        singleplayerLabel.setY(singleplayerText.getY() - 12);
        multiplayerText.setPosition(singleplayerText.getX(), singleplayerText.getY() + 55);
        multiplayerLabel.setX(multiplayerText.getX());
        multiplayerLabel.setY(multiplayerText.getY() - 12);

        logToChat.setX(width / 8);
        logToChat.setY(height / 3);
        disableConfigMenu.setPosition(logToChat.getX(), logToChat.getY() + 15);

        hypixel.setPosition(width / 8, height / 4);
        hive.setPosition(hypixel.getX(), hypixel.getY() + 15);
        customMsg.setPosition(hypixel.getX(), hive.getY() + 15);
    }

    @Override
    public boolean doesEscCloseGui() {
        return false;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}