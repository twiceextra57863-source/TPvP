package com.heartindicator.mixin;

import com.heartindicator.client.gui.HeartIndicatorSettingsScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends net.minecraft.client.gui.screen.Screen {

    protected GameMenuScreenMixin() {
        super(Text.empty());
    }

    @Inject(method = "initWidgets", at = @At("TAIL"))
    private void heartindicator$addButton(CallbackInfo ci) {
        int btnW = 160;
        int btnH = 20;
        int x    = this.width  - btnW - 8;
        int y    = this.height - btnH - 8;

        this.addDrawableChild(
            ButtonWidget.builder(
                Text.of("§c❤ §fHeart Indicator"),
                btn -> this.client.setScreen(
                    new HeartIndicatorSettingsScreen(this)
                )
            )
            .dimensions(x, y, btnW, btnH)
            .build()
        );
    }
}
