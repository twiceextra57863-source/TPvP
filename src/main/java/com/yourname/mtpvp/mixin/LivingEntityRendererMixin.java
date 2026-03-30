package com.yourname.mtpvp.mixin;

import com.yourname.mtpvp.client.render.HeartIndicatorRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin<T extends LivingEntity> {
    
    @Inject(
        method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
        at = @At("RETURN"),
        remap = true
    )
    private void renderHeartIndicator(T entity, float f, float g, MatrixStack matrixStack, 
                                      VertexConsumerProvider vertexConsumerProvider, int i, 
                                      CallbackInfo ci) {
        try {
            HeartIndicatorRenderer.renderIndicator(entity, matrixStack, vertexConsumerProvider, i);
        } catch (Exception e) {
            // Silent fail
        }
    }
}
