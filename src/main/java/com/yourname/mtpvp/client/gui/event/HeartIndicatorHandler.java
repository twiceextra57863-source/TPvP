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
            if (HeartIndicatorRenderer.currentDesign == HeartIndicatorRenderer.DesignType.DISABLED) return;
            
            DrawContext drawContext = new DrawContext(client, client.getBufferBuilders().getEntityVertexConsumers());
            Camera camera = context.camera();
            
            for (var entity : client.world.getEntities()) {
                if (entity instanceof PlayerEntity player && player != client.player) {
                    renderIndicator(player, drawContext, camera, client);
                }
            }
        });
    }
    
    private static void renderIndicator(PlayerEntity player, DrawContext drawContext, 
                                        Camera camera, MinecraftClient client) {
        try {
            Vec3d playerPos = player.getPos().add(0, player.getHeight() + 0.8, 0);
            Vec3d cameraPos = camera.getPos();
            
            double dx = playerPos.x - cameraPos.x;
            double dy = playerPos.y - cameraPos.y;
            double dz = playerPos.z - cameraPos.z;
            
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (distance > 25) return;
            
            double scale = 50 / distance;
            int screenX = (int)(client.getWindow().getScaledWidth() / 2 + dx * scale);
            int screenY = (int)(client.getWindow().getScaledHeight() / 2 - dy * scale - 25);
            
            if (screenX < 0 || screenX > client.getWindow().getScaledWidth() ||
                screenY < 0 || screenY > client.getWindow().getScaledHeight()) {
                return;
            }
            
            float health = player.getHealth();
            float maxHealth = player.getMaxHealth();
            float healthPercent = health / maxHealth;
            TextRenderer textRenderer = client.textRenderer;
            
            switch (HeartIndicatorRenderer.currentDesign) {
                case VANILLA:
                    renderVanillaHearts(drawContext, textRenderer, screenX, screenY, health, maxHealth);
                    break;
                case STATUS_BAR:
                    renderStatusBar(drawContext, textRenderer, screenX, screenY, healthPercent);
                    break;
                case PLAYER_HEAD:
                    renderPlayerHead(drawContext, textRenderer, client, player, screenX, screenY, health, maxHealth);
                    break;
            }
        } catch (Exception e) {}
    }
    
    private static void renderVanillaHearts(DrawContext ctx, TextRenderer tr, int x, int y, float health, float maxHealth) {
        int heartCount = (int) Math.ceil(maxHealth / 2);
        int displayed = (int) Math.ceil(health / 2);
        int startX = x - (heartCount * 5);
        
        for (int i = 0; i < heartCount; i++) {
            String heart = "❤";
            int color = (i >= displayed) ? 0x663333 : 0xFF5555;
            ctx.drawText(tr, heart, startX + i * 10, y, color, true);
        }
    }
    
    private static void renderStatusBar(DrawContext ctx, TextRenderer tr, int x, int y, float percent) {
        int color = percent > 0.66 ? 0x55FF55 : (percent > 0.33 ? 0xFFAA55 : 0xFF5555);
        int filled = (int)(50 * percent);
        String bar = "[" + "=".repeat(filled) + " ".repeat(50 - filled) + "]";
        ctx.drawText(tr, bar, x - 30, y, color, true);
        ctx.drawText(tr, String.format("%d%%", (int)(percent * 100)), x + 35, y, 0xFFFFFF, true);
    }
    
    private static void renderPlayerHead(DrawContext ctx, TextRenderer tr, MinecraftClient client, 
                                         PlayerEntity player, int x, int y, float health, float maxHealth) {
        ctx.drawText(tr, String.format("%.0f/%.0f ❤", health, maxHealth), x - 45, y, 0xFFFFFF, true);
        
        int hitsToKill = HeartIndicatorRenderer.calculateHitsToKill(client.player, player);
        ctx.drawText(tr, String.format("⚔️ HTK: %d", hitsToKill), x - 45, y + 12, 0xFFFF55, true);
        
        if (health <= maxHealth * 0.25) {
            ctx.drawText(tr, "⚠️ DEATH ZONE", x - 45, y + 24, 0xFF5555, true);
        }
        
        ctx.drawText(tr, "👤", x + 40, y - 5, 0x55AAFF, true);
    }
}
