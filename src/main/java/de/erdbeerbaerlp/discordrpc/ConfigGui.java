package de.erdbeerbaerlp.discordrpc;

import de.erdbeerbaerlp.guilib.components.Button;
import static de.erdbeerbaerlp.guilib.components.Button.DefaultButtonIcons.*;
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
		backToMenu.visible = false;
		gameName.setText(RPCconfig.NAME);
		joinReqOnServers.setIsChecked(RPCconfig.ENABLE_JOIN_REQUESTS_ON_SERVERS);
		
		addAllComponents(title, cancel, save, general, backToMenu, gameName, gameNameLabel, joinReqOnServers);
		
	}
	
	@Override
	public void updateGui() {
		title.x = width/2-80;
		save.x = width/2-65;
		save.y = height-40;
		cancel.y = save.y;
		cancel.x = save.x+66;
		general.x = width/8;
		general.y = height/3;
		backToMenu.y = save.y;
		backToMenu.x = save.x;
		gameName.x = width/7;
		gameName.y = height/3;
		gameNameLabel.x = gameName.x;
		gameNameLabel.y = gameName.y-15;
		joinReqOnServers.x = gameName.x;
		joinReqOnServers.y = gameName.y + 30;
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