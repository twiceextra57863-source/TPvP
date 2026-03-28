package com.mtpvp.mixin;

import com.mtpvp.gui.MtpvpDashboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({TitleScreen.class, GameMenuScreen.class})
public abstract class MenuMixin extends Screen {
    protected MenuMixin(Text title) { super(title); }

    @Inject(at = @At("TAIL"), method = "init")
    private void addMtpvpButton(CallbackInfo ci) {
        // Lunar style placement: Top-Left
        this.addDrawableChild(ButtonWidget.builder(Text.literal("MTPVP"), (button) -> {
            MinecraftClient.getInstance().setScreen(new MtpvpDashboard(this));
        }).dimensions(10, 10, 60, 20).build());
    }
}
