package com.yourname.mtpvp.client.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.AxeItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.RotationAxis;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class HeartIndicatorRenderer {
    
    public enum DesignType {
        VANILLA,
        STATUS_BAR,
        PLAYER_HEAD,
        DISABLED
    }
    
    private static DesignType currentDesign = DesignType.VANILLA;
    private static final Identifier DEFAULT_SKIN = Identifier.of("minecraft", "textures/entity/steve.png");
    
    public static void setDesignType(DesignType design) {
        currentDesign = design;
    }
    
    public static void renderIndicator(DrawContext context, LivingEntity entity, TextRenderer textRenderer, float tickDelta) {
        if (currentDesign == DesignType.DISABLED) return;
        
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        
        // Don't render indicator on self
        if (entity == client.player) return;
        
        // Calculate position above entity
        double x = entity.getX();
        double y = entity.getY() + entity.getHeight() + 0.5;
        double z = entity.getZ();
        
        Vec3d screenPos = projectToScreen(client, x, y, z, tickDelta);
        if (screenPos == null) return;
        
        int screenX = (int) screenPos.x;
        int screenY = (int) screenPos.y;
        
        float health = entity.getHealth();
        float maxHealth = entity.getMaxHealth();
        float healthPercent = health / maxHealth;
        
        switch (currentDesign) {
            case VANILLA:
                renderVanillaHearts(context, screenX, screenY, health, maxHealth);
                break;
            case STATUS_BAR:
                renderStatusBar(context, screenX, screenY, healthPercent);
                break;
            case PLAYER_HEAD:
                renderPlayerHeadWithHTK(context, client, entity, screenX, screenY, health, maxHealth);
                break;
        }
    }
    
    private static void renderVanillaHearts(DrawContext context, int x, int y, float health, float maxHealth) {
        int heartCount = MathHelper.ceil(maxHealth / 2);
        int displayedHearts = MathHelper.ceil(health / 2);
        
        int startX = x - (heartCount * 4);
        int heartY = y;
        
        for (int i = 0; i < heartCount; i++) {
            int heartX = startX + i * 8;
            Identifier heartTexture;
            
            if (i >= displayedHearts) {
                heartTexture = Identifier.of("minecraft", "textures/gui/heart.png");
                context.drawTexture(heartTexture, heartX, heartY, 16, 0, 9, 9, 256, 256);
            } else {
                heartTexture = Identifier.of("minecraft", "textures/gui/heart.png");
                context.drawTexture(heartTexture, heartX, heartY, 52, 0, 9, 9, 256, 256);
            }
        }
    }
    
    private static void renderStatusBar(DrawContext context, int x, int y, float healthPercent) {
        int barWidth = 50;
        int barHeight = 5;
        int filledWidth = (int)(barWidth * healthPercent);
        
        int barX = x - (barWidth / 2);
        int barY = y;
        
        // Background
        context.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF333333);
        
        // Health color based on percentage
        int color;
        if (healthPercent > 0.66) {
            color = 0xFF00FF00; // Green
        } else if (healthPercent > 0.33) {
            color = 0xFFFFAA00; // Orange
        } else {
            color = 0xFFFF0000; // Red
        }
        
        // Foreground (health)
        context.fill(barX, barY, barX + filledWidth, barY + barHeight, color);
        
        // Add brackets
        String barString = "[" + "=".repeat(Math.max(1, filledWidth / 2)) + "]";
        context.drawText(textRenderer, barString, x - 25, y - 10, 0xFFFFFF, true);
    }
    
    private static void renderPlayerHeadWithHTK(DrawContext context, MinecraftClient client, LivingEntity entity, 
                                                int x, int y, float health, float maxHealth) {
        // Render player head
        int headSize = 16;
        int headX = x - headSize - 5;
        int headY = y - 8;
        
        // Draw head background
        context.fill(headX - 1, headY - 1, headX + headSize + 1, headY + headSize + 1, 0xFF000000);
        
        // Get player skin
        Identifier skinTexture = DEFAULT_SKIN;
        if (entity instanceof PlayerEntity player) {
            skinTexture = client.getSkinProvider().getSkinTextures(player).texture();
        }
        
        // Draw player head (simple rectangle for now, but we'll draw the actual skin)
        context.drawTexture(skinTexture, headX, headY, 8, 8, 8, 8, 64, 64);
        
        // Calculate hits needed to kill based on current weapon
        int hitsToKill = calculateHitsToKill(client.player, entity);
        
        // Display health and hits to kill
        String healthText = String.format("%.0f/%.0f", health, maxHealth);
        String htkText = String.format("HTK: %d", hitsToKill);
        
        context.drawText(textRenderer, healthText, x + 5, y - 8, 0xFFAAAA, true);
        context.drawText(textRenderer, htkText, x + 5, y, 0xFFFFAA, true);
        
        // Add death hit indicator (critical hit zone)
        int deathZone = (int)(maxHealth * 0.25f); // 25% health is death zone
        if (health <= deathZone) {
            context.drawText(textRenderer, "⚠️ DEATH ZONE", x + 5, y + 8, 0xFF0000, true);
        }
    }
    
    private static int calculateHitsToKill(PlayerEntity attacker, LivingEntity target) {
        float totalHealth = target.getHealth();
        float damagePerHit = getPlayerDamage(attacker);
        
        if (damagePerHit <= 0) return 99;
        
        return (int)Math.ceil(totalHealth / damagePerHit);
    }
    
    private static float getPlayerDamage(PlayerEntity player) {
        ItemStack mainHand = player.getMainHandStack();
        float baseDamage = 1.0f;
        
        if (mainHand.getItem() instanceof SwordItem sword) {
            baseDamage = sword.getAttackDamage() + 2.0f;
        } else if (mainHand.getItem() instanceof AxeItem axe) {
            baseDamage = axe.getAttackDamage() + 2.0f;
        } else if (mainHand.getItem() instanceof BowItem) {
            baseDamage = 6.0f; // Base bow damage
        } else if (mainHand.getItem() instanceof CrossbowItem) {
            baseDamage = 8.0f; // Crossbow damage
        }
        
        // Add critical hit calculation (simplified)
        if (player.fallDistance > 0 && !player.isOnGround()) {
            baseDamage *= 1.5f;
        }
        
        return baseDamage;
    }
    
    private static Vec3d projectToScreen(MinecraftClient client, double x, double y, double z, float tickDelta) {
        if (client.player == null) return null;
        
        double deltaX = x - client.player.getX();
        double deltaY = y - (client.player.getY() + client.player.getEyeHeight(client.player.getPose()));
        double deltaZ = z - client.player.getZ();
        
        double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
        if (distance > 20) return null; // Don't render too far
        
        // Simple projection - in real implementation, use camera projection
        if (client.getCameraEntity() == null) return null;
        
        Vec3d screenPos = client.getCameraEntity().getPos().add(deltaX, deltaY, deltaZ);
        
        // This is a simplified projection - you might want to use actual camera transformation
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        
        // Very basic perspective projection
        double scale = 1.0 / distance;
        double screenX = screenWidth / 2 + (deltaX * 100 * scale);
        double screenY = screenHeight / 2 - (deltaY * 100 * scale) - 30;
        
        if (screenX < 0 || screenX > screenWidth || screenY < 0 || screenY > screenHeight) {
            return null;
        }
        
        return new Vec3d(screenX, screenY, 0);
    }
}
