package com.mtpvp.mixin;

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
public abstract class GameMenuMixin extends Screen {

    protected GameMenuMixin(Text title) {
        super(title);
    }

    @Inject(at = @At("TAIL"), method = "init")
    private void addMtpvpPauseButton(CallbackInfo info) {
        // Escape menu mein button ko bilkul niche right side mein rakha hai
        // Position: bottom right se thoda upar
        this.addDrawableChild(ButtonWidget.builder(Text.literal("§6Mtpvp §fSettings"), (button) -> {
            if (this.client != null) {
                this.client.setScreen(new MtpvpDashboard(this));
            }
        }).dimensions(this.width - 100, this.height - 30, 90, 20).build());
    }
}
