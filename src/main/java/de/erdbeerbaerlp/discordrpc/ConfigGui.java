package de.erdbeerbaerlp.discordrpc;

import static de.erdbeerbaerlp.guilib.components.Button.DefaultButtonIcons.DELETE;
import static de.erdbeerbaerlp.guilib.components.Button.DefaultButtonIcons.SAVE;

import de.erdbeerbaerlp.guilib.components.Button;
import de.erdbeerbaerlp.guilib.components.Button.DefaultButtonIcons;
import de.erdbeerbaerlp.guilib.components.CheckBox;
import de.erdbeerbaerlp.guilib.components.Label;
import de.erdbeerbaerlp.guilib.components.TextField;
import de.erdbeerbaerlp.guilib.gui.BetterGuiScreen;
import net.minecraft.client.gui.GuiScreen;

public class ConfigGui extends BetterGuiScreen {

	private final GuiScreen parentScreen;

	// "Global" components
	private Label title;
	private Button cancel, save, backToMenu; 

	//Page 0 Components
	private Button general; 

	//Page 1 Components
	private TextField gameName;
	private Label gameNameLabel;
	private CheckBox joinReqOnServers;

	public ConfigGui(GuiScreen parentScreen) {
		super();
		this.parentScreen = parentScreen;
		setAmountOfPages(5);
	}

	@Override
	public void buildGui() {
		title = new Label("Discord RPC Config", 0, 5);
		cancel = new Button(0, 0, 65, "Cancel", DELETE);
		save = new Button(0, 0, 65, "Save",  SAVE);
		general = new Button(0, 0, "General Settings");
		backToMenu = new Button(0, 0, 130, "Back", DefaultButtonIcons.ARROW_LEFT);

		gameName = new TextField(0, 0, 100);
		gameNameLabel = new Label("Game Name", 0, 0);
		joinReqOnServers = new CheckBox(0, 0, "Enable join requests on servers");

		backToMenu.setTooltips("Back to main config menu");
		general.setTooltips("Some basic settings");
		gameName.setTooltips("The first line of your Rich Presence", "Default: Minecraft 1.12");
		joinReqOnServers.setTooltips("Should other discord users be able to send join requests when you are on a server?");

		general.assignToPage(0);
		save.assignToPage(0);
		cancel.assignToPage(0);
		gameName.assignToPage(1);
		gameNameLabel.assignToPage(1);
		joinReqOnServers.assignToPage(1);

		save.setClickListener(()->{
			RPCconfig.ENABLE_JOIN_REQUESTS_ON_SERVERS = joinReqOnServers.isChecked();
			RPCconfig.NAME = gameName.getText();
			RPCconfig.saveChanges();
			openGui(parentScreen);
		});

		cancel.setClickListener(()->{
			openGui(parentScreen);
		});
		general.setClickListener(()->{
			this.setPage(1);
		});
		backToMenu.setClickListener(()->{
			this.setPage(0);
		});


		title.setCentered();
		backToMenu.setVisible(false);
		gameName.setText(RPCconfig.NAME);
		joinReqOnServers.setIsChecked(RPCconfig.ENABLE_JOIN_REQUESTS_ON_SERVERS);

		addAllComponents(title, cancel, save, general, backToMenu, gameName, gameNameLabel, joinReqOnServers);

	}

	@Override
	public void updateGui() {
		title.setX(width/2-80);
		save.setX(width/2-65);
		save.setY(height-40);
		cancel.setY(save.getY());
		cancel.setX(save.getX()+66);
		general.setX(width/8);
		general.setY(height/3);
		backToMenu.setY(save.getY());
		backToMenu.setX(save.getX());
		gameName.setX(width/7);
		gameName.setY(height/3);
		gameNameLabel.setX(gameName.getX());
		gameNameLabel.setY(gameName.getY()-15);
		joinReqOnServers.setX(gameName.getX());
		joinReqOnServers.setY(gameName.getY() + 30);
		backToMenu.setVisible(getCurrentPage() > 0);
	}

	@Override
	public boolean doesEscCloseGui() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean doesGuiPauseGame() {
		// TODO Auto-generated method stub
		return false;
	}



}
