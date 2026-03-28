package com.tpvp.mixin;

import com.tpvp.accessor.IEntityRenderState; // Import naya package
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState> {
    
    @Inject(method = "updateRenderState", at = @At("TAIL"))
    private void onUpdateState(T entity, S state, float tickDelta, CallbackInfo ci) {
        if (state instanceof IEntityRenderState access) {
            access.tpvp$setHealth(entity.getHealth());
            access.tpvp$setMaxHealth(entity.getMaxHealth());
            
            var player = MinecraftClient.getInstance().player;
            if (player != null) {
                access.tpvp$setAttackDamage(player.getAttributeValue(EntityAttributes.ATTACK_DAMAGE));
            }
        }
    }
}
