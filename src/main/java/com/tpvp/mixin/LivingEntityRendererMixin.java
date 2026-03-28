package com.tpvp.mixin;

import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
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
        EntityRenderStateMixin stateAccess = (EntityRenderStateMixin) (Object) state;
        stateAccess.tpvp$health = entity.getHealth();
        stateAccess.tpvp$maxHealth = entity.getMaxHealth();
        
        var player = MinecraftClient.getInstance().player;
        if (player != null) {
            stateAccess.tpvp$attackDamage = player.getAttributeValue(EntityAttributes.ATTACK_DAMAGE);
        }
    }
}
