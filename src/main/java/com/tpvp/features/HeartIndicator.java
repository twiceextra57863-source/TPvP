package com.tpvp.features;

import com.tpvp.TPvPConfig;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.EntityHitResult;

public class HeartIndicator implements HudRenderCallback {
    @Override
    public void onHudRender(DrawContext context, float tickDelta) {
        if (!TPvPConfig.heartIndicatorEnabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.crosshairTarget instanceof EntityHitResult hit && hit.getEntity() instanceof LivingEntity target) {
            
            float health = target.getHealth();
            float maxHealth = target.getMaxHealth();
            
            // Calculate Hits to Kill (Rough Estimation)
            float damage = (float) client.player.getAttributeValue(net.minecraft.entity.attribute.EntityAttributes.GENERIC_ATTACK_DAMAGE);
            int hitsToKill = (int) Math.ceil(health / (damage <= 0 ? 1 : damage));

            // Color changing logic
            int color = 0x00FF00; // Green
            if (health < maxHealth * 0.5) color = 0xFFFF00; // Yellow
            if (health < maxHealth * 0.2) color = 0xFF0000; // Red

            // Render Logic
            String info = String.format("HP: %.1f | Hits: %d", health, hitsToKill);
            context.drawTextWithShadow(client.textRenderer, info, (context.getScaledWindowWidth()/2) + 10, (context.getScaledWindowHeight()/2) + 10, color);
        }
    }
}
