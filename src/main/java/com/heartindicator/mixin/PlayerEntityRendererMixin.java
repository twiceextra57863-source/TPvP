package com.heartindicator.mixin;

import com.heartindicator.hud.HeartIndicatorHud;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class PlayerEntityRendererMixin {

    @Inject(
        method = "renderLabelIfPresent",
        at = @At("TAIL")
    )
    private void heartindicator$renderHealthAboveTag(
            LivingEntity entity,
            Text text,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light,
            CallbackInfo ci
    ) {
        // Sirf PlayerEntity ke liye render karo
        if (entity instanceof AbstractClientPlayerEntity player) {
            HeartIndicatorHud.renderAbovePlayer(matrices, vertexConsumers, player, light);
        }
    }
}
