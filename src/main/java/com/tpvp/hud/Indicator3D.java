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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Indicator3D {
    
    // --- KILL TRACKING & SOULS DATA ---
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
        // 1. SOUL RAGDOLL RENDERER (Ascension Animation)
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
                float alpha = 1.0f - (float)Math.pow(life, 2); // Exponential fade out
                
                // Wavy Cloth (Ragdoll) Physics
                double waveX = Math.sin(life * 10) * 0.3;
                double waveZ = Math.cos(life * 12) * 0.3;
                double upY = life * 4.0; // Float upwards
                
                matrices.push();
                matrices.translate(soul.pos.x - camPos.x + waveX, soul.pos.y - camPos.y + upY, soul.pos.z - camPos.z + waveZ);
                
                // Always face the camera
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw())); 
                
                // Wiggle rotation (kapda hilega side-to-side)
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)Math.sin(life * 20) * 15f)); 
                matrices.scale(-0.5F, -0.5F, 0.5F); // Scale down slightly
                
                Matrix4f mat = matrices.peek().getPositionMatrix();
                VertexConsumer soulBuffer = immediate.getBuffer(RenderLayer.getEntityTranslucentCull(soul.skin));
                
                // Draw Full 2D Player Body for the Soul
                int l = LightmapTextureManager.MAX_LIGHT_COORDINATE;
                drawTextureQuad(mat, soulBuffer, -1, -2, 2, 2, 8f/64f, 8f/64f, 16f/64f, 16f/64f, 1f, 1f, 1f, alpha, l); // Head
                drawTextureQuad(mat, soulBuffer, -1, 0, 2, 3, 20f/64f, 20f/64f, 28f/64f, 32f/64f, 1f, 1f, 1f, alpha, l); // Body
                drawTextureQuad(mat, soulBuffer, -2, 0, 1, 3, 44f/64f, 20f/64f, 48f/64f, 32f/64f, 1f, 1f, 1f, alpha, l); // Left Arm
                drawTextureQuad(mat, soulBuffer, 1, 0, 1, 3, 44f/64f, 20f/64f, 48f/64f, 32f/64f, 1f, 1f, 1f, alpha, l); // Right Arm
                
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

        double weaponDmg = client.player.getAttributeValue(EntityAttributes.ATTACK_DAMAGE);
        if (weaponDmg <= 0) weaponDmg = 1.0;

        // ---------------------------------------------------------
        // 3. LIVING ENTITIES MAIN LOOP
        // ---------------------------------------------------------
        for (Entity entity : client.world.getEntities()) {
            // Filter out NPCs, Armor Stands, and the Client Player
            if (!(entity instanceof LivingEntity target) || target == client.player || target instanceof ArmorStandEntity) continue;
            
            float health = target.getHealth();
            int id = target.getId();

            // --- KILL DETECTION FOR BANNERS AND SOULS ---
            if (lastHealthMap.containsKey(id)) {
                float lastHealth = lastHealthMap.get(id);
                // Check if target died in this tick
                if (lastHealth > 0 && health <= 0) {
                    if (target.distanceTo(client.player) < 10.0) { // If close, assume we got the kill
                        KillBannerHud.addKill(); 
                    }
                    if (target instanceof AbstractClientPlayerEntity pt) {
                        // Add soul to the world
                        activeSouls.add(new DeadSoul(target.getPos(), pt.getSkinTextures().texture())); 
                    }
                }
            }
            lastHealthMap.put(id, health); // Update Health History

            // Skip rendering if dead, invisible or too far
            if (health <= 0 || target.isInvisible() || target.distanceTo(client.player) > 64.0) continue;

            Vec3d tPos = target.getLerpedPos(tickDelta);
            double x = tPos.x - camPos.x;
            double y = tPos.y - camPos.y;
            double z = tPos.z - camPos.z;

            // --- DRAGON AURA (Solid 3D Ribbon) & BOUNCING ARROW ---
            if (target.getName().getString().equals(activeTarget)) {
                
                // DRAGON AURA HELIX
                if (ModConfig.dragonAuraEnabled) {
                    matrices.push();
                    matrices.translate(x, y, z);
                    Matrix4f mat = matrices.peek().getPositionMatrix();
                    
                    // Transparent Solid Layer for Ribbon Effect
                    VertexConsumer quadBuffer = immediate.getBuffer(RenderLayer.getGui()); 
                    
                    long time = System.currentTimeMillis();
                    float t = (time % 2000) / 2000.0f; // 0 to 1 loop
                    float h = target.getHeight();
                    float radius = target.getWidth() + 0.3f;

                    float headX = 0, headY = 0, headZ = 0;

                    // Triple Layer Helix (Thick Ribbon)
                    for (int layer = 0; layer < 3; layer++) {
                        float offset = layer * 0.05f;
                        
                        for (int i = 0; i < 30; i++) {
                            float pt1 = (t + (i / 30.0f)) % 1.0f; 
                            float pt2 = (t + ((i + 1) / 30.0f)) % 1.0f;
                            
                            // Prevent stitching bug when returning to bottom
                            if (pt2 < pt1) continue; 

                            float py1 = pt1 * h;
                            float px1 = (float) Math.cos((pt1 + offset) * Math.PI * 4) * radius;
                            float pz1 = (float) Math.sin((pt1 + offset) * Math.PI * 4) * radius;

                            float py2 = pt2 * h;
                            float px2 = (float) Math.cos((pt2 + offset) * Math.PI * 4) * radius;
                            float pz2 = (float) Math.sin((pt2 + offset) * Math.PI * 4) * radius;

                            // Save top point for Dragon Head
                            if (py2 > headY) { 
                                headX = px2; headY = py2; headZ = pz2; 
                            }

                            // Draw Flat 3D Plane (Red to Gold Glow)
                            float r = 1f, g = 0.1f, b = 0.1f, alpha = 0.8f;
                            drawQuad(mat, quadBuffer, px1, py1, pz1, px1, py1 + 0.15f, pz1, px2, py2 + 0.15f, pz2, px2, py2, pz2, r, g, b, alpha, 15728880);
                        }
                    }

                    // DRAGON HEAD (Ruby Core Block)
                    draw3DBox(mat, quadBuffer, headX, headY + 0.1f, headZ, 0.2f, 1f, 0f, 0f, 1f, 15728880);

                    matrices.pop();
                }

                // BOUNCING TARGET ARROW
                matrices.push();
                double bounce = Math.sin(System.currentTimeMillis() / 150.0) * 0.2;
                matrices.translate(x, y + target.getHeight() + 1.8 + bounce, z); 
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
                matrices.scale(-0.1F, -0.1F, 0.1F); 
                
                Matrix4f matArrow = matrices.peek().getPositionMatrix();
                client.textRenderer.draw("▼", -client.textRenderer.getWidth("▼") / 2f, 0, 0xFFFF2222, true, matArrow, immediate, TextRenderer.TextLayerType.SEE_THROUGH, 0x00000000, LightmapTextureManager.MAX_LIGHT_COORDINATE);
                matrices.pop();
            }

            // --- PRO HITBOX RENDERING ---
            if (ModConfig.hitboxEnabled) {
                matrices.push();
                matrices.translate(x, y, z);
                VertexConsumer lineBuffer = immediate.getBuffer(RenderLayer.getLines());
                Matrix4f matrix = matrices.peek().getPositionMatrix();
                float tw = target.getWidth() / 2.0f;
                float th = target.getHeight();
                
                drawLine(matrix, lineBuffer, -tw, 0, -tw, tw, 0, -tw); 
                drawLine(matrix, lineBuffer, tw, 0, -tw, tw, 0, tw); 
                drawLine(matrix, lineBuffer, tw, 0, tw, -tw, 0, tw); 
                drawLine(matrix, lineBuffer, -tw, 0, tw, -tw, 0, -tw);
                
                drawLine(matrix, lineBuffer, -tw, th, -tw, tw, th, -tw); 
                drawLine(matrix, lineBuffer, tw, th, -tw, tw, th, tw); 
                drawLine(matrix, lineBuffer, tw, th, tw, -tw, th, tw); 
                drawLine(matrix, lineBuffer, -tw, th, tw, -tw, th, -tw);
                
                drawLine(matrix, lineBuffer, -tw, 0, -tw, -tw, th, -tw); 
                drawLine(matrix, lineBuffer, tw, 0, -tw, tw, th, -tw); 
                drawLine(matrix, lineBuffer, tw, 0, tw, tw, th, tw); 
                drawLine(matrix, lineBuffer, -tw, 0, tw, -tw, th, tw);
                
                matrices.pop();
            }

            // --- 3-STYLE FLOATING INDICATORS ---
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
                    // HEARTS STYLE
                    int tH = (int) Math.ceil(Math.max(target.getMaxHealth(), health) / 2.0f);
                    Sprite fH = client.getGuiAtlasManager().getSprite(Identifier.ofVanilla("hud/heart/full"));
                    Sprite hH = client.getGuiAtlasManager().getSprite(Identifier.ofVanilla("hud/heart/half"));
                    Sprite eH = client.getGuiAtlasManager().getSprite(Identifier.ofVanilla("hud/heart/container"));
                    
                    VertexConsumer hc = immediate.getBuffer(RenderLayer.getTextSeeThrough(fH.getAtlasId()));
                    float stX = -(tH * 9f) / 2f;
                    
                    for (int i = 0; i < tH; i++) {
                        drawTextureQuad(pM, hc, stX + (i * 9), 0, 9, 9, eH.getMinU(), eH.getMinV(), eH.getMaxU(), eH.getMaxV(), 1f, 1f, 1f, 1f, l);
                        if (health >= (i * 2) + 2) drawTextureQuad(pM, hc, stX + (i * 9), 0, 9, 9, fH.getMinU(), fH.getMinV(), fH.getMaxU(), fH.getMaxV(), 1f, 1f, 1f, 1f, l);
                        else if (health > (i * 2)) drawTextureQuad(pM, hc, stX + (i * 9), 0, 9, 9, hH.getMinU(), hH.getMinV(), hH.getMaxU(), hH.getMaxV(), 1f, 1f, 1f, 1f, l);
                    }
                } 
                else if (ModConfig.indicatorStyle == 1) { 
                    // BAR STYLE
                    float cW = 50f * hpPercent;
                    int bC = (hpPercent < 0.3f) ? 0xFFFF3333 : (hpPercent < 0.6f) ? 0xFFFFAA00 : 0xFF00FF00;
                    
                    VertexConsumer bc = immediate.getBuffer(RenderLayer.getTextBackgroundSeeThrough());
                    drawColorQuad(pM, bc, -26, 0, 52, 7, 0xFF000000, l); 
                    drawColorQuad(pM, bc, -25, 1, 50, 5, 0xFF333333, l); 
                    if (cW > 0) drawColorQuad(pM, bc, -25, 1, cW, 5, bC, l); 
                    
                    String pt = (int)(hpPercent * 100) + "%";
                    client.textRenderer.draw(pt, -client.textRenderer.getWidth(pt) / 2f, -9, 0xFFFFFF, false, pM, immediate, TextRenderer.TextLayerType.SEE_THROUGH, 0, l);
                } 
                else if (ModConfig.indicatorStyle == 2) { 
                    // HEAD + HITS TO KILL
                    int hitsToKill = (int) Math.ceil(health / weaponDmg);
                    String text = (target instanceof PlayerEntity) ? target.getName().getString() + " | Hits: " + hitsToKill : "Hits: " + hitsToKill;
                    int txtColor = (hpPercent < 0.3f) ? 0xFF0000 : (hpPercent < 0.6f) ? 0xFFFF00 : 0x00FF00;
                    float stX = -client.textRenderer.getWidth(text) / 2f;
                    
                    if (target instanceof AbstractClientPlayerEntity pt) {
                        VertexConsumer hc = immediate.getBuffer(RenderLayer.getTextSeeThrough(pt.getSkinTextures().texture()));
                        drawTextureQuad(pM, hc, stX - 12, -1, 10, 10, 8f/64f, 8f/64f, 16f/64f, 16f/64f, 1f, 1f, 1f, 1f, l);
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
        v.vertex(m, x1, y1, z1).color(1f, 0f, 0.2f, 1f).normal(x2-x1, y2-y1, z2-z1);
        v.vertex(m, x2, y2, z2).color(1f, 0f, 0.2f, 1f).normal(x2-x1, y2-y1, z2-z1);
    }
    
    private static void drawQuad(Matrix4f m, VertexConsumer v, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4, float r, float g, float b, float a, int l) {
        v.vertex(m, x1, y1, z1).color(r,g,b,a).light(l); 
        v.vertex(m, x2, y2, z2).color(r,g,b,a).light(l);
        v.vertex(m, x3, y3, z3).color(r,g,b,a).light(l); 
        v.vertex(m, x4, y4, z4).color(r,g,b,a).light(l);
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
    
    private static void drawTextureQuad(Matrix4f m, VertexConsumer v, float x, float y, float w, float h, float u1, float v1, float u2, float v2, float r, float g, float b, float a, int l) {
        v.vertex(m, x, y, 0).color(r,g,b,a).texture(u1, v1).light(l); 
        v.vertex(m, x, y+h, 0).color(r,g,b,a).texture(u1, v2).light(l);
        v.vertex(m, x+w, y+h, 0).color(r,g,b,a).texture(u2, v2).light(l); 
        v.vertex(m, x+w, y, 0).color(r,g,b,a).texture(u2, v1).light(l);
    }
    
    private static void drawColorQuad(Matrix4f m, VertexConsumer v, float x, float y, float w, float h, int c, int l) {
        float a=(c>>24&255)/255.0F, r=(c>>16&255)/255.0F, g=(c>>8&255)/255.0F, b=(c&255)/255.0F;
        drawQuad(m, v, x, y, 0, x, y+h, 0, x+w, y+h, 0, x+w, y, 0, r, g, b, a, l);
    }
                         }
