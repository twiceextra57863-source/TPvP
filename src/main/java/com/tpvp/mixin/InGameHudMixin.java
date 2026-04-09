package com.tpvp.mixin;

import com.tpvp.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void onRenderCrosshair(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (!ModConfig.crosshairEnabled) return; // Agar off hai toh Vanilla chalega

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.options.getPerspective().isFirstPerson()) {
            // Cancel Vanilla Crosshair completely
            ci.cancel();

            int screenWidth = client.getWindow().getScaledWidth();
            int screenHeight = client.getWindow().getScaledHeight();

            // 100% PERFECT MATHEMATICAL CENTER
            int cx = screenWidth / 2;
            int cy = screenHeight / 2;

            // Colors
            int[] colors = {0xFFFFFFFF, 0xFF00FF00, 0xFFFF3333, 0xFF00FFFF, 0xFF000000};
            int color = colors[ModConfig.crosshairColor];

            int style = ModConfig.crosshairStyle;

            // PVP CROSSHAIR DESIGNS
            if (style == 0) {
                // 0: Perfect PvP Plus (Gap in middle)
                int gap = 2; int len = 4; int t = 1; // t = thickness
                context.fill(cx - t, cy - gap - len, cx + t, cy - gap, color); // Top
                context.fill(cx - t, cy + gap, cx + t, cy + gap + len, color); // Bottom
                context.fill(cx - gap - len, cy - t, cx - gap, cy + t, color); // Left
                context.fill(cx + gap, cy - t, cx + gap + len, cy + t, color); // Right
            } 
            else if (style == 1) {
                // 1: Pro Dot (2x2 pixel perfectly centered)
                context.fill(cx - 1, cy - 1, cx + 1, cy + 1, color);
            } 
            else if (style == 2) {
                // 2: Hollow Circle/Square
                int rad = 3; int t = 1;
                context.fill(cx - rad, cy - rad, cx + rad, cy - rad + t, color); // Top
                context.fill(cx - rad, cy + rad - t, cx + rad, cy + rad, color); // Bottom
                context.fill(cx - rad, cy - rad, cx - rad + t, cy + rad, color); // Left
                context.fill(cx + rad - t, cy - rad, cx + rad, cy + rad, color); // Right
            } 
            else if (style == 3) {
                // 3: T-Shape (CS:GO Spray Control Style)
                int gap = 2; int len = 5; int t = 1;
                context.fill(cx - t, cy + gap, cx + t, cy + gap + len, color); // Bottom
                context.fill(cx - gap - len, cy - t, cx - gap, cy + t, color); // Left
                context.fill(cx + gap, cy - t, cx + gap + len, cy + t, color); // Right
            } 
            else if (style == 4) {
                // 4: Hollow Square + Center Dot (Sniper Focus)
                int rad = 4; int t = 1;
                context.fill(cx - rad, cy - rad, cx + rad, cy - rad + t, color); // Top
                context.fill(cx - rad, cy + rad - t, cx + rad, cy + rad, color); // Bottom
                context.fill(cx - rad, cy - rad, cx - rad + t, cy + rad, color); // Left
                context.fill(cx + rad - t, cy - rad, cx + rad, cy + rad, color); // Right
                context.fill(cx - 1, cy - 1, cx + 1, cy + 1, color); // Center Dot
            }
        }
    }
}
