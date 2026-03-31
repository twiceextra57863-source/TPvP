package com.tpvp.hud;

import com.tpvp.config.ModConfig;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class Indicator3D {
    
    // Mod load hote waqt isko register karenge
    public static void register() {
        WorldRenderEvents.LAST.register(Indicator3D::onWorldRender);
    }

    private static void onWorldRender(WorldRenderContext context) {
        if (!ModConfig.indicatorEnabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.targetedEntity == null) return;

        // Agar jisko dekh raha hai wo LivingEntity (Player/Mob) hai
        if (client.targetedEntity instanceof LivingEntity target) {
            Camera camera = context.camera();
            Vec3d cameraPos = camera.getPos();
            
            // 1.21.2+ me Tick Delta lene ka sahi tarika
            float tickDelta = context.tickCounter().getTickDelta(true);

            // Position Calculation: Nametag ke theek upar
            // Vanilla nametag target.getHeight() + 0.5 par hota hai, hum 0.9 par rakhenge taaki overlap na ho
            Vec3d targetPos = target.getLerpedPos(tickDelta);
            double x = targetPos.x - cameraPos.x;
            double y = targetPos.y - cameraPos.y + target.getHeight() + 0.9;
            double z = targetPos.z - cameraPos.z;

            MatrixStack matrices = context.matrixStack();
            matrices.push();
            
            // Entity ki position par jao
            matrices.translate(x, y, z);
            
            // Camera (Player) ki taraf rotate karo
            matrices.multiply(camera.getRotation());
            
            // Nametag jaisa chota scale karo (-0.025 vanilla nametag scale hai)
            matrices.scale(-0.025F, -0.025F, 0.025F);

            // Math Logic & Stats
            float health = target.getHealth();
            float maxHealth = target.getMaxHealth();
            double weaponDamage = client.player.getAttributeValue(EntityAttributes.ATTACK_DAMAGE);
            if (weaponDamage <= 0) weaponDamage = 1.0; // Fallback
            int hitsToKill = (int) Math.ceil(health / weaponDamage);

            // Dynamic Color
            float healthPercent = health / maxHealth;
            int color = 0x00FF00; // Green
            if (healthPercent < 0.3f) color = 0xFF0000; // Red
            else if (healthPercent < 0.6f) color = 0xFFFF00; // Yellow

            TextRenderer textRenderer = client.textRenderer;
            VertexConsumerProvider.Immediate vertexConsumers = client.getBufferBuilders().getEntityVertexConsumers();
            Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
            int light = LightmapTextureManager.MAX_LIGHT_COORDINATE; // Hamesha bright dikhega

            // 3D GUI Render Styles
            if (ModConfig.indicatorStyle == 0) { // Heart Style
                String text = String.format("❤ %.1f", health); // 3D me clear dikhne ke liye Heart icon
                float textWidth = textRenderer.getWidth(text);
                textRenderer.draw(text, -textWidth / 2f, 0, color, false, positionMatrix, vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0x40000000, light);
            } 
            else if (ModConfig.indicatorStyle == 1) { // Bar Style
                String hpText = String.format("HP: %.1f", health);
                float hpWidth = textRenderer.getWidth(hpText);
                textRenderer.draw(hpText, -hpWidth / 2f, -10, color, false, positionMatrix, vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0x40000000, light);
                
                // Text-based clean status bar for 3D world: [||||||....]
                int totalBars = 10;
                int activeBars = (int) (healthPercent * totalBars);
                StringBuilder barStr = new StringBuilder("[");
                for (int i = 0; i < totalBars; i++) {
                    barStr.append(i < activeBars ? "|" : ".");
                }
                barStr.append("]");
                String bar = barStr.toString();
                float barWidth = textRenderer.getWidth(bar);
                textRenderer.draw(bar, -barWidth / 2f, 0, color, false, positionMatrix, vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0x40000000, light);
            } 
            else if (ModConfig.indicatorStyle == 2) { // Hits To Kill
                String text = "Hits: " + hitsToKill;
                if (target instanceof PlayerEntity) {
                    text = target.getName().getString() + " | Hits: " + hitsToKill;
                }
                float textWidth = textRenderer.getWidth(text);
                textRenderer.draw(text, -textWidth / 2f, 0, color, false, positionMatrix, vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0x40000000, light);
            }

            matrices.pop();
        }
    }
}
