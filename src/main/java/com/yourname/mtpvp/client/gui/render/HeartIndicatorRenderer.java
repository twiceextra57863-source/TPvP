package com.yourname.mtpvp.client.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.math.MathHelper;

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
    
    public static void renderIndicator(LivingEntity entity, MatrixStack matrices, 
                                       VertexConsumerProvider vertexConsumers, int light) {
        if (currentDesign == DesignType.DISABLED) return;
        
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.player == entity) return;
        
        TextRenderer textRenderer = client.textRenderer;
        
        matrices.push();
        
        // Indicator ko entity ke upar position karo
        double yOffset = entity.getHeight() + 0.5;
        matrices.translate(0, yOffset, 0);
        matrices.scale(0.02f, -0.02f, 0.02f);
        
        float health = entity.getHealth();
        float maxHealth = entity.getMaxHealth();
        float healthPercent = health / maxHealth;
        
        int x = 0;
        int y = 0;
        
        switch (currentDesign) {
            case VANILLA:
                renderVanillaHearts(matrices, vertexConsumers, textRenderer, x, y, health, maxHealth);
                break;
            case STATUS_BAR:
                renderStatusBar(matrices, vertexConsumers, textRenderer, x, y, healthPercent);
                break;
            case PLAYER_HEAD:
                renderPlayerHeadWithHTK(client, matrices, vertexConsumers, textRenderer, entity, x, y, health, maxHealth);
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
            int heartX = startX + i * 12;
            String heart = "❤";
            int color = (i >= displayedHearts) ? 0x663333 : 0xFF5555;
            
            matrices.push();
            matrices.translate(heartX, y, 0);
            textRenderer.draw(heart, 0, 0, color, false, matrices.peek().getPositionMatrix(), 
                             vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);
            matrices.pop();
        }
    }
    
    private static void renderStatusBar(MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                                        TextRenderer textRenderer, int x, int y, float healthPercent) {
        int barLength = 40;
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
        if (healthPercent > 0.66) color = 0x55FF55;
        else if (healthPercent > 0.33) color = 0xFFAA55;
        else color = 0xFF5555;
        
        matrices.push();
        matrices.translate(x - 20, y, 0);
        textRenderer.draw(bar.toString(), 0, 0, color, false, matrices.peek().getPositionMatrix(),
                         vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);
        matrices.pop();
        
        // Percentage show karo
        matrices.push();
        matrices.translate(x + 25, y, 0);
        textRenderer.draw(String.format("%d%%", (int)(healthPercent * 100)), 0, 0, 0xFFFFFF, false,
                         matrices.peek().getPositionMatrix(), vertexConsumers, 
                         TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);
        matrices.pop();
    }
    
    private static void renderPlayerHeadWithHTK(MinecraftClient client, MatrixStack matrices,
                                                VertexConsumerProvider vertexConsumers, TextRenderer textRenderer,
                                                LivingEntity entity, int x, int y, float health, float maxHealth) {
        // Health text
        String healthText = String.format("%.0f/%.0f ❤", health, maxHealth);
        matrices.push();
        matrices.translate(x - 40, y, 0);
        textRenderer.draw(healthText, 0, 0, 0xFFFFFF, false, matrices.peek().getPositionMatrix(),
                         vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);
        matrices.pop();
        
        // Hits to Kill calculate karo
        int hitsToKill = calculateHitsToKill(client.player, entity);
        String htkText = String.format("⚔️ HTK: %d", hitsToKill);
        
        matrices.push();
        matrices.translate(x - 40, y + 10, 0);
        textRenderer.draw(htkText, 0, 0, 0xFFFF55, false, matrices.peek().getPositionMatrix(),
                         vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);
        matrices.pop();
        
        // Death Zone indicator (25% se kam health)
        if (health <= maxHealth * 0.25) {
            matrices.push();
            matrices.translate(x - 40, y + 20, 0);
            textRenderer.draw("⚠️ DEATH ZONE", 0, 0, 0xFF5555, false, matrices.peek().getPositionMatrix(),
                             vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);
            matrices.pop();
        }
        
        // Player head icon
        matrices.push();
        matrices.translate(x + 30, y - 5, 0);
        textRenderer.draw("👤", 0, 0, 0x55AAFF, false, matrices.peek().getPositionMatrix(),
                         vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);
        matrices.pop();
    }
    
    private static int calculateHitsToKill(PlayerEntity attacker, LivingEntity target) {
        if (attacker == null) return 99;
        
        float totalHealth = target.getHealth();
        float damagePerHit = getPlayerDamage(attacker);
        
        if (damagePerHit <= 0) return 99;
        
        return (int) Math.ceil(totalHealth / damagePerHit);
    }
    
    private static float getPlayerDamage(PlayerEntity player) {
        float damage = 2.0f; // Default fist damage
        
        ItemStack mainHand = player.getMainHandStack();
        
        if (!mainHand.isEmpty()) {
            Item item = mainHand.getItem();
            
            // Weapon ke according damage set karo
            if (item instanceof SwordItem) {
                if (item.toString().contains("netherite")) damage = 8.0f;
                else if (item.toString().contains("diamond")) damage = 7.0f;
                else if (item.toString().contains("iron")) damage = 6.0f;
                else if (item.toString().contains("stone")) damage = 5.0f;
                else damage = 4.0f;
            }
            else if (item instanceof AxeItem) {
                if (item.toString().contains("netherite")) damage = 10.0f;
                else if (item.toString().contains("diamond")) damage = 9.0f;
                else if (item.toString().contains("iron")) damage = 8.0f;
                else if (item.toString().contains("stone")) damage = 7.0f;
                else damage = 6.0f;
            }
            else if (item instanceof BowItem) {
                damage = 6.0f;
            }
            else if (item instanceof CrossbowItem) {
                damage = 8.0f;
            }
        }
        
        // Strength effect
        if (player.hasStatusEffect(net.minecraft.entity.effect.StatusEffects.STRENGTH)) {
            int amplifier = player.getStatusEffect(net.minecraft.entity.effect.StatusEffects.STRENGTH).getAmplifier();
            damage += (amplifier + 1) * 1.5f;
        }
        
        // Critical hit (falling se)
        if (player.fallDistance > 0 && !player.isOnGround()) {
            damage *= 1.5f;
        }
        
        return damage;
    }
}
