package com.tpvp.mixin;

import com.tpvp.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void renderCustomCrosshair(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (!ModConfig.smartCrosshair) return;
        ci.cancel(); 

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        // Exact Pixel Center
        int cx = client.getWindow().getScaledWidth() / 2;
        int cy = client.getWindow().getScaledHeight() / 2;

        boolean canHit = false;
        if (client.crosshairTarget != null && client.crosshairTarget.getType() == HitResult.Type.ENTITY) {
            Entity target = ((EntityHitResult) client.crosshairTarget).getEntity();
            if (target instanceof LivingEntity && client.player.distanceTo(target) <= 3.0f) canHit = true;
        }

        int color = canHit ? 0xFF00FFCC : 0xFFFFFFFF; // Neon Cyan on target
        
        // Smooth Cooldown Expansion
        float cooldown = client.player.getAttackCooldownProgress(tickCounter.getTickDelta(true));
        float anim = 1.0f - cooldown; 
        float s = ModConfig.crosshairSize;

        context.getMatrices().push();
        context.getMatrices().translate(cx, cy, 0);
        context.getMatrices().scale(s, s, 1.0f);

        if (ModConfig.crosshairStyle == 0) { // EXACT PRO PLUS
            int gap = 2 + (int)(anim * 5);
            context.fill(-1, -gap-4, 1, -gap, color); // T
            context.fill(-1, gap, 1, gap+4, color); // B
            context.fill(-gap-4, -1, -gap, 1, color); // L
            context.fill(gap, -1, gap+4, 1, color); // R
            if (cooldown == 1.0f) context.fill(-1, -1, 1, 1, color); // Center Dot
        } 
        else if (ModConfig.crosshairStyle == 1) { // CRISP HOLLOW DOT
            int r = 2 + (int)(anim * 3);
            context.fill(-r, -1, r+1, 1, color); // H
            context.fill(-1, -r, 1, r+1, color); // V
            context.fill(-r+1, -r+1, r, r, 0x00000000); // Clear core
        } 
        else if (ModConfig.crosshairStyle == 2) { // MODERN FPS CHEVRON
            int gap = 3 + (int)(anim * 5);
            context.fill(-gap-2, -gap-1, -gap, -gap, color);
            context.fill(-gap-1, -gap-2, -gap, -gap, color);
            context.fill(gap, gap, gap+2, gap+1, color);
            context.fill(gap, gap, gap+1, gap+2, color);
        }

        context.getMatrices().pop();
    }
}
