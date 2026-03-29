package com.example.mod.mixin;

import com.example.mod.DashboardScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({TitleScreen.class, GameMenuScreen.class})
public abstract class MenuMixin extends Screen {
    protected MenuMixin(Text title) { super(title); }

    @Inject(method = "init", at = @At("TAIL"))
    private void addDashboardButton(CallbackInfo ci) {
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Health Mod"), b -> {
            this.client.setScreen(new DashboardScreen());
        }).dimensions(5, 5, 80, 20).build());
    }
}
