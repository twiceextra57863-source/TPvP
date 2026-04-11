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

        // Stop vanilla crosshair from rendering
        ci.cancel(); 

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        int width = client.getWindow().getScaledWidth();
        int height = client.getWindow().getScaledHeight();
        int cx = width / 2;
        int cy = height / 2;

        boolean isTargeting = false;
        
        // Check if cursor is on an entity and within hit range (3.0 blocks)
        if (client.crosshairTarget != null && client.crosshairTarget.getType() == HitResult.Type.ENTITY) {
            Entity target = ((EntityHitResult) client.crosshairTarget).getEntity();
            if (target instanceof LivingEntity && client.player.distanceTo(target) <= 3.0f) {
                isTargeting = true;
            }
        }

        // Green color if you can hit them, otherwise White
        int color = isTargeting ? 0xFF00FF00 : 0xFFFFFFFF; 

        // Crosshair Shape (Hollow Plus + with Center Dot)
        int gap = 3;
        int len = 5;
        int thick = 1;

        context.fill(cx - thick, cy - gap - len, cx + thick + 1, cy - gap, color); // Top
        context.fill(cx - thick, cy + gap, cx + thick + 1, cy + gap + len, color); // Bottom
        context.fill(cx - gap - len, cy - thick, cx - gap, cy + thick + 1, color); // Left
        context.fill(cx + gap, cy - thick, cx + gap + len, cy + thick + 1, color); // Right
        context.fill(cx, cy, cx + 1, cy + 1, color); // Center Dot
    }
}
