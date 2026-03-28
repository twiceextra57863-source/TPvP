package com.tpvp.mixin;

import com.mtpvp.gui.MtpvpDashboard;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends Screen {
    protected GameMenuScreenMixin(Text title) { super(title); }

    @Inject(method = "initWidgets", at = @At("TAIL"))
    private void addMtpvpSettingsButton(CallbackInfo ci) {
        // Positioned higher to avoid overlapping Disconnect button
        int x = this.width / 2 - 102;
        int y = this.height / 4 + 48; // Adjusted from 80/104 to 48 (Safe spot)

        this.addDrawableChild(ButtonWidget.builder(Text.literal("§b§lMTPVP SETTINGS"), (button) -> {
            this.client.setScreen(new MtpvpDashboard(this));
        }).dimensions(x, y, 204, 20).build());
    }
}
