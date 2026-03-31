package com.tpvp.hud;

import com.tpvp.config.ModConfig;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class Indicator3D {

    public static void register() {
        WorldRenderEvents.LAST.register(Indicator3D::onWorldRender);
    }

    private static void onWorldRender(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        Camera camera = context.camera();
        Vec3d cameraPos = camera.getPos();
        float tickDelta = context.tickCounter().getTickDelta(true);
        MatrixStack matrices = context.matrixStack();
        VertexConsumerProvider.Immediate immediate = client.getBufferBuilders().getEntityVertexConsumers();

        // Target Lock Logic
        PlayerEntity lockedTarget = null;
        if (ModConfig.targetEnabled) {
            if (ModConfig.targetMode == 1) { 
                float minHp = 999f;
                for (PlayerEntity p : client.world.getPlayers()) {
                    if (p == client.player || p.isSpectator()) continue;
                    if (p.distanceTo(client.player) > ModConfig.autoRange) continue;
                    if (p.getHealth() < minHp) {
                        minHp = p.getHealth();
                        lockedTarget = p;
                    }
                }
            } else { 
                if (!ModConfig.taggedPlayerName.isEmpty()) {
                    for (PlayerEntity p : client.world.getPlayers()) {
                        if (p.getName().getString().equals(ModConfig.taggedPlayerName)) {
                            lockedTarget = p; break;
                        }
                    }
                }
            }
        }

        double weaponDamage = client.player.getAttributeValue(EntityAttributes.ATTACK_DAMAGE);
        if (weaponDamage <= 0) weaponDamage = 1.0;

        for (Entity entity : client.world.getEntities()) {
            if (entity instanceof LivingEntity target && entity != client.player) {
                if (target.isInvisible() || target instanceof ArmorStandEntity) continue;
                if (target.distanceTo(client.player) > 32.0) continue;

                boolean isLocked = (target == lockedTarget);
                // GLOWING FEATURE REMOVED AS REQUESTED

                double yOffset = target.getHeight() + 0.825;
                Vec3d targetPos = target.getLerpedPos(tickDelta);
                double x = targetPos.x - cameraPos.x;
                double y = targetPos.y - cameraPos.y + yOffset;
                double z = targetPos.z - cameraPos.z;

                // --- RENDER EPIC 3D CROWN ---
                if (isLocked) {
                    matrices.push();
                    matrices.translate(x, y + 0.5, z);
                    
                    // Crown ka rotation
                    float time = client.world.getTime() + tickDelta;
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(time * 6f));
                    matrices.scale(ModConfig.crownScale, ModConfig.crownScale, ModConfig.crownScale);

                    VertexConsumer buffer = immediate.getBuffer(RenderLayer.getTextBackgroundSeeThrough()); 

                    int cColor = 0xFFFFD700; // Gold
                    if (ModConfig.crownColor == 1) cColor = 0xFFFF3333; // Red
                    else if (ModConfig.crownColor == 2) cColor = 0xFF33FFFF; // Diamond
                    else if (ModConfig.crownColor == 3) cColor = 0xFF33FF33; // Emerald

                    if (ModConfig.crownStyle == 0) {
                        // ACTUAL 3D HOLLOW CROWN MODEL GENERATION
                        for (int i = 0; i < 4; i++) {
                            matrices.push();
                            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(i * 90f));
                            matrices.translate(0, 0, 0.2f); // Crown ki motai/radius
                            
                            Matrix4f posMatrix = matrices.peek().getPositionMatrix();
                            // Crown ka Base (Niche ka border)
                            drawDoubleSidedQuad(posMatrix, buffer, -0.2f, 0, 0.4f, 0.1f, cColor, 255);
                            // Corner Spike 1 (Left)
                            drawDoubleSidedQuad(posMatrix, buffer, -0.2f, 0.1f, 0.1f, 0.2f, cColor, 255);
                            // Corner Spike 2 (Right)
                            drawDoubleSidedQuad(posMatrix, buffer, 0.1f, 0.1f, 0.1f, 0.2f, cColor, 255);
                            
                            matrices.pop();
                        }
                    } else {
                        // Floating Diamond Style (2 Intersecting Planes)
                        Matrix4f posMatrix = matrices.peek().getPositionMatrix();
                        drawDoubleSidedQuad(posMatrix, buffer, -0.2f, -0.2f, 0.4f, 0.4f, cColor, 255);
                        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90f));
                        drawDoubleSidedQuad(matrices.peek().getPositionMatrix(), buffer, -0.2f, -0.2f, 0.4f, 0.4f, cColor, 255);
                    }
                    matrices.pop();

                    // Text Target Label
                    if (ModConfig.showTargetHealth) {
                        matrices.push();
                        matrices.translate(x, y + 0.2, z);
                        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
                        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
                        matrices.scale(-0.025F, -0.025F, 0.025F);
                        String hpTxt = "§lTARGET: §c" + String.format("%.1f ♥", target.getHealth());
                        client.textRenderer.draw(hpTxt, -client.textRenderer.getWidth(hpTxt)/2f, 0, 0xFFFFFF, false, matrices.peek().getPositionMatrix(), immediate, TextRenderer.TextLayerType.SEE_THROUGH, 0x4D000000, 255);
                        matrices.pop();
                    }
                }

                // --- REGULAR HUD BARS ---
                if (!ModConfig.indicatorEnabled) continue;
                
                matrices.push();
                matrices.translate(x, y, z);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
                matrices.scale(-0.025F, -0.025F, 0.025F);

                Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
                int light = LightmapTextureManager.MAX_LIGHT_COORDINATE;

                float health = target.getHealth();
                float maxHealth = target.getMaxHealth();
                float healthPercent = Math.max(0, Math.min(1, health / maxHealth));
                
                if (ModConfig.indicatorStyle == 1) { 
                    VertexConsumer barConsumer = immediate.getBuffer(RenderLayer.getTextBackgroundSeeThrough());
                    float barWidth = 50f; 
                    float barHeight = 5f; 
                    float currentWidth = barWidth * healthPercent;
                    int barColor = (healthPercent < 0.3f) ? 0xFFFF3333 : (healthPercent < 0.6f) ? 0xFFFFAA00 : 0xFF00FF00; 

                    drawColorQuad(positionMatrix, barConsumer, -barWidth/2 - 1, 0, barWidth + 2, barHeight + 2, 0xFF000000, light);
                    drawColorQuad(positionMatrix, barConsumer, -barWidth/2, 1, barWidth, barHeight, 0xFF333333, light);
                    if (currentWidth > 0) drawColorQuad(positionMatrix, barConsumer, -barWidth/2, 1, currentWidth, barHeight, barColor, light);

                    String percentText = (int)(healthPercent * 100) + "%";
                    client.textRenderer.draw(percentText, -client.textRenderer.getWidth(percentText) / 2f, -9, 0xFFFFFF, false, positionMatrix, immediate, TextRenderer.TextLayerType.SEE_THROUGH, 0x00000000, light);
                }
                matrices.pop();
            }
        }
        immediate.draw();
    }

    // Is method se color ekdum solid aata hai aur dono side se dikhta hai (No invisible side bug)
    private static void drawDoubleSidedQuad(Matrix4f matrix, VertexConsumer consumer, float x, float y, float width, float height, int argb, int light) {
        float a = (argb >> 24 & 255) / 255.0F;
        float r = (argb >> 16 & 255) / 255.0F;
        float g = (argb >> 8 & 255) / 255.0F;
        float b = (argb & 255) / 255.0F;
        
        // Front Face
        consumer.vertex(matrix, x, y, 0).color(r, g, b, a).light(light);
        consumer.vertex(matrix, x, y + height, 0).color(r, g, b, a).light(light);
        consumer.vertex(matrix, x + width, y + height, 0).color(r, g, b, a).light(light);
        consumer.vertex(matrix, x + width, y, 0).color(r, g, b, a).light(light);

        // Back Face (Andar se bhi dikhega)
        consumer.vertex(matrix, x + width, y, 0).color(r, g, b, a).light(light);
        consumer.vertex(matrix, x + width, y + height, 0).color(r, g, b, a).light(light);
        consumer.vertex(matrix, x, y + height, 0).color(r, g, b, a).light(light);
        consumer.vertex(matrix, x, y, 0).color(r, g, b, a).light(light);
    }

    private static void drawColorQuad(Matrix4f matrix, VertexConsumer consumer, float x, float y, float width, float height, int argb, int light) {
        drawDoubleSidedQuad(matrix, consumer, x, y, width, height, argb, light);
    }
}
