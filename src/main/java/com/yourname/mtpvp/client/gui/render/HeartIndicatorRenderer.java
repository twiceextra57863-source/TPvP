package com.yourname.mtpvp.client.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.text.Text;

public class HeartIndicatorRenderer {
    
    public enum DesignType {
        VANILLA,
        STATUS_BAR,
        PLAYER_HEAD,
        DISABLED
    }
    
    public static DesignType currentDesign = DesignType.VANILLA;
    
    public static void setDesignType(DesignType design) {
        currentDesign = design;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(Text.literal("§a[§6MTPVP§a] §fHeart indicator set to: §e" + design.name()), false);
        }
    }
    
    public static int calculateHitsToKill(PlayerEntity attacker, LivingEntity target) {
        if (attacker == null) return 99;
        float totalHealth = target.getHealth();
        float damagePerHit = getPlayerDamage(attacker);
        if (damagePerHit <= 0) return 99;
        return (int) Math.ceil(totalHealth / damagePerHit);
    }
    
    private static float getPlayerDamage(PlayerEntity player) {
        float damage = 2.0f;
        ItemStack mainHand = player.getMainHandStack();
        
        if (!mainHand.isEmpty()) {
            Item item = mainHand.getItem();
            if (item instanceof SwordItem) {
                if (item.toString().contains("netherite")) damage = 8.0f;
                else if (item.toString().contains("diamond")) damage = 7.0f;
                else if (item.toString().contains("iron")) damage = 6.0f;
                else if (item.toString().contains("stone")) damage = 5.0f;
                else damage = 4.0f;
            } else if (item instanceof AxeItem) {
                if (item.toString().contains("netherite")) damage = 10.0f;
                else if (item.toString().contains("diamond")) damage = 9.0f;
                else damage = 8.0f;
            } else if (item instanceof BowItem || item instanceof CrossbowItem) {
                damage = 7.0f;
            }
        }
        return damage;
    }
}
