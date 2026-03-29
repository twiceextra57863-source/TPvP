package com.mtpvp.mixin;

import com.mtpvp.gui.MtpvpDashboard;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(at = @At("TAIL"), method = "init")
    private void addMtpvpButton(CallbackInfo info) {
        // Button ko Top-Left corner mein rakha hai jahan space khali hota hai
        // Position: x=10, y=10 | Size: width=80, height=20
        this.addDrawableChild(ButtonWidget.builder(Text.literal("§6Mtpvp §fClient"), (button) -> {
            if (this.client != null) {
                this.client.setScreen(new MtpvpDashboard(this));
            }
        }).dimensions(10, 10, 90, 20).build());
    }
}
