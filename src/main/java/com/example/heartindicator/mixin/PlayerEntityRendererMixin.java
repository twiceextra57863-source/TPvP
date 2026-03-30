package com.example.heartindicator.mixin;

import com.example.heartindicator.HeartIndicatorMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin {
    private static final Identifier HEART_TEXTURE = Identifier.of(HeartIndicatorMod.MOD_ID, "textures/gui/heart.png");
    private static final int HEART_WIDTH = 9;
    private static final int HEART_HEIGHT = 9;

    @Inject(method = "renderNameTag", at = @At("RETURN"))
    private void renderHeartIndicator(PlayerEntity player, Text displayName, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (!HeartIndicatorMod.getConfig().isEnabled()) return;

        // Only render for other players (optional, you can also render for yourself)
        // if (player == MinecraftClient.getInstance().player) return;

        float health = player.getHealth();
        float maxHealth = player.getMaxHealth();
        int fullHearts = (int) Math.floor(health / 2);
        boolean halfHeart = (health % 2) >= 1; // 1 health = half heart

        // Position above the name tag
        double yOffset = player.getHeight() + 0.5 + 0.3; // 0.5 above entity + 0.3 extra
        matrices.push();
        matrices.translate(0, yOffset, 0);

        // Calculate total width of the heart row
        int totalHearts = (int) Math.ceil(maxHealth / 2);
        int startX = - (totalHearts * (HEART_WIDTH + 1)) / 2; // centered

        // Get a VertexConsumer for the GUI texture (we'll use a simple RenderLayer)
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getText(HEART_TEXTURE));

        for (int i = 0; i < totalHearts; i++) {
            int x = startX + i * (HEART_WIDTH + 1);
            if (i < fullHearts) {
                // Full heart
                drawHeart(matrices, vertexConsumer, x, 0, light, 0, 0, HEART_WIDTH, HEART_HEIGHT);
            } else if (i == fullHearts && halfHeart) {
                // Half heart
                drawHeart(matrices, vertexConsumer, x, 0, light, HEART_WIDTH, 0, HEART_WIDTH, HEART_HEIGHT);
            } else {
                // Empty heart (optional: draw an empty heart texture)
                // You can provide an empty heart texture if you like.
                // Here we skip empty hearts for simplicity.
            }
        }

        matrices.pop();
    }

    private void drawHeart(MatrixStack matrices, VertexConsumer vertexConsumer, int x, int y, int light, int u, int v, int width, int height) {
        MatrixStack.Entry entry = matrices.peek();
        // Render a textured quad
        vertexConsumer.vertex(entry.getPositionMatrix(), x, y + height, 0)
                .color(1f, 1f, 1f, 1f)
                .texture((float) u / HEART_WIDTH, (float) (v + height) / HEART_HEIGHT)
                .light(light)
                .next();
        vertexConsumer.vertex(entry.getPositionMatrix(), x + width, y + height, 0)
                .color(1f, 1f, 1f, 1f)
                .texture((float) (u + width) / HEART_WIDTH, (float) (v + height) / HEART_HEIGHT)
                .light(light)
                .next();
        vertexConsumer.vertex(entry.getPositionMatrix(), x + width, y, 0)
                .color(1f, 1f, 1f, 1f)
                .texture((float) (u + width) / HEART_WIDTH, (float) v / HEART_HEIGHT)
                .light(light)
                .next();
        vertexConsumer.vertex(entry.getPositionMatrix(), x, y, 0)
                .color(1f, 1f, 1f, 1f)
                .texture((float) u / HEART_WIDTH, (float) v / HEART_HEIGHT)
                .light(light)
                .next();
    }
}
