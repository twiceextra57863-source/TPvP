package com.mtpvp.hud;

import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class pvpUtils {
    public static int getHitsToKill(PlayerEntity target, PlayerEntity attacker) {
        float health = target.getHealth();
        if (health <= 0) return 0;

        // 1.21.4 Fix: GENERIC_ATTACK_DAMAGE -> ATTACK_DAMAGE
        double damage = attacker.getAttributeValue(EntityAttributes.ATTACK_DAMAGE);
        
        // Agar player ke haath mein weapon hai toh uska damage bhi automatically isme include ho jata hai
        // Lekin safety ke liye check:
        if (damage <= 0) damage = 1.0; 

        return (int) Math.ceil(health / damage);
    }
}
