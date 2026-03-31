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
import net.minecraft.util.math.RotationAxis; // Naya import camera rotation ke liye
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class Indicator3D {
    
    public static void register() {
        WorldRenderEvents.LAST.register(Indicator3D::onWorldRender);
    }

    private static void onWorldRender(WorldRenderContext context) {
        if (!ModConfig.indicatorEnabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.targetedEntity == null) return;

        // Agar jisko dekh raha hai wo Player/Mob hai
        if (client.targetedEntity instanceof LivingEntity target) {
            Camera camera = context.camera();
            Vec3d cameraPos = camera.getPos();
            
            float tickDelta = context.tickCounter().getTickDelta(true);

            // Vanilla nametag ki height target.getHeight() + 0.5 hoti hai.
            // Hum 0.825 rakhenge taaki humara indicator theek Nametag ke upar aaye.
            double yOffset = target.getHeight() + 0.825;
            Vec3d targetPos = target.getLerpedPos(tickDelta);
            
            double x = targetPos.x - cameraPos.x;
            double y = targetPos.y - cameraPos.y + yOffset;
            double z = targetPos.z - cameraPos.z;

            MatrixStack matrices = context.matrixStack();
            matrices.push();
            
            // Nametag ki position par set karna
            matrices.translate(x, y, z);
            
            // Ye 2 lines EXACTLY vanilla nametag ki tarah player ke camera ko face karwayengi
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
            
            // Nametag size set karna
            matrices.scale(-0.025F, -0.025F, 0.025F);

            // Stats calculation
            float health = target.getHealth();
            float maxHealth = target.getMaxHealth();
            double weaponDamage = client.player.getAttributeValue(EntityAttributes.ATTACK_DAMAGE);
            if (weaponDamage <= 0) weaponDamage = 1.0; 
            int hitsToKill = (int) Math.ceil(health / weaponDamage);

            float healthPercent = health / maxHealth;
            int color = 0x00FF00; // Green
            if (healthPercent < 0.3f) color = 0xFF0000; // Red
            else if (healthPercent < 0.6f) color = 0xFFFF00; // Yellow

            TextRenderer textRenderer = client.textRenderer;
            VertexConsumerProvider.Immediate immediate = client.getBufferBuilders().getEntityVertexConsumers();
            Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
            int light = LightmapTextureManager.MAX_LIGHT_COORDINATE; // Hamesha bright dikhega

            // Text Setup
            String text = "";
            if (ModConfig.indicatorStyle == 0) {
                text = String.format("❤ %.1f", health);
            } else if (ModConfig.indicatorStyle == 1) {
                int totalBars = 10;
                int activeBars = (int) (healthPercent * totalBars);
                StringBuilder barStr = new StringBuilder("HP: [");
                for (int i = 0; i < totalBars; i++) barStr.append(i < activeBars ? "|" : ".");
                barStr.append("]");
                text = barStr.toString();
            } else if (ModConfig.indicatorStyle == 2) {
                text = "Hits: " + hitsToKill;
                if (target instanceof PlayerEntity) {
                    text = target.getName().getString() + " | Hits: " + hitsToKill;
                }
            }

            float textWidth = textRenderer.getWidth(text);
            
            // SEE_THROUGH use kiya hai taaki indicator kisi block/body part ke peeche chhupe nahi
            textRenderer.draw(text, -textWidth / 2f, 0, color, false, positionMatrix, immediate, TextRenderer.TextLayerType.SEE_THROUGH, 0x40000000, light);

            // **SABSE ZYADA IMPORTANT**: Buffer ko draw karna taaki text actually screen par aaye!
            immediate.draw();

            matrices.pop();
        }
    }
}
