package com.tpvp.hud;

import com.tpvp.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
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
        // 1. DRAGON AURA (ENEMIES) - WITH ROAR & BELLY
        // ---------------------------------------------------------
        if (tName.equals(activeTarget) && ModConfig.dragonAuraEnabled) {
            long popTime = totemPopMap.getOrDefault(target.getId(), 0L);
            long now = System.currentTimeMillis();
            boolean isTotemPop = (now - popTime) < 3000;

            matrices.push(); 
            matrices.translate(x, y, z);
            Matrix4f mat = matrices.peek().getPositionMatrix();
            VertexConsumer quadBuffer = immediate.getBuffer(RenderLayer.getGui()); 
            
            float h = target.getHeight();
            float radius = target.getWidth() + 0.4f;
            float headX = 0, headY = 0, headZ = 0;
            
            // Dragon Color Settings
            float dr = 1f, dg = 0.1f, db = 0.1f; // 0=Ruby Red
            if (ModConfig.dragonColor == 1) { dr = 0.6f; dg = 0f; db = 1f; } // 1=Void Purple
            else if (ModConfig.dragonColor == 2) { dr = 0f; dg = 0.8f; db = 1f; } // 2=Frost Blue

            long cycle = 6000; 
            float t = (System.currentTimeMillis() % cycle) / (float) cycle;
            
            float progress = 0f, shatter = 0f, alpha = 0.8f, yOff = 0f; 
            float lookYaw = target.getYaw(); 
            
            if (isTotemPop) {
                float popT = (now - popTime) / 3000.0f;
                if (popT < 0.2f) { 
                    progress = 1.0f; 
                    yOff = (popT / 0.2f) * 6.0f; 
                } else if (popT < 0.5f) { 
                    progress = 1.0f; 
                    yOff = 6.0f - (((popT - 0.2f) / 0.3f) * 6.0f); 
                } else { 
                    shatter = (popT - 0.5f) / 0.5f; 
                    alpha = 0.8f * (1.0f - shatter); 
                } 
            } else {
                if (t < 0.3f) { 
                    progress = t / 0.3f; 
                } else if (t < 0.6f) { 
                    progress = 1.0f;
                    headX = (float) Math.cos(Math.toRadians(lookYaw + 90)) * radius;
                    headZ = (float) Math.sin(Math.toRadians(lookYaw + 90)) * radius;
                    headY = h + 0.2f;
                } else if (t < 0.8f) { 
                    progress = 1.0f; 
                    shatter = (t - 0.6f) / 0.2f; 
                    alpha = 0.8f * (1.0f - shatter); 
                } else { 
                    alpha = 0f; 
                } 
            }

            if (alpha > 0.05f) {
                float rotSpeed = (now % 2000) / 2000.0f;
                int segments = 30;
                int activeSegs = (int)(segments * progress);
                boolean drawHead = (t >= 0.3f && t < 0.6f && !isTotemPop); 

                for (int layer = 0; layer < 3; layer++) {
                    float offset = layer * 0.05f;
                    for (int i = 0; i < activeSegs; i++) {
                        float pt1 = (i / (float) segments);
                        float pt2 = ((i + 1) / (float) segments);
                        
                        float py1 = pt1 * h + yOff;
                        float px1 = (float) Math.cos((pt1 + rotSpeed + offset) * Math.PI * 4) * radius;
                        float pz1 = (float) Math.sin((pt1 + rotSpeed + offset) * Math.PI * 4) * radius;

                        float py2 = pt2 * h + yOff;
                        float px2 = (float) Math.cos((pt2 + rotSpeed + offset) * Math.PI * 4) * radius;
                        float pz2 = (float) Math.sin((pt2 + rotSpeed + offset) * Math.PI * 4) * radius;

                        if (shatter > 0) {
                            float sx = (float) Math.sin(i * 13) * shatter * 3.0f;
                            float sy = shatter * 5.0f; 
                            float sz = (float) Math.sin(i * 23) * shatter * 3.0f;
                            px1 += sx; py1 += sy; pz1 += sz;
                            px2 += sx; py2 += sy; pz2 += sz;
                        }

                        if (!drawHead && layer == 1 && i == activeSegs - 1) { 
                            headX = px2; headY = py2; headZ = pz2; drawHead = true; 
                        }

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

            // Enemy Target Down Arrow
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
        // 2. FRIEND PET DOLL (Cute Companion System)
        // ---------------------------------------------------------
        if (tName.equals(ModConfig.taggedFriendName) && ModConfig.dragonAuraEnabled) {
            
            boolean isStill = target.getVelocity().lengthSquared() < 0.01 && target.distanceTo(client.player) < 5.0;
            long now = System.currentTimeMillis();
            
            matrices.push();
            float dollX, dollY, dollZ;
            
            float hover = (float) Math.sin(now / 300.0) * 0.2f; 
            float dollRot = 0f;
            
            if (isStill) {
                // Return to Player and Hug/Handshake 
                dollX = (float) (x + Math.cos(Math.toRadians(target.getYaw() + 90)) * 1.5);
                dollZ = (float) (z + Math.sin(Math.toRadians(target.getYaw() + 90)) * 1.5);
                dollY = (float) (y + target.getHeight() / 2 + hover);
                dollRot = -camera.getYaw(); // Look at you
            } else {
                // Fly behind the friend (Chura ke bhagna)
                dollX = (float) (x + Math.cos(Math.toRadians(target.getYaw() - 90)) * 1.5);
                dollZ = (float) (z + Math.sin(Math.toRadians(target.getYaw() - 90)) * 1.5);
                dollY = (float) (y + target.getHeight() + hover + 0.5f); // Up high
                dollRot = target.getYaw(); // Look same direction
            }

            matrices.translate(dollX, dollY, dollZ);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(dollRot));
            
            matrices.scale(0.3F, 0.3F, 0.3F);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180f)); 
            
            Identifier fSkin = (target instanceof AbstractClientPlayerEntity pt) ? pt.getSkinTextures().texture() : Identifier.ofVanilla("textures/entity/steve.png");
            
            // Animation Limbs
            float armP = isStill ? -120f : (float) Math.sin(now / 100.0) * 60f; // Hug pose if still, Flapping if running
            float legP = isStill ? 0f : (float) Math.sin(now / 100.0) * 45f; // Kicking when flying
            float headP = isStill ? -20f : 0f; // Look up cute

            RenderUtils3D.drawDoll(matrices, immediate, fSkin, 1.0f, armP, legP, headP, 0f);
            
            matrices.pop();

            // Friend Name Tag (Green Arrow)
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
