package de.erdbeerbaerlp.discordrpc;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;

import javax.annotation.Nullable;
import java.util.Set;

@SuppressWarnings("unused")
public class CfgGuiFactory implements IModGuiFactory {

    @Override
    public void initialize(Minecraft minecraftInstance) {

    }

    @Override
    public boolean hasConfigGui() {
        return RPCconfig.CONFIG_GUI_ENABLED;
    }

    @Override
    public GuiScreen createConfigGui(GuiScreen parentScreen) {
        return RPCconfig.CONFIG_GUI_ENABLED ? (new ConfigGui(parentScreen)) : null;
    }

    @Nullable
    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
        return null;
    }


}
