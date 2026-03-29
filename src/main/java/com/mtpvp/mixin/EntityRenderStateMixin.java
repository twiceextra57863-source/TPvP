package com.mtpvp.mixin;

import com.tpvp.accessor.IEntityRenderState;
import net.minecraft.client.render.entity.state.EntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EntityRenderState.class)
public class EntityRenderStateMixin implements IEntityRenderState {
    @Unique private float h;
    @Unique private float mh;
    @Unique private double ad;

    @Override public float tpvp$getHealth() { return h; }
    @Override public void tpvp$setHealth(float val) { this.h = val; }
    @Override public float tpvp$getMaxHealth() { return mh; }
    @Override public void tpvp$setMaxHealth(float val) { this.mh = val; }
    @Override public double tpvp$getAttackDamage() { return ad; }
    @Override public void tpvp$setAttackDamage(double val) { this.ad = val; }
}
