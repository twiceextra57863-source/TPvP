package com.tpvp.hud;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tpvp.config.ModConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
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

    // --- JSON SAVE & LOAD SYSTEM FOR STATS ---
    private static final File STATS_FILE = FabricLoader.getInstance().getConfigDir().resolve("tpvp_stats.json").toFile();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void load() {
        if (!STATS_FILE.exists()) {
            save(); 
            return;
        }
        try (FileReader reader = new FileReader(STATS_FILE)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            
            if (json.has("totalKills")) totalKills = json.get("totalKills").getAsInt();
            if (json.has("totalDeaths")) totalDeaths = json.get("totalDeaths").getAsInt();

            if (json.has("modeKills")) {
                JsonObject mk = json.getAsJsonObject("modeKills");
                for (String key : mk.keySet()) modeKills.put(key, mk.get(key).getAsInt());
            }
            if (json.has("modeDeaths")) {
                JsonObject md = json.getAsJsonObject("modeDeaths");
                for (String key : md.keySet()) modeDeaths.put(key, md.get(key).getAsInt());
            }

            if (json.has("matchHistory")) {
                JsonArray historyArray = json.getAsJsonArray("matchHistory");
                matchHistory.clear();
                for (int i = 0; i < historyArray.size(); i++) {
                    JsonObject obj = historyArray.get(i).getAsJsonObject();
                    matchHistory.add(new MatchRecord(
                        obj.get("killer").getAsString(),
                        obj.get("victim").getAsString(),
                        obj.get("mode").getAsString(),
                        obj.get("won").getAsBoolean()
                    ));
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to load TPvP Stats!");
        }
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(STATS_FILE)) {
            JsonObject json = new JsonObject();
            
            json.addProperty("totalKills", totalKills);
            json.addProperty("totalDeaths", totalDeaths);

            JsonObject mk = new JsonObject();
            for (Map.Entry<String, Integer> entry : modeKills.entrySet()) mk.addProperty(entry.getKey(), entry.getValue());
            json.add("modeKills", mk);

            JsonObject md = new JsonObject();
            for (Map.Entry<String, Integer> entry : modeDeaths.entrySet()) md.addProperty(entry.getKey(), entry.getValue());
            json.add("modeDeaths", md);

            JsonArray historyArray = new JsonArray();
            for (MatchRecord r : matchHistory) {
                JsonObject obj = new JsonObject();
                obj.addProperty("killer", r.killer);
                obj.addProperty("victim", r.victim);
                obj.addProperty("mode", r.mode);
                obj.addProperty("won", r.won);
                historyArray.add(obj);
            }
            json.add("matchHistory", historyArray);

            GSON.toJson(json, writer);
        } catch (Exception e) {
            System.out.println("Failed to save TPvP Stats!");
        }
    }

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
        if (matchHistory.size() > 50) matchHistory.remove(50); // Keep max 50 history
        
        if (ModConfig.evalActive && ModConfig.evalMode.equals(mode)) {
            ModConfig.evalKills++;
            ModConfig.evalCurrentMatches++;
        }
        save(); // Save progress
    }

    public static void addDeath(String killer, String victim, String mode) {
        totalDeaths++;
        modeDeaths.put(mode, modeDeaths.getOrDefault(mode, 0) + 1);
        matchHistory.add(0, new MatchRecord(killer, victim, mode, false)); 
        if (matchHistory.size() > 50) matchHistory.remove(50);
        
        if (ModConfig.evalActive && ModConfig.evalMode.equals(mode)) {
            ModConfig.evalDeaths++;
            ModConfig.evalCurrentMatches++;
        }
        save(); // Save progress
    }
    }
