package com.tpvp.hud;

import net.minecraft.client.render.VertexConsumer;
import org.joml.Matrix4f;

public class RenderUtils3D {
    public static void drawLine(Matrix4f m, VertexConsumer v, float x1, float y1, float z1, float x2, float y2, float z2) {
        v.vertex(m, x1, y1, z1).color(1f, 0f, 0.2f, 1f).normal(x2-x1, y2-y1, z2-z1);
        v.vertex(m, x2, y2, z2).color(1f, 0f, 0.2f, 1f).normal(x2-x1, y2-y1, z2-z1);
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
        drawQuad(m, v, x-h, y+h, z-h, x+h, y+h, z-h, x+h, y+h, z+h, x-h, y+h, z+h, r*1.2f, g, b, a, l); 
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
    
    // FIX: Restored full 23 parameters to fix length error!
    public static void drawSoulQuad(Matrix4f m, VertexConsumer v, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4, float u1, float v1, float u2, float v2, float r, float g, float b, float a, int l) {
        v.vertex(m, x1, y1, z1).color(r, g, b, a).texture(u1, v1).overlay(net.minecraft.client.render.OverlayTexture.DEFAULT_UV).light(l).normal(0, 0, 1);
        v.vertex(m, x2, y2, z2).color(r, g, b, a).texture(u1, v2).overlay(net.minecraft.client.render.OverlayTexture.DEFAULT_UV).light(l).normal(0, 0, 1);
        v.vertex(m, x3, y3, z3).color(r, g, b, a).texture(u2, v2).overlay(net.minecraft.client.render.OverlayTexture.DEFAULT_UV).light(l).normal(0, 0, 1);
        v.vertex(m, x4, y4, z4).color(r, g, b, a).texture(u2, v1).overlay(net.minecraft.client.render.OverlayTexture.DEFAULT_UV).light(l).normal(0, 0, 1);
    }
}
