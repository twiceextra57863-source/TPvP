package com.mtpvp.mixin;

import com.mtpvp.accessor.IEntityRenderState;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState> {
    @Inject(method = "updateRenderState", at = @At("RETURN"))
    private void onUpdate(T entity, S state, float tickDelta, CallbackInfo ci) {
        if (state instanceof IEntityRenderState accessor) {
            accessor.mtpvp$setHealth(entity.getHealth());
            accessor.mtpvp$setMaxHealth(entity.getMaxHealth());
        }
    }
}
