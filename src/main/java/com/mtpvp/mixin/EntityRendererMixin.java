package com.mtpvp.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity> {

    @Inject(method = "render", at = @At("HEAD"))
    private void renderHeartIndicator(T entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (entity instanceof PlayerEntity target && target != MinecraftClient.getInstance().player) {
            PlayerEntity self = MinecraftClient.getInstance().player;
            if (self == null) return;

            // Logic: Calculate hits based on current weapon
            float health = target.getHealth();
            double damage = self.getAttributeValue(net.minecraft.entity.attribute.EntityAttributes.GENERIC_ATTACK_DAMAGE);
            int hitsToKill = (int) Math.ceil(health / (damage <= 0 ? 1 : damage));

            // Rendering setup
            String info = "❤ " + (int)health + " | Hits: " + hitsToKill;
            Text text = Text.literal(info);
            
            matrices.push();
            // Nametag ke thoda upar position set karna
            matrices.translate(0.0D, entity.getHeight() + 0.5D, 0.0D);
            matrices.multiply(MinecraftClient.getInstance().getEntityRenderDispatcher().getRotation());
            matrices.scale(-0.025F, -0.025F, 0.025F);

            Matrix4f matrix4f = matrices.peek().getPositionMatrix();
            TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
            float xOffset = (float)(-textRenderer.getWidth(text) / 2);

            // Render Background (Semi-transparent black)
            textRenderer.draw(text, xOffset, 0, 553648127, false, matrix4f, vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, 1052688, light);
            // Render Foreground (Red for PvP feel)
            textRenderer.draw(text, xOffset, 0, 0xFFFF0000, false, matrix4f, vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, light);

            matrices.pop();
        }
    }
}
