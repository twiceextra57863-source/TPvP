package com.mtpvp.renderer;

import com.mtpvp.config.MtpvpConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

public class HealthRenderer {

    public static void renderIndicator(PlayerEntity player, MatrixStack matrices, VertexConsumerProvider consumers, int light) {
        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;

        matrices.push();
        // Player ke nametag ke upar place karna
        matrices.translate(0, player.getHeight() + 0.7f, 0);
        matrices.multiply(client.getEntityRenderDispatcher().getRotation());
        matrices.scale(-0.025f, -0.025f, 0.025f);

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        float hp = player.getHealth();
        float maxHp = player.getMaxHealth();

        if (MtpvpConfig.healthMode == 0) { // STYLE: PRO BAR
            renderProBar(consumers, matrix, hp, maxHp);
        } 
        else if (MtpvpConfig.healthMode == 1) { // STYLE: VANILLA HEARTS
            renderHearts(textRenderer, matrix, consumers, hp, light);
        } 
        else if (MtpvpConfig.healthMode == 2) { // STYLE: HEAD + DYNAMIC HITS
            renderHeadAndHits(client, player, textRenderer, matrix, consumers, hp, light);
        }

        matrices.pop();
    }

    private static void renderProBar(VertexConsumerProvider consumers, Matrix4f matrix, float hp, float maxHp) {
        float width = 40;
        float progress = (hp / maxHp) * width;
        drawBox(consumers, matrix, -width/2 - 1, -1, width/2 + 1, 3, 0xAA000000); // BG
        int color = hp > 10 ? 0xFF00FF00 : (hp > 5 ? 0xFFFFFF00 : 0xFFFF0000); // Green -> Yellow -> Red
        drawBox(consumers, matrix, -width/2, 0, -width/2 + progress, 2, color | 0xFF000000);
    }

    private static void renderHearts(TextRenderer tr, Matrix4f matrix, VertexConsumerProvider consumers, float hp, int light) {
        String hearts = "§c❤ ".repeat((int)Math.ceil(hp/2f));
        if (hearts.length() > 20) hearts = "§c❤ §f" + (int)hp; // Long health fallback
        tr.draw(hearts, -tr.getWidth(hearts)/2f, 0, 0xFFFFFF, false, matrix, consumers, TextRenderer.TextLayerType.NORMAL, 0, light);
    }

    private static void renderHeadAndHits(MinecraftClient client, PlayerEntity target, TextRenderer tr, Matrix4f matrix, VertexConsumerProvider consumers, float hp, int light) {
        // 1. Draw Player Head (Texture)
        Identifier skin = client.getNetworkHandler().getPlayerListEntry(target.getUuid()).getSkinTextures().texture();
        VertexConsumer buffer = consumers.getBuffer(RenderLayer.getEntityCutoutNoCull(skin));
        
        // Head rendering (UV coordinates for face)
        float s = 8f;
        drawTexture(buffer, matrix, -20, -4, -12, 4, 8, 8, 8, 8, 64, 64);

        // 2. Calculate Hits to Kill based on CURRENT weapon
        double damage = client.player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        int hits = (int) Math.ceil(hp / (damage > 0 ? damage : 1));
        
        String text = "§b" + hits + " Hits";
        tr.draw(text, -2, -2, 0xFFFFFF, false, matrix, consumers, TextRenderer.TextLayerType.NORMAL, 0, light);
    }

    // Helper methods for drawing
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

    private static void drawTexture(VertexConsumer buffer, Matrix4f matrix, float x1, float y1, float x2, float y2, float u1, float v1, float u2, float v2, int texW, int texH) {
        buffer.vertex(matrix, x1, y1, 0).color(1f, 1f, 1f, 1f).texture(u1/texW, v1/texH).light(15728880);
        buffer.vertex(matrix, x1, y2, 0).color(1f, 1f, 1f, 1f).texture(u1/texW, v2/texH).light(15728880);
        buffer.vertex(matrix, x2, y2, 0).color(1f, 1f, 1f, 1f).texture(u2/texW, v2/texH).light(15728880);
        buffer.vertex(matrix, x2, y1, 0).color(1f, 1f, 1f, 1f).texture(u2/texW, v1/texH).light(15728880);
    }
}
