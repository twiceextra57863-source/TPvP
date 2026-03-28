package com.tpvp.mixin;

import com.tpvp.accessor.IEntityRenderState; // Import naya package
import net.minecraft.client.render.entity.state.EntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EntityRenderState.class)
public class EntityRenderStateMixin implements IEntityRenderState {
    @Unique private float health;
    @Unique private float maxHealth;
    @Unique private double attackDamage;

    @Override public float tpvp$getHealth() { return health; }
    @Override public void tpvp$setHealth(float h) { this.health = h; }
    @Override public float tpvp$getMaxHealth() { return maxHealth; }
    @Override public void tpvp$setMaxHealth(float mh) { this.maxHealth = mh; }
    @Override public double tpvp$getAttackDamage() { return attackDamage; }
    @Override public void tpvp$setAttackDamage(double d) { this.attackDamage = d; }
}
