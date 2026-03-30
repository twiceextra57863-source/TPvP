package com.yourname.mtpvp.mixin;

import com.yourname.mtpvp.client.gui.MtpvpDashboardScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin extends Screen {
    
    protected TitleScreenMixin(Text title) {
        super(title);
    }
    
    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        this.addDrawableChild(ButtonWidget.builder(Text.literal("⚔️ MTPVP"), button -> {
            if (client != null) {
                client.setScreen(new MtpvpDashboardScreen(this));
            }
        }).dimensions(this.width / 2 - 100, this.height / 4 + 96 + 48 - 30, 200, 20).build());
    }
}
