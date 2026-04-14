package com.tpvp.hud;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PvPStatsManager {
    
    public static int totalKills = 0;
    public static int totalDeaths = 0;
    
    // Matches History
    public static class MatchRecord {
        public String opponent; public String mode; public boolean won;
        public MatchRecord(String o, String m, boolean w) { opponent = o; mode = m; won = w; }
    }
    public static List<MatchRecord> matchHistory = new ArrayList<>();
    
    // Mode Stats
    public static Map<String, Integer> modeKills = new HashMap<>();
    public static Map<String, Integer> modeDeaths = new HashMap<>();

    // --- THE "SHARP MIND" AUTO KIT DETECTOR ---
    public static String detectCurrentMode(PlayerEntity player) {
        boolean hasCrystals = false, hasObsidian = false;
        boolean hasPots = false, hasPearl = false;
        boolean hasAxe = false, hasShield = false;
        boolean hasCobwebs = false;

        for (ItemStack item : player.getInventory().main) {
            if (item.isOf(Items.END_CRYSTAL)) hasCrystals = true;
            if (item.isOf(Items.OBSIDIAN)) hasObsidian = true;
            if (item.isOf(Items.SPLASH_POTION)) hasPots = true;
            if (item.isOf(Items.ENDER_PEARL)) hasPearl = true;
            if (item.getItem().toString().contains("axe")) hasAxe = true;
            if (item.isOf(Items.SHIELD)) hasShield = true;
            if (item.isOf(Items.COBWEB)) hasCobwebs = true;
        }

        if (hasCrystals && hasObsidian) return "Crystal PvP";
        if (hasPots && hasPearl) return "Nodebuff (Pots)";
        if (hasAxe && hasShield) return "Axe PvP";
        if (hasCobwebs) return "UHC / Nethpot";
        return "Sword / Classic";
    }

    // --- TIER CALCULATION ALGORITHM ---
    public static float calculateSkillPercentage(String mode) {
        int k = modeKills.getOrDefault(mode, totalKills);
        int d = modeDeaths.getOrDefault(mode, totalDeaths);
        if (k + d == 0) return 0f; // Unranked
        
        float winrate = (float) k / (k + d);
        float kdaBonus = Math.min(2.0f, (float) k / Math.max(1, d)) / 2.0f; // Max bonus if KDA is 2.0+
        
        return Math.min(100f, (winrate * 60f) + (kdaBonus * 40f)); // 100% max
    }

    public static String getTierFromPercent(float percent) {
        if (percent == 0) return "§7Unranked";
        if (percent >= 90) return "§6§lHT1 (God)";
        if (percent >= 80) return "§e§lLT1 (Pro)";
        if (percent >= 65) return "§d§lHT2 (Expert)";
        if (percent >= 50) return "§b§lLT2 (Skilled)";
        if (percent >= 35) return "§a§lHT3 (Average)";
        return "§c§lTier 4 (Noob)";
    }
    
    public static void addKill(String victim) {
        totalKills++;
        String mode = "Crystal PvP"; // Default fallback
        modeKills.put(mode, modeKills.getOrDefault(mode, 0) + 1);
        matchHistory.add(0, new MatchRecord(victim, mode, true)); // Add to top of history
    }
}
