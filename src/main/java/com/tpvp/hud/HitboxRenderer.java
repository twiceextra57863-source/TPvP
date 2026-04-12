package com.tpvp.hud;

import com.tpvp.config.ModConfig;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import org.joml.Matrix4f;

public class HitboxRenderer {
    public static void render(LivingEntity target, MatrixStack matrices, VertexConsumerProvider.Immediate immediate, double x, double y, double z) {
        if (!ModConfig.hitboxEnabled) return;
        
        matrices.push(); 
        matrices.translate(x, y, z); 
        VertexConsumer lb = immediate.getBuffer(RenderLayer.getLines()); 
        Matrix4f m = matrices.peek().getPositionMatrix();
        
        float tw = target.getWidth() / 2.0f;
        float th = target.getHeight();
        
        // FIX: Passed the Ruby Red Color (r,g,b,a) correctly!
        float r = 1f, g = 0f, b = 0.2f, a = 1f; 
        
        RenderUtils3D.drawLine(m, lb, -tw, 0, -tw, tw, 0, -tw, r, g, b, a); 
        RenderUtils3D.drawLine(m, lb, tw, 0, -tw, tw, 0, tw, r, g, b, a); 
        RenderUtils3D.drawLine(m, lb, tw, 0, tw, -tw, 0, tw, r, g, b, a); 
        RenderUtils3D.drawLine(m, lb, -tw, 0, tw, -tw, 0, -tw, r, g, b, a);
        
        RenderUtils3D.drawLine(m, lb, -tw, th, -tw, tw, th, -tw, r, g, b, a); 
        RenderUtils3D.drawLine(m, lb, tw, th, -tw, tw, th, tw, r, g, b, a); 
        RenderUtils3D.drawLine(m, lb, tw, th, tw, -tw, th, tw, r, g, b, a); 
        RenderUtils3D.drawLine(m, lb, -tw, th, tw, -tw, th, -tw, r, g, b, a);
        
        RenderUtils3D.drawLine(m, lb, -tw, 0, -tw, -tw, th, -tw, r, g, b, a); 
        RenderUtils3D.drawLine(m, lb, tw, 0, -tw, tw, th, -tw, r, g, b, a); 
        RenderUtils3D.drawLine(m, lb, tw, 0, tw, tw, th, tw, r, g, b, a); 
        RenderUtils3D.drawLine(m, lb, -tw, 0, tw, -tw, th, tw, r, g, b, a);
        
        matrices.pop();
    }
}
