package com.yourname.mtpvp.client.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.Identifier;

public class HeartIndicatorRenderer {
    
    public enum DesignType {
        VANILLA,
        STATUS_BAR,
        PLAYER_HEAD,
        DISABLED
    }
    
    private static DesignType currentDesign = DesignType.VANILLA;
    
    public static void setDesignType(DesignType design) {
        currentDesign = design;
    }
    
    public static void renderIndicator(LivingEntity entity, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        if (currentDesign == DesignType.DISABLED) return;
        
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.player == entity) return;
        
        TextRenderer textRenderer = client.textRenderer;
        
        matrices.push();
        
        double yOffset = entity.getHeight() + 0.5;
        matrices.translate(0, yOffset, 0);
        matrices.scale(0.025f, -0.025f, 0.025f);
        
        float health = entity.getHealth();
        float maxHealth = entity.getMaxHealth();
        float healthPercent = health / maxHealth;
        
        int centerX = 0;
        int yPos = 0;
        
        switch (currentDesign) {
            case VANILLA:
                renderVanillaHearts(matrices, vertexConsumers, textRenderer, centerX, yPos, health, maxHealth);
                break;
            case STATUS_BAR:
                renderStatusBar(matrices, vertexConsumers, textRenderer, centerX, yPos, healthPercent);
                break;
            case PLAYER_HEAD:
                renderPlayerHeadWithHTK(client, matrices, vertexConsumers, textRenderer, entity, centerX, yPos, health, maxHealth);
                break;
        }
        
        matrices.pop();
    }
    
    private static void renderVanillaHearts(MatrixStack matrices, VertexConsumerProvider vertexConsumers, 
                                            TextRenderer textRenderer, int x, int y, float health, float maxHealth) {
        int heartCount = (int) Math.ceil(maxHealth / 2);
        int displayedHearts = (int) Math.ceil(health / 2);
        
        int startX = x - (heartCount * 8);
        
        for (int i = 0; i < heartCount; i++) {
            int heartX = startX + i * 16;
            String heart = "❤";
            int color = (i >= displayedHearts) ? 0x663333 : 0xFF5555;
            
            matrices.push();
            matrices.translate(heartX, y, 0);
            textRenderer.draw(heart, 0, 0, color, false, matrices.peek().getPositionMatrix(), vertexConsumers, 
                             TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);
            matrices.pop();
        }
    }
    
    private static void renderStatusBar(MatrixStack matrices, VertexConsumerProvider vertexConsumers, 
                                        TextRenderer textRenderer, int x, int y, float healthPercent) {
        int barLength = 50;
        int filledLength = (int)(barLength * healthPercent);
        
        StringBuilder bar = new StringBuilder("[");
        for (int i = 0; i < barLength / 2; i++) {
            if (i < filledLength / 2) {
                bar.append("=");
            } else {
                bar.append(" ");
            }
        }
        bar.append("]");
        
        int color;
        if (healthPercent > 0.66) {
            color = 0x55FF55;
        } else if (healthPercent > 0.33) {
            color = 0xFFAA55;
        } else {
            color = 0xFF5555;
        }
        
        matrices.push();
        matrices.translate(x - (barLength / 2), y, 0);
        textRenderer.draw(bar.toString(), 0, 0, color, false, matrices.peek().getPositionMatrix(), vertexConsumers, 
                         TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);
        matrices.pop();
        
        matrices.push();
        matrices.translate(x + (barLength / 2) + 10, y, 0);
        textRenderer.draw(String.format("%d%%", (int)(healthPercent * 100)), 0, 0, 0xFFFFFF, false, 
                         matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);
        matrices.pop();
    }
    
    private static void renderPlayerHeadWithHTK(MinecraftClient client, MatrixStack matrices, 
                                                VertexConsumerProvider vertexConsumers, TextRenderer textRenderer,
                                                LivingEntity entity, int x, int y, float health, float maxHealth) {
        String healthText = String.format("%.0f/%.0f", health, maxHealth);
        matrices.push();
        matrices.translate(x - 30, y, 0);
        textRenderer.draw(healthText, 0, 0, 0xFFFFFF, false, matrices.peek().getPositionMatrix(), vertexConsumers, 
                         TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);
        matrices.pop();
        
        int hitsToKill = calculateHitsToKill(client.player, entity);
        String htkText = String.format("HTK: %d", hitsToKill);
        
        matrices.push();
        matrices.translate(x - 30, y + 12, 0);
        textRenderer.draw(htkText, 0, 0, 0xFFFFAA, false, matrices.peek().getPositionMatrix(), vertexConsumers, 
                         TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);
        matrices.pop();
        
        int deathZone = (int)(maxHealth * 0.25f);
        if (health <= deathZone) {
            matrices.push();
            matrices.translate(x - 30, y + 24, 0);
            textRenderer.draw("⚠️ DEATH ZONE", 0, 0, 0xFF5555, false, matrices.peek().getPositionMatrix(), vertexConsumers, 
                             TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);
            matrices.pop();
        }
        
        if (entity instanceof PlayerEntity) {
            matrices.push();
            matrices.translate(x + 10, y - 8, 0);
            textRenderer.draw("👤", 0, 0, 0x55AAFF, false, matrices.peek().getPositionMatrix(), vertexConsumers, 
                             TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);
            matrices.pop();
        }
    }
    
    private static int calculateHitsToKill(PlayerEntity attacker, LivingEntity target) {
        if (attacker == null) return 99;
        
        float totalHealth = target.getHealth();
        float damagePerHit = getPlayerDamage(attacker);
        
        if (damagePerHit <= 0) return 99;
        
        return (int)Math.ceil(totalHealth / damagePerHit);
    }
    
    private static float getPlayerDamage(PlayerEntity player) {
        ItemStack mainHand = player.getMainHandStack();
        float baseDamage = 2.0f; // Default fist damage
        
        if (mainHand.isEmpty()) {
            return baseDamage;
        }
        
        Item item = mainHand.getItem();
        
        // Get attack damage from item attribute
        var attackDamageAttribute = mainHand.getAttributeModifiers(ItemAttributeModifierContext.ATTACK_DAMAGE_MODIFIER_TYPE);
        
        if (attackDamageAttribute != null && !attackDamageAttribute.isEmpty()) {
            // Try to get the attack damage from the item's attribute modifiers
            for (var modifier : attackDamageAttribute.values()) {
                if (modifier.getId().toString().contains("attack_damage")) {
                    baseDamage += (float) modifier.value();
                    break;
                }
            }
        } else {
            // Fallback damage values based on item type
            if (item instanceof SwordItem) {
                baseDamage = 4.0f;
            } else if (item instanceof AxeItem) {
                baseDamage = 5.0f;
            } else if (item instanceof BowItem) {
                baseDamage = 6.0f;
            } else if (item instanceof CrossbowItem) {
                baseDamage = 8.0f;
            } else if (item instanceof PickaxeItem) {
                baseDamage = 3.0f;
            } else if (item instanceof ShovelItem) {
                baseDamage = 2.5f;
            }
        }
        
        // Add strength effect bonus
        if (player.hasStatusEffect(net.minecraft.entity.effect.StatusEffects.STRENGTH)) {
            int amplifier = player.getStatusEffect(net.minecraft.entity.effect.StatusEffects.STRENGTH).getAmplifier();
            baseDamage += (amplifier + 1) * 1.5f;
        }
        
        // Critical hit bonus (if falling)
        if (player.fallDistance > 0 && !player.isOnGround()) {
            baseDamage *= 1.5f;
        }
        
        return baseDamage;
    }
}
