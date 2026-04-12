package com.tpvp.hud;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;
import net.minecraft.client.util.math.MatrixStack;

public class RenderUtils3D {
    
    public static void drawLine(Matrix4f m, VertexConsumer v, float x1, float y1, float z1, float x2, float y2, float z2, float r, float g, float b, float a) {
        v.vertex(m, x1, y1, z1).color(r,g,b,a).normal(x2-x1, y2-y1, z2-z1);
        v.vertex(m, x2, y2, z2).color(r,g,b,a).normal(x2-x1, y2-y1, z2-z1);
    }
    
    public static void drawQuad(Matrix4f m, VertexConsumer v, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4, float r, float g, float b, float a, int l) {
        v.vertex(m, x1, y1, z1).color(r,g,b,a).light(l); v.vertex(m, x2, y2, z2).color(r,g,b,a).light(l);
        v.vertex(m, x3, y3, z3).color(r,g,b,a).light(l); v.vertex(m, x4, y4, z4).color(r,g,b,a).light(l);
    }
    
    public static void draw3DBox(Matrix4f m, VertexConsumer v, float x, float y, float z, float s, float r, float g, float b, float a, int l) {
        float h = s/2f;
        drawQuad(m, v, x-h, y-h, z-h, x+h, y-h, z-h, x+h, y+h, z-h, x-h, y+h, z-h, r, g, b, a, l); 
        drawQuad(m, v, x-h, y-h, z+h, x+h, y-h, z+h, x+h, y+h, z+h, x-h, y+h, z+h, r*0.8f, g*0.8f, b*0.8f, a, l); 
        drawQuad(m, v, x-h, y-h, z-h, x-h, y-h, z+h, x-h, y+h, z+h, x-h, y+h, z-h, r*0.9f, g*0.9f, b*0.9f, a, l); 
        drawQuad(m, v, x+h, y-h, z-h, x+h, y-h, z+h, x+h, y+h, z+h, x+h, y+h, z-h, r*0.9f, g*0.9f, b*0.9f, a, l); 
        drawQuad(m, v, x-h, y+h, z-h, x+h, y+h, z-h, x+h, y+h, z+h, x-h, y+h, z+h, r*1.2f, g*1.2f, b*1.2f, a, l); 
        drawQuad(m, v, x-h, y-h, z-h, x+h, y-h, z-h, x+h, y-h, z+h, x-h, y-h, z+h, r*0.5f, g*0.5f, b*0.5f, a, l); 
    }
    
    public static void drawTextureQuad(Matrix4f m, VertexConsumer v, float x, float y, float w, float h, float u1, float v1, float u2, float v2, float r, float g, float b, float a, int l) {
        v.vertex(m, x, y, 0).color(r,g,b,a).texture(u1, v1).light(l); v.vertex(m, x, y+h, 0).color(r,g,b,a).texture(u1, v2).light(l);
        v.vertex(m, x+w, y+h, 0).color(r,g,b,a).texture(u2, v2).light(l); v.vertex(m, x+w, y, 0).color(r,g,b,a).texture(u2, v1).light(l);
    }
    
    public static void drawColorQuad(Matrix4f m, VertexConsumer v, float x, float y, float w, float h, int c, int l) {
        float a=(c>>24&255)/255.0F, r=(c>>16&255)/255.0F, g=(c>>8&255)/255.0F, b=(c&255)/255.0F;
        drawQuad(m, v, x, y, 0, x, y+h, 0, x+w, y+h, 0, x+w, y, 0, r, g, b, a, l);
    }
    
    public static void drawSoulQuad(Matrix4f m, VertexConsumer v, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4, float u1, float v1, float u2, float v2, float r, float g, float b, float a, int l) {
        v.vertex(m, x1, y1, z1).color(r, g, b, a).texture(u1, v1).overlay(net.minecraft.client.render.OverlayTexture.DEFAULT_UV).light(l).normal(0, 0, 1);
        v.vertex(m, x2, y2, z2).color(r, g, b, a).texture(u1, v2).overlay(net.minecraft.client.render.OverlayTexture.DEFAULT_UV).light(l).normal(0, 0, 1);
        v.vertex(m, x3, y3, z3).color(r, g, b, a).texture(u2, v2).overlay(net.minecraft.client.render.OverlayTexture.DEFAULT_UV).light(l).normal(0, 0, 1);
        v.vertex(m, x4, y4, z4).color(r, g, b, a).texture(u2, v1).overlay(net.minecraft.client.render.OverlayTexture.DEFAULT_UV).light(l).normal(0, 0, 1);
    }

    // --- 100% ACCURATE MINECRAFT SKIN UV MAPPER ---
    public static void drawSkinPart(Matrix4f m, VertexConsumer v, float x, float y, float z, float w, float h, float d, int u, int vTex, int tw, int th, int td, float a, int l) {
        float p = 1f/64f; 
        drawSoulQuad(m, v, x, y, z-d, x+w, y, z-d, x+w, y+h, z-d, x, y+h, z-d, (u+td)*p, (vTex+td)*p, (u+td+tw)*p, (vTex+td+th)*p, 1f,1f,1f,a, l); // Front
        drawSoulQuad(m, v, x+w, y, z, x, y, z, x, y+h, z, x+w, y+h, z, (u+td+tw+td)*p, (vTex+td)*p, (u+td+tw+td+tw)*p, (vTex+td+th)*p, 0.8f,0.8f,0.8f,a, l); // Back
        drawSoulQuad(m, v, x, y, z, x, y, z-d, x, y+h, z-d, x, y+h, z, u*p, (vTex+td)*p, (u+td)*p, (vTex+td+th)*p, 0.9f,0.9f,0.9f,a, l); // Right
        drawSoulQuad(m, v, x+w, y, z-d, x+w, y, z, x+w, y+h, z, x+w, y+h, z-d, (u+td+tw)*p, (vTex+td)*p, (u+td+tw+td)*p, (vTex+td+th)*p, 0.9f,0.9f,0.9f,a, l); // Left
        drawSoulQuad(m, v, x, y+h, z, x+w, y+h, z, x+w, y+h, z-d, x, y+h, z-d, (u+td)*p, (vTex)*p, (u+td+tw)*p, (vTex+td)*p, 1.1f,1.1f,1.1f,a, l); // Top
        drawSoulQuad(m, v, x, y, z-d, x+w, y, z-d, x+w, y, z, x, y, z, (u+td+tw)*p, (vTex)*p, (u+td+tw+tw)*p, (vTex+td)*p, 0.5f,0.5f,0.5f,a, l); // Bottom
    }

    public static void drawDoll(MatrixStack matrices, VertexConsumerProvider.Immediate imm, Identifier skin, float alpha, float armPitch, float legPitch, float headPitch, float headYaw) {
        Matrix4f mat = matrices.peek().getPositionMatrix();
        VertexConsumer v = imm.getBuffer(RenderLayer.getEntityTranslucent(skin));
        int l = net.minecraft.client.render.LightmapTextureManager.MAX_LIGHT_COORDINATE;

        // Exact Minecraft Proportions
        matrices.push(); matrices.translate(0, -1.5f, 0); matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(headYaw)); matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(headPitch));
        drawSkinPart(matrices.peek().getPositionMatrix(), v, -0.25f, -0.25f, -0.25f, 0.5f, 0.5f, 0.5f, 0, 0, 8, 8, 8, alpha, l); // Head
        matrices.pop();

        matrices.push(); matrices.translate(0, -0.75f, 0);
        drawSkinPart(matrices.peek().getPositionMatrix(), v, -0.25f, -0.375f, -0.125f, 0.5f, 0.75f, 0.25f, 16, 16, 8, 12, 4, alpha, l); // Body
        matrices.pop();

        matrices.push(); matrices.translate(-0.375f, -1.0f, 0); matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-armPitch));
        drawSkinPart(matrices.peek().getPositionMatrix(), v, -0.125f, -0.375f, -0.125f, 0.25f, 0.75f, 0.25f, 40, 16, 4, 12, 4, alpha, l); // Right Arm
        matrices.pop();

        matrices.push(); matrices.translate(0.375f, -1.0f, 0); matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(armPitch));
        drawSkinPart(matrices.peek().getPositionMatrix(), v, -0.125f, -0.375f, -0.125f, 0.25f, 0.75f, 0.25f, 32, 48, 4, 12, 4, alpha, l); // Left Arm
        matrices.pop();

        matrices.push(); matrices.translate(-0.125f, -0.375f, 0); matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-legPitch));
        drawSkinPart(matrices.peek().getPositionMatrix(), v, -0.125f, -0.375f, -0.125f, 0.25f, 0.75f, 0.25f, 0, 16, 4, 12, 4, alpha, l); // Right Leg
        matrices.pop();

        matrices.push(); matrices.translate(0.125f, -0.375f, 0); matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(legPitch));
        drawSkinPart(matrices.peek().getPositionMatrix(), v, -0.125f, -0.375f, -0.125f, 0.25f, 0.75f, 0.25f, 16, 48, 4, 12, 4, alpha, l); // Left Leg
        matrices.pop();
    }
                                                                                                    }
