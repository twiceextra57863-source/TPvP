package com.mtpvp.renderer;

import com.mtpvp.config.MtpvpConfig;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.joml.Matrix4f;

public class HealthRenderer {

    public static void init() {
        WorldRenderEvents.AFTER_ENTITIES.register(HealthRenderer::render);
    }

    private static void render(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity cameraPlayer = client.player;
        if (cameraPlayer == null || !MtpvpConfig.enabled) return;

        for (AbstractClientPlayerEntity player : client.world.getPlayers()) {
            // Khud ko indicator mat dikhao aur dead players ko mat dikhao
            if (player == cameraPlayer || player.isInvisible() || !player.isAlive()) continue;
            
            // 32 blocks ki range
            if (player.distanceTo(cameraPlayer) > 32) continue;

            renderIndicator(context, player);
        }
    }

    private static void renderIndicator(WorldRenderContext context, PlayerEntity player) {
        MatrixStack matrices = context.matrixStack();
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        float tickDelta = context.tickCounter().getTickDelta(true);
        
        matrices.push();
        
        // Position interpolation (smooth movement ke liye)
        double x = player.prevX + (player.getX() - player.prevX) * tickDelta - context.camera().getPos().x;
        // player.getHeight() + 0.5f ka use kiya hai getNameLabelHeight ki jagah
        double y = player.prevY + (player.getY() - player.prevY) * tickDelta - context.camera().getPos().y + player.getHeight() + 0.5;
        double z = player.prevZ + (player.getZ() - player.prevZ) * tickDelta - context.camera().getPos().z;

        matrices.translate(x, y, z);
        matrices.multiply(context.camera().getRotation()); // Billboard effect (hamesha player ki taraf face karega)
        matrices.scale(-0.025f, -0.025f, 0.025f);

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        float health = player.getHealth();
        float maxHealth = player.getMaxHealth();
        VertexConsumerProvider consumers = context.consumers();

        if (MtpvpConfig.healthMode == 0) { // MODE 0: Progress Bar
            renderBar(consumers, matrix, health, maxHealth);
        } else if (MtpvpConfig.healthMode == 1) { // MODE 1: Vanilla Hearts
            String hearts = "❤".repeat(Math.max(0, (int) (health / 2)));
            textRenderer.draw(hearts, -textRenderer.getWidth(hearts) / 2f, 0, 0xFF0000, false, matrix, consumers, TextRenderer.TextLayerType.NORMAL, 0, 15728880);
        } else if (MtpvpConfig.healthMode == 2) { // MODE 2: Head + Hits
            // Sharpness 5 Diamond Sword average damage approx 1.5 - 2 hearts per hit maan kar
            int hits = (int) Math.ceil(health / 2.0); 
            String text = "Hits: " + hits;
            textRenderer.draw(text, -textRenderer.getWidth(text) / 2f, 0, 0xFFFF00, false, matrix, consumers, TextRenderer.TextLayerType.NORMAL, 0, 15728880);
        }

        matrices.pop();
    }

    private static void renderBar(VertexConsumerProvider consumers, Matrix4f matrix, float health, float maxHealth) {
        float width = 40;
        float healthWidth = (health / maxHealth) * width;
        
        // Background Bar (Black)
        drawBox(consumers, matrix, -width/2 - 1, -1, width/2 + 1, 4, 0xAA000000);
        // Health Bar (Green/Red based on health)
        int color = health > 10 ? 0xFF00FF00 : 0xFFFF0000; // Green if > 5 hearts, else Red
        drawBox(consumers, matrix, -width/2, 0, -width/2 + healthWidth, 3, color | 0xFF000000);
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
