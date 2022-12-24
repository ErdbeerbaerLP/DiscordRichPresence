package de.erdbeerbaerlp.discordrpc.client.gui;

import de.erdbeerbaerlp.discordrpc.DRPC;
import de.erdbeerbaerlp.discordrpc.client.ClientConfig;
import de.erdbeerbaerlp.discordrpc.client.Discord;
import de.erdbeerbaerlp.guilib.components.Button;
import de.erdbeerbaerlp.guilib.components.Label;
import de.erdbeerbaerlp.guilib.components.Slider;
import de.erdbeerbaerlp.guilib.gui.ExtendedScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class IngameDiscordGui extends ExtendedScreen {
    private Label title;
    private Button configButton, exitButton;

    private Label noSingleplayerLabel;


    private Button createLobby;
    private Slider capacitySlider;


    private Button leaveLobby;
    private Label connectedPlayers;


    private Label notImplemented;

    public IngameDiscordGui(Screen parentGui) {
        super(parentGui);
    }

    @Override
    public void buildGui() {
        title = new Label("Discord Rich Presence", 0, 5);
        title.setCentered();
        configButton = new Button(0, 0, new ResourceLocation(DRPC.MODID, "textures/gui/buttonicons/gear.png"));
        configButton.setClickListener(() -> Minecraft.getInstance().setScreen(new ConfigGui(getParentGui())));
        configButton.setVisible(!ClientConfig.instance().configGUIDisabled);

        exitButton = new Button(0, 0, Button.DefaultButtonIcons.DELETE);
        exitButton.setClickListener(this::close);

        addAllComponents(title, configButton, exitButton);


        noSingleplayerLabel = new Label("Join requests are unavailable in singleplayer :(", 0, 0);
        noSingleplayerLabel.assignToPage(10);
        noSingleplayerLabel.setCentered();
        addComponent(noSingleplayerLabel);


        capacitySlider = new Slider(0, 0, 150, 20, "Max.", "Players", 2, 10, 4, false, true);
        createLobby = new Button(0, 0, "Create Lobby");
        createLobby.setClickListener(() -> {
            Discord.createLobby(Minecraft.getInstance().getCurrentServer().ip, Minecraft.getInstance().player.getName().getString(), capacitySlider.getValueInt());
            updateCurrentPage();

        });
        createLobby.assignToPage(0);
        capacitySlider.assignToPage(0);

        addAllComponents(createLobby, capacitySlider);


        leaveLobby = new Button(0, 0, "Leave Lobby");
        leaveLobby.setClickListener(() -> {
            final long currentLobbyID = Discord.getCurrentLobbyID();
            if (Discord.isLobbyOwner(currentLobbyID)) {
                Discord.deleteLobby(currentLobbyID);
            } else {
                Discord.leaveLobby(currentLobbyID);
            }
        });
        leaveLobby.assignToPage(5);

        connectedPlayers = new Label(0, 0);
        connectedPlayers.assignToPage(5);

        addAllComponents(leaveLobby, connectedPlayers);


        notImplemented = new Label(0, 0);
        notImplemented.setText("Discord party invitations are not yet implemented!");
        notImplemented.assignToPage(50);
        notImplemented.setCentered();
        addComponent(notImplemented);

    }

    private void updateCurrentPage() {
        if (Minecraft.getInstance().getCurrentServer() == null) setPage(10);
        else {
            if (Discord.getCurrentLobbyID() == -1) {
                setPage(50);//0
            } else {
                setPage(5);
            }
        }
    }

    @Override
    public void updateGui() {
        updateCurrentPage();
        title.setX(width / 2);
        title.setY(10);
        this.exitButton.setX(this.width - this.exitButton.getWidth() - 6);
        this.exitButton.setY(6);
        configButton.setX(exitButton.getX() - configButton.getWidth() - 3);
        configButton.setY(exitButton.getY());

        noSingleplayerLabel.setPosition(width / 2, height / 2);

        createLobby.setPosition(width / 3, height - height / 8);
        capacitySlider.setPosition(createLobby.getX(), createLobby.getY() - 40);


        leaveLobby.setPosition(width / 2, height - 100);
        connectedPlayers.setPosition(leaveLobby.getX() - 100, leaveLobby.getY() - 200);
        if (Discord.getCurrentLobbyID() != -1) {
            leaveLobby.setText(Discord.isLobbyOwner(Discord.getCurrentLobbyID()) ? "Delete Lobby" : "Leave Lobby");

            final List<String> playersInLobby = Discord.getPlayersInLobby(Discord.getCurrentLobbyID());
            StringBuilder b = new StringBuilder();
            for (String player : playersInLobby) {
                b.append(player).append("\n");
            }
            connectedPlayers.setText("Connected Players: \n" + b);
        }

        notImplemented.setPosition(width / 2, height / 2);

    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public boolean doesEscCloseGui() {
        return true;
    }
}
