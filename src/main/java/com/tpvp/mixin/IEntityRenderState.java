package com.tpvp.mixin;

public interface IEntityRenderState {
    float tpvp$getHealth();
    void tpvp$setHealth(float health);

    float tpvp$getMaxHealth();
    void tpvp$setMaxHealth(float maxHealth);

    double tpvp$getAttackDamage();
    void tpvp$setAttackDamage(double damage);
}
