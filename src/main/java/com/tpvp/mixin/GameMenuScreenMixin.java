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
        // Top right corner me button add karna Pause menu me
        this.addDrawableChild(ButtonWidget.builder(Text.literal("§bTPvP"), button -> {
            this.client.setScreen(new TPvPDashboardScreen());
        }).dimensions(10, 10, 60, 20).build());
    }
}
