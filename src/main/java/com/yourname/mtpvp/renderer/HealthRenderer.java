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

        // Scale indicator down for nametag size
        matrices.scale(-0.025f, -0.025f, 0.025f);
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        
        float hp = player.getHealth();
        float maxHp = player.getMaxHealth();

        if (MtpvpConfig.healthMode == 0) { // STYLE: STATUS BAR
            renderStatusBar(consumers, matrix, hp, maxHp);
        } 
        else if (MtpvpConfig.healthMode == 1) { // STYLE: VANILLA HEARTS
            String text = "§c❤ §f" + (int)hp;
            textRenderer.draw(text, -textRenderer.getWidth(text)/2f, 0, 0xFFFFFF, false, matrix, consumers, TextRenderer.TextLayerType.NORMAL, 0, light);
        } 
        else if (MtpvpConfig.healthMode == 2) { // STYLE: HEAD + HITS
            renderHeadAndHits(client, player, textRenderer, matrix, consumers, hp, light);
        }
    }

    private static void renderStatusBar(VertexConsumerProvider consumers, Matrix4f matrix, float hp, float maxHp) {
        float width = 40;
        float progress = (hp / maxHp) * width;
        // Background
        drawBox(consumers, matrix, -width/2 - 1, -1, width/2 + 1, 3, 0xAA000000);
        // Progress Color
        int color = hp > 10 ? 0xFF00FF00 : (hp > 5 ? 0xFFFFFF00 : 0xFFFF0000);
        drawBox(consumers, matrix, -width/2, 0, -width/2 + progress, 2, color | 0xFF000000);
    }

    private static void renderHeadAndHits(MinecraftClient client, PlayerEntity target, TextRenderer tr, Matrix4f matrix, VertexConsumerProvider consumers, float hp, int light) {
        // 1. Draw Player Face Skin
        try {
            Identifier skin = client.getNetworkHandler().getPlayerListEntry(target.getUuid()).getSkinTextures().texture();
            VertexConsumer buffer = consumers.getBuffer(RenderLayer.getEntityCutoutNoCull(skin));
            // Head UV (Front face: 8,8 to 16,16)
            float u1 = 8f/64f, v1 = 8f/64f, u2 = 16f/64f, v2 = 16f/64f;
            buffer.vertex(matrix, -25, -4, 0).color(1f, 1f, 1f, 1f).texture(u1, v1).light(light);
            buffer.vertex(matrix, -25, 6, 0).color(1f, 1f, 1f, 1f).texture(u1, v2).light(light);
            buffer.vertex(matrix, -15, 6, 0).color(1f, 1f, 1f, 1f).texture(u2, v2).light(light);
            buffer.vertex(matrix, -15, -4, 0).color(1f, 1f, 1f, 1f).texture(u2, v1).light(light);
        } catch (Exception ignored) {}

        // 2. Hits to Kill calculation (Based on current weapon)
        double myDamage = client.player.getAttributeValue(EntityAttributes.ATTACK_DAMAGE);
        int hits = (int) Math.ceil(hp / (myDamage > 0 ? myDamage : 1));
        
        String text = "§bHits: §f" + hits;
        tr.draw(text, -10, 0, 0xFFFFFF, false, matrix, consumers, TextRenderer.TextLayerType.NORMAL, 0, light);
    }

    private static void drawBox(VertexConsumerProvider consumers, Matrix4f matrix, float x1, float y1, float x2, float y2, int color) {
        VertexConsumer buffer = consumers.getBuffer(RenderLayer.getGui());
        float a = (color >> 24 & 255) / 255f;
        float r = (color >> 16 & 255) / 255f;
        float g = (color >> 8 & 255) / 255f;
        float b = (color & 255) / 255f;
        buffer.vertex(matrix, x1, y1, 0.01f).color(r, g, b, a);
        buffer.vertex(matrix, x1, y2, 0.01f).color(r, g, b, a);
        buffer.vertex(matrix, x2, y2, 0.01f).color(r, g, b, a);
        buffer.vertex(matrix, x2, y1, 0.01f).color(r, g, b, a);
    }
}
