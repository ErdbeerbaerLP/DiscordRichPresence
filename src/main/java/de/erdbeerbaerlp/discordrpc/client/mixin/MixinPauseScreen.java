package de.erdbeerbaerlp.discordrpc.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import de.erdbeerbaerlp.discordrpc.DRPC;
import de.erdbeerbaerlp.discordrpc.client.gui.IngameDiscordGui;
import de.erdbeerbaerlp.guilib.components.Button;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PauseScreen.class)
public abstract class MixinPauseScreen extends Screen {
    private final Button discordButton = new Button(0, 0, new ResourceLocation(DRPC.MODID, "textures/gui/buttonicons/discord.png")) {
        @Override
        public void mouseClick(double mouseX, double mouseY, int mouseButton) {
            if (clicked(mouseX, mouseY)) {

                Minecraft.getInstance().setScreen(new IngameDiscordGui(MixinPauseScreen.this));
            }
        }
    };

    protected MixinPauseScreen(Component p_96550_) {
        super(p_96550_);
    }

    @Inject(method = "createPauseMenu", at = @At("RETURN"))
    public void createPauseMenu(CallbackInfo ci) {
        discordButton.setPosition(this.width / 2 - 102 - 70, this.height / 4 + 96 + -16);
        addRenderableWidget(discordButton);

    }

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;IIF)V", at = @At(value = "HEAD"))
    public void render(PoseStack p_96310_, int p_96311_, int p_96312_, float p_96313_, CallbackInfo ci) {
        discordButton.setPosition(this.width / 2 - 102 - 19, this.height / 4 + 96 + -16);
        discordButton.render(p_96310_, p_96311_, p_96312_, p_96313_);

    }


}
