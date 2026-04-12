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

        // DRAGON AURA (Enemies)
        if (tName.equals(activeTarget) && ModConfig.dragonAuraEnabled) {
            long popTime = totemPopMap.getOrDefault(target.getId(), 0L);
            long now = System.currentTimeMillis();
            boolean isTotemPop = (now - popTime) < 3000;

            matrices.push(); matrices.translate(x, y, z);
            Matrix4f mat = matrices.peek().getPositionMatrix();
            VertexConsumer quadBuffer = immediate.getBuffer(RenderLayer.getGui()); 
            
            float h = target.getHeight(), radius = target.getWidth() + 0.3f;
            float headX = 0, headY = 0, headZ = 0;
            float yOff = 0f, rotMult = 1f, shatter = 0f, alpha = 0.8f;
            
            if (isTotemPop) {
                float popT = (now - popTime) / 3000.0f;
                if (popT < 0.5f) { radius += popT * 5.0f; yOff = popT * 10.0f; rotMult = 5.0f; } 
                else { shatter = (popT - 0.5f) / 0.5f; alpha = 0.8f * (1.0f - shatter); }
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
                        RenderUtils3D.drawQuad(mat, quadBuffer, px1, py1, pz1, px1, py1+0.15f, pz1, px2, py2+0.15f, pz2, px2, py2, pz2, red, green, blue, alpha, 15728880);
                    }
                }
                if (shatter == 0) RenderUtils3D.draw3DBox(mat, quadBuffer, headX, headY+0.1f, headZ, 0.2f, 1f, isTotemPop ? 0.8f : 0f, 0f, alpha, 15728880);
            }
            matrices.pop();

            // Bouncing Arrow
            matrices.push(); 
            double bounce = Math.sin(System.currentTimeMillis() / 150.0) * 0.2; 
            matrices.translate(x, y + target.getHeight() + 1.8 + bounce, z); 
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw())); 
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch())); 
            matrices.scale(-0.1F, -0.1F, 0.1F); 
            client.textRenderer.draw("▼", -client.textRenderer.getWidth("▼") / 2f, 0, 0xFFFF2222, true, matrices.peek().getPositionMatrix(), immediate, TextRenderer.TextLayerType.SEE_THROUGH, 0x00000000, 15728880);
            matrices.pop();
        }

        // FRIEND HALO
        if (tName.equals(ModConfig.taggedFriendName) && ModConfig.dragonAuraEnabled) {
            matrices.push();
            matrices.translate(x, y + target.getHeight() / 2, z); 
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw())); 
            long time = System.currentTimeMillis();
            float t = (time % 3000) / 3000.0f; 
            float radius = 1.0f + (float)Math.sin(time / 200.0) * 0.1f; 
            float shatter = 0f, alpha = 0.8f;
            
            if (t > 0.7f) { shatter = (t - 0.7f) / 0.3f; alpha = 0.8f * (1.0f - shatter); radius += shatter * 2.0f; } 
            else if (t < 0.3f) { radius -= (0.3f - t) * 2.0f; }

            Matrix4f mat = matrices.peek().getPositionMatrix();
            VertexConsumer lineBuffer = immediate.getBuffer(RenderLayer.getLines());
            
            for (int i = 0; i < 360; i += 10) {
                float r1 = (float) Math.toRadians(i), r2 = (float) Math.toRadians(i + 10);
                float px1 = (float) Math.cos(r1) * radius, py1 = (float) Math.sin(r1) * radius;
                float px2 = (float) Math.cos(r2) * radius, py2 = (float) Math.sin(r2) * radius;

                if (shatter > 0 || t < 0.3f) {
                    float sx = (float) Math.sin(i * 13) * 0.5f, sy = (float) Math.cos(i * 17) * 0.5f;
                    px1 += sx; py1 += sy; px2 += sx; py2 += sy;
                }
                RenderUtils3D.drawLine(mat, lineBuffer, px1, py1, -0.5f, px2, py2, -0.5f); // Used helper
            }
            matrices.pop();
        }
    }
}
