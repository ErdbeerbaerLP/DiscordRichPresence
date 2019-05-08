package de.erdbeerbaerlp.discordrpc;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;

/**
 * Gui for the modconfigs menu added by forge
 * @author Max
 *
 */
public class CfgGui extends GuiConfig {

	private static  List<IConfigElement> getConfigElements() {
		List<IConfigElement> elements = new ArrayList<IConfigElement>();
		elements.addAll(new ConfigElement(RPCconfig.config.getCategory(RPCconfig.CATEGORY_PRESENCE)).getChildElements());
		return elements;
	}

	protected CfgGui(GuiScreen guiScreen) {
		super(guiScreen, getConfigElements(), ModClass.MODID, false, false, GuiConfig.getAbridgedConfigPath(RPCconfig.config.toString()));
	}


}
