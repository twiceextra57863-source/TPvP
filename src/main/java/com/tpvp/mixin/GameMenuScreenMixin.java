package com.tpvp.mixin;

import com.tpvp.gui.TPvPDashboardScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameMenuScreen.class)
public class GameMenuScreenMixin extends Screen {
    protected GameMenuScreenMixin(Text title) {
        super(title);
    }

    @Inject(at = @At("RETURN"), method = "init")
    private void addDashboardButton(CallbackInfo info) {
        // Pause Menu me Bottom-Right corner
        this.addDrawableChild(ButtonWidget.builder(Text.literal("§bTPvP"), button -> {
            // Waha par bhi line aise change kardo:
this.client.setScreen(new TPvPDashboardScreen(this));
        }).dimensions(this.width - 90, this.height - 30, 80, 20).build());
    }
}
