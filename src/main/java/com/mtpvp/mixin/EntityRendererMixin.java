package com.mtpvp.mixin;

import com.mtpvp.gui.MtpvpDashboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class EntityRendererMixin {

    @Inject(method = "render", at = @At("RETURN"))
    private void onRenderWorld(RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f positionMatrix, Matrix4f projectionMatrix, CallbackInfo ci) {
        if (!MtpvpDashboard.heartEnabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) return;

        TextRenderer tr = client.textRenderer;
        MatrixStack matrices = new MatrixStack();
        VertexConsumerProvider.Immediate immediate = client.getBufferBuilders().getEntityVertexConsumers();

        for (PlayerEntity target : client.world.getPlayers()) {
            // Apne upar mat dikhao, aur invisible/dead players par bhi nahi
            if (target == client.player || target.isInvisible() || !target.isAlive()) continue;

            // Distance check (32 blocks tak dikhega)
            double dist = client.cameraEntity.squaredDistanceTo(target);
            if (dist > 1024) continue;

            float hp = target.getHealth();
            String info = switch (MtpvpDashboard.styleIndex) {
                case 1 -> "Hits: " + (int) Math.ceil(hp / 3.5f);
                case 2 -> (int)hp + " HP | " + target.getName().getString();
                default -> "❤ " + (int)hp;
            };

            int color = (hp > 10) ? 0x55FF55 : 0xFF5555; // 10 HP se upar green, neeche red

            matrices.push();
            // Player ki exact position calculation
            double x = target.prevX + (target.getX() - target.prevX) * tickCounter.getTickDelta(true) - client.getEntityRenderDispatcher().camera.getPos().x;
            double y = target.prevY + (target.getY() - target.prevY) * tickCounter.getTickDelta(true) - client.getEntityRenderDispatcher().camera.getPos().y;
            double z = target.prevZ + (target.getZ() - target.prevZ) * tickCounter.getTickDelta(true) - client.getEntityRenderDispatcher().camera.getPos().z;

            matrices.translate(x, y + target.getHeight() + 0.5, z);
            matrices.multiply(client.getEntityRenderDispatcher().getRotation());
            matrices.scale(-0.025f, -0.025f, 0.025f);

            Matrix4f matrix = matrices.peek().getPositionMatrix();
            float xOffset = (float) (-tr.getWidth(info) / 2);

            // Text Draw
            tr.draw(info, xOffset, 0, color, false, matrix, immediate, TextRenderer.TextLayerType.SEE_THROUGH, 0, 15728880);
            
            matrices.pop();
        }
        immediate.draw();
    }
}
