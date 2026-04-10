package com.yourname.skinchanger.mixin;

import com.yourname.skinchanger.screen.SkinSelectScreen;
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
        // Add button in a good position (below the main menu buttons)
        int y = this.height / 4 + 96;
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("§6§lChange Skin"), 
            button -> {
                if (this.client != null) {
                    this.client.setScreen(new SkinSelectScreen(this));
                }
            })
            .dimensions(this.width / 2 - 100, y, 200, 20)
            .build());
    }
}
