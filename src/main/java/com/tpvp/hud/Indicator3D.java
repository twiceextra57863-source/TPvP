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

    // ---------------------------------------------------------
    // --- TRACKERS AND DATA STRUCTURES ---
    // ---------------------------------------------------------
    private static final Set<Integer> deadPlayers = new HashSet<>();
    private static final Map<Integer, Boolean> lastTotemMap = new HashMap<>();
    private static final Map<Integer, Long> totemPopMap = new HashMap<>();
    private static final Map<Integer, Float> lastHealthMap = new HashMap<>();
    private static final List<DeadSoul> activeSouls = new ArrayList<>();

    // Dead Soul Object (Used for Ascension Animation)
    private static class DeadSoul {
        Vec3d pos;
        Identifier skin;
        long startTime;

        DeadSoul(Vec3d pos, Identifier skin) {
            this.pos = pos;
            this.skin = skin;
            this.startTime = System.currentTimeMillis();
        }
    }

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

        // ---------------------------------------------------------
        // 1. SOUL RAGDOLL RENDERER (ASCENSION ANIMATION)
        // ---------------------------------------------------------
        if (ModConfig.soulAnimationEnabled) {
            long now = System.currentTimeMillis();
            Iterator<DeadSoul> iter = activeSouls.iterator();

            while (iter.hasNext()) {
                DeadSoul soul = iter.next();
                long age = now - soul.startTime;

                // Remove soul after 4 seconds
                if (age > 4000) {
                    iter.remove();
                    continue;
                }

                float life = age / 4000.0f; // 0.0 to 1.0 progression
                float alpha = 1.0f - (float) Math.pow(life, 2); // Exponential fade out

                // Wavy Cloth (Ragdoll) Physics
                double waveX = Math.sin(life * 10) * 0.3;
                double waveZ = Math.cos(life * 12) * 0.3;
                double upY = life * 4.0; // Float upwards

                matrices.push();
                matrices.translate(soul.pos.x - camPos.x + waveX, soul.pos.y - camPos.y + upY, soul.pos.z - camPos.z + waveZ);

                // Always face the camera
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));

                // Wiggle rotation (kapda hilega side-to-side)
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float) Math.sin(life * 20) * 15f));

                // Fix scale so faces aren't culled, then rotate 180 to face camera correctly
                matrices.scale(0.8F, 0.8F, 0.8F);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180f));

                Matrix4f mat = matrices.peek().getPositionMatrix();
                
                // CRASH FIX: Used getEntityTranslucent for 1.21.2+ Mappings
                VertexConsumer soulBuffer = immediate.getBuffer(RenderLayer.getEntityTranslucent(soul.skin));
                int l = LightmapTextureManager.MAX_LIGHT_COORDINATE;

                // Draw Full 2D Player Body for the Soul
                drawSoulQuad(mat, soulBuffer, -1, -2, 2, 2, 8f / 64f, 8f / 64f, 16f / 64f, 16f / 64f, 1f, 1f, 1f, alpha, l); // Head
                drawSoulQuad(mat, soulBuffer, -1, 0, 2, 3, 20f / 64f, 20f / 64f, 28f / 64f, 32f / 64f, 1f, 1f, 1f, alpha, l); // Body
                drawSoulQuad(mat, soulBuffer, -2, 0, 1, 3, 44f / 64f, 20f / 64f, 48f / 64f, 32f / 64f, 1f, 1f, 1f, alpha, l); // Left Arm
                drawSoulQuad(mat, soulBuffer, 1, 0, 1, 3, 44f / 64f, 20f / 64f, 48f / 64f, 32f / 64f, 1f, 1f, 1f, alpha, l); // Right Arm

                matrices.pop();
            }
        }

        // ---------------------------------------------------------
        // 2. AUTO TRACK LOGIC (Lowest HP Finder)
        // ---------------------------------------------------------
        String activeTarget = ModConfig.taggedPlayerName;
        if (ModConfig.autoTrack) {
            double lowestHp = 9999;
            for (Entity e : client.world.getEntities()) {
                if (e instanceof LivingEntity le && e != client.player && !e.isInvisible() && !(e instanceof ArmorStandEntity)) {
                    if (client.player.distanceTo(le) < 32.0 && le.getHealth() < lowestHp && le.getHealth() > 0) {
                        lowestHp = le.getHealth();
                        activeTarget = le.getName().getString();
                    }
                }
            }
        }

        double weaponDmg = Math.max(1.0, client.player.getAttributeValue(EntityAttributes.ATTACK_DAMAGE));

        // ---------------------------------------------------------
        // 3. LIVING ENTITIES MAIN LOOP
        // ---------------------------------------------------------
        for (Entity entity : client.world.getEntities()) {

            // Filter out NPCs, Armor Stands, and the Client Player
            if (!(entity instanceof LivingEntity target) || target == client.player || target instanceof ArmorStandEntity) {
                continue;
            }

            float health = target.getHealth();
            int id = target.getId();

            // --- EPIC TOTEM POP DETECTION ---
            boolean hasTotem = target.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING) || target.getMainHandStack().isOf(Items.TOTEM_OF_UNDYING);
            boolean hadTotem = lastTotemMap.getOrDefault(id, false);
            
            if (hadTotem && !hasTotem && health > 0 && health < 10) {
                totemPopMap.put(id, System.currentTimeMillis()); // TOTEM POPPED!
            }
            lastTotemMap.put(id, hasTotem);

            // --- 100% RELIABLE KILL DETECTION ---
            if (health <= 0 || target.isDead()) {
                if (!deadPlayers.contains(id)) {
                    deadPlayers.add(id);
                    if (target.distanceTo(client.player) < 32.0) {
                        KillBannerHud.addKill();
                    }
                    if (target instanceof AbstractClientPlayerEntity pt) {
                        Identifier skin = pt.getSkinTextures() != null ? pt.getSkinTextures().texture() : Identifier.ofVanilla("textures/entity/steve.png");
                        activeSouls.add(new DeadSoul(target.getPos(), skin));
                    }
                }
                continue; // Do not render HUD on dead bodies
            } else {
                deadPlayers.remove(id); // Reset if respawned
            }

            // Skip rendering if invisible or too far
            if (target.isInvisible() || target.distanceTo(client.player) > 64.0) {
                continue;
            }

            Vec3d tPos = target.getLerpedPos(tickDelta);
            double x = tPos.x - camPos.x;
            double y = tPos.y - camPos.y;
            double z = tPos.z - camPos.z;

            // ---------------------------------------------------------
            // --- 4. DRAGON AURA (NORMAL & TOTEM POP ANIMATIONS) ---
            // ---------------------------------------------------------
            if (target.getName().getString().equals(activeTarget)) {

                long popTime = totemPopMap.getOrDefault(id, 0L);
                long now = System.currentTimeMillis();
                boolean isTotemPop = (now - popTime) < 3000; // 3 second epic animation

                if (ModConfig.dragonAuraEnabled) {
                    matrices.push();
                    matrices.translate(x, y, z);
                    Matrix4f mat = matrices.peek().getPositionMatrix();
                    VertexConsumer quadBuffer = immediate.getBuffer(RenderLayer.getGui());

                    float h = target.getHeight();
                    float radius = target.getWidth() + 0.3f;
                    float headX = 0, headY = 0, headZ = 0;

                    // ANIMATION MODIFIERS
                    float yOff = 0f, rotMult = 1f, shatter = 0f, alpha = 0.8f;

                    if (isTotemPop) {
                        float popT = (now - popTime) / 3000.0f;
                        if (popT < 0.2f) { 
                            yOff = (popT / 0.2f) * 6.0f; // Phase 1: SHOOT UP
                        } else if (popT < 0.4f) { 
                            yOff = 6.0f - (((popT - 0.2f) / 0.2f) * 6.0f); // Phase 2: DIVE DOWN
                        } else if (popT < 0.7f) { 
                            rotMult = 10.0f; // Phase 3: SUPER TWIRL
                        } else { 
                            shatter = (popT - 0.7f) / 0.3f; // Phase 4: SHATTER
                            alpha = 0.8f * (1.0f - shatter);
                        }
                    } else {
                        // Normal Cinematic Loop
                        float tNorm = (now % 4000) / 4000.0f;
                        if (tNorm > 0.6f) {
                            shatter = (tNorm - 0.6f) / 0.1f;
                            if (shatter > 1.0f) {
                                shatter = 1.0f;
                                alpha = 0f;
                            } else {
                                alpha = 0.8f * (1.0f - shatter);
                            }
                        }
                    }

                    if (alpha > 0.05f) {
                        float tRot = ((now % 2000) / 2000.0f) * rotMult;
                        int segments = 30;
                        boolean drawHead = false;

                        for (int layer = 0; layer < 3; layer++) {
                            float offset = layer * 0.05f;
                            
                            for (int i = 0; i < segments; i++) {
                                float pt1 = (i / (float) segments);
                                float pt2 = ((i + 1) / (float) segments);
                                
                                float py1 = pt1 * h + yOff;
                                float px1 = (float) Math.cos((pt1 + offset + tRot) * Math.PI * 4) * radius;
                                float pz1 = (float) Math.sin((pt1 + offset + tRot) * Math.PI * 4) * radius;

                                float py2 = pt2 * h + yOff;
                                float px2 = (float) Math.cos((pt2 + offset + tRot) * Math.PI * 4) * radius;
                                float pz2 = (float) Math.sin((pt2 + offset + tRot) * Math.PI * 4) * radius;

                                // DUST / SHATTER EFFECT
                                if (shatter > 0) {
                                    float sx = (float) Math.sin(i * 13 + layer) * shatter * 3.0f;
                                    float sy = (float) Math.cos(i * 17 + layer) * shatter * 3.0f;
                                    float sz = (float) Math.sin(i * 23 + layer) * shatter * 3.0f;
                                    px1 += sx; py1 += sy; pz1 += sz;
                                    px2 += sx; py2 += sy; pz2 += sz;
                                }

                                // Mark head position
                                if (layer == 1 && i == segments - 1) {
                                    headX = px2;
                                    headY = py2;
                                    headZ = pz2;
                                    drawHead = true;
                                }

                                float red = 1f, green = 0.1f, blue = 0.1f;
                                
                                // Golden Tip
                                if (i > segments - 5) {
                                    green = 0.8f;
                                    blue = 0f;
                                } 
                                // Totally Golden during Totem pop
                                if (isTotemPop) {
                                    green = 0.8f;
                                    blue = 0.2f;
                                }

                                drawQuad(mat, quadBuffer, px1, py1, pz1, px1, py1 + 0.15f, pz1, px2, py2 + 0.15f, pz2, px2, py2, pz2, red, green, blue, alpha, 15728880);
                            }
                        }
                        
                        // Draw Dragon Head (Ruby / Gold Box)
                        if (drawHead && shatter == 0) {
                            float headG = isTotemPop ? 0.8f : 0f;
                            draw3DBox(mat, quadBuffer, headX, headY + 0.1f, headZ, 0.2f, 1f, headG, 0f, alpha, 15728880);
                        }
                    }
                    matrices.pop();
                }

                // BOUNCING TARGET ARROW
                matrices.push();
                double bounce = Math.sin(System.currentTimeMillis() / 150.0) * 0.2;
                matrices.translate(x, y + target.getHeight() + 1.8 + bounce, z);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
                matrices.scale(-0.1F, -0.1F, 0.1F);
                
                client.textRenderer.draw("▼", -client.textRenderer.getWidth("▼") / 2f, 0, 0xFFFF2222, true, matrices.peek().getPositionMatrix(), immediate, TextRenderer.TextLayerType.SEE_THROUGH, 0x00000000, 15728880);
                matrices.pop();
            }

            // ---------------------------------------------------------
            // --- 5. HITBOXES RENDERING ---
            // ---------------------------------------------------------
            if (ModConfig.hitboxEnabled) {
                matrices.push();
                matrices.translate(x, y, z);
                VertexConsumer lb = immediate.getBuffer(RenderLayer.getLines());
                Matrix4f m = matrices.peek().getPositionMatrix();
                
                float tw = target.getWidth() / 2.0f;
                float th = target.getHeight();
                
                // Bottom Box
                drawLine(m, lb, -tw, 0, -tw, tw, 0, -tw);
                drawLine(m, lb, tw, 0, -tw, tw, 0, tw);
                drawLine(m, lb, tw, 0, tw, -tw, 0, tw);
                drawLine(m, lb, -tw, 0, tw, -tw, 0, -tw);
                
                // Top Box
                drawLine(m, lb, -tw, th, -tw, tw, th, -tw);
                drawLine(m, lb, tw, th, -tw, tw, th, tw);
                drawLine(m, lb, tw, th, tw, -tw, th, tw);
                drawLine(m, lb, -tw, th, tw, -tw, th, -tw);
                
                // Vertical Pillars
                drawLine(m, lb, -tw, 0, -tw, -tw, th, -tw);
                drawLine(m, lb, tw, 0, -tw, tw, th, -tw);
                drawLine(m, lb, tw, 0, tw, tw, th, tw);
                drawLine(m, lb, -tw, 0, tw, -tw, th, tw);
                
                matrices.pop();
            }

            // ---------------------------------------------------------
            // --- 6. 3-STYLE INDICATORS ---
            // ---------------------------------------------------------
            if (ModConfig.indicatorEnabled && target.distanceTo(client.player) < 32.0) {
                matrices.push();
                matrices.translate(x, y + target.getHeight() + 1.2, z);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
                matrices.scale(-0.025F, -0.025F, 0.025F);
                
                Matrix4f pM = matrices.peek().getPositionMatrix();
                int l = LightmapTextureManager.MAX_LIGHT_COORDINATE;
                float hpPercent = Math.max(0, Math.min(1, health / target.getMaxHealth()));

                if (ModConfig.indicatorStyle == 0) {
                    // STYLE 0: HEARTS
                    int tH = (int) Math.ceil(Math.max(target.getMaxHealth(), health) / 2.0f);
                    Sprite fH = client.getGuiAtlasManager().getSprite(Identifier.ofVanilla("hud/heart/full"));
                    Sprite hH = client.getGuiAtlasManager().getSprite(Identifier.ofVanilla("hud/heart/half"));
                    Sprite eH = client.getGuiAtlasManager().getSprite(Identifier.ofVanilla("hud/heart/container"));
                    
                    VertexConsumer hc = immediate.getBuffer(RenderLayer.getTextSeeThrough(fH.getAtlasId()));
                    float stX = -(tH * 9f) / 2f;
                    
                    for (int i = 0; i < tH; i++) {
                        drawTextureQuad(pM, hc, stX + (i * 9), 0, 9, 9, eH.getMinU(), eH.getMinV(), eH.getMaxU(), eH.getMaxV(), 1f, 1f, 1f, 1f, l);
                        if (health >= (i * 2) + 2) {
                            drawTextureQuad(pM, hc, stX + (i * 9), 0, 9, 9, fH.getMinU(), fH.getMinV(), fH.getMaxU(), fH.getMaxV(), 1f, 1f, 1f, 1f, l);
                        } else if (health > (i * 2)) {
                            drawTextureQuad(pM, hc, stX + (i * 9), 0, 9, 9, hH.getMinU(), hH.getMinV(), hH.getMaxU(), hH.getMaxV(), 1f, 1f, 1f, 1f, l);
                        }
                    }
                } 
                else if (ModConfig.indicatorStyle == 1) {
                    // STYLE 1: HEALTH BAR
                    float cW = 50f * hpPercent;
                    int bC = (hpPercent < 0.3f) ? 0xFFFF3333 : (hpPercent < 0.6f) ? 0xFFFFAA00 : 0xFF00FF00;
                    
                    VertexConsumer bc = immediate.getBuffer(RenderLayer.getTextBackgroundSeeThrough());
                    drawColorQuad(pM, bc, -26, 0, 52, 7, 0xFF000000, l);
                    drawColorQuad(pM, bc, -25, 1, 50, 5, 0xFF333333, l);
                    if (cW > 0) {
                        drawColorQuad(pM, bc, -25, 1, cW, 5, bC, l);
                    }
                    
                    String pt = (int) (hpPercent * 100) + "%";
                    client.textRenderer.draw(pt, -client.textRenderer.getWidth(pt) / 2f, -9, 0xFFFFFF, false, pM, immediate, TextRenderer.TextLayerType.SEE_THROUGH, 0, l);
                } 
                else if (ModConfig.indicatorStyle == 2) {
                    // STYLE 2: HEAD + HITS
                    int hitsToKill = (int) Math.ceil(health / weaponDmg);
                    String text = (target instanceof PlayerEntity) ? target.getName().getString() + " | Hits: " + hitsToKill : "Hits: " + hitsToKill;
                    int txtColor = (hpPercent < 0.3f) ? 0xFF0000 : (hpPercent < 0.6f) ? 0xFFFF00 : 0x00FF00;
                    float stX = -client.textRenderer.getWidth(text) / 2f;
                    
                    if (target instanceof AbstractClientPlayerEntity pt) {
                        VertexConsumer hc = immediate.getBuffer(RenderLayer.getTextSeeThrough(pt.getSkinTextures().texture()));
                        drawTextureQuad(pM, hc, stX - 12, -1, 10, 10, 8f / 64f, 8f / 64f, 16f / 64f, 16f / 64f, 1f, 1f, 1f, 1f, l);
                    }
                    
                    client.textRenderer.draw(text, stX, 0, txtColor, false, pM, immediate, TextRenderer.TextLayerType.SEE_THROUGH, 0x40000000, l);
                }
                matrices.pop();
            }
        }
        immediate.draw();
    }

    // ---------------------------------------------------------
    // --- HELPER RENDERING METHODS ---
    // ---------------------------------------------------------

    private static void drawLine(Matrix4f m, VertexConsumer v, float x1, float y1, float z1, float x2, float y2, float z2) {
        v.vertex(m, x1, y1, z1).color(1f, 0f, 0.2f, 1f).normal(x2 - x1, y2 - y1, z2 - z1);
        v.vertex(m, x2, y2, z2).color(1f, 0f, 0.2f, 1f).normal(x2 - x1, y2 - y1, z2 - z1);
    }

    private static void drawQuad(Matrix4f m, VertexConsumer v, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4, float r, float g, float b, float a, int l) {
        v.vertex(m, x1, y1, z1).color(r, g, b, a).light(l);
        v.vertex(m, x2, y2, z2).color(r, g, b, a).light(l);
        v.vertex(m, x3, y3, z3).color(r, g, b, a).light(l);
        v.vertex(m, x4, y4, z4).color(r, g, b, a).light(l);
    }

    private static void draw3DBox(Matrix4f m, VertexConsumer v, float x, float y, float z, float s, float r, float g, float b, float a, int l) {
        float h = s / 2f;
        drawQuad(m, v, x - h, y - h, z - h, x + h, y - h, z - h, x + h, y + h, z -h, x - h, y + h, z - h, r, g, b, a, l);
        drawQuad(m, v, x - h, y - h, z + h, x + h, y - h, z + h, x + h, y + h, z + h, x - h, y + h, z + h, r * 0.8f, g * 0.8f, b * 0.8f, a, l);
        drawQuad(m, v, x - h, y - h, z - h, x - h, y - h, z + h, x - h, y + h, z + h, x - h, y + h, z - h, r * 0.9f, g * 0.9f, b * 0.9f, a, l);
        drawQuad(m, v, x + h, y - h, z - h, x + h, y - h, z + h, x + h, y + h, z + h, x + h, y + h, z - h, r * 0.9f, g * 0.9f, b * 0.9f, a, l);
        drawQuad(m, v, x - h, y + h, z - h, x + h, y + h, z - h, x + h, y + h, z + h, x - h, y + h, z + h, r * 1.2f, g, b, a, l);
        drawQuad(m, v, x - h, y - h, z - h, x + h, y - h, z - h, x + h, y - h, z + h, x - h, y - h, z + h, r * 0.5f, g * 0.5f, b * 0.5f, a, l);
    }

    private static void drawTextureQuad(Matrix4f m, VertexConsumer v, float x, float y, float w, float h, float u1, float v1, float u2, float v2, float r, float g, float b, float a, int l) {
        v.vertex(m, x, y, 0).color(r, g, b, a).texture(u1, v1).light(l);
        v.vertex(m, x, y + h, 0).color(r, g, b, a).texture(u1, v2).light(l);
        v.vertex(m, x + w, y + h, 0).color(r, g, b, a).texture(u2, v2).light(l);
        v.vertex(m, x + w, y, 0).color(r, g, b, a).texture(u2, v1).light(l);
    }

    private static void drawColorQuad(Matrix4f m, VertexConsumer v, float x, float y, float w, float h, int c, int l) {
        float a = (c >> 24 & 255) / 255.0F;
        float r = (c >> 16 & 255) / 255.0F;
        float g = (c >> 8 & 255) / 255.0F;
        float b = (c & 255) / 255.0F;
        drawQuad(m, v, x, y, 0, x, y + h, 0, x + w, y + h, 0, x + w, y, 0, r, g, b, a, l);
    }

    // --- CRASH PROOF SOUL RENDERER ---
    private static void drawSoulQuad(Matrix4f m, VertexConsumer v, float x, float y, float w, float h, float u1, float v1, float u2, float v2, float r, float g, float b, float a, int l) {
        v.vertex(m, x, y, 0).color(r, g, b, a).texture(u1, v1).overlay(net.minecraft.client.render.OverlayTexture.DEFAULT_UV).light(l).normal(0, 0, 1);
        v.vertex(m, x, y + h, 0).color(r, g, b, a).texture(u1, v2).overlay(net.minecraft.client.render.OverlayTexture.DEFAULT_UV).light(l).normal(0, 0, 1);
        v.vertex(m, x + w, y + h, 0).color(r, g, b, a).texture(u2, v2).overlay(net.minecraft.client.render.OverlayTexture.DEFAULT_UV).light(l).normal(0, 0, 1);
        v.vertex(m, x + w, y, 0).color(r, g, b, a).texture(u2, v1).overlay(net.minecraft.client.render.OverlayTexture.DEFAULT_UV).light(l).normal(0, 0, 1);
    }
}
