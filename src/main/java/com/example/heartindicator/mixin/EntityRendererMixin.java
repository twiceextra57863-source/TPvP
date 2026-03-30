package com.example.heartindicator.mixin;

import com.example.heartindicator.client.HeartIndicatorClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public class EntityRendererMixin<T extends LivingEntity> {
    @Inject(method = "renderLabelIfPresent", at = @At("HEAD"))
    private void renderHeartIndicator(T entity, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        // Only for players and if the mod is enabled
        if (!HeartIndicatorClient.isEnabled()) return;
        if (!(entity instanceof PlayerEntity player)) return;

        // Get current health (as integer, can also show float)
        float health = player.getHealth();
        float maxHealth = player.getMaxHealth();
        int hearts = (int) Math.ceil(health / 2); // each heart is 2 health

        // Build text: e.g., "❤ 10"
        Text healthText = Text.literal("❤ " + hearts);

        // Render the health text slightly above the name tag
        matrices.push();
        matrices.translate(0, -0.3f, 0); // shift up above the nametag
        TextRenderer textRenderer = net.minecraft.client.MinecraftClient.getInstance().textRenderer;
        float scale = 0.025f;
        matrices.scale(scale, scale, scale);
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        int width = textRenderer.getWidth(healthText) / 2;
        textRenderer.draw(healthText, -width, 0, 0xFFFFFF, true, matrix, vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, light);
        matrices.pop();
    }
}
