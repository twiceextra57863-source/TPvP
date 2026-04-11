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

        int cx = client.getWindow().getScaledWidth() / 2;
        int cy = client.getWindow().getScaledHeight() / 2;

        boolean canHit = false;
        if (client.crosshairTarget != null && client.crosshairTarget.getType() == HitResult.Type.ENTITY) {
            Entity target = ((EntityHitResult) client.crosshairTarget).getEntity();
            if (target instanceof LivingEntity && client.player.distanceTo(target) <= 3.0f) canHit = true;
        }

        // Color & Animation
        int color = canHit ? 0xFF00FF00 : 0xFFFFFFFF; 
        float cooldown = client.player.getAttackCooldownProgress(tickCounter.getTickDelta(true));
        float anim = 1.0f - cooldown; // Expands when hitting
        float s = ModConfig.crosshairSize;

        context.getMatrices().push();
        context.getMatrices().translate(cx, cy, 0);
        context.getMatrices().scale(s, s, 1.0f);

        // 3 Pro Styles
        if (ModConfig.crosshairStyle == 0) { // STYLE 0: Pro Plus
            int gap = 2 + (int)(anim * 4);
            int len = 4;
            context.fill(-1, -gap-len, 1, -gap, color); // Top
            context.fill(-1, gap, 1, gap+len, color); // Bottom
            context.fill(-gap-len, -1, -gap, 1, color); // Left
            context.fill(gap, -1, gap+len, 1, color); // Right
            if(cooldown == 1.0f) context.fill(0, 0, 1, 1, color); // Dot

        } else if (ModConfig.crosshairStyle == 1) { // STYLE 1: Hollow Dot
            int r = 2 + (int)(anim * 3);
            context.fill(-r, -1, r, 1, color);
            context.fill(-1, -r, 1, r, color);
            context.fill(-r+1, -r+1, r-1, r-1, 0x00000000); // Clear center

        } else if (ModConfig.crosshairStyle == 2) { // STYLE 2: Angle/Arrows
            int gap = 3 + (int)(anim * 5);
            int t = 1;
            // Top Left
            context.fill(-gap-3, -gap-t, -gap, -gap, color);
            context.fill(-gap-t, -gap-3, -gap, -gap, color);
            // Bottom Right
            context.fill(gap, gap, gap+3, gap+t, color);
            context.fill(gap, gap, gap+t, gap+3, color);
        }

        context.getMatrices().pop();
    }
}
