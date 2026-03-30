package com.yourname.mtpvp.mixin;

import com.yourname.mtpvp.client.render.HeartIndicatorRenderer;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public class NameTagRendererMixin {
    
    @Inject(
        method = "renderLabel",
        at = @At("HEAD")
    )
    private void renderHeartIndicator(Entity entity, Text text, MatrixStack matrices, 
                                      VertexConsumerProvider vertexConsumers, int light,
                                      CallbackInfo ci) {
        if (entity instanceof LivingEntity living) {
            try {
                HeartIndicatorRenderer.renderIndicator(living, matrices, vertexConsumers, light);
            } catch (Exception e) {
                // Silent fail
            }
        }
    }
}
