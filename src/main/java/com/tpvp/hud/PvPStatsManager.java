package com.tpvp.hud;

import com.tpvp.config.ModConfig;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PvPStatsManager {
    
    public static int totalKills = 0;
    public static int totalDeaths = 0;
    
    public static class MatchRecord {
        public String killer; 
        public String victim; 
        public String mode; 
        public boolean won;
        public MatchRecord(String k, String v, String m, boolean w) { 
            killer = k; victim = v; mode = m; won = w; 
        }
    }
    public static List<MatchRecord> matchHistory = new ArrayList<>();
    
    public static Map<String, Integer> modeKills = new HashMap<>();
    public static Map<String, Integer> modeDeaths = new HashMap<>();

    public static final String[] ALL_MODES = {
        "Crystal PvP", "Axe PvP", "Nodebuff", "UHC / Classic", 
        "Cart PvP", "Nethpot", "Mace PvP", "Spear/Trident", "Beast PvP", "Iron Pots", "Dia SMP"
    };

    // --- SUPER SHARP MIND KIT DETECTOR ---
    public static String detectCurrentMode(PlayerEntity player) {
        boolean crystal = false, obsidian = false, splash = false, pearl = false;
        boolean axe = false, shield = false, cobweb = false, minecart = false;
        boolean mace = false, trident = false, netheriteArmor = false, ironArmor = false, diaArmor = false;

        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack item = player.getInventory().getStack(i);
            String name = item.getItem().toString().toLowerCase();

            if (name.contains("end_crystal")) crystal = true;
            if (name.contains("obsidian")) obsidian = true;
            if (name.contains("splash_potion")) splash = true;
            if (name.contains("ender_pearl")) pearl = true;
            if (name.contains("axe")) axe = true;
            if (name.contains("shield")) shield = true;
            if (name.contains("cobweb")) cobweb = true;
            if (name.contains("minecart") || name.contains("tnt_minecart")) minecart = true;
            if (name.contains("mace")) mace = true;
            if (name.contains("trident")) trident = true;
        }
        
        for (ItemStack item : player.getArmorItems()) {
            String name = item.getItem().toString().toLowerCase();
            if (name.contains("netherite")) netheriteArmor = true;
            if (name.contains("iron")) ironArmor = true;
            if (name.contains("diamond")) diaArmor = true;
        }

        // Extremely accurate kit deduction
        if (minecart) return "Cart PvP";
        if (mace) return "Mace PvP";
        if (trident) return "Spear/Trident";
        if (crystal && obsidian) return "Crystal PvP";
        if (splash && pearl && netheriteArmor) return "Nethpot";
        if (splash && pearl && ironArmor) return "Iron Pots";
        if (splash && pearl) return "Nodebuff";
        if (axe && shield) return "Axe PvP";
        if (cobweb) return "UHC / Classic";
        if (diaArmor && !crystal && !splash) return "Dia SMP";
        if (netheriteArmor && axe) return "Beast PvP";
        
        return "Sword / Classic";
    }

    public static float calculateSkillPercentage(String mode) {
        int k = mode.equals("Overall") ? totalKills : modeKills.getOrDefault(mode, 0);
        int d = mode.equals("Overall") ? totalDeaths : modeDeaths.getOrDefault(mode, 0);
        
        if (k + d == 0) return 0f; 
        
        float winrate = (float) k / (k + d);
        float kdaBonus = Math.min(2.0f, (float) k / Math.max(1, d)) / 2.0f; 
        
        return Math.min(100f, (winrate * 60f) + (kdaBonus * 40f)); 
    }

    public static String getTierFromPercent(float percent) {
        if (percent == 0) return "§7Unranked";
        if (percent >= 95) return "§6§lHT1";
        if (percent >= 85) return "§e§lLT1";
        if (percent >= 75) return "§d§lHT2";
        if (percent >= 60) return "§b§lLT2";
        if (percent >= 50) return "§a§lHT3";
        if (percent >= 40) return "§2§lLT3";
        if (percent >= 30) return "§c§lHT4";
        if (percent >= 20) return "§4§lLT4";
        if (percent >= 10) return "§8§lHT5";
        return "§0§lLT5";
    }

    public static void addKill(String killer, String victim, String mode) {
        totalKills++;
        modeKills.put(mode, modeKills.getOrDefault(mode, 0) + 1);
        matchHistory.add(0, new MatchRecord(killer, victim, mode, true)); 
        
        // Ranked Eval Logic
        if (ModConfig.evalActive && ModConfig.evalMode.equals(mode)) {
            ModConfig.evalKills++;
            ModConfig.evalCurrentMatches++;
        }
    }

    public static void addDeath(String killer, String victim, String mode) {
        totalDeaths++;
        modeDeaths.put(mode, modeDeaths.getOrDefault(mode, 0) + 1);
        matchHistory.add(0, new MatchRecord(killer, victim, mode, false)); 
        
        // Ranked Eval Logic
        if (ModConfig.evalActive && ModConfig.evalMode.equals(mode)) {
            ModConfig.evalDeaths++;
            ModConfig.evalCurrentMatches++;
        }
    }
}
