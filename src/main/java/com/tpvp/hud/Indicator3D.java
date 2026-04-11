package com.tpvp.hud;

import com.tpvp.config.ModConfig;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class Indicator3D {
    public static void register() {
        WorldRenderEvents.LAST.register(Indicator3D::onWorldRender);
    }

    private static void onWorldRender(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        Camera camera = context.camera();
        Vec3d camPos = camera.getPos();
        float tickDelta = context.tickCounter().getTickDelta(true);
        MatrixStack matrices = context.matrixStack();
        VertexConsumerProvider.Immediate immediate = client.getBufferBuilders().getEntityVertexConsumers();

        for (Entity target : client.world.getEntities()) {
            // STRICT FILTER: Sirf asli players, NPCs nahi. (ArmorStands ignore honge)
            if (!(target instanceof PlayerEntity) || target == client.player || target.isInvisible()) continue;
            if (target.distanceTo(client.player) > 64.0) continue;

            Vec3d tPos = target.getLerpedPos(tickDelta);
            double x = tPos.x - camPos.x;
            double y = tPos.y - camPos.y;
            double z = tPos.z - camPos.z;

            // --- TARGET ARROW (Bouncing & Rotating) ---
            if (target.getName().getString().equals(ModConfig.taggedPlayerName)) {
                matrices.push();
                // Bouncing math
                double bounce = Math.sin(System.currentTimeMillis() / 150.0) * 0.15;
                matrices.translate(x, y + target.getHeight() + 1.2 + bounce, z);
                
                // Rotating math
                float rot = (System.currentTimeMillis() % 3600) / 10.0f;
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rot));
                
                matrices.scale(0.3f, 0.3f, 0.3f);
                Matrix4f mat = matrices.peek().getPositionMatrix();
                VertexConsumer triBuffer = immediate.getBuffer(RenderLayer.getGui()); // Solid color layer
                
                // Draw 3D Triangle pointing down (Red Color)
                float r=1f, g=0.2f, b=0.2f, a=1f;
                triBuffer.vertex(mat, 0, -1, 0).color(r,g,b,a).light(15728880);
                triBuffer.vertex(mat, -0.5f, 0, -0.5f).color(r,g,b,a).light(15728880);
                triBuffer.vertex(mat, 0.5f, 0, -0.5f).color(r,g,b,a).light(15728880);

                triBuffer.vertex(mat, 0, -1, 0).color(r,g,b,a).light(15728880);
                triBuffer.vertex(mat, 0.5f, 0, 0.5f).color(r,g,b,a).light(15728880);
                triBuffer.vertex(mat, -0.5f, 0, 0.5f).color(r,g,b,a).light(15728880);
                matrices.pop();
            }

            // --- STANDARD HITBOX & INDICATOR LOGIC HERE (Same as previous) ---
            if (ModConfig.indicatorEnabled && target.distanceTo(client.player) < 32.0) {
                matrices.push();
                matrices.translate(x, y + target.getHeight() + 0.8, z);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
                matrices.scale(-0.025F, -0.025F, 0.025F);

                Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
                int light = LightmapTextureManager.MAX_LIGHT_COORDINATE;
                String text = target.getName().getString();
                
                client.textRenderer.draw(text, -client.textRenderer.getWidth(text) / 2f, 0, 0xFFFFFF, false, positionMatrix, immediate, TextRenderer.TextLayerType.SEE_THROUGH, 0x40000000, light);
                matrices.pop();
            }
        }
        immediate.draw();
    }
}
