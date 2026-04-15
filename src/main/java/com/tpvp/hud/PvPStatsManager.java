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
    
    // MATCH HISTORY TRACKER
    public static class MatchRecord {
        public String opponent; 
        public String mode; 
        public boolean won;
        public MatchRecord(String o, String m, boolean w) { 
            opponent = o; mode = m; won = w; 
        }
    }
    public static List<MatchRecord> matchHistory = new ArrayList<>();
    
    // INDIVIDUAL KIT STATS
    public static Map<String, Integer> modeKills = new HashMap<>();
    public static Map<String, Integer> modeDeaths = new HashMap<>();

    // --- THE "SHARP MIND" AUTO KIT DETECTOR ---
    public static String detectCurrentMode(PlayerEntity player) {
        boolean hasCrystals = false, hasObsidian = false;
        boolean hasPots = false, hasPearl = false;
        boolean hasAxe = false, hasShield = false;
        boolean hasCobwebs = false;

        // Scans the entire inventory to figure out the game mode
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack item = player.getInventory().getStack(i);
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
        if (hasCobwebs) return "UHC / Classic";
        return "Sword / Classic";
    }

    // --- TIER MATH ALGORITHM ---
    public static float calculateSkillPercentage(String mode) {
        int k = mode.equals("Overall") ? totalKills : modeKills.getOrDefault(mode, 0);
        int d = mode.equals("Overall") ? totalDeaths : modeDeaths.getOrDefault(mode, 0);
        
        if (k + d == 0) return 0f; // Unranked (0 matches played)
        
        float winrate = (float) k / (k + d);
        float kdaBonus = Math.min(2.0f, (float) k / Math.max(1, d)) / 2.0f; // Max bonus if KDA is 2.0+
        
        return Math.min(100f, (winrate * 60f) + (kdaBonus * 40f)); // Max 100%
    }

    public static String getTierFromPercent(float percent) {
        if (percent == 0) return "§7Unranked";
        if (percent >= 90) return "§6§lHT1 (God)";
        if (percent >= 80) return "§e§lLT1 (Pro)";
        if (percent >= 65) return "§d§lHT2 (Expert)";
        if (percent >= 50) return "§b§lLT2 (Skilled)";
        if (percent >= 35) return "§a§lHT3 (Average)";
        if (percent >= 20) return "§c§lLT3 (Beginner)";
        return "§8§lTier 4 (Noob)";
    }
    
    // --- ADDING KILLS AND DEATHS TO SYSTEM ---
    public static void addKill(String victim, String mode) {
        totalKills++;
        modeKills.put(mode, modeKills.getOrDefault(mode, 0) + 1);
        matchHistory.add(0, new MatchRecord(victim, mode, true)); // Add to top
    }

    public static void addDeath(String killer, String mode) {
        totalDeaths++;
        modeDeaths.put(mode, modeDeaths.getOrDefault(mode, 0) + 1);
        matchHistory.add(0, new MatchRecord(killer, mode, false)); // Add to top
    }
}
