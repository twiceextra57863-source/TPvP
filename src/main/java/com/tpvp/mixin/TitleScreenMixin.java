package com.tpvp.mixin;

import com.tpvp.gui.TPvPScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin extends Screen {
    protected TitleScreenMixin(Text title) { super(title); }

    @Inject(at = @At("TAIL"), method = "init")
    private void init(CallbackInfo info) {
        this.addDrawableChild(ButtonWidget.builder(Text.literal("TPvP"), button -> {
            this.client.setScreen(new TPvPScreen());
        }).dimensions(this.width / 2 - 100, this.height / 4 + 144, 200, 20).build());
    }
}
