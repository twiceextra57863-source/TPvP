package com.heartindicator.mixin;

import com.heartindicator.client.gui.HeartIndicatorSettingsScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin extends net.minecraft.client.gui.screen.Screen {

    protected TitleScreenMixin() {
        super(Text.empty());
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void heartindicator$addButton(CallbackInfo ci) {
        TitleScreen self = (TitleScreen)(Object) this;

        // Bottom-right corner button — sits in the empty area
        int btnW = 160;
        int btnH = 20;
        int x    = self.width  - btnW - 8;
        int y    = self.height - btnH - 8;

        self.addDrawableChild(
            ButtonWidget.builder(
                Text.of("§c❤ §fHeart Indicator"),
                btn -> self.client.setScreen(
                    new HeartIndicatorSettingsScreen(self)
                )
            )
            .dimensions(x, y, btnW, btnH)
            .build()
        );
    }
}
