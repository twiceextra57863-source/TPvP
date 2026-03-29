package com.mtpvp.mixin;

import com.mtpvp.config.MtpvpConfig;
import com.mtpvp.renderer.HealthRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity> {

    @Inject(method = "renderLabelIfPresent", at = @At("HEAD"))
    private void onRenderLabel(T entity, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, float tickDelta, CallbackInfo ci) {
        // Sirf players ke liye render karo, aur khud ke liye nahi
        if (entity instanceof PlayerEntity player && MtpvpConfig.enabled) {
            if (!player.isMainPlayer()) {
                matrices.push();
                
                // Nametag ke upar shift karne ke liye (y coordinate adjust)
                // 0.3f ka matlab hai nametag ke thoda upar
                matrices.translate(0.0D, 0.3D, 0.0D);
                
                // Hamara custom renderer call karo
                HealthRenderer.renderIndicatorInsideMixin(player, matrices, vertexConsumers, light);
                
                matrices.pop();
            }
        }
    }
}
