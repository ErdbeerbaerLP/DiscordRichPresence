package de.erdbeerbaerlp.discordrpc;

import de.erdbeerbaerlp.guilib.gui.BetterGuiScreen;
import net.minecraft.client.gui.GuiScreen;

public class ConfigGui extends BetterGuiScreen {

	private GuiScreen parentScreen;
	public ConfigGui(GuiScreen parentScreen) {
		super();
		this.parentScreen = parentScreen;
	}

	@Override
	public void buildGui() {
		// TODO Auto-generated method stub

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

	@Override
	public void updateGui() {
		// TODO Auto-generated method stub

	}

}
