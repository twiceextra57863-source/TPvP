package com.tpvp.mixin;

import com.tpvp.gui.TPvPDashboardScreen;
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

    @Inject(at = @At("RETURN"), method = "init")
    private void addCustomButton(CallbackInfo info) {
        // Title Screen par TPvP Client ka button add karna
        this.addDrawableChild(ButtonWidget.builder(Text.literal("§b§lTPvP Client"), button -> {
            this.client.setScreen(new TPvPDashboardScreen());
        }).dimensions(this.width / 2 - 100, this.height / 4 + 48 + 72 + 24, 200, 20).build());
    }
}
