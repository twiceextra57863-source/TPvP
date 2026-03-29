package com.mtpvp.renderer;

import com.mtpvp.config.MtpvpConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import org.joml.Matrix4f;

public class HealthRenderer {

    public static void renderIndicator(PlayerEntity player, MatrixStack matrices, VertexConsumerProvider consumers, int light) {
        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;

        // Billboard rotation (taaki indicator hamesha camera ki taraf dekhe)
        matrices.multiply(client.getEntityRenderDispatcher().getRotation());
        matrices.scale(-0.025f, -0.025f, 0.025f);

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        float hp = player.getHealth();
        float maxHp = player.getMaxHealth();

        if (MtpvpConfig.healthMode == 0) { // Bar Style
            renderProBar(consumers, matrix, hp, maxHp);
        } else if (MtpvpConfig.healthMode == 1) { // Hearts Style
            String text = "§c❤ §f" + (int)hp;
            textRenderer.draw(text, -textRenderer.getWidth(text)/2f, 0, 0xFFFFFF, false, matrix, consumers, TextRenderer.TextLayerType.NORMAL, 0, light);
        } else if (MtpvpConfig.healthMode == 2) { // Head + Hits Style
            double damage = client.player.getAttributeValue(EntityAttributes.ATTACK_DAMAGE);
            int hits = (int) Math.ceil(hp / (damage > 0 ? damage : 1));
            String text = "§bHits: §f" + hits;
            textRenderer.draw(text, -textRenderer.getWidth(text)/2f, 0, 0xFFFFFF, false, matrix, consumers, TextRenderer.TextLayerType.NORMAL, 0, light);
        }
    }

    private static void renderProBar(VertexConsumerProvider consumers, Matrix4f matrix, float hp, float maxHp) {
        float width = 40;
        float progress = (hp / maxHp) * width;
        drawBox(consumers, matrix, -width/2 - 1, -1, width/2 + 1, 3, 0xAA000000);
        int color = hp > 10 ? 0xFF00FF00 : (hp > 5 ? 0xFFFFFF00 : 0xFFFF0000);
        drawBox(consumers, matrix, -width/2, 0, -width/2 + progress, 2, color | 0xFF000000);
    }

    private static void drawBox(VertexConsumerProvider consumers, Matrix4f matrix, float x1, float y1, float x2, float y2, int color) {
        VertexConsumer buffer = consumers.getBuffer(RenderLayer.getGui());
        float a = (color >> 24 & 255) / 255f;
        float r = (color >> 16 & 255) / 255f;
        float g = (color >> 8 & 255) / 255f;
        float b = (color & 255) / 255f;
        buffer.vertex(matrix, x1, y1, 0).color(r, g, b, a);
        buffer.vertex(matrix, x1, y2, 0).color(r, g, b, a);
        buffer.vertex(matrix, x2, y2, 0).color(r, g, b, a);
        buffer.vertex(matrix, x2, y1, 0).color(r, g, b, a);
    }
}
