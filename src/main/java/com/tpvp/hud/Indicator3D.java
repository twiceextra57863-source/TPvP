package com.tpvp.hud;

import com.tpvp.config.ModConfig;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
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

        // ---------------- TARGET LOCK LOGIC ----------------
        PlayerEntity lockedTarget = null;
        if (ModConfig.targetEnabled) {
            if (ModConfig.targetMode == 1) { // Auto: Lowest HP
                float minHp = 999f;
                for (PlayerEntity p : client.world.getPlayers()) {
                    if (p == client.player || p.isSpectator()) continue;
                    if (p.distanceTo(client.player) > ModConfig.autoRange) continue;
                    if (p.getHealth() < minHp) {
                        minHp = p.getHealth();
                        lockedTarget = p;
                    }
                }
            } else { // Manual Tag
                if (!ModConfig.taggedPlayerName.isEmpty()) {
                    for (PlayerEntity p : client.world.getPlayers()) {
                        if (p.getName().getString().equals(ModConfig.taggedPlayerName)) {
                            lockedTarget = p; break;
                        }
                    }
                }
            }
        }

        // ---------------- RENDER LOOP ----------------
        double weaponDamage = client.player.getAttributeValue(EntityAttributes.ATTACK_DAMAGE);
        if (weaponDamage <= 0) weaponDamage = 1.0;

        for (Entity entity : client.world.getEntities()) {
            if (entity instanceof LivingEntity target && entity != client.player) {
                if (target.isInvisible() || target instanceof ArmorStandEntity) continue;
                if (target.distanceTo(client.player) > 32.0) continue;

                boolean isLocked = (target == lockedTarget);

                // Target Glow Effect (Minecraft Vanilla Glowing)
                if (isLocked && ModConfig.glowEffect) {
                    target.setGlowing(true); // Epic Vanilla Glow
                } else if (!isLocked && target.isGlowing()) {
                    target.setGlowing(false); // Remove if not target
                }

                double yOffset = target.getHeight() + 0.825;
                Vec3d targetPos = target.getLerpedPos(tickDelta);
                double x = targetPos.x - cameraPos.x;
                double y = targetPos.y - cameraPos.y + yOffset;
                double z = targetPos.z - cameraPos.z;

                // --- 1. RENDER 3D CROWN IF TARGETED ---
                if (isLocked) {
                    matrices.push();
                    // Crown thoda aur upar float karega
                    matrices.translate(x, y + 0.6, z);
                    
                    // Rotate automatically over time
                    float time = client.world.getTime() + tickDelta;
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(time * 5f)); // Spin speed
                    matrices.scale(ModConfig.crownScale, ModConfig.crownScale, ModConfig.crownScale);

                    Matrix4f posMatrix = matrices.peek().getPositionMatrix();
                    VertexConsumer buffer = immediate.getBuffer(RenderLayer.getGui()); // Solid color layer

                    // Get Color
                    int cColor = 0xFFFFD700; // Gold
                    if (ModConfig.crownColor == 1) cColor = 0xFFFF3333; // Red
                    else if (ModConfig.crownColor == 2) cColor = 0xFF33FFFF; // Diamond
                    else if (ModConfig.crownColor == 3) cColor = 0xFF33FF33; // Emerald

                    if (ModConfig.crownStyle == 0) {
                        // Style 0: 3D Crown (A ring with 4 spikes)
                        float s = 0.25f; // size
                        // Draw flat square base
                        drawColorQuad(posMatrix, buffer, -s, 0, s*2, 0.05f, cColor, 255);
                        // Draw 4 spikes (Front, Back, Left, Right)
                        drawColorQuad(posMatrix, buffer, -s, 0.05f, 0.1f, 0.2f, cColor, 255); // Left Spike
                        drawColorQuad(posMatrix, buffer, s-0.1f, 0.05f, 0.1f, 0.2f, cColor, 255); // Right Spike
                    } else {
                        // Style 1: Floating Diamond
                        float s = 0.2f;
                        drawColorQuad(posMatrix, buffer, -s, -s, s*2, s*2, cColor, 255);
                    }

                    matrices.pop();

                    // Optional Health Text below Crown
                    if (ModConfig.showTargetHealth) {
                        matrices.push();
                        matrices.translate(x, y + 0.3, z);
                        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
                        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
                        matrices.scale(-0.025F, -0.025F, 0.025F);
                        String hpTxt = "§lTARGET: §c" + String.format("%.1f ♥", target.getHealth());
                        client.textRenderer.draw(hpTxt, -client.textRenderer.getWidth(hpTxt)/2f, 0, 0xFFFFFF, false, matrices.peek().getPositionMatrix(), immediate, TextRenderer.TextLayerType.SEE_THROUGH, 0x4D000000, 255);
                        matrices.pop();
                    }
                }

                // --- 2. REGULAR 3D INDICATORS (Purana wala Code) ---
                if (!ModConfig.indicatorEnabled) continue;
                
                matrices.push();
                matrices.translate(x, y, z);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
                matrices.scale(-0.025F, -0.025F, 0.025F);

                Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
                int light = LightmapTextureManager.MAX_LIGHT_COORDINATE;

                // [Purana Health bar rendering code yahan aayega, maine jagah bachane ke liye skip nahi kiya, waise hi chalega jaise pehle banaya tha]
                float health = target.getHealth();
                float maxHealth = target.getMaxHealth();
                float healthPercent = Math.max(0, Math.min(1, health / maxHealth));
                
                if (ModConfig.indicatorStyle == 1) { // Bar Style
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

    private static void drawColorQuad(Matrix4f matrix, VertexConsumer consumer, float x, float y, float width, float height, int argb, int light) {
        float a = (argb >> 24 & 255) / 255.0F;
        float r = (argb >> 16 & 255) / 255.0F;
        float g = (argb >> 8 & 255) / 255.0F;
        float b = (argb & 255) / 255.0F;
        consumer.vertex(matrix, x, y, 0).color(r, g, b, a).light(light);
        consumer.vertex(matrix, x, y + height, 0).color(r, g, b, a).light(light);
        consumer.vertex(matrix, x + width, y + height, 0).color(r, g, b, a).light(light);
        consumer.vertex(matrix, x + width, y, 0).color(r, g, b, a).light(light);
    }
}
