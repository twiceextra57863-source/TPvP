package com.example.heartindicator.mixin;

import com.example.heartindicator.client.HeartIndicatorClient;
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
    
    @Inject(method = "init", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        // Add toggle button in game menu
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal(getToggleButtonText()),
            button -> {
                HeartIndicatorClient.showHeartIndicator = !HeartIndicatorClient.showHeartIndicator;
                button.setMessage(Text.literal(getToggleButtonText()));
            }
        ).dimensions(this.width / 2 - 100, this.height / 2 - 50, 200, 20).build());
    }
    
    private String getToggleButtonText() {
        return HeartIndicatorClient.showHeartIndicator ? 
            "§aHeart Indicator: ON" : 
            "§cHeart Indicator: OFF";
    }
}
