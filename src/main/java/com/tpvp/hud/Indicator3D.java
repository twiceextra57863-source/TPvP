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
        if (!ModConfig.indicatorEnabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        Camera camera = context.camera();
        Vec3d cameraPos = camera.getPos();
        float tickDelta = context.tickCounter().getTickDelta(true);
        MatrixStack matrices = context.matrixStack();

        VertexConsumerProvider.Immediate immediate = client.getBufferBuilders().getEntityVertexConsumers();

        double weaponDamage = client.player.getAttributeValue(EntityAttributes.ATTACK_DAMAGE);
        if (weaponDamage <= 0) weaponDamage = 1.0;

        for (Entity entity : client.world.getEntities()) {
            if (entity instanceof LivingEntity target && entity != client.player) {
                if (target.distanceTo(client.player) > 32.0) continue;

                double yOffset = target.getHeight() + 0.825;
                Vec3d targetPos = target.getLerpedPos(tickDelta);
                
                double x = targetPos.x - cameraPos.x;
                double y = targetPos.y - cameraPos.y + yOffset;
                double z = targetPos.z - cameraPos.z;

                matrices.push();
                matrices.translate(x, y, z);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
                matrices.scale(-0.025F, -0.025F, 0.025F);

                Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
                int light = LightmapTextureManager.MAX_LIGHT_COORDINATE;

                float health = target.getHealth();
                float maxHealth = target.getMaxHealth();
                int hitsToKill = (int) Math.ceil(health / weaponDamage);
                float healthPercent = Math.max(0, Math.min(1, health / maxHealth));

                int textColor = 0x00FF00;
                if (healthPercent < 0.3f) textColor = 0xFF0000;
                else if (healthPercent < 0.6f) textColor = 0xFFFF00;

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
                        drawSpriteQuad(positionMatrix, heartConsumer, hx, 0, heartSize, heartSize, emptyHeart, light);
                        if (health >= (i * 2) + 2) drawSpriteQuad(positionMatrix, heartConsumer, hx, 0, heartSize, heartSize, fullHeart, light);
                        else if (health > (i * 2)) drawSpriteQuad(positionMatrix, heartConsumer, hx, 0, heartSize, heartSize, halfHeart, light);
                    }
                } else if (ModConfig.indicatorStyle == 1) {
                    VertexConsumer barConsumer = immediate.getBuffer(RenderLayer.getTextBackgroundSeeThrough());
                    float barWidth = 50f, barHeight = 5f, currentWidth = barWidth * healthPercent;
                    
                    int barColor = 0xFF00FF00;
                    if (healthPercent < 0.3f) barColor = 0xFFFF3333;
                    else if (healthPercent < 0.6f) barColor = 0xFFFFAA00;

                    drawColorQuad(positionMatrix, barConsumer, -barWidth/2 - 1, 0, barWidth + 2, barHeight + 2, 0xFF000000, light);
                    drawColorQuad(positionMatrix, barConsumer, -barWidth/2, 1, barWidth, barHeight, 0xFF333333, light);
                    if (currentWidth > 0) drawColorQuad(positionMatrix, barConsumer, -barWidth/2, 1, currentWidth, barHeight, barColor, light);

                    TextRenderer textRenderer = client.textRenderer;
                    String percentText = (int)(healthPercent * 100) + "%";
                    float textWidth = textRenderer.getWidth(percentText);
                    textRenderer.draw(percentText, -textWidth / 2f, -9, 0xFFFFFF, false, positionMatrix, immediate, TextRenderer.TextLayerType.SEE_THROUGH, 0x00000000, light);
                } else if (ModConfig.indicatorStyle == 2) {
                    TextRenderer textRenderer = client.textRenderer;
                    String text = "Hits to kill: " + hitsToKill;
                    if (target instanceof PlayerEntity) text = target.getName().getString() + " | Hits: " + hitsToKill;
                    
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

    // --- YE HELPER METHODS HAIN JINME SE .next() HATA DIYA GAYA HAI ---

    private static void drawSpriteQuad(Matrix4f matrix, VertexConsumer consumer, float x, float y, float width, float height, Sprite sprite, int light) {
        drawTextureQuad(matrix, consumer, x, y, width, height, sprite.getMinU(), sprite.getMinV(), sprite.getMaxU(), sprite.getMaxV(), light);
    }

    private static void drawTextureQuad(Matrix4f matrix, VertexConsumer consumer, float x, float y, float width, float height, float u1, float v1, float u2, float v2, int light) {
        consumer.vertex(matrix, x, y, 0).color(1f, 1f, 1f, 1f).texture(u1, v1).light(light);
        consumer.vertex(matrix, x, y + height, 0).color(1f, 1f, 1f, 1f).texture(u1, v2).light(light);
        consumer.vertex(matrix, x + width, y + height, 0).color(1f, 1f, 1f, 1f).texture(u2, v2).light(light);
        consumer.vertex(matrix, x + width, y, 0).color(1f, 1f, 1f, 1f).texture(u2, v1).light(light);
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
