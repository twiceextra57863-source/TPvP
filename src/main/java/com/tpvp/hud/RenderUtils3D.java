package com.tpvp.hud;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;
import net.minecraft.client.util.math.MatrixStack;

public class RenderUtils3D {
    
    // --- BASIC 3D LINE RENDERER ---
    public static void drawLine(Matrix4f m, VertexConsumer v, float x1, float y1, float z1, float x2, float y2, float z2, float r, float g, float b, float a) {
        v.vertex(m, x1, y1, z1).color(r, g, b, a).normal(x2 - x1, y2 - y1, z2 - z1);
        v.vertex(m, x2, y2, z2).color(r, g, b, a).normal(x2 - x1, y2 - y1, z2 - z1);
    }
    
    // --- BASIC 3D FLAT QUAD RENDERER ---
    public static void drawQuad(Matrix4f m, VertexConsumer v, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4, float r, float g, float b, float a, int l) {
        v.vertex(m, x1, y1, z1).color(r, g, b, a).light(l); 
        v.vertex(m, x2, y2, z2).color(r, g, b, a).light(l);
        v.vertex(m, x3, y3, z3).color(r, g, b, a).light(l); 
        v.vertex(m, x4, y4, z4).color(r, g, b, a).light(l);
    }
    
    // --- TRANSPARENT HOLLOW 3D BOX (Used for Hitboxes & Dragon Heads) ---
    public static void draw3DBox(Matrix4f m, VertexConsumer v, float x, float y, float z, float s, float r, float g, float b, float a, int l) {
        float h = s / 2f;
        drawQuad(m, v, x-h, y-h, z-h, x+h, y-h, z-h, x+h, y+h, z-h, x-h, y+h, z-h, r, g, b, a, l); // Front
        drawQuad(m, v, x-h, y-h, z+h, x+h, y-h, z+h, x+h, y+h, z+h, x-h, y+h, z+h, r*0.8f, g*0.8f, b*0.8f, a, l); // Back
        drawQuad(m, v, x-h, y-h, z-h, x-h, y-h, z+h, x-h, y+h, z+h, x-h, y+h, z-h, r*0.9f, g*0.9f, b*0.9f, a, l); // Left
        drawQuad(m, v, x+h, y-h, z-h, x+h, y-h, z+h, x+h, y+h, z+h, x+h, y+h, z-h, r*0.9f, g*0.9f, b*0.9f, a, l); // Right
        drawQuad(m, v, x-h, y+h, z-h, x+h, y+h, z-h, x+h, y+h, z+h, x-h, y+h, z+h, r*1.2f, g*1.2f, b*1.2f, a, l); // Top
        drawQuad(m, v, x-h, y-h, z-h, x+h, y-h, z-h, x+h, y-h, z+h, x-h, y-h, z+h, r*0.5f, g*0.5f, b*0.5f, a, l); // Bottom
    }

    // --- SOLID COLORED 3D BOX (Used for JJK Monster Body Parts) ---
    public static void drawSolidBox(Matrix4f m, VertexConsumer v, float x1, float y1, float z1, float x2, float y2, float z2, float r, float g, float b, float a, int l) {
        // Front & Back
        drawQuad(m, v, x1, y1, z1, x2, y1, z1, x2, y2, z1, x1, y2, z1, r*0.9f, g*0.9f, b*0.9f, a, l); // Front
        drawQuad(m, v, x1, y1, z2, x2, y1, z2, x2, y2, z2, x1, y2, z2, r*0.8f, g*0.8f, b*0.8f, a, l); // Back
        
        // Left & Right
        drawQuad(m, v, x1, y1, z1, x1, y1, z2, x1, y2, z2, x1, y2, z1, r*0.85f, g*0.85f, b*0.85f, a, l); // Left
        drawQuad(m, v, x2, y1, z1, x2, y1, z2, x2, y2, z2, x2, y2, z1, r*0.85f, g*0.85f, b*0.85f, a, l); // Right
        
        // Top & Bottom
        drawQuad(m, v, x1, y2, z1, x2, y2, z1, x2, y2, z2, x1, y2, z2, r*1.1f, g*1.1f, b*1.1f, a, l); // Top
        drawQuad(m, v, x1, y1, z1, x2, y1, z1, x2, y1, z2, x1, y1, z2, r*0.5f, g*0.5f, b*0.5f, a, l); // Bottom
    }
    
    // --- TEXTURED 2D FLAT QUAD (Used for Indicators) ---
    public static void drawTextureQuad(Matrix4f m, VertexConsumer v, float x, float y, float w, float h, float u1, float v1, float u2, float v2, float r, float g, float b, float a, int l) {
        v.vertex(m, x, y, 0).color(r, g, b, a).texture(u1, v1).light(l); 
        v.vertex(m, x, y+h, 0).color(r, g, b, a).texture(u1, v2).light(l);
        v.vertex(m, x+w, y+h, 0).color(r, g, b, a).texture(u2, v2).light(l); 
        v.vertex(m, x+w, y, 0).color(r, g, b, a).texture(u2, v1).light(l);
    }
    
    // --- COLORED 2D FLAT QUAD (Used for Health Bars) ---
    public static void drawColorQuad(Matrix4f m, VertexConsumer v, float x, float y, float w, float h, int c, int l) {
        float a = (c >> 24 & 255) / 255.0F;
        float r = (c >> 16 & 255) / 255.0F;
        float g = (c >> 8 & 255) / 255.0F;
        float b = (c & 255) / 255.0F;
        drawQuad(m, v, x, y, 0, x, y+h, 0, x+w, y+h, 0, x+w, y, 0, r, g, b, a, l);
    }
    
    // --- CRASH-FREE TEXTURED 3D QUAD (Overlay & Normal mappings for Entities) ---
    public static void drawSoulQuad(Matrix4f m, VertexConsumer v, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4, float u1, float v1, float u2, float v2, float r, float g, float b, float a, int l) {
        v.vertex(m, x1, y1, z1).color(r, g, b, a).texture(u1, v1).overlay(net.minecraft.client.render.OverlayTexture.DEFAULT_UV).light(l).normal(0, 0, 1);
        v.vertex(m, x2, y2, z2).color(r, g, b, a).texture(u1, v2).overlay(net.minecraft.client.render.OverlayTexture.DEFAULT_UV).light(l).normal(0, 0, 1);
        v.vertex(m, x3, y3, z3).color(r, g, b, a).texture(u2, v2).overlay(net.minecraft.client.render.OverlayTexture.DEFAULT_UV).light(l).normal(0, 0, 1);
        v.vertex(m, x4, y4, z4).color(r, g, b, a).texture(u2, v1).overlay(net.minecraft.client.render.OverlayTexture.DEFAULT_UV).light(l).normal(0, 0, 1);
    }

    // ---------------------------------------------------------
    // --- 100% ACCURATE 64x64 MINECRAFT SKIN MAPPING SYSTEM ---
    // ---------------------------------------------------------
    public static void drawSkinPart(Matrix4f m, VertexConsumer v, float x, float y, float z, float w, float h, float d, float texU, float texV, float texW, float texH, float texD, float a, int l) {
        float p = 1f / 64f; // Converts 64x64 pixels to normalized 0-1 texture range
        float r = 1f, g = 1f, b = 1f;

        // Front Face
        drawSoulQuad(m, v, x, y, z-d, x+w, y, z-d, x+w, y+h, z-d, x, y+h, z-d, 
            (texU + texD) * p, (texV + texD) * p, 
            (texU + texD + texW) * p, (texV + texD + texH) * p, r, g, b, a, l);
        
        // Back Face
        drawSoulQuad(m, v, x+w, y, z, x, y, z, x, y+h, z, x+w, y+h, z, 
            (texU + texD + texW + texD) * p, (texV + texD) * p, 
            (texU + texD + texW + texD + texW) * p, (texV + texD + texH) * p, r*0.8f, g*0.8f, b*0.8f, a, l);
        
        // Right Face
        drawSoulQuad(m, v, x, y, z, x, y, z-d, x, y+h, z-d, x, y+h, z, 
            texU * p, (texV + texD) * p, 
            (texU + texD) * p, (texV + texD + texH) * p, r*0.9f, g*0.9f, b*0.9f, a, l);
        
        // Left Face
        drawSoulQuad(m, v, x+w, y, z-d, x+w, y, z, x+w, y+h, z, x+w, y+h, z-d, 
            (texU + texD + texW) * p, (texV + texD) * p, 
            (texU + texD + texW + texD) * p, (texV + texD + texH) * p, r*0.9f, g*0.9f, b*0.9f, a, l);
        
        // Top Face
        drawSoulQuad(m, v, x, y+h, z, x+w, y+h, z, x+w, y+h, z-d, x, y+h, z-d, 
            (texU + texD) * p, texV * p, 
            (texU + texD + texW) * p, (texV + texD) * p, r*1.1f, g*1.1f, b*1.1f, a, l);
        
        // Bottom Face
        drawSoulQuad(m, v, x, y, z-d, x+w, y, z-d, x+w, y, z, x, y, z, 
            (texU + texD + texW) * p, texV * p, 
            (texU + texD + texW + texW) * p, (texV + texD) * p, r*0.5f, g*0.5f, b*0.5f, a, l);
    }

    // ---------------------------------------------------------
    // --- FULL 3D DOLL/RAGDOLL RENDERER (Minecraft Shape) ---
    // ---------------------------------------------------------
    public static void drawDoll(MatrixStack matrices, VertexConsumerProvider.Immediate imm, Identifier skin, float alpha, float armPitch, float legPitch, float headPitch, float headYaw) {
        Matrix4f mat = matrices.peek().getPositionMatrix();
        VertexConsumer v = imm.getBuffer(RenderLayer.getEntityTranslucent(skin)); // Keeps transparency active (e.g. glass on skin)
        int l = net.minecraft.client.render.LightmapTextureManager.MAX_LIGHT_COORDINATE; // Maximum brightness (No shadows)

        // Head (Minecraft Texture Offset: 0, 0 | Size: 8x8x8)
        matrices.push(); 
        matrices.translate(0, -1.5f, 0); // Position exactly above neck
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(headYaw)); 
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(headPitch));
        drawSkinPart(matrices.peek().getPositionMatrix(), v, -0.25f, -0.25f, -0.25f, 0.5f, 0.5f, 0.5f, 0, 0, 8, 8, 8, alpha, l);
        matrices.pop();

        // Body/Chest (Minecraft Texture Offset: 16, 16 | Size: 8x12x4)
        matrices.push(); 
        matrices.translate(0, -0.75f, 0); // Core Position
        drawSkinPart(matrices.peek().getPositionMatrix(), v, -0.25f, -0.375f, -0.125f, 0.5f, 0.75f, 0.25f, 16, 16, 8, 12, 4, alpha, l);
        matrices.pop();

        // Right Arm (Minecraft Texture Offset: 40, 16 | Size: 4x12x4)
        matrices.push(); 
        matrices.translate(-0.375f, -1.0f, 0); // Pivot at shoulder joint
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-armPitch));
        drawSkinPart(matrices.peek().getPositionMatrix(), v, -0.125f, -0.375f, -0.125f, 0.25f, 0.75f, 0.25f, 40, 16, 4, 12, 4, alpha, l);
        matrices.pop();

        // Left Arm (Minecraft Texture Offset: 32, 48 | Size: 4x12x4)
        matrices.push(); 
        matrices.translate(0.375f, -1.0f, 0); // Pivot at shoulder joint
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(armPitch));
        drawSkinPart(matrices.peek().getPositionMatrix(), v, -0.125f, -0.375f, -0.125f, 0.25f, 0.75f, 0.25f, 32, 48, 4, 12, 4, alpha, l);
        matrices.pop();

        // Right Leg (Minecraft Texture Offset: 0, 16 | Size: 4x12x4)
        matrices.push(); 
        matrices.translate(-0.125f, -0.375f, 0); // Pivot at hip joint
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-legPitch));
        drawSkinPart(matrices.peek().getPositionMatrix(), v, -0.125f, -0.375f, -0.125f, 0.25f, 0.75f, 0.25f, 0, 16, 4, 12, 4, alpha, l);
        matrices.pop();

        // Left Leg (Minecraft Texture Offset: 16, 48 | Size: 4x12x4)
        matrices.push(); 
        matrices.translate(0.125f, -0.375f, 0); // Pivot at hip joint
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(legPitch));
        drawSkinPart(matrices.peek().getPositionMatrix(), v, -0.125f, -0.375f, -0.125f, 0.25f, 0.75f, 0.25f, 16, 48, 4, 12, 4, alpha, l);
        matrices.pop();
    }
}
