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
    private float animationLerp = 0;

    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void onRenderCrosshair(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (!ModConfig.crosshairEnabled) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (!client.options.getPerspective().isFirstPerson()) return;

        ci.cancel();
        int cx = client.getWindow().getScaledWidth() / 2;
        int cy = client.getWindow().getScaledHeight() / 2;
        int[] colors = {0xFFFFFFFF, 0xFF00FF00, 0xFFFF3333, 0xFF00FFFF, 0xFF000000};
        int color = colors[ModConfig.crosshairColor];

        // Animation logic: Jab attack key press hogi, crosshair expand karega
        boolean attacking = client.options.attackKey.isPressed();
        if (attacking) animationLerp = 2.0f;
        else animationLerp = Math.max(0, animationLerp - 0.2f);

        context.getMatrices().push();
        // Scale down to 0.5 for extra thinness
        context.getMatrices().translate(cx, cy, 0);
        context.getMatrices().scale(0.5f, 0.5f, 1.0f);
        
        int ani = (int)animationLerp;

        switch (ModConfig.crosshairStyle) {
            case 0 -> { // Plus Animated
                int gap = 3 + ani; int len = 8;
                context.fill(-1, -gap - len, 1, -gap, color);
                context.fill(-1, gap, 1, gap + len, color);
                context.fill(-gap - len, -1, -gap, 1, color);
                context.fill(gap, -1, gap + len, 1, color);
            }
            case 1 -> { // Dot Animated (Glows)
                int size = 2 + ani;
                context.fill(-size, -size, size, size, color);
            }
            case 2 -> { // Circle Animated (Pulses)
                int r = 6 + ani;
                context.drawBorder(-r, -r, r * 2, r * 2, color);
            }
            case 3 -> { // T-Shape (Recoil feel)
                int gap = 3; int len = 10;
                context.fill(-1, gap + ani, 1, gap + len + ani, color);
                context.fill(-gap - len, -1, -gap, 1, color);
                context.fill(gap, -1, gap + len, 1, color);
            }
            case 4 -> { // Square Dot
                int r = 5 - ani;
                context.drawBorder(-r, -r, r * 2, r * 2, color);
                context.fill(-1, -1, 1, 1, color);
            }
        }
        context.getMatrices().pop();
    }
}
