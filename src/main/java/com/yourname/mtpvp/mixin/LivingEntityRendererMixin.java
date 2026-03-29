package com.mtpvp.mixin;

import com.mtpvp.config.MtpvpConfig;
import com.mtpvp.renderer.HealthRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity> {

    @Inject(method = "renderLabelIfPresent", at = @At("HEAD"))
    protected void onRenderLabel(T entity, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        // Sirf doosre players ke liye render karo
        if (entity instanceof PlayerEntity player && MtpvpConfig.enabled && !player.isMainPlayer()) {
            matrices.push();
            // Nametag ke upar shift karna
            matrices.translate(0.0D, 0.35D, 0.0D);
            
            // Health indicator render call
            HealthRenderer.renderIndicator(player, matrices, vertexConsumers, light);
            
            matrices.pop();
        }
    }
}
