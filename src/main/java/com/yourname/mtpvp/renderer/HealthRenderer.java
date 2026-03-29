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
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

public class HealthRenderer {

    public static void renderIndicator(PlayerEntity player, MatrixStack matrices, VertexConsumerProvider consumers, int light) {
        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;

        matrices.multiply(client.getEntityRenderDispatcher().getRotation());
        matrices.scale(-0.025f, -0.025f, 0.025f);

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        float hp = player.getHealth();
        float maxHp = player.getMaxHealth();

        if (MtpvpConfig.healthMode == 0) { // STYLE: Progress Bar
            float width = 40;
            float progress = (hp / maxHp) * width;
            drawBox(consumers, matrix, -width/2 - 1, -1, width/2 + 1, 3, 0xAA000000);
            int color = hp > 10 ? 0xFF00FF00 : (hp > 5 ? 0xFFFFFF00 : 0xFFFF0000);
            drawBox(consumers, matrix, -width/2, 0, -width/2 + progress, 2, color | 0xFF000000);
            
        } else if (MtpvpConfig.healthMode == 1) { // STYLE: Vanilla Hearts
            String text = "§c❤ §f" + (int)hp;
            textRenderer.draw(text, -textRenderer.getWidth(text)/2f, 0, 0xFFFFFF, false, matrix, consumers, TextRenderer.TextLayerType.NORMAL, 0, light);
            
        } else if (MtpvpConfig.healthMode == 2) { // STYLE: Head + Hits
            // Render Head
            Identifier skin = client.getNetworkHandler().getPlayerListEntry(player.getUuid()).getSkinTextures().texture();
            VertexConsumer buffer = consumers.getBuffer(RenderLayer.getEntityCutoutNoCull(skin));
            // Head UV (8, 8 to 16, 16)
            drawTexturedHead(buffer, matrix, -20, -4, -12, 4);

            // Dynamic Hits to Kill
            double attackDamage = client.player.getAttributeValue(EntityAttributes.ATTACK_DAMAGE);
            int hits = (int) Math.ceil(hp / (attackDamage > 0 ? attackDamage : 1));
            String text = "§bHits: §f" + hits;
            textRenderer.draw(text, -2, 0, 0xFFFFFF, false, matrix, consumers, TextRenderer.TextLayerType.NORMAL, 0, light);
        }
    }

    private static void drawTexturedHead(VertexConsumer buffer, Matrix4f matrix, float x1, float y1, float x2, float y2) {
        float u1 = 8.0f/64.0f, v1 = 8.0f/64.0f;
        float u2 = 16.0f/64.0f, v2 = 16.0f/64.0f;
        buffer.vertex(matrix, x1, y1, 0).color(1f,1f,1f,1f).texture(u1, v1).light(15728880);
        buffer.vertex(matrix, x1, y2, 0).color(1f,1f,1f,1f).texture(u1, v2).light(15728880);
        buffer.vertex(matrix, x2, y2, 0).color(1f,1f,1f,1f).texture(u2, v2).light(15728880);
        buffer.vertex(matrix, x2, y1, 0).color(1f,1f,1f,1f).texture(u2, v1).light(15728880);
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
