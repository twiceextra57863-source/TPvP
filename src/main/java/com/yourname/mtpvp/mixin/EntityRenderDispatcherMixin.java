package com.mtpvp.mixin;

import com.mtpvp.config.MtpvpConfig;
import com.mtpvp.renderer.HealthRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {

    // 1.21.4 mein 'tickDelta' parameter nahi hota, isliye use hata diya gaya hai
    @Inject(method = "render", at = @At("RETURN"))
    private <E extends Entity> void onRender(E entity, double x, double y, double z, float yaw, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (entity instanceof PlayerEntity player && MtpvpConfig.enabled) {
            // Khud ko indicator mat dikhao aur sirf alive players ko dikhao
            if (!player.isMainPlayer() && player.isAlive() && !player.isInvisible()) {
                matrices.push();
                // Position adjustment above head
                matrices.translate(x, y + player.getHeight() + 0.75, z);
                
                HealthRenderer.renderIndicator(player, matrices, vertexConsumers, light);
                
                matrices.pop();
            }
        }
    }
}
