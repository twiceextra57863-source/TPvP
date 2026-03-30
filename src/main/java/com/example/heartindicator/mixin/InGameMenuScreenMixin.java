package com.example.heartindicator.mixin;

import com.example.heartindicator.client.gui.HeartIndicatorScreen;
import net.minecraft.client.gui.screen.ingame.InGameMenuScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameMenuScreen.class)
public class InGameMenuScreenMixin {
    @Inject(method = "init", at = @At("RETURN"))
    private void addHeartIndicatorButton(CallbackInfo ci) {
        InGameMenuScreen screen = (InGameMenuScreen) (Object) this;
        // Place button at bottom left corner (x = 4, y = screen.height - 24)
        int x = 4;
        int y = screen.height - 24;
        screen.addDrawableChild(ButtonWidget.builder(
                Text.literal("❤️"),
                button -> screen.client.setScreen(new HeartIndicatorScreen()))
                .dimensions(x, y, 20, 20)
                .build());
    }
}
