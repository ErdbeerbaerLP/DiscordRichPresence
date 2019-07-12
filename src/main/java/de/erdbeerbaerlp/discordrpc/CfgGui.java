package de.erdbeerbaerlp.discordrpc;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;

import java.util.ArrayList;
import java.util.List;

/**
 * Gui for the modconfigs menu added by forge
 *
 * @author Max
 */
public class CfgGui extends GuiConfig {

    protected CfgGui(GuiScreen guiScreen) {
        super(guiScreen, getConfigElements(), ModClass.MODID, false, false, GuiConfig.getAbridgedConfigPath(RPCconfig.config.toString()));
    }

    private static List<IConfigElement> getConfigElements() {
        return new ArrayList<>(new ConfigElement(RPCconfig.config.getCategory(RPCconfig.CATEGORY_PRESENCE)).getChildElements());
    }


}
