package com.tpvp.mixin;

import com.mtpvp.gui.MtpvpDashboard;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    protected TitleScreenMixin(Text title) { super(title); }

    @Inject(method = "init", at = @At("TAIL"))
    private void addMtpvpTitleButton(CallbackInfo ci) {
        // Title Screen par "Language" button ke pass set kiya hai
        this.addDrawableChild(ButtonWidget.builder(Text.literal("§bMTPVP"), (button) -> {
            this.client.setScreen(new MtpvpDashboard(this));
        }).dimensions(this.width / 2 + 104, this.height / 4 + 48 + 72 + 12, 45, 20).build());
    }
}
