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

        // ------------------------------------------
        // 1. TARGET SELECTION LOGIC
        // ------------------------------------------
        PlayerEntity lockedTarget = null;
        if (ModConfig.targetEnabled) {
            if (ModConfig.targetMode == 1) { // AUTO MODE
                float minHp = 999f;
                for (PlayerEntity p : client.world.getPlayers()) {
                    if (p == client.player || p.isSpectator()) continue;
                    if (p.distanceTo(client.player) > ModConfig.autoRange) continue;
                    if (p.getHealth() < minHp) {
                        minHp = p.getHealth();
                        lockedTarget = p;
                    }
                }
            } else { // MANUAL MODE
                if (!ModConfig.taggedPlayerName.isEmpty()) {
                    for (PlayerEntity p : client.world.getPlayers()) {
                        if (p.getName().getString().equals(ModConfig.taggedPlayerName)) {
                            lockedTarget = p; break;
                        }
                    }
                }
            }
        }

        // ------------------------------------------
        // 2. WORLD ENTITY RENDER LOOP
        // ------------------------------------------
        double weaponDamage = client.player.getAttributeValue(EntityAttributes.ATTACK_DAMAGE);
        if (weaponDamage <= 0) weaponDamage = 1.0;

        for (Entity entity : client.world.getEntities()) {
            if (entity instanceof LivingEntity target && entity != client.player) {
                // Filters
                if (target.isInvisible() || target instanceof ArmorStandEntity) continue;
                if (target.distanceTo(client.player) > 32.0) continue;

                boolean isLocked = (target == lockedTarget);
                double yOffset = target.getHeight() + 0.825;
                Vec3d tPos = target.getLerpedPos(tickDelta);
                double x = tPos.x - cameraPos.x;
                double y = tPos.y - cameraPos.y + yOffset;
                double z = tPos.z - cameraPos.z;

                // --- RENDER 3D VOXEL CROWN ---
                if (isLocked) {
                    matrices.push();
                    matrices.translate(x, y + 0.6, z);
                    float rotation = (client.world.getTime() + tickDelta) * 6f;
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotation));
                    matrices.scale(ModConfig.crownScale, ModConfig.crownScale, ModConfig.crownScale);

                    VertexConsumer buffer = immediate.getBuffer(RenderLayer.getTextBackgroundSeeThrough()); 
                    int c = (ModConfig.crownColor == 1) ? 0xFFFF3333 : (ModConfig.crownColor == 2) ? 0xFF33FFFF : (ModConfig.crownColor == 3) ? 0xFF33FF33 : 0xFFFFD700;

                    // Draw Crown Base
                    float s = 0.2f, th = 0.05f;
                    drawCube(matrices.peek().getPositionMatrix(), buffer, -s, 0, -s, s, th, s, c, 255);
                    // Draw 4 Spikes
                    drawCube(matrices.peek().getPositionMatrix(), buffer, -s, th, -s, -s+0.08f, 0.25f, -s+0.08f, c, 255);
                    drawCube(matrices.peek().getPositionMatrix(), buffer, s-0.08f, th, -s, s, 0.25f, -s+0.08f, c, 255);
                    drawCube(matrices.peek().getPositionMatrix(), buffer, -s, th, s-0.08f, -s+0.08f, 0.25f, s, c, 255);
                    drawCube(matrices.peek().getPositionMatrix(), buffer, s-0.08f, th, s-0.08f, s, 0.25f, s, c, 255);
                    
                    matrices.pop();

                    // Target Label
                    matrices.push();
                    matrices.translate(x, y + 0.25, z);
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
                    matrices.scale(-0.025f, -0.025f, 0.025f);
                    String label = "§lTARGET LOCK";
                    client.textRenderer.draw(label, -client.textRenderer.getWidth(label)/2f, 0, 0xFFFFFF, false, matrices.peek().getPositionMatrix(), immediate, TextRenderer.TextLayerType.SEE_THROUGH, 0x4D000000, 255);
                    matrices.pop();
                }

                // --- RENDER REGULAR INDICATORS ---
                if (ModConfig.indicatorEnabled) {
                    matrices.push();
                    matrices.translate(x, y, z);
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
                    matrices.scale(-0.025f, -0.025f, 0.025f);

                    float hp = target.getHealth(), max = target.getMaxHealth(), pct = hp/max;
                    int color = (pct < 0.3f) ? 0xFFFF3333 : (pct < 0.6f) ? 0xFFFFAA00 : 0xFF00FF00;
                    Matrix4f m = matrices.peek().getPositionMatrix();

                    if (ModConfig.indicatorStyle == 0) { // HEARTS
                        int hearts = (int) Math.ceil(max / 2.0f);
                        Sprite full = client.getGuiAtlasManager().getSprite(Identifier.ofVanilla("hud/heart/full"));
                        Sprite container = client.getGuiAtlasManager().getSprite(Identifier.ofVanilla("hud/heart/container"));
                        VertexConsumer hBuf = immediate.getBuffer(RenderLayer.getTextSeeThrough(full.getAtlasId()));
                        float startX = -(hearts * 9) / 2f;
                        for (int i=0; i<hearts; i++) {
                            drawSpriteQuad(m, hBuf, startX + (i*9), 0, 9, 9, container, 255);
                            if (hp >= (i*2)+2) drawSpriteQuad(m, hBuf, startX + (i*9), 0, 9, 9, full, 255);
                        }
                    } 
                    else if (ModConfig.indicatorStyle == 1) { // BAR
                        drawColorQuad(m, immediate.getBuffer(RenderLayer.getTextBackgroundSeeThrough()), -26, 0, 52, 7, 0xFF000000, 255);
                        drawColorQuad(m, immediate.getBuffer(RenderLayer.getTextBackgroundSeeThrough()), -25, 1, 50 * pct, 5, color | 0xFF000000, 255);
                        String pTxt = (int)(pct*100) + "%";
                        client.textRenderer.draw(pTxt, -client.textRenderer.getWidth(pTxt)/2f, -9, 0xFFFFFF, false, m, immediate, TextRenderer.TextLayerType.SEE_THROUGH, 0, 255);
                    }
                    else if (ModConfig.indicatorStyle == 2) { // STATS
                        int hits = (int) Math.ceil(hp / weaponDamage);
                        String s = (target instanceof PlayerEntity) ? target.getName().getString() + " | Hits: " + hits : "Hits: " + hits;
                        client.textRenderer.draw(s, -client.textRenderer.getWidth(s)/2f, 0, color, false, m, immediate, TextRenderer.TextLayerType.SEE_THROUGH, 0x4D000000, 255);
                    }
                    matrices.pop();
                }
            }
        }
        immediate.draw();
    }

    // ==========================================
    // RENDERING HELPERS (FULLY EXPANDED)
    // ==========================================

    private static void drawQuad3D(Matrix4f m, VertexConsumer v, float x1, float y1, float z1, float x2, float y2, float z2, int c, int l) {
        float a=(c>>24&255)/255f, r=(c>>16&255)/255f, g=(c>>8&255)/255f, b=(c&255)/255f;
        v.vertex(m,x1,y1,z1).color(r,g,b,a).light(l).next();
        v.vertex(m,x1,y2,z2).color(r,g,b,a).light(l).next();
        v.vertex(m,x2,y2,z2).color(r,g,b,a).light(l).next();
        v.vertex(m,x2,y1,z1).color(r,g,b,a).light(l).next();
    }

    private static void drawCube(Matrix4f m, VertexConsumer v, float x1, float y1, float z1, float x2, float y2, float z2, int c, int l) {
        drawQuad3D(m, v, x1, y1, z1, x2, y2, z1, c, l); // Front
        drawQuad3D(m, v, x1, y1, z2, x2, y2, z2, c, l); // Back
        drawQuad3D(m, v, x1, y1, z1, x1, y2, z2, c, l); // Left
        drawQuad3D(m, v, x2, y1, z1, x2, y2, z2, c, l); // Right
        drawQuad3D(m, v, x1, y2, z1, x2, y2, z2, c, l); // Top
        drawQuad3D(m, v, x1, y1, z1, x2, y1, z2, c, l); // Bottom
    }

    private static void drawColorQuad(Matrix4f m, VertexConsumer v, float x, float y, float w, float h, int c, int l) {
        float a=(c>>24&255)/255f, r=(c>>16&255)/255f, g=(c>>8&255)/255f, b=(c&255)/255f;
        v.vertex(m, x, y, 0).color(r, g, b, a).light(l).next();
        v.vertex(m, x, y + h, 0).color(r, g, b, a).light(l).next();
        v.vertex(m, x + w, y + h, 0).color(r, g, b, a).light(l).next();
        v.vertex(m, x + w, y, 0).color(r, g, b, a).light(l).next();
    }

    private static void drawSpriteQuad(Matrix4f m, VertexConsumer v, float x, float y, float w, float h, Sprite s, int l) {
        drawTextureQuad(m, v, x, y, w, h, s.getMinU(), s.getMinV(), s.getMaxU(), s.getMaxV(), l);
    }

    private static void drawTextureQuad(Matrix4f m, VertexConsumer v, float x, float y, float w, float h, float u1, float v1, float u2, float v2, int l) {
        v.vertex(m, x, y, 0).color(1f, 1f, 1f, 1f).texture(u1, v1).light(l).normal(0, 0, 1).next();
        v.vertex(m, x, y + h, 0).color(1f, 1f, 1f, 1f).texture(u1, v2).light(l).normal(0, 0, 1).next();
        v.vertex(m, x + w, y + h, 0).color(1f, 1f, 1f, 1f).texture(u2, v2).light(l).normal(0, 0, 1).next();
        v.vertex(m, x + w, y, 0).color(1f, 1f, 1f, 1f).texture(u2, v1).light(l).normal(0, 0, 1).next();
    }
}
