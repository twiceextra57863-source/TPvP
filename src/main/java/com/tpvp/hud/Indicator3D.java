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
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.*;

public class Indicator3D {
    
    private static final Map<Integer, Float> lastHealthMap = new HashMap<>();
    private static final Map<Integer, Boolean> lastTotemMap = new HashMap<>();
    private static final Map<Integer, Long> totemPopMap = new HashMap<>();
    private static final List<DeadSoul> activeSouls = new ArrayList<>();

    private static class DeadSoul {
        Vec3d pos; Identifier skin; long startTime;
        DeadSoul(Vec3d pos, Identifier skin) { this.pos = pos; this.skin = skin; this.startTime = System.currentTimeMillis(); }
    }

    public static void register() { WorldRenderEvents.LAST.register(Indicator3D::onWorldRender); }

    private static void onWorldRender(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        Camera camera = context.camera();
        Vec3d camPos = camera.getPos();
        float tickDelta = context.tickCounter().getTickDelta(true);
        MatrixStack matrices = context.matrixStack();
        VertexConsumerProvider.Immediate immediate = client.getBufferBuilders().getEntityVertexConsumers();

        // ---------------------------------------------------------
        // 1. EPIC TRUE 3D SOUL RAGDOLL DANCE
        // ---------------------------------------------------------
        if (ModConfig.soulAnimationEnabled) {
            long now = System.currentTimeMillis();
            Iterator<DeadSoul> iter = activeSouls.iterator();
            
            while (iter.hasNext()) {
                DeadSoul soul = iter.next();
                long age = now - soul.startTime;
                if (age > 5000) { iter.remove(); continue; } 

                float life = age / 5000.0f; // 0.0 to 1.0
                double upY = life * 6.0; // Float up 6 blocks
                
                matrices.push();
                matrices.translate(soul.pos.x - camPos.x, soul.pos.y - camPos.y + upY, soul.pos.z - camPos.z);
                
                // Spin entire body like a ritual dance
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(life * 360f * 4f)); 
                
                // Proper scaling and orientation
                matrices.scale(0.8F, 0.8F, 0.8F); 
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180f)); // FIX UPSIDE DOWN!
                
                VertexConsumer buffer = immediate.getBuffer(RenderLayer.getEntityTranslucent(soul.skin));
                int l = LightmapTextureManager.MAX_LIGHT_COORDINATE;
                
                // Limbs animation math
                float armPitch = (float) Math.sin(life * Math.PI) * 160f; // Arms go up
                float legPitch = (float) Math.sin(life * Math.PI) * 90f; // Legs go up
                float headPitch = (float) Math.sin(life * Math.PI) * -45f; // Head tilts down

                // Draw Head
                matrices.push(); matrices.translate(0, -1.5f, 0); matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(headPitch));
                draw3DSkinBox(matrices.peek().getPositionMatrix(), buffer, -0.25f, -0.25f, -0.25f, 0.5f, 0.5f, 0.5f, 8, 8, 8, 8, l);
                matrices.pop();

                // Draw Body
                matrices.push(); matrices.translate(0, -0.75f, 0);
                draw3DSkinBox(matrices.peek().getPositionMatrix(), buffer, -0.25f, -0.375f, -0.125f, 0.5f, 0.75f, 0.25f, 20, 16, 8, 12, l);
                matrices.pop();

                // Draw Right Arm
                matrices.push(); matrices.translate(-0.375f, -1.0f, 0); matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-armPitch));
                draw3DSkinBox(matrices.peek().getPositionMatrix(), buffer, -0.125f, 0, -0.125f, 0.25f, 0.75f, 0.25f, 44, 16, 4, 12, l);
                matrices.pop();

                // Draw Left Arm
                matrices.push(); matrices.translate(0.375f, -1.0f, 0); matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-armPitch));
                draw3DSkinBox(matrices.peek().getPositionMatrix(), buffer, -0.125f, 0, -0.125f, 0.25f, 0.75f, 0.25f, 36, 52, 4, 12, l);
                matrices.pop();

                // Draw Right Leg
                matrices.push(); matrices.translate(-0.125f, -0.375f, 0); matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-legPitch));
                draw3DSkinBox(matrices.peek().getPositionMatrix(), buffer, -0.125f, 0, -0.125f, 0.25f, 0.75f, 0.25f, 0, 16, 4, 12, l);
                matrices.pop();

                // Draw Left Leg
                matrices.push(); matrices.translate(0.125f, -0.375f, 0); matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-legPitch));
                draw3DSkinBox(matrices.peek().getPositionMatrix(), buffer, -0.125f, 0, -0.125f, 0.25f, 0.75f, 0.25f, 16, 52, 4, 12, l);
                matrices.pop();

                matrices.pop();
            }
        }

        // ---------------------------------------------------------
        // 2. ENTITY LOOP (Kills, Totems, Auras)
        // ---------------------------------------------------------
        String enemyTarget = ModConfig.taggedPlayerName;
        String friendTarget = ModConfig.taggedFriendName;

        for (Entity entity : client.world.getEntities()) {
            if (!(entity instanceof LivingEntity target) || target == client.player || target instanceof ArmorStandEntity) continue;
            
            float health = target.getHealth();
            int id = target.getId();

            // --- TOTEM POP DETECTION ---
            boolean hasTotem = target.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING) || target.getMainHandStack().isOf(Items.TOTEM_OF_UNDYING);
            boolean hadTotem = lastTotemMap.getOrDefault(id, false);
            if (hadTotem && !hasTotem && health > 0 && health < 10) totemPopMap.put(id, System.currentTimeMillis());
            lastTotemMap.put(id, hasTotem);

            // --- KILL DETECTION ---
            if (lastHealthMap.containsKey(id)) {
                float lastHealth = lastHealthMap.get(id);
                if (lastHealth > 0 && health <= 0) {
                    if (target.distanceTo(client.player) < 30.0) {
                        // Pass Names and Skins to Banner!
                        Identifier vSkin = (target instanceof AbstractClientPlayerEntity pt) ? pt.getSkinTextures().texture() : Identifier.ofVanilla("textures/entity/steve.png");
                        KillBannerHud.addKill(client.player.getName().getString(), client.player.getSkinTextures().texture(), target.getName().getString(), vSkin); 
                    }
                    if (target instanceof AbstractClientPlayerEntity pt) {
                        activeSouls.add(new DeadSoul(target.getPos(), pt.getSkinTextures().texture())); 
                    }
                }
            }
            lastHealthMap.put(id, health);

            if (health <= 0 || target.isInvisible() || target.distanceTo(client.player) > 64.0) continue;

            Vec3d tPos = target.getLerpedPos(tickDelta);
            double x = tPos.x - camPos.x, y = tPos.y - camPos.y, z = tPos.z - camPos.z;

            // ---------------------------------------------------------
            // --- ENEMY DRAGON AURA (WITH TOTEM BLAST) ---
            // ---------------------------------------------------------
            if (target.getName().getString().equals(enemyTarget) && ModConfig.dragonAuraEnabled) {
                long popTime = totemPopMap.getOrDefault(id, 0L);
                long now = System.currentTimeMillis();
                boolean isTotemPop = (now - popTime) < 3000;

                matrices.push(); matrices.translate(x, y, z);
                Matrix4f mat = matrices.peek().getPositionMatrix();
                VertexConsumer quadBuffer = immediate.getBuffer(RenderLayer.getGui()); 
                
                float h = target.getHeight(), radius = target.getWidth() + 0.3f;
                float headX = 0, headY = 0, headZ = 0;
                
                float yOff = 0f, rotMult = 1f, shatter = 0f, alpha = 0.8f;
                
                // EPIC TOTEM BLAST SPIRAL
                if (isTotemPop) {
                    float popT = (now - popTime) / 3000.0f;
                    if (popT < 0.5f) { // Spiral Blast Outward
                        radius += popT * 5.0f;
                        yOff = popT * 10.0f;
                        rotMult = 5.0f;
                    } else { // Turn to Dust
                        shatter = (popT - 0.5f) / 0.5f;
                        alpha = 0.8f * (1.0f - shatter);
                    }
                } else {
                    float tNorm = (now % 4000) / 4000.0f;
                    if (tNorm > 0.6f) { shatter = (tNorm - 0.6f) / 0.1f; if (shatter > 1.0f) { shatter = 1.0f; alpha = 0f; } else alpha = 0.8f * (1.0f - shatter); }
                }

                if (alpha > 0.05f) {
                    float tRot = ((now % 2000) / 2000.0f) * rotMult;
                    for (int layer = 0; layer < 3; layer++) {
                        for (int i = 0; i < 30; i++) {
                            float pt1 = (i / 30.0f), pt2 = ((i + 1) / 30.0f);
                            float py1 = pt1 * h + yOff, px1 = (float) Math.cos((pt1 + tRot) * Math.PI * 4) * radius, pz1 = (float) Math.sin((pt1 + tRot) * Math.PI * 4) * radius;
                            float py2 = pt2 * h + yOff, px2 = (float) Math.cos((pt2 + tRot) * Math.PI * 4) * radius, pz2 = (float) Math.sin((pt2 + tRot) * Math.PI * 4) * radius;

                            if (shatter > 0) {
                                px1 += (float) Math.sin(i * 13) * shatter * 4.0f; py1 += (float) Math.cos(i * 17) * shatter * 4.0f; pz1 += (float) Math.sin(i * 23) * shatter * 4.0f;
                                px2 += (float) Math.sin(i * 13) * shatter * 4.0f; py2 += (float) Math.cos(i * 17) * shatter * 4.0f; pz2 += (float) Math.sin(i * 23) * shatter * 4.0f;
                            }
                            if (layer == 1 && i == 29) { headX = px2; headY = py2; headZ = pz2; }
                            
                            float red = 1f, green = isTotemPop ? 0.8f : 0.1f, blue = isTotemPop ? 0.2f : 0.1f;
                            drawQuad(mat, quadBuffer, px1, py1, pz1, px1, py1+0.15f, pz1, px2, py2+0.15f, pz2, px2, py2, pz2, red, green, blue, alpha, 15728880);
                        }
                    }
                    if (shatter == 0) draw3DBox(mat, quadBuffer, headX, headY+0.1f, headZ, 0.2f, 1f, isTotemPop ? 0.8f : 0f, 0f, alpha, 15728880);
                }
                matrices.pop();

                matrices.push(); double bounce = Math.sin(System.currentTimeMillis() / 150.0) * 0.2; matrices.translate(x, y + target.getHeight() + 1.8 + bounce, z); 
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw())); matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch())); matrices.scale(-0.1F, -0.1F, 0.1F); 
                client.textRenderer.draw("▼", -client.textRenderer.getWidth("▼") / 2f, 0, 0xFFFF2222, true, matrices.peek().getPositionMatrix(), immediate, TextRenderer.TextLayerType.SEE_THROUGH, 0x00000000, 15728880);
                matrices.pop();
            }

            // ---------------------------------------------------------
            // --- FRIEND CIRCLE AURA (HALO) ---
            // ---------------------------------------------------------
            if (target.getName().getString().equals(friendTarget) && ModConfig.dragonAuraEnabled) {
                matrices.push();
                matrices.translate(x, y + target.getHeight() / 2, z); // Center of body
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw())); // Always face camera
                
                long time = System.currentTimeMillis();
                float t = (time % 3000) / 3000.0f; // 3 sec loop
                float radius = 1.0f + (float)Math.sin(time / 200.0) * 0.1f; // Pulsing radius
                
                float shatter = 0f, alpha = 0.8f;
                if (t > 0.7f) {
                    shatter = (t - 0.7f) / 0.3f;
                    alpha = 0.8f * (1.0f - shatter);
                    radius += shatter * 2.0f; // Expand into dust
                } else if (t < 0.3f) {
                    radius -= (0.3f - t) * 2.0f; // Reverse dust in
                }

                Matrix4f mat = matrices.peek().getPositionMatrix();
                VertexConsumer lineBuffer = immediate.getBuffer(RenderLayer.getLines());
                
                for (int i = 0; i < 360; i += 10) {
                    float r1 = (float) Math.toRadians(i);
                    float r2 = (float) Math.toRadians(i + 10);
                    
                    float px1 = (float) Math.cos(r1) * radius;
                    float py1 = (float) Math.sin(r1) * radius;
                    float px2 = (float) Math.cos(r2) * radius;
                    float py2 = (float) Math.sin(r2) * radius;

                    if (shatter > 0 || t < 0.3f) {
                        float sx = (float) Math.sin(i * 13) * 0.5f;
                        float sy = (float) Math.cos(i * 17) * 0.5f;
                        px1 += sx; py1 += sy; px2 += sx; py2 += sy;
                    }

                    // Green/Cyan Friend Halo
                    drawLine(mat, lineBuffer, px1, py1, -0.5f, px2, py2, -0.5f, 0f, 1f, 0.5f, alpha);
                }
                matrices.pop();
            }
        }
        immediate.draw();
    }

    // --- PRO HELPER RENDERING METHODS ---

    private static void drawLine(Matrix4f m, VertexConsumer v, float x1, float y1, float z1, float x2, float y2, float z2, float r, float g, float b, float a) {
        v.vertex(m, x1, y1, z1).color(r, g, b, a).normal(x2-x1, y2-y1, z2-z1);
        v.vertex(m, x2, y2, z2).color(r, g, b, a).normal(x2-x1, y2-y1, z2-z1);
    }
    
    private static void drawQuad(Matrix4f m, VertexConsumer v, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4, float r, float g, float b, float a, int l) {
        v.vertex(m, x1, y1, z1).color(r,g,b,a).light(l); v.vertex(m, x2, y2, z2).color(r,g,b,a).light(l);
        v.vertex(m, x3, y3, z3).color(r,g,b,a).light(l); v.vertex(m, x4, y4, z4).color(r,g,b,a).light(l);
    }
    
    private static void draw3DBox(Matrix4f m, VertexConsumer v, float x, float y, float z, float s, float r, float g, float b, float a, int l) {
        float h = s/2f;
        drawQuad(m, v, x-h, y-h, z-h, x+h, y-h, z-h, x+h, y+h, z-h, x-h, y+h, z-h, r, g, b, a, l); 
        drawQuad(m, v, x-h, y-h, z+h, x+h, y-h, z+h, x+h, y+h, z+h, x-h, y+h, z+h, r*0.8f, g*0.8f, b*0.8f, a, l); 
        drawQuad(m, v, x-h, y-h, z-h, x-h, y-h, z+h, x-h, y+h, z+h, x-h, y+h, z-h, r*0.9f, g*0.9f, b*0.9f, a, l); 
        drawQuad(m, v, x+h, y-h, z-h, x+h, y-h, z+h, x+h, y+h, z+h, x+h, y+h, z-h, r*0.9f, g*0.9f, b*0.9f, a, l); 
        drawQuad(m, v, x-h, y+h, z-h, x+h, y+h, z-h, x+h, y+h, z+h, x-h, y+h, z+h, r*1.2f, g, b, a, l); 
        drawQuad(m, v, x-h, y-h, z-h, x+h, y-h, z-h, x+h, y-h, z+h, x-h, y-h, z+h, r*0.5f, g*0.5f, b*0.5f, a, l); 
    }

    // Advanced 3D Box Builder for Player Skins
    private static void draw3DSkinBox(Matrix4f m, VertexConsumer v, float x, float y, float z, float w, float h, float d, float u, float vTex, float texW, float texH, int l) {
        float r = 1f, g = 1f, b = 1f, a = 1f;
        float pU = 1f/64f, pV = 1f/64f; // 1 pixel in UV
        
        // Front Face
        drawSoulQuad(m, v, x, y, z-d, x+w, y, z-d, x+w, y+h, z-d, x, y+h, z-d, (u+d)*pU, (vTex+d)*pU, (u+d+w)*pU, (vTex+d+h)*pU, r,g,b,a, l);
        // Back Face
        drawSoulQuad(m, v, x+w, y, z, x, y, z, x, y+h, z, x+w, y+h, z, (u+d+w+d)*pU, (vTex+d)*pU, (u+d+w+d+w)*pU, (vTex+d+h)*pU, r,g,b,a, l);
        // Left Face
        drawSoulQuad(m, v, x, y, z, x, y, z-d, x, y+h, z-d, x, y+h, z, u*pU, (vTex+d)*pU, (u+d)*pU, (vTex+d+h)*pU, r,g,b,a, l);
        // Right Face
        drawSoulQuad(m, v, x+w, y, z-d, x+w, y, z, x+w, y+h, z, x+w, y+h, z-d, (u+d+w)*pU, (vTex+d)*pU, (u+d+w+d)*pU, (vTex+d+h)*pU, r,g,b,a, l);
    }
    
    private static void drawSoulQuad(Matrix4f m, VertexConsumer v, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4, float u1, float v1, float u2, float v2, float r, float g, float b, float a, int l) {
        v.vertex(m, x1, y1, z1).color(r, g, b, a).texture(u1, v1).overlay(net.minecraft.client.render.OverlayTexture.DEFAULT_UV).light(l).normal(0, 0, 1);
        v.vertex(m, x2, y2, z2).color(r, g, b, a).texture(u1, v2).overlay(net.minecraft.client.render.OverlayTexture.DEFAULT_UV).light(l).normal(0, 0, 1);
        v.vertex(m, x3, y3, z3).color(r, g, b, a).texture(u2, v2).overlay(net.minecraft.client.render.OverlayTexture.DEFAULT_UV).light(l).normal(0, 0, 1);
        v.vertex(m, x4, y4, z4).color(r, g, b, a).texture(u2, v1).overlay(net.minecraft.client.render.OverlayTexture.DEFAULT_UV).light(l).normal(0, 0, 1);
    }
                                     }
