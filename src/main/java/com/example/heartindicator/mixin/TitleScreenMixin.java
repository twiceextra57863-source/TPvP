package com.example.heartindicator.mixin;

import com.example.heartindicator.client.HeartIndicatorClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {
    @Inject(method = "init", at = @At("TAIL"))
    private void addHeartButton(CallbackInfo ci) {
        TitleScreen screen = (TitleScreen)(Object)this;
        // Position: bottom left corner, above the "Language" button if present
        int buttonWidth = 100;
        int buttonHeight = 20;
        int x = 5;
        int y = screen.height - buttonHeight - 5;

        screen.addDrawableChild(ButtonWidget.builder(
                Text.literal("Heart Indicator: " + (HeartIndicatorClient.isEnabled() ? "ON" : "OFF")),
                button -> {
                    HeartIndicatorClient.toggle();
                    button.setMessage(Text.literal("Heart Indicator: " + (HeartIndicatorClient.isEnabled() ? "ON" : "OFF")));
                })
                .dimensions(x, y, buttonWidth, buttonHeight)
                .build());
    }
}
