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
    private float anim = 0;

    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void drawCustomCrosshair(DrawContext context, RenderTickCounter tick, CallbackInfo ci) {
        if (!ModConfig.crosshairEnabled) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (!client.options.getPerspective().isFirstPerson()) return;

        ci.cancel(); // Remove Vanilla
        int cx = client.getWindow().getScaledWidth() / 2;
        int cy = client.getWindow().getScaledHeight() / 2;
        
        int[] colors = {0xFFFFFFFF, 0xFF00FF00, 0xFFFF3333, 0xFF00FFFF, 0xFF000000};
        int color = colors[ModConfig.crosshairColor];

        // Click Animation Logic
        if (client.options.attackKey.isPressed()) anim = 3.0f;
        else anim = Math.max(0, anim - 0.4f);

        context.getMatrices().push();
        context.getMatrices().translate(cx, cy, 0);
        context.getMatrices().scale(0.5f, 0.5f, 1.0f); // THIN LOOK

        int a = (int)anim;
        switch (ModConfig.crosshairStyle) {
            case 0 -> { // Plus
                int g = 4 + a; int l = 8;
                context.fill(-1, -g-l, 1, -g, color);
                context.fill(-1, g, 1, g+l, color);
                context.fill(-g-l, -1, -g, 1, color);
                context.fill(g, -1, g+l, 1, color);
            }
            case 1 -> { // Dot
                int s = 2 + a;
                context.fill(-s, -s, s, s, color);
            }
            case 2 -> { // Circle
                int r = 6 + a;
                context.drawBorder(-r, -r, r*2, r*2, color);
            }
            case 3 -> { // T-Shape
                int g = 4; int l = 10;
                context.fill(-1, g+a, 1, g+l+a, color);
                context.fill(-g-l, -1, -g, 1, color);
                context.fill(g, -1, g+l, 1, color);
            }
            case 4 -> { // Square
                int r = 5 - a;
                context.drawBorder(-r, -r, r*2, r*2, color);
                context.fill(-1, -1, 1, 1, color);
            }
        }
        context.getMatrices().pop();
    }
}
