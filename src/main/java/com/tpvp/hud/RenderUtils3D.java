package com.tpvp.hud;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;
import net.minecraft.client.util.math.MatrixStack;

public class RenderUtils3D {
    
    // ---------------------------------------------------------
    // --- BASIC LINE & QUAD RENDERERS ---
    // ---------------------------------------------------------
    public static void drawLine(Matrix4f m, VertexConsumer v, float x1, float y1, float z1, float x2, float y2, float z2, float r, float g, float b, float a) {
        v.vertex(m, x1, y1, z1).color(r, g, b, a).normal(x2 - x1, y2 - y1, z2 - z1);
        v.vertex(m, x2, y2, z2).color(r, g, b, a).normal(x2 - x1, y2 - y1, z2 - z1);
    }
    
    public static void drawQuad(Matrix4f m, VertexConsumer v, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4, float r, float g, float b, float a, int l) {
        v.vertex(m, x1, y1, z1).color(r, g, b, a).light(l); 
        v.vertex(m, x2, y2, z2).color(r, g, b, a).light(l);
        v.vertex(m, x3, y3, z3).color(r, g, b, a).light(l); 
        v.vertex(m, x4, y4, z4).color(r, g, b, a).light(l);
    }

    // ---------------------------------------------------------
    // --- SOLID & TRANSPARENT 3D BOXES ---
    // ---------------------------------------------------------
    public static void draw3DBox(Matrix4f m, VertexConsumer v, float x, float y, float z, float s, float r, float g, float b, float a, int l) {
        float h = s / 2f;
        drawQuad(m, v, x-h, y-h, z-h, x+h, y-h, z-h, x+h, y+h, z-h, x-h, y+h, z-h, r, g, b, a, l); // Front
        drawQuad(m, v, x-h, y-h, z+h, x+h, y-h, z+h, x+h, y+h, z+h, x-h, y+h, z+h, r*0.8f, g*0.8f, b*0.8f, a, l); // Back
        drawQuad(m, v, x-h, y-h, z-h, x-h, y-h, z+h, x-h, y+h, z+h, x-h, y+h, z-h, r*0.9f, g*0.9f, b*0.9f, a, l); // Left
        drawQuad(m, v, x+h, y-h, z-h, x+h, y-h, z+h, x+h, y+h, z+h, x+h, y+h, z-h, r*0.9f, g*0.9f, b*0.9f, a, l); // Right
        drawQuad(m, v, x-h, y+h, z-h, x+h, y+h, z-h, x+h, y+h, z+h, x-h, y+h, z+h, r*1.2f, g*1.2f, b*1.2f, a, l); // Top
        drawQuad(m, v, x-h, y-h, z-h, x+h, y-h, z-h, x+h, y-h, z+h, x-h, y-h, z+h, r*0.5f, g*0.5f, b*0.5f, a, l); // Bottom
    }

    public static void drawSolidBox(Matrix4f m, VertexConsumer v, float x1, float y1, float z1, float x2, float y2, float z2, float r, float g, float b, float a, int l) {
        // Front & Back
        drawQuad(m, v, x1, y1, z1, x2, y1, z1, x2, y2, z1, x1, y2, z1, r*0.9f, g*0.9f, b*0.9f, a, l); 
        drawQuad(m, v, x1, y1, z2, x2, y1, z2, x2, y2, z2, x1, y2, z2, r*0.8f, g*0.8f, b*0.8f, a, l); 
        
        // Left & Right
        drawQuad(m, v, x1, y1, z1, x1, y1, z2, x1, y2, z2, x1, y2, z1, r*0.85f, g*0.85f, b*0.85f, a, l); 
        drawQuad(m, v, x2, y1, z1, x2, y1, z2, x2, y2, z2, x2, y2, z1, r*0.85f, g*0.85f, b*0.85f, a, l); 
        
        // Top & Bottom
        drawQuad(m, v, x1, y2, z1, x2, y2, z1, x2, y2, z2, x1, y2, z2, r*1.1f, g*1.1f, b*1.1f, a, l); 
        drawQuad(m, v, x1, y1, z1, x2, y1, z1, x2, y1, z2, x1, y1, z2, r*0.5f, g*0.5f, b*0.5f, a, l); 
    }

    // ---------------------------------------------------------
    // --- TEXTURE AND COLOR QUADS (FOR 2D UI) ---
    // ---------------------------------------------------------
    public static void drawTextureQuad(Matrix4f m, VertexConsumer v, float x, float y, float w, float h, float u1, float v1, float u2, float v2, float r, float g, float b, float a, int l) {
        v.vertex(m, x, y, 0).color(r, g, b, a).texture(u1, v1).light(l); 
        v.vertex(m, x, y+h, 0).color(r, g, b, a).texture(u1, v2).light(l);
        v.vertex(m, x+w, y+h, 0).color(r, g, b, a).texture(u2, v2).light(l); 
        v.vertex(m, x+w, y, 0).color(r, g, b, a).texture(u2, v1).light(l);
    }
    
    public static void drawColorQuad(Matrix4f m, VertexConsumer v, float x, float y, float w, float h, int c, int l) {
        float a = (c >> 24 & 255) / 255.0F;
        float r = (c >> 16 & 255) / 255.0F;
        float g = (c >> 8 & 255) / 255.0F;
        float b = (c & 255) / 255.0F;
        drawQuad(m, v, x, y, 0, x, y+h, 0, x+w, y+h, 0, x+w, y, 0, r, g, b, a, l);
    }

    // ---------------------------------------------------------
    // --- CRASH-FREE SOUL QUAD (Requires Normal & Overlay) ---
    // ---------------------------------------------------------
    public static void drawSoulQuad(Matrix4f m, VertexConsumer v, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4, float u1, float v1, float u2, float v2, float r, float g, float b, float a, int l) {
        v.vertex(m, x1, y1, z1).color(r, g, b, a).texture(u1, v1).overlay(net.minecraft.client.render.OverlayTexture.DEFAULT_UV).light(l).normal(0, 0, 1);
        v.vertex(m, x2, y2, z2).color(r, g, b, a).texture(u1, v2).overlay(net.minecraft.client.render.OverlayTexture.DEFAULT_UV).light(l).normal(0, 0, 1);
        v.vertex(m, x3, y3, z3).color(r, g, b, a).texture(u2, v2).overlay(net.minecraft.client.render.OverlayTexture.DEFAULT_UV).light(l).normal(0, 0, 1);
        v.vertex(m, x4, y4, z4).color(r, g, b, a).texture(u2, v1).overlay(net.minecraft.client.render.OverlayTexture.DEFAULT_UV).light(l).normal(0, 0, 1);
    }

    // ---------------------------------------------------------
    // --- EXACT 64x64 MINECRAFT SKIN MAPPING (NO GLITCHES) ---
    // ---------------------------------------------------------
    public static void drawSkinPart(Matrix4f m, VertexConsumer v, float x, float y, float z, float w, float h, float d, float texU, float texV, float texW, float texH, float texD, float a, int l) {
        float p = 1f / 64f; 
        float r = 1f, g = 1f, b = 1f;

        // Front Face
        drawSoulQuad(m, v, x, y, z-d, x+w, y, z-d, x+w, y+h, z-d, x, y+h, z-d, 
            (texU + texD) * p, (texV + texD) * p, (texU + texD + texW) * p, (texV + texD + texH) * p, r, g, b, a, l);
        
        // Back Face
        drawSoulQuad(m, v, x+w, y, z, x, y, z, x, y+h, z, x+w, y+h, z, 
            (texU + texD + texW + texD) * p, (texV + texD) * p, (texU + texD + texW + texD + texW) * p, (texV + texD + texH) * p, r*0.8f, g*0.8f, b*0.8f, a, l);
        
        // Right Face
        drawSoulQuad(m, v, x, y, z, x, y, z-d, x, y+h, z-d, x, y+h, z, 
            texU * p, (texV + texD) * p, (texU + texD) * p, (texV + texD + texH) * p, r*0.9f, g*0.9f, b*0.9f, a, l);
        
        // Left Face
        drawSoulQuad(m, v, x+w, y, z-d, x+w, y, z, x+w, y+h, z, x+w, y+h, z-d, 
            (texU + texD + texW) * p, (texV + texD) * p, (texU + texD + texW + texD) * p, (texV + texD + texH) * p, r*0.9f, g*0.9f, b*0.9f, a, l);
        
        // Top Face
        drawSoulQuad(m, v, x, y+h, z, x+w, y+h, z, x+w, y+h, z-d, x, y+h, z-d, 
            (texU + texD) * p, texV * p, (texU + texD + texW) * p, (texV + texD) * p, r*1.1f, g*1.1f, b*1.1f, a, l);
        
        // Bottom Face
        drawSoulQuad(m, v, x, y, z-d, x+w, y, z-d, x+w, y, z, x, y, z, 
            (texU + texD + texW) * p, texV * p, (texU + texD + texW + texW) * p, (texV + texD) * p, r*0.5f, g*0.5f, b*0.5f, a, l);
    }

    // ---------------------------------------------------------
    // --- FULL 3D DOLL/RAGDOLL RENDERER ---
    // ---------------------------------------------------------
    public static void drawDoll(MatrixStack matrices, VertexConsumerProvider.Immediate imm, Identifier skin, float alpha, float armPitch, float legPitch, float headPitch, float headYaw) {
        Matrix4f mat = matrices.peek().getPositionMatrix();
        VertexConsumer v = imm.getBuffer(RenderLayer.getEntityTranslucent(skin)); 
        int l = net.minecraft.client.render.LightmapTextureManager.MAX_LIGHT_COORDINATE;

        // Head
        matrices.push(); 
        matrices.translate(0, -1.5f, 0); 
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(headYaw)); 
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(headPitch));
        drawSkinPart(matrices.peek().getPositionMatrix(), v, -0.25f, -0.25f, -0.25f, 0.5f, 0.5f, 0.5f, 0, 0, 8, 8, 8, alpha, l);
        matrices.pop();

        // Body
        matrices.push(); 
        matrices.translate(0, -0.75f, 0); 
        drawSkinPart(matrices.peek().getPositionMatrix(), v, -0.25f, -0.375f, -0.125f, 0.5f, 0.75f, 0.25f, 16, 16, 8, 12, 4, alpha, l);
        matrices.pop();

        // Right Arm
        matrices.push(); 
        matrices.translate(-0.375f, -1.0f, 0); 
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-armPitch));
        drawSkinPart(matrices.peek().getPositionMatrix(), v, -0.125f, -0.375f, -0.125f, 0.25f, 0.75f, 0.25f, 40, 16, 4, 12, 4, alpha, l);
        matrices.pop();

        // Left Arm
        matrices.push(); 
        matrices.translate(0.375f, -1.0f, 0); 
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(armPitch));
        drawSkinPart(matrices.peek().getPositionMatrix(), v, -0.125f, -0.375f, -0.125f, 0.25f, 0.75f, 0.25f, 32, 48, 4, 12, 4, alpha, l);
        matrices.pop();

        // Right Leg
        matrices.push(); 
        matrices.translate(-0.125f, -0.375f, 0); 
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-legPitch));
        drawSkinPart(matrices.peek().getPositionMatrix(), v, -0.125f, -0.375f, -0.125f, 0.25f, 0.75f, 0.25f, 0, 16, 4, 12, 4, alpha, l);
        matrices.pop();

        // Left Leg
        matrices.push(); 
        matrices.translate(0.125f, -0.375f, 0); 
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(legPitch));
        drawSkinPart(matrices.peek().getPositionMatrix(), v, -0.125f, -0.375f, -0.125f, 0.25f, 0.75f, 0.25f, 16, 48, 4, 12, 4, alpha, l);
        matrices.pop();
    }

    // ---------------------------------------------------------
    // --- ANIMATED CIRCULAR PROGRESS BAR (FOR TIER UI) ---
    // ---------------------------------------------------------
    public static void drawThickArc(Matrix4f m, VertexConsumer v, float cx, float cy, float radius, float thickness, float startAngle, float endAngle, int color, int light) {
        float a = (color >> 24 & 255) / 255.0F;
        float r = (color >> 16 & 255) / 255.0F;
        float g = (color >> 8 & 255) / 255.0F;
        float b = (color & 255) / 255.0F;

        int segments = 60; // Smoothness of the circle
        float angleStep = 360f / segments;

        for (float angle = startAngle; angle < endAngle; angle += angleStep) {
            float nextAngle = Math.min(angle + angleStep, endAngle);

            float rad1 = (float) Math.toRadians(angle - 90); 
            float rad2 = (float) Math.toRadians(nextAngle - 90);

            float inX1 = cx + (float) Math.cos(rad1) * (radius - thickness);
            float inY1 = cy + (float) Math.sin(rad1) * (radius - thickness);
            float outX1 = cx + (float) Math.cos(rad1) * radius;
            float outY1 = cy + (float) Math.sin(rad1) * radius;

            float inX2 = cx + (float) Math.cos(rad2) * (radius - thickness);
            float inY2 = cy + (float) Math.sin(rad2) * (radius - thickness);
            float outX2 = cx + (float) Math.cos(rad2) * radius;
            float outY2 = cy + (float) Math.sin(rad2) * radius;

            drawQuad(m, v, inX1, inY1, 0, outX1, outY1, 0, outX2, outY2, 0, inX2, inY2, 0, r, g, b, a, light);
        }
    }
}
