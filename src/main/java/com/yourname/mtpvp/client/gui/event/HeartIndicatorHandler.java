package com.yourname.mtpvp.client.event;

import com.yourname.mtpvp.client.render.HeartIndicatorRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

public class HeartIndicatorHandler {
    
    public static void register() {
        WorldRenderEvents.LAST.register(context -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.world == null || client.player == null) return;
            
            // Create a DrawContext for rendering
            DrawContext drawContext = new DrawContext(client, client.getBufferBuilders().getEntityVertexConsumers());
            
            Camera camera = context.camera();
            
            // Render indicators for all players except self
            for (var entity : client.world.getEntities()) {
                if (entity instanceof PlayerEntity player && player != client.player) {
                    renderIndicatorAbovePlayer(player, drawContext, camera, client);
                }
            }
        });
    }
    
    private static void renderIndicatorAbovePlayer(PlayerEntity player, DrawContext drawContext, 
                                                   Camera camera, MinecraftClient client) {
        try {
            // Get player position above head
            Vec3d playerPos = player.getPos().add(0, player.getHeight() + 0.5, 0);
            Vec3d cameraPos = camera.getPos();
            
            // Calculate screen position
            double dx = playerPos.x - cameraPos.x;
            double dy = playerPos.y - cameraPos.y;
            double dz = playerPos.z - cameraPos.z;
            
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (distance > 20) return; // Don't render too far
            
            // Project to screen coordinates
            double scale = 60 / distance;
            int screenX = (int)(client.getWindow().getScaledWidth() / 2 + dx * scale);
            int screenY = (int)(client.getWindow().getScaledHeight() / 2 - dy * scale - 30);
            
            if (screenX < 0 || screenX > client.getWindow().getScaledWidth() ||
                screenY < 0 || screenY > client.getWindow().getScaledHeight()) {
                return;
            }
            
            // Render health and HTK
            float health = player.getHealth();
            float maxHealth = player.getMaxHealth();
            float healthPercent = health / maxHealth;
            
            TextRenderer textRenderer = client.textRenderer;
            
            // Design 1: Vanilla Hearts
            if (HeartIndicatorRenderer.currentDesign == HeartIndicatorRenderer.DesignType.VANILLA) {
                int heartCount = (int) Math.ceil(maxHealth / 2);
                int displayedHearts = (int) Math.ceil(health / 2);
                int startX = screenX - (heartCount * 5);
                
                for (int i = 0; i < heartCount; i++) {
                    String heart = "❤";
                    int color = (i >= displayedHearts) ? 0x663333 : 0xFF5555;
                    drawContext.drawText(textRenderer, heart, startX + i * 10, screenY, color, true);
                }
            }
            // Design 2: Status Bar
            else if (HeartIndicatorRenderer.currentDesign == HeartIndicatorRenderer.DesignType.STATUS_BAR) {
                int color;
                if (healthPercent > 0.66) color = 0x55FF55;
                else if (healthPercent > 0.33) color = 0xFFAA55;
                else color = 0xFF5555;
                
                int barLength = 50;
                int filled = (int)(barLength * healthPercent);
                String bar = "[" + "=".repeat(filled) + " ".repeat(barLength - filled) + "]";
                drawContext.drawText(textRenderer, bar, screenX - 25, screenY, color, true);
                drawContext.drawText(textRenderer, String.format("%d%%", (int)(healthPercent * 100)), 
                                   screenX + 30, screenY, 0xFFFFFF, true);
            }
            // Design 3: Player Head + HTK
            else if (HeartIndicatorRenderer.currentDesign == HeartIndicatorRenderer.DesignType.PLAYER_HEAD) {
                drawContext.drawText(textRenderer, String.format("%.0f/%.0f ❤", health, maxHealth), 
                                   screenX - 40, screenY, 0xFFFFFF, true);
                
                int hitsToKill = HeartIndicatorRenderer.calculateHitsToKill(client.player, player);
                drawContext.drawText(textRenderer, String.format("⚔️ HTK: %d", hitsToKill), 
                                   screenX - 40, screenY + 12, 0xFFFF55, true);
                
                if (health <= maxHealth * 0.25) {
                    drawContext.drawText(textRenderer, "⚠️ DEATH ZONE", screenX - 40, screenY + 24, 0xFF5555, true);
                }
                
                drawContext.drawText(textRenderer, "👤", screenX + 35, screenY - 5, 0x55AAFF, true);
            }
            
        } catch (Exception e) {
            // Silent fail
        }
    }
}
