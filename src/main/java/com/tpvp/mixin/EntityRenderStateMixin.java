package com.tpvp.mixin;

import net.minecraft.client.render.entity.state.EntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EntityRenderState.class)
public class EntityRenderStateMixin {
    @Unique
    public float tpvp$health;
    @Unique
    public float tpvp$maxHealth;
    @Unique
    public double tpvp$attackDamage;
}
