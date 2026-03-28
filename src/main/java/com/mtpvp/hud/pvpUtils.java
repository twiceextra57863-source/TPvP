package com.mtpvp.hud;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.attribute.EntityAttributes;

public class pvpUtils {
    public static int getHitsToKill(PlayerEntity target, PlayerEntity attacker) {
        float health = target.getHealth();
        ItemStack stack = attacker.getMainHandStack();
        // Simple calculation: Base Damage
        double damage = attacker.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        
        if (damage <= 0) return (int) health;
        return (int) Math.ceil(health / damage);
    }
}
