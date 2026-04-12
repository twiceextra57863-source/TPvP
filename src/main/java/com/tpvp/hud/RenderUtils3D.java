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
        drawQuad(m, v, x-h, y-h, z-h, x+h, y-h, z-h, x+h, y+h, z-h, x-h, y+h, z-h, r, g, b, a, l); // Front
        drawQuad(m, v, x-h, y-h, z+h, x+h, y-h, z+h, x+h, y+h, z+h, x-h, y+h, z+h, r*0.8f, g*0.8f, b*0.8f, a, l); // Back
        drawQuad(m, v, x-h, y-h, z-h, x-h, y-h, z+h, x-h, y+h, z+h, x-h, y+h, z-h, r*0.9f, g*0.9f, b*0.9f, a, l); // Left
        drawQuad(m, v, x+h, y-h, z-h, x+h, y-h, z+h, x+h, y+h, z+h, x+h, y+h, z-h, r*0.9f, g*0.9f, b*0.9f, a, l); // Right
        drawQuad(m, v, x-h, y+h, z-h, x+h, y+h, z-h, x+h, y+h, z+h, x-h, y+h, z+h, r*1.2f, g*1.2f, b*1.2f, a, l); // Top
        drawQuad(m, v, x-h, y-h, z-h, x+h, y-h, z-h, x+h, y-h, z+h, x-h, y-h, z+h, r*0.5f, g*0.5f, b*0.5f, a, l); // Bottom
    }

    // FIX 1: drawTextureQuad (15 Parameters fixed with exact Float/Int mappings)
    public static void drawTextureQuad(Matrix4f m, VertexConsumer v, float x, float y, float w, float h, float u1, float v1, float u2, float v2, float r, float g, float b, float a, int l) {
        v.vertex(m, x, y, 0).color(r,g,b,a).texture(u1, v1).light(l); 
        v.vertex(m, x, y+h, 0).color(r,g,b,a).texture(u1, v2).light(l);
        v.vertex(m, x+w, y+h, 0).color(r,g,b,a).texture(u2, v2).light(l); 
        v.vertex(m, x+w, y, 0).color(r,g,b,a).texture(u2, v1).light(l);
    }
    
    // FIX 2: drawColorQuad (8 Parameters fixed)
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

    public static void drawDoll(MatrixStack matrices, VertexConsumerProvider.Immediate imm, Identifier skin, float alpha, float armPitch, float legPitch, float headPitch, float headYaw) {
        Matrix4f mat = matrices.peek().getPositionMatrix();
        VertexConsumer v = imm.getBuffer(RenderLayer.getEntityTranslucent(skin));
        int l = net.minecraft.client.render.LightmapTextureManager.MAX_LIGHT_COORDINATE;
        float p = 1f/64f; 

        matrices.push(); matrices.translate(0, -1.5f, 0); 
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(headYaw)); matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(headPitch));
        drawSkinBox(matrices.peek().getPositionMatrix(), v, -0.25f, -0.25f, -0.25f, 0.5f, 0.5f, 0.5f, 8, 8, 1f, alpha, l);
        matrices.pop();

        matrices.push(); matrices.translate(0, -0.75f, 0);
        drawSkinBox(matrices.peek().getPositionMatrix(), v, -0.25f, -0.375f, -0.125f, 0.5f, 0.75f, 0.25f, 20, 20, 1f, alpha, l);
        matrices.pop();

        matrices.push(); matrices.translate(-0.375f, -1.0f, 0); matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-armPitch));
        drawSkinBox(matrices.peek().getPositionMatrix(), v, -0.125f, 0, -0.125f, 0.25f, 0.75f, 0.25f, 44, 20, 1f, alpha, l);
        matrices.pop();

        matrices.push(); matrices.translate(0.375f, -1.0f, 0); matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(armPitch));
        drawSkinBox(matrices.peek().getPositionMatrix(), v, -0.125f, 0, -0.125f, 0.25f, 0.75f, 0.25f, 36, 52, 1f, alpha, l);
        matrices.pop();

        matrices.push(); matrices.translate(-0.125f, -0.375f, 0); matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-legPitch));
        drawSkinBox(matrices.peek().getPositionMatrix(), v, -0.125f, 0, -0.125f, 0.25f, 0.75f, 0.25f, 0, 20, 1f, alpha, l);
        matrices.pop();

        matrices.push(); matrices.translate(0.125f, -0.375f, 0); matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(legPitch));
        drawSkinBox(matrices.peek().getPositionMatrix(), v, -0.125f, 0, -0.125f, 0.25f, 0.75f, 0.25f, 16, 52, 1f, alpha, l);
        matrices.pop();
    }

    private static void drawSkinBox(Matrix4f m, VertexConsumer v, float x, float y, float z, float w, float h, float d, int texU, int texV, float c, float a, int l) {
        float p = 1f/64f; 
        drawSoulQuad(m, v, x, y, z-d, x+w, y, z-d, x+w, y+h, z-d, x, y+h, z-d, (texU+4)*p, (texV+4)*p, (texU+8)*p, (texV+16)*p, c,c,c,a, l);
        drawSoulQuad(m, v, x+w, y, z, x, y, z, x, y+h, z, x+w, y+h, z, (texU+12)*p, (texV+4)*p, (texU+16)*p, (texV+16)*p, c*0.8f,c*0.8f,c*0.8f,a, l);
        drawSoulQuad(m, v, x, y, z, x, y, z-d, x, y+h, z-d, x, y+h, z, texU*p, (texV+4)*p, (texU+4)*p, (texV+16)*p, c*0.9f,c*0.9f,c*0.9f,a, l);
        drawSoulQuad(m, v, x+w, y, z-d, x+w, y, z, x+w, y+h, z, x+w, y+h, z-d, (texU+8)*p, (texV+4)*p, (texU+12)*p, (texV+16)*p, c*0.9f,c*0.9f,c*0.9f,a, l);
    }
}
