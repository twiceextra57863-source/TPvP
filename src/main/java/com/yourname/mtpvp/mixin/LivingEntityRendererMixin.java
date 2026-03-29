package com.mtpvp.mixin;

import com.mtpvp.config.MtpvpConfig;
import com.mtpvp.renderer.HealthRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity> {

    // 1.21.4 ka exact render signature (Entity, Yaw, TickDelta, Matrices, Consumers, Light)
    @Inject(method = "render", at = @At("RETURN"))
    public void onRender(T entity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        if (entity instanceof PlayerEntity player && MtpvpConfig.enabled) {
            // Khud ko mat dikhao aur sirf alive players ko dikhao
            if (!player.isMainPlayer() && player.isAlive() && !player.isInvisible()) {
                matrixStack.push();
                
                // Position adjustment: Player ke head ke upar
                matrixStack.translate(0.0D, player.getHeight() + 0.5D, 0.0D);
                
                // Hamara renderer call karo
                HealthRenderer.renderIndicator(player, matrixStack, vertexConsumerProvider, i);
                
                matrixStack.pop();
            }
        }
    }
}
