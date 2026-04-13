package com.tpvp.hud;

import com.tpvp.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Map;

public class TargetAuraRenderer {
    public static final Map<Integer, Long> totemPopMap = new HashMap<>();

    public static void render(LivingEntity target, MatrixStack matrices, VertexConsumerProvider.Immediate immediate, Camera camera, double x, double y, double z, String activeTarget) {
        MinecraftClient client = MinecraftClient.getInstance();
        String tName = target.getName().getString();

        // ---------------------------------------------------------
        // 1. DRAGON AURA (ENEMIES)
        // ---------------------------------------------------------
        if (tName.equals(activeTarget) && ModConfig.dragonAuraEnabled) {
            // ... (Same exact working Dragon code as previous)
            long popTime = totemPopMap.getOrDefault(target.getId(), 0L);
            long now = System.currentTimeMillis();
            boolean isTotemPop = (now - popTime) < 3000;

            matrices.push(); matrices.translate(x, y, z);
            Matrix4f mat = matrices.peek().getPositionMatrix();
            VertexConsumer quadBuffer = immediate.getBuffer(RenderLayer.getGui()); 
            
            float h = target.getHeight(), radius = target.getWidth() + 0.4f;
            float headX = 0, headY = 0, headZ = 0;
            
            float dr = 1f, dg = 0.1f, db = 0.1f; 
            if (ModConfig.dragonColor == 1) { dr = 0.6f; dg = 0f; db = 1f; } 
            else if (ModConfig.dragonColor == 2) { dr = 0f; dg = 0.8f; db = 1f; } 

            long cycle = 6000; 
            float t = (System.currentTimeMillis() % cycle) / (float) cycle;
            
            float progress = 0f, shatter = 0f, alpha = 0.8f, yOff = 0f; 
            float lookYaw = target.getYaw(); 
            
            if (isTotemPop) {
                float popT = (now - popTime) / 3000.0f;
                if (popT < 0.2f) { progress = 1.0f; yOff = (popT / 0.2f) * 6.0f; } 
                else if (popT < 0.5f) { progress = 1.0f; yOff = 6.0f - (((popT - 0.2f) / 0.3f) * 6.0f); } 
                else { shatter = (popT - 0.5f) / 0.5f; alpha = 0.8f * (1.0f - shatter); } 
            } else {
                if (t < 0.3f) { progress = t / 0.3f; } 
                else if (t < 0.6f) { 
                    progress = 1.0f;
                    headX = (float) Math.cos(Math.toRadians(lookYaw + 90)) * radius;
                    headZ = (float) Math.sin(Math.toRadians(lookYaw + 90)) * radius;
                    headY = h + 0.2f;
                } 
                else if (t < 0.8f) { progress = 1.0f; shatter = (t - 0.6f) / 0.2f; alpha = 0.8f * (1.0f - shatter); } 
                else { alpha = 0f; } 
            }

            if (alpha > 0.05f) {
                float rotSpeed = (now % 2000) / 2000.0f;
                int segments = 30;
                int activeSegs = (int)(segments * progress);
                boolean drawHead = (t >= 0.3f && t < 0.6f && !isTotemPop); 

                for (int layer = 0; layer < 3; layer++) {
                    float offset = layer * 0.05f;
                    for (int i = 0; i < activeSegs; i++) {
                        float pt1 = (i / (float) segments), pt2 = ((i + 1) / (float) segments);
                        float py1 = pt1 * h + yOff, px1 = (float) Math.cos((pt1 + rotSpeed + offset) * Math.PI * 4) * radius, pz1 = (float) Math.sin((pt1 + rotSpeed + offset) * Math.PI * 4) * radius;
                        float py2 = pt2 * h + yOff, px2 = (float) Math.cos((pt2 + rotSpeed + offset) * Math.PI * 4) * radius, pz2 = (float) Math.sin((pt2 + rotSpeed + offset) * Math.PI * 4) * radius;

                        if (shatter > 0) {
                            float sx = (float) Math.sin(i * 13) * shatter * 3.0f;
                            float sy = shatter * 5.0f; 
                            float sz = (float) Math.sin(i * 23) * shatter * 3.0f;
                            px1 += sx; py1 += sy; pz1 += sz;
                            px2 += sx; py2 += sy; pz2 += sz;
                        }

                        if (!drawHead && layer == 1 && i == activeSegs - 1) { headX = px2; headY = py2; headZ = pz2; drawHead = true; }

                        float cr = dr, cg = dg, cb = db;
                        if (layer == 0) { cr *= 0.5f; cg *= 0.5f; cb *= 0.5f; } 
                        if (i > segments - 5) { cg = 0.8f; cb = 0f; } 
                        if (isTotemPop) { cg = 0.8f; cb = 0.2f; } 

                        RenderUtils3D.drawQuad(mat, quadBuffer, px1, py1, pz1, px1, py1+0.15f, pz1, px2, py2+0.15f, pz2, px2, py2, pz2, cr, cg, cb, alpha, 15728880);
                    }
                }
                if (drawHead && shatter == 0) {
                    RenderUtils3D.draw3DBox(mat, quadBuffer, headX, headY+0.1f, headZ, 0.2f, 1f, isTotemPop ? 0.8f : 0f, 0f, alpha, 15728880);
                }
            }
            matrices.pop();

            matrices.push(); double bounce = Math.sin(System.currentTimeMillis() / 150.0) * 0.2; matrices.translate(x, y + target.getHeight() + 1.8 + bounce, z); 
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw())); matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch())); matrices.scale(-0.1F, -0.1F, 0.1F); 
            client.textRenderer.draw("▼", -client.textRenderer.getWidth("▼") / 2f, 0, 0xFFFF2222, true, matrices.peek().getPositionMatrix(), immediate, TextRenderer.TextLayerType.SEE_THROUGH, 0x00000000, 15728880);
            matrices.pop();
        }

        // ---------------------------------------------------------
        // 2. DIVINE FRIEND AURA (Golden Halo)
        // ---------------------------------------------------------
        if (tName.equals(ModConfig.taggedFriendName) && ModConfig.dragonAuraEnabled) {
            matrices.push();
            matrices.translate(x, y + 0.1, z); // Just above feet
            
            long now = System.currentTimeMillis();
            float rot = (now % 3000) / 3000.0f * 360f; // Spin continuously
            float hover = (float) Math.sin(now / 500.0) * 0.2f + 0.5f; // Float up and down
            
            matrices.translate(0, hover, 0); // Apply hover
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rot)); 
            
            Matrix4f mat = matrices.peek().getPositionMatrix();
            VertexConsumer lineBuffer = immediate.getBuffer(RenderLayer.getLines());
            
            float radius = target.getWidth() + 0.4f;

            // Draw a Golden Glowing Ring with particles rising
            for(int i = 0; i < 360; i += 20) {
                float r1 = (float)Math.toRadians(i);
                float r2 = (float)Math.toRadians(i + 20);
                
                float px1 = (float)Math.cos(r1) * radius;
                float pz1 = (float)Math.sin(r1) * radius;
                float px2 = (float)Math.cos(r2) * radius;
                float pz2 = (float)Math.sin(r2) * radius;
                
                // Base Ring
                RenderUtils3D.drawLine(mat, lineBuffer, px1, 0, pz1, px2, 0, pz2, 1.0f, 0.9f, 0.2f, 1.0f); // Gold

                // Flying Light Particles
                if (i % 40 == 0) {
                    float upY = (now % 1000) / 1000.0f * 2.0f; // Fly up 2 blocks
                    float fade = 1.0f - (upY / 2.0f);
                    RenderUtils3D.drawLine(mat, lineBuffer, px1, upY, pz1, px1, upY + 0.2f, pz1, 1.0f, 0.9f, 0.2f, fade);
                }
            }
            matrices.pop();

            // Green Friend Marker
            matrices.push(); 
            double bounce = Math.sin(System.currentTimeMillis() / 150.0) * 0.2; 
            matrices.translate(x, y + target.getHeight() + 1.8 + bounce, z); 
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw())); 
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch())); 
            matrices.scale(-0.1F, -0.1F, 0.1F); 
            client.textRenderer.draw("▼", -client.textRenderer.getWidth("▼") / 2f, 0, 0xFF00FF00, true, matrices.peek().getPositionMatrix(), immediate, TextRenderer.TextLayerType.SEE_THROUGH, 0x00000000, 15728880);
            matrices.pop();
        }
    }
}
