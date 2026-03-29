package com.mtpvp.renderer;

import com.mtpvp.config.MtpvpConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import org.joml.Matrix4f;

public class HealthRenderer {

    // Isme initialize karne ki ab zarurat nahi hai kyunki Mixin directly call kar raha hai
    public static void init() { }

    public static void renderIndicatorInsideMixin(PlayerEntity player, MatrixStack matrices, VertexConsumerProvider consumers, int light) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        
        float hp = player.getHealth();
        float maxHp = player.getMaxHealth();
        
        // --- STYLE 0: Progress Bar ---
        if (MtpvpConfig.healthMode == 0) {
            float width = 40f;
            float healthWidth = (hp / maxHp) * width;
            
            // Background Bar
            drawBox(consumers, matrix, -width/2 - 1, -1, width/2 + 1, 3, 0xAA000000);
            // Dynamic Color (Health ke hisaab se color change)
            int color = hp > 10 ? 0xFF00FF00 : (hp > 5 ? 0xFFFFFF00 : 0xFFFF0000);
            drawBox(consumers, matrix, -width/2, 0, -width/2 + healthWidth, 2, color | 0xFF000000);
        } 
        // --- STYLE 1: Vanilla Hearts ---
        else if (MtpvpConfig.healthMode == 1) {
            String heartText = "§c❤ §f" + (int)hp;
            float xOffset = -textRenderer.getWidth(heartText) / 2f;
            textRenderer.draw(heartText, xOffset, 0, 0xFFFFFF, false, matrix, consumers, TextRenderer.TextLayerType.NORMAL, 0, light);
        } 
        // --- STYLE 2: Head + Hits ---
        else if (MtpvpConfig.healthMode == 2) {
            int hitsToKill = (int) Math.ceil(hp / 2.0); // Assume 2 damage per hit
            String text = "§bHits: §f" + hitsToKill;
            float xOffset = -textRenderer.getWidth(text) / 2f;
            textRenderer.draw(text, xOffset, 0, 0xFFFFFF, false, matrix, consumers, TextRenderer.TextLayerType.NORMAL, 0, light);
        }
    }

    private static void drawBox(VertexConsumerProvider consumers, Matrix4f matrix, float x1, float y1, float x2, float y2, int color) {
        VertexConsumer buffer = consumers.getBuffer(RenderLayer.getGui());
        float a = (float)(color >> 24 & 255) / 255.0F;
        float r = (float)(color >> 16 & 255) / 255.0F;
        float g = (float)(color >> 8 & 255) / 255.0F;
        float b = (float)(color & 255) / 255.0F;
        buffer.vertex(matrix, x1, y1, 0.001f).color(r, g, b, a); // 0.001f to prevent Z-fighting
        buffer.vertex(matrix, x1, y2, 0.001f).color(r, g, b, a);
        buffer.vertex(matrix, x2, y2, 0.001f).color(r, g, b, a);
        buffer.vertex(matrix, x2, y1, 0.001f).color(r, g, b, a);
    }
}
