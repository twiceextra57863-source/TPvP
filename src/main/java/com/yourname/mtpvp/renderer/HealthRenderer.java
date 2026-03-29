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

        matrices.push();
        matrices.translate(0, player.getHeight() + 0.5f, 0);
        matrices.multiply(client.getEntityRenderDispatcher().getRotation());
        matrices.scale(-0.025f, -0.025f, 0.025f);

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        float hp = player.getHealth();
        float maxHp = player.getMaxHealth();

        if (MtpvpConfig.healthMode == 0) {
            renderProBar(consumers, matrix, hp, maxHp);
        } else if (MtpvpConfig.healthMode == 1) {
            renderHearts(textRenderer, matrix, consumers, hp, light);
        } else if (MtpvpConfig.healthMode == 2) {
            renderHeadAndHits(client, player, textRenderer, matrix, consumers, hp, light);
        }

        matrices.pop();
    }

    private static void renderProBar(VertexConsumerProvider consumers, Matrix4f matrix, float hp, float maxHp) {
        float width = 40;
        float progress = (hp / maxHp) * width;
        drawBox(consumers, matrix, -width/2 - 1, -1, width/2 + 1, 3, 0xAA000000);
        int color = hp > 10 ? 0xFF00FF00 : (hp > 5 ? 0xFFFFFF00 : 0xFFFF0000);
        drawBox(consumers, matrix, -width/2, 0, -width/2 + progress, 2, color | 0xFF000000);
    }

    private static void renderHearts(TextRenderer tr, Matrix4f matrix, VertexConsumerProvider consumers, float hp, int light) {
        String hearts = "§c❤ §f" + (int)hp;
        tr.draw(hearts, -tr.getWidth(hearts)/2f, 0, 0xFFFFFF, false, matrix, consumers, TextRenderer.TextLayerType.NORMAL, 0, light);
    }

    private static void renderHeadAndHits(MinecraftClient client, PlayerEntity target, TextRenderer tr, Matrix4f matrix, VertexConsumerProvider consumers, float hp, int light) {
        // Player Head render (Simplistic UV)
        Identifier skin = client.getNetworkHandler().getPlayerListEntry(target.getUuid()).getSkinTextures().texture();
        VertexConsumer buffer = consumers.getBuffer(RenderLayer.getEntityCutoutNoCull(skin));
        drawBox(consumers, matrix, -22, -2, -12, 8, 0xFFFFFFFF); // Head placeholder logic
        
        // Dynamic Hits calculation (Fixed Attribute for 1.21.4)
        double damage = client.player.getAttributeValue(EntityAttributes.ATTACK_DAMAGE);
        int hits = (int) Math.ceil(hp / (damage > 0 ? damage : 1));
        
        String text = "§bHits: §f" + hits;
        tr.draw(text, -2, 0, 0xFFFFFF, false, matrix, consumers, TextRenderer.TextLayerType.NORMAL, 0, light);
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
