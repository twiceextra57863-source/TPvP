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
        Vec3d camPos = camera.getPos();
        float tickDelta = context.tickCounter().getTickDelta(true);
        MatrixStack matrices = context.matrixStack();
        VertexConsumerProvider.Immediate immediate = client.getBufferBuilders().getEntityVertexConsumers();

        // ---------------- AUTO TRACK LOGIC ----------------
        String activeTarget = ModConfig.taggedPlayerName;
        if (ModConfig.autoTrack) {
            double lowestHp = 9999;
            for (Entity e : client.world.getEntities()) {
                if (e instanceof LivingEntity le && e != client.player && !e.isInvisible() && !(e instanceof ArmorStandEntity)) {
                    if (client.player.distanceTo(le) < 32.0 && le.getHealth() < lowestHp && le.getHealth() > 0) {
                        lowestHp = le.getHealth();
                        activeTarget = le.getName().getString(); // Lowest HP wale pe target set karo
                    }
                }
            }
        }

        double weaponDmg = client.player.getAttributeValue(EntityAttributes.ATTACK_DAMAGE);
        if (weaponDmg <= 0) weaponDmg = 1.0;

        for (Entity entity : client.world.getEntities()) {
            // FIX: PlayerEntity hٹا kar LivingEntity kar diya (ab Mobs pe bhi chalega).
            // ArmorStandEntity filter kar diya taaki NPC dupe na aaye!
            if (!(entity instanceof LivingEntity target) || target == client.player || target.isInvisible() || target instanceof ArmorStandEntity) continue;
            
            if (target.distanceTo(client.player) > 64.0) continue;

            Vec3d tPos = target.getLerpedPos(tickDelta);
            double x = tPos.x - camPos.x;
            double y = tPos.y - camPos.y;
            double z = tPos.z - camPos.z;

            // 1. BOUNCING ARROW (Active Target)
            if (target.getName().getString().equals(activeTarget)) {
                matrices.push();
                double bounce = Math.sin(System.currentTimeMillis() / 150.0) * 0.2;
                matrices.translate(x, y + target.getHeight() + 1.8 + bounce, z); 
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
                matrices.scale(-0.1F, -0.1F, 0.1F); 
                
                Matrix4f mat = matrices.peek().getPositionMatrix();
                client.textRenderer.draw("▼", -client.textRenderer.getWidth("▼") / 2f, 0, 0xFFFF2222, true, mat, immediate, TextRenderer.TextLayerType.SEE_THROUGH, 0x00000000, LightmapTextureManager.MAX_LIGHT_COORDINATE);
                matrices.pop();
            }

            // 2. HITBOXES
            if (ModConfig.hitboxEnabled) {
                matrices.push();
                matrices.translate(x, y, z);
                VertexConsumer lineBuffer = immediate.getBuffer(RenderLayer.getLines());
                Matrix4f matrix = matrices.peek().getPositionMatrix();
                float w = target.getWidth() / 2.0f, h = target.getHeight();
                float r = 1f, g = 0f, b = 0.2f, a = 1f; // Ruby Red Hitbox
                
                drawLine(matrix, lineBuffer, -w, 0, -w, w, 0, -w, r, g, b, a); drawLine(matrix, lineBuffer, w, 0, -w, w, 0, w, r, g, b, a);
                drawLine(matrix, lineBuffer, w, 0, w, -w, 0, w, r, g, b, a); drawLine(matrix, lineBuffer, -w, 0, w, -w, 0, -w, r, g, b, a);
                drawLine(matrix, lineBuffer, -w, h, -w, w, h, -w, r, g, b, a); drawLine(matrix, lineBuffer, w, h, -w, w, h, w, r, g, b, a);
                drawLine(matrix, lineBuffer, w, h, w, -w, h, w, r, g, b, a); drawLine(matrix, lineBuffer, -w, h, w, -w, h, -w, r, g, b, a);
                drawLine(matrix, lineBuffer, -w, 0, -w, -w, h, -w, r, g, b, a); drawLine(matrix, lineBuffer, w, 0, -w, w, h, -w, r, g, b, a);
                drawLine(matrix, lineBuffer, w, 0, w, w, h, w, r, g, b, a); drawLine(matrix, lineBuffer, -w, 0, w, -w, h, w, r, g, b, a);
                matrices.pop();
            }

            // 3. INDICATOR
            if (ModConfig.indicatorEnabled && target.distanceTo(client.player) < 32.0) {
                matrices.push();
                matrices.translate(x, y + target.getHeight() + 1.2, z); 
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
                matrices.scale(-0.025F, -0.025F, 0.025F);

                Matrix4f posMat = matrices.peek().getPositionMatrix();
                int light = LightmapTextureManager.MAX_LIGHT_COORDINATE;

                float health = target.getHealth(), maxHealth = target.getMaxHealth();
                float hpPercent = Math.max(0, Math.min(1, health / maxHealth));

                if (ModConfig.indicatorStyle == 0) { 
                    int tH = (int) Math.ceil(Math.max(maxHealth, health) / 2.0f);
                    Sprite fH = client.getGuiAtlasManager().getSprite(Identifier.ofVanilla("hud/heart/full"));
                    Sprite hH = client.getGuiAtlasManager().getSprite(Identifier.ofVanilla("hud/heart/half"));
                    Sprite eH = client.getGuiAtlasManager().getSprite(Identifier.ofVanilla("hud/heart/container"));
                    VertexConsumer hc = immediate.getBuffer(RenderLayer.getTextSeeThrough(fH.getAtlasId()));
                    float stX = -(tH * 9f) / 2f;
                    for (int i = 0; i < tH; i++) {
                        drawTextureQuad(posMat, hc, stX + (i*9), 0, 9, 9, eH.getMinU(), eH.getMinV(), eH.getMaxU(), eH.getMaxV(), light);
                        if (health >= (i*2)+2) drawTextureQuad(posMat, hc, stX + (i*9), 0, 9, 9, fH.getMinU(), fH.getMinV(), fH.getMaxU(), fH.getMaxV(), light);
                        else if (health > (i*2)) drawTextureQuad(posMat, hc, stX + (i*9), 0, 9, 9, hH.getMinU(), hH.getMinV(), hH.getMaxU(), hH.getMaxV(), light);
                    }
                } else if (ModConfig.indicatorStyle == 1) { 
                    VertexConsumer bc = immediate.getBuffer(RenderLayer.getTextBackgroundSeeThrough());
                    float cW = 50f * hpPercent;
                    int bC = (hpPercent < 0.3f) ? 0xFFFF3333 : (hpPercent < 0.6f) ? 0xFFFFAA00 : 0xFF00FF00;
                    drawColorQuad(posMat, bc, -26, 0, 52, 7, 0xFF000000, light);
                    drawColorQuad(posMat, bc, -25, 1, 50, 5, 0xFF333333, light); 
                    if (cW > 0) drawColorQuad(posMat, bc, -25, 1, cW, 5, bC, light); 
                    String pt = (int)(hpPercent * 100) + "%";
                    client.textRenderer.draw(pt, -client.textRenderer.getWidth(pt) / 2f, -9, 0xFFFFFF, false, posMat, immediate, TextRenderer.TextLayerType.SEE_THROUGH, 0x00000000, light);
                } else if (ModConfig.indicatorStyle == 2) { 
                    int hitsToKill = (int) Math.ceil(health / weaponDmg);
                    String text = target.getName().getString() + " | Hits: " + hitsToKill;
                    int txtColor = (hpPercent < 0.3f) ? 0xFF0000 : (hpPercent < 0.6f) ? 0xFFFF00 : 0x00FF00;
                    float stX = -client.textRenderer.getWidth(text) / 2f;
                    
                    if (target instanceof AbstractClientPlayerEntity pt) {
                        VertexConsumer hc = immediate.getBuffer(RenderLayer.getTextSeeThrough(pt.getSkinTextures().texture()));
                        drawTextureQuad(posMat, hc, stX - 12, -1, 10, 10, 8f/64f, 8f/64f, 16f/64f, 16f/64f, light);
                    }
                    client.textRenderer.draw(text, stX, 0, txtColor, false, posMat, immediate, TextRenderer.TextLayerType.SEE_THROUGH, 0x40000000, light);
                }
                matrices.pop();
            }
        }
        immediate.draw();
    }

    private static void drawLine(Matrix4f m, VertexConsumer v, float x1, float y1, float z1, float x2, float y2, float z2, float r, float g, float b, float a) {
        v.vertex(m, x1, y1, z1).color(r, g, b, a).normal(x2-x1, y2-y1, z2-z1);
        v.vertex(m, x2, y2, z2).color(r, g, b, a).normal(x2-x1, y2-y1, z2-z1);
    }
    private static void drawTextureQuad(Matrix4f m, VertexConsumer v, float x, float y, float w, float h, float u1, float v1, float u2, float v2, int l) {
        v.vertex(m, x, y, 0).color(1f, 1f, 1f, 1f).texture(u1, v1).light(l);
        v.vertex(m, x, y+h, 0).color(1f, 1f, 1f, 1f).texture(u1, v2).light(l);
        v.vertex(m, x+w, y+h, 0).color(1f, 1f, 1f, 1f).texture(u2, v2).light(l);
        v.vertex(m, x+w, y, 0).color(1f, 1f, 1f, 1f).texture(u2, v1).light(l);
    }
    private static void drawColorQuad(Matrix4f m, VertexConsumer v, float x, float y, float w, float h, int c, int l) {
        float a = (c >> 24 & 255) / 255.0F, r = (c >> 16 & 255) / 255.0F, g = (c >> 8 & 255) / 255.0F, b = (c & 255) / 255.0F;
        v.vertex(m, x, y, 0).color(r, g, b, a).light(l);
        v.vertex(m, x, y+h, 0).color(r, g, b, a).light(l);
        v.vertex(m, x+w, y+h, 0).color(r, g, b, a).light(l);
        v.vertex(m, x+w, y, 0).color(r, g, b, a).light(l);
    }
                        }
