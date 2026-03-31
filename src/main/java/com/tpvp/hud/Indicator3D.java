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
                double yOffset = target.getHeight() + 0.825;
                Vec3d targetPos = target.getLerpedPos(tickDelta);
                double x = targetPos.x - cameraPos.x;
                double y = targetPos.y - cameraPos.y + yOffset;
                double z = targetPos.z - cameraPos.z;

                // --- 1. RENDER 3D TARGET CROWN ---
                if (isLocked) {
                    matrices.push();
                    matrices.translate(x, y + 0.5, z);
                    
                    float time = client.world.getTime() + tickDelta;
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(time * 6f));
                    matrices.scale(ModConfig.crownScale, ModConfig.crownScale, ModConfig.crownScale);

                    VertexConsumer buffer = immediate.getBuffer(RenderLayer.getTextBackgroundSeeThrough()); 

                    int cColor = 0xFFFFD700; 
                    if (ModConfig.crownColor == 1) cColor = 0xFFFF3333; 
                    else if (ModConfig.crownColor == 2) cColor = 0xFF33FFFF; 
                    else if (ModConfig.crownColor == 3) cColor = 0xFF33FF33; 

                    if (ModConfig.crownStyle == 0) {
                        for (int i = 0; i < 4; i++) {
                            matrices.push();
                            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(i * 90f));
                            matrices.translate(0, 0, 0.2f); 
                            Matrix4f posMatrix = matrices.peek().getPositionMatrix();
                            drawDoubleSidedQuad(posMatrix, buffer, -0.2f, 0, 0.4f, 0.1f, cColor, 255);
                            drawDoubleSidedQuad(posMatrix, buffer, -0.2f, 0.1f, 0.1f, 0.2f, cColor, 255);
                            drawDoubleSidedQuad(posMatrix, buffer, 0.1f, 0.1f, 0.1f, 0.2f, cColor, 255);
                            matrices.pop();
                        }
                    } else {
                        Matrix4f posMatrix = matrices.peek().getPositionMatrix();
                        drawDoubleSidedQuad(posMatrix, buffer, -0.2f, -0.2f, 0.4f, 0.4f, cColor, 255);
                        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90f));
                        drawDoubleSidedQuad(matrices.peek().getPositionMatrix(), buffer, -0.2f, -0.2f, 0.4f, 0.4f, cColor, 255);
                    }
                    matrices.pop();

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

                // --- 2. REGULAR 3D INDICATORS ---
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
                int hitsToKill = (int) Math.ceil(health / weaponDamage);
                int textColor = (healthPercent < 0.3f) ? 0xFFFF3333 : (healthPercent < 0.6f) ? 0xFFFFAA00 : 0xFF00FF00;

                // --------- STYLE 0: REAL MINECRAFT HEARTS ---------
                if (ModConfig.indicatorStyle == 0) {
                    int totalHearts = (int) Math.ceil(Math.max(maxHealth, health) / 2.0f);
                    
                    Sprite fullHeart = client.getGuiAtlasManager().getSprite(Identifier.ofVanilla("hud/heart/full"));
                    Sprite halfHeart = client.getGuiAtlasManager().getSprite(Identifier.ofVanilla("hud/heart/half"));
                    Sprite emptyHeart = client.getGuiAtlasManager().getSprite(Identifier.ofVanilla("hud/heart/container"));
                    
                    VertexConsumer heartConsumer = immediate.getBuffer(RenderLayer.getTextSeeThrough(fullHeart.getAtlasId()));
                    
                    float heartSize = 9f;
                    float startX = -(totalHearts * heartSize) / 2f;

                    for (int i = 0; i < totalHearts; i++) {
                        float hx = startX + (i * heartSize);
                        // Empty background heart
                        drawSpriteQuad(positionMatrix, heartConsumer, hx, 0, heartSize, heartSize, emptyHeart, light);
                        // Full / Half heart based on health
                        if (health >= (i * 2) + 2) {
                            drawSpriteQuad(positionMatrix, heartConsumer, hx, 0, heartSize, heartSize, fullHeart, light);
                        } else if (health > (i * 2)) {
                            drawSpriteQuad(positionMatrix, heartConsumer, hx, 0, heartSize, heartSize, halfHeart, light);
                        }
                    }
                } 
                // --------- STYLE 1: MODERN PROGRESS BAR ---------
                else if (ModConfig.indicatorStyle == 1) { 
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
                // --------- STYLE 2: HEAD + HITS TO KILL ---------
                else if (ModConfig.indicatorStyle == 2) {
                    TextRenderer textRenderer = client.textRenderer;
                    String text = target instanceof PlayerEntity 
                                ? target.getName().getString() + " | Hits: " + hitsToKill 
                                : "Hits to kill: " + hitsToKill;
                    
                    float textWidth = textRenderer.getWidth(text);
                    float textStartX = -textWidth / 2f;

                    if (target instanceof AbstractClientPlayerEntity playerTarget) {
                        Identifier skin = playerTarget.getSkinTextures().texture();
                        VertexConsumer headConsumer = immediate.getBuffer(RenderLayer.getTextSeeThrough(skin));
                        drawTextureQuad(positionMatrix, headConsumer, textStartX - 12, -1, 10, 10, 8f/64f, 8f/64f, 16f/64f, 16f/64f, light);
                    }

                    textRenderer.draw(text, textStartX, 0, textColor, false, positionMatrix, immediate, TextRenderer.TextLayerType.SEE_THROUGH, 0x40000000, light);
                }
                matrices.pop();
            }
        }
        immediate.draw();
    }

    // --- BUG FIX: ADDED .normal(0, 0, 1) FOR TEXTURES TO RENDER PROPERLY IN 3D WORLD ---
    
    private static void drawDoubleSidedQuad(Matrix4f matrix, VertexConsumer consumer, float x, float y, float width, float height, int argb, int light) {
        float a = (argb >> 24 & 255) / 255.0F;
        float r = (argb >> 16 & 255) / 255.0F;
        float g = (argb >> 8 & 255) / 255.0F;
        float b = (argb & 255) / 255.0F;
        
        consumer.vertex(matrix, x, y, 0).color(r, g, b, a).light(light);
        consumer.vertex(matrix, x, y + height, 0).color(r, g, b, a).light(light);
        consumer.vertex(matrix, x + width, y + height, 0).color(r, g, b, a).light(light);
        consumer.vertex(matrix, x + width, y, 0).color(r, g, b, a).light(light);
    }

    private static void drawColorQuad(Matrix4f matrix, VertexConsumer consumer, float x, float y, float width, float height, int argb, int light) {
        drawDoubleSidedQuad(matrix, consumer, x, y, width, height, argb, light);
    }

    private static void drawSpriteQuad(Matrix4f matrix, VertexConsumer consumer, float x, float y, float width, float height, Sprite sprite, int light) {
        drawTextureQuad(matrix, consumer, x, y, width, height, sprite.getMinU(), sprite.getMinV(), sprite.getMaxU(), sprite.getMaxV(), light);
    }

    private static void drawTextureQuad(Matrix4f matrix, VertexConsumer consumer, float x, float y, float width, float height, float u1, float v1, float u2, float v2, int light) {
        // EXACT FIX: Added .normal(0, 0, 1) which prevents the buffer crash for textures!
        consumer.vertex(matrix, x, y, 0).color(255, 255, 255, 255).texture(u1, v1).light(light).normal(0, 0, 1);
        consumer.vertex(matrix, x, y + height, 0).color(255, 255, 255, 255).texture(u1, v2).light(light).normal(0, 0, 1);
        consumer.vertex(matrix, x + width, y + height, 0).color(255, 255, 255, 255).texture(u2, v2).light(light).normal(0, 0, 1);
        consumer.vertex(matrix, x + width, y, 0).color(255, 255, 255, 255).texture(u2, v1).light(light).normal(0, 0, 1);
    }
}
