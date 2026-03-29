package com.mtpvp.renderer;

import com.mtpvp.config.MtpvpConfig;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
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
            if (player == cameraPlayer || player.isInvisible()) continue;
            if (player.distanceTo(cameraPlayer) > 32) continue; // Range limit

            renderIndicator(context, player);
        }
    }

    private static void renderIndicator(WorldRenderContext context, PlayerEntity player) {
        MatrixStack matrices = context.matrixStack();
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        
        matrices.push();
        
        // Position it above the nametag
        double x = player.prevX + (player.getX() - player.prevX) * context.tickCounter().getTickDelta(true) - context.camera().getPos().x;
        double y = player.prevY + (player.getY() - player.prevY) * context.tickCounter().getTickDelta(true) - context.camera().getPos().y + player.getNameLabelHeight() + 0.5;
        double z = player.prevZ + (player.getZ() - player.prevZ) * context.tickCounter().getTickDelta(true) - context.camera().getPos().z;

        matrices.translate(x, y, z);
        matrices.multiply(context.camera().getRotation()); // Billboard effect
        matrices.scale(-0.025f, -0.025f, 0.025f);

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        float health = player.getHealth();
        float maxHealth = player.getMaxHealth();

        if (MtpvpConfig.healthMode == 0) { // MODE 0: Progress Bar
            renderBar(context, matrix, health, maxHealth);
        } else if (MtpvpConfig.healthMode == 1) { // MODE 1: Vanilla Hearts
            String hearts = "❤".repeat((int) (health / 2));
            textRenderer.draw(hearts, -textRenderer.getWidth(hearts) / 2f, 0, 0xFF0000, false, matrix, context.consumers(), TextRenderer.TextLayerType.NORMAL, 0, 15728880);
        } else if (MtpvpConfig.healthMode == 2) { // MODE 2: Head + Hits
            int hits = (int) Math.ceil(health / 1.5); // Example: 1.5 damage per hit
            String text = "Hits: " + hits;
            textRenderer.draw(text, -textRenderer.getWidth(text) / 2f, 0, 0xFFFF00, false, matrix, context.consumers(), TextRenderer.TextLayerType.NORMAL, 0, 15728880);
        }

        matrices.pop();
    }

    private static void renderBar(WorldRenderContext context, Matrix4f matrix, float health, float maxHealth) {
        float width = 40;
        float healthWidth = (health / maxHealth) * width;
        // Background
        fill(context, matrix, -width/2, 0, width/2, 3, 0xAA000000);
        // Health bar
        fill(context, matrix, -width/2, 0, -width/2 + healthWidth, 3, 0xFF00FF00);
    }

    private static void fill(WorldRenderContext context, Matrix4f matrix, float x1, float y1, float x2, float y2, int color) {
        // Simple fill logic using VertexConsumer
    }
}
