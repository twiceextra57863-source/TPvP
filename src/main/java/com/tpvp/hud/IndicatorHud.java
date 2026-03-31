package com.tpvp.hud;

import com.tpvp.config.ModConfig;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public class IndicatorHud implements HudRenderCallback {
    private static final Identifier HEART_TEXTURE = Identifier.of("minecraft", "textures/gui/sprites/hud/heart/full.png");

    @Override
    public void onHudRender(DrawContext context, float tickDelta) {
        if (!ModConfig.indicatorEnabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.targetedEntity == null) return;

        // Agar jisko dekh raha hai wo ek player/mob hai
        if (client.targetedEntity instanceof LivingEntity target) {
            float health = target.getHealth();
            float maxHealth = target.getMaxHealth();

            // Hand me jo item hai uska damage calculate karna
            double weaponDamage = client.player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
            // Hits to kill: (Health / Damage) round up
            int hitsToKill = (int) Math.ceil(health / weaponDamage);

            // Dynamic Color based on health percentage
            float healthPercent = health / maxHealth;
            int color = 0x00FF00; // Green
            if (healthPercent < 0.3f) color = 0xFF0000; // Red
            else if (healthPercent < 0.6f) color = 0xFFFF00; // Yellow

            int width = client.getWindow().getScaledWidth();
            int height = client.getWindow().getScaledHeight();
            int x = width / 2 + 15; // Crosshair ke thoda right me
            int y = height / 2 - 5;

            // Design Styles
            if (ModConfig.indicatorStyle == 0) { // Heart Style
                String text = String.format("%.1f", health);
                context.drawTextWithShadow(client.textRenderer, text, x + 12, y + 1, color);
                // Draw a simple rect as placeholder for heart icon to ensure compatibility
                context.fill(x, y, x + 8, y + 8, 0xFFFF5555); 
            } 
            else if (ModConfig.indicatorStyle == 1) { // Bar Style
                int barWidth = 40;
                int currentBarWidth = (int) (barWidth * healthPercent);
                context.fill(x, y, x + barWidth, y + 6, 0x66000000); // Background
                context.fill(x, y, x + currentBarWidth, y + 6, color | 0xFF000000); // Health Bar
            } 
            else if (ModConfig.indicatorStyle == 2) { // Head + Hits To Kill System
                String text = "Hits to kill: " + hitsToKill;
                if (target instanceof PlayerEntity) {
                    text = target.getName().getString() + " | Hits: " + hitsToKill;
                }
                context.drawTextWithShadow(client.textRenderer, text, x, y, color);
            }
        }
    }
}
