package com.tpvp.features;

import com.tpvp.TPvPConfig;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.util.hit.EntityHitResult;

public class HeartIndicator implements HudRenderCallback {
    @Override
    public void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        if (!TPvPConfig.heartIndicatorEnabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        if (client.crosshairTarget instanceof EntityHitResult hit && hit.getEntity() instanceof LivingEntity target) {
            float health = target.getHealth();
            float maxHealth = target.getMaxHealth();
            
            // 1.21 mein GENERIC_ATTACK_DAMAGE ko aise access karte hain
            double attackDamage = client.player.getAttributeValue(EntityAttributes.ATTACK_DAMAGE);
            int hitsToKill = (int) Math.ceil(health / (attackDamage <= 0 ? 1 : attackDamage));

            int color = 0x00FF00; // Green
            if (health < maxHealth * 0.5) color = 0xFFFF00; // Yellow
            if (health < maxHealth * 0.2) color = 0xFF0000; // Red

            String info = String.format("HP: %.1f | Hits: %d", health, hitsToKill);
            context.drawTextWithShadow(client.textRenderer, info, (context.getScaledWindowWidth()/2) + 10, (context.getScaledWindowHeight()/2) + 10, color);
        }
    }
}
