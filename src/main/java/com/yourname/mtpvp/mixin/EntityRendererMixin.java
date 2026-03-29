package com.mtpvp.mixin;

import com.mtpvp.config.MtpvpConfig;
import com.mtpvp.renderer.HealthRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity> {

    @Inject(method = "render", at = @At("RETURN"))
    private void onRender(T entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (entity instanceof PlayerEntity player && MtpvpConfig.enabled) {
            if (!player.isMainPlayer() && player.isAlive() && !player.isInvisible()) {
                HealthRenderer.renderIndicator(player, matrices, vertexConsumers, light);
            }
        }
    }
}
