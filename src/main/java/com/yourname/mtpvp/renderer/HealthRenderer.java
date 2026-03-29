package com.mtpvp.renderer;

import com.mtpvp.config.MtpvpConfig;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import org.joml.Matrix4f;

public class HealthRenderer {

    public static void init() {
        WorldRenderEvents.AFTER_ENTITIES.register(HealthRenderer::render);
    }

    private static void render(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || !MtpvpConfig.enabled) return;

        for (AbstractClientPlayerEntity player : client.world.getPlayers()) {
            if (player == client.player || player.isInvisible() || !player.isAlive()) continue;
            if (player.distanceTo(client.player) > 32) continue;

            renderIndicator(context, player);
        }
    }

    private static void renderIndicator(WorldRenderContext context, PlayerEntity player) {
        MatrixStack matrices = context.matrixStack();
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        
        matrices.push();
        
        // Lerp position for smoothness
        double x = player.prevX + (player.getX() - player.prevX) * context.tickCounter().getTickDelta(true) - context.camera().getPos().x;
        // Height = Height of player + 0.8 (perfectly above nametag)
        double y = player.prevY + (player.getY() - player.prevY) * context.tickCounter().getTickDelta(true) - context.camera().getPos().y + player.getHeight() + 0.8;
        double z = player.prevZ + (player.getZ() - player.prevZ) * context.tickCounter().getTickDelta(true) - context.camera().getPos().z;

        matrices.translate(x, y, z);
        matrices.multiply(context.camera().getRotation());
        matrices.scale(-0.025f, -0.025f, 0.025f);

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        float hp = player.getHealth();
        float maxHp = player.getMaxHealth();
        VertexConsumerProvider consumers = context.consumers();

        // --- STYLE SELECTION LOGIC ---
        if (MtpvpConfig.healthMode == 0) { // STYLE 1: Bar
            drawBox(consumers, matrix, -21, -1, 21, 4, 0xAA000000); // BG
            int color = hp > 10 ? 0xFF00FF00 : 0xFFFF0000;
            drawBox(consumers, matrix, -20, 0, -20 + (hp/maxHp * 40), 3, color | 0xFF000000);
            
        } else if (MtpvpConfig.healthMode == 1) { // STYLE 2: Hearts
            String heartIcon = "§c❤ §f" + (int)hp;
            textRenderer.draw(heartIcon, -textRenderer.getWidth(heartIcon)/2f, 0, 0xFFFFFF, false, matrix, consumers, TextRenderer.TextLayerType.NORMAL, 0, 15728880);
            
        } else if (MtpvpConfig.healthMode == 2) { // STYLE 3: Head + Hits
            // Calculating hits (Assumes average hit is 2 damage)
            int hits = (int) Math.ceil(hp / 2.0);
            String text = "§b[Head] §fHits to Kill: §e" + hits;
            textRenderer.draw(text, -textRenderer.getWidth(text)/2f, 0, 0xFFFFFF, false, matrix, consumers, TextRenderer.TextLayerType.NORMAL, 0, 15728880);
        }

        matrices.pop();
    }

    private static void drawBox(VertexConsumerProvider consumers, Matrix4f matrix, float x1, float y1, float x2, float y2, int color) {
        VertexConsumer buffer = consumers.getBuffer(RenderLayer.getGui());
        float a = (float)(color >> 24 & 255) / 255.0F;
        float r = (float)(color >> 16 & 255) / 255.0F;
        float g = (float)(color >> 8 & 255) / 255.0F;
        float b = (float)(color & 255) / 255.0F;
        buffer.vertex(matrix, x1, y1, 0).color(r, g, b, a);
        buffer.vertex(matrix, x1, y2, 0).color(r, g, b, a);
        buffer.vertex(matrix, x2, y2, 0).color(r, g, b, a);
        buffer.vertex(matrix, x2, y1, 0).color(r, g, b, a);
    }
}
