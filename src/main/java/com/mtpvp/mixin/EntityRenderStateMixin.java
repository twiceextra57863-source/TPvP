package com.mtpvp.mixin;

import com.mtpvp.accessor.IEntityRenderState;
import net.minecraft.client.render.entity.state.EntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EntityRenderState.class)
public class EntityRenderStateMixin implements IEntityRenderState {
    @Unique private float health;
    @Unique private float maxHealth;

    @Override public float mtpvp$getHealth() { return health; }
    @Override public float mtpvp$getMaxHealth() { return maxHealth; }
    @Override public void mtpvp$setHealth(float h) { this.health = h; }
    @Override public void mtpvp$setMaxHealth(float mh) { this.maxHealth = mh; }
}
