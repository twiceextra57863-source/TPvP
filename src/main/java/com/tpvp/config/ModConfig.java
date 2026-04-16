package com.tpvp.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class ModConfig {
    // Combat Settings
    public static boolean indicatorEnabled = true;
    public static int indicatorStyle = 1; 
    public static boolean hitboxEnabled = true;
    public static boolean nearbyEnabled = true;

    // Crosshair Settings
    public static boolean smartCrosshair = true;
    public static int crosshairStyle = 0; 
    public static float crosshairSize = 1.0f;

    // Armor HUD Settings
    public static boolean armorVertical = true;
    public static int armorX = 20, armorY = 100;
    public static float armorScale = 1.0f;
    public static int armorCrackThreshold = 20;

    // Radar & Item Settings
    public static int radarX = 10, radarY = 30;
    public static float radarScale = 1.0f;
    public static int heldItemX = 50, heldItemY = 50;
    public static float heldItemScale = 1.0f;
    public static int killFeedX = 10, killFeedY = 50;
    public static boolean potionHudEnabled = true;
    public static int potionHudX = 10, potionHudY = 150;

    // Target System
    public static String taggedPlayerName = "";
    public static String taggedFriendName = ""; 
    public static int targetMode = 0; 
    public static boolean autoTrack = false;
    
    public static boolean dragonAuraEnabled = true;
    public static int dragonColor = 0; 
    
    // Effects System
    public static boolean killBannerEnabled = true;
    public static int bannerColorTheme = 0; 
    public static boolean soulAnimationEnabled = true;

    // Performance System
    public static boolean fpsBoostEnabled = true; 
    public static boolean smoothGameEnabled = true; 
    public static boolean deviceCooler = true; 
    public static int cottonSensitivity = 100; 

    // Ranked Evaluation System
    public static boolean evalActive = false;
    public static String evalMode = "Crystal PvP";
    public static int evalTotalMatches = 10;
    public static int evalCurrentMatches = 0;
    public static int evalKills = 0;
    public static int evalDeaths = 0;

    // --- JSON SAVE & LOAD SYSTEM ---
    private static final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("tpvp_settings.json").toFile();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void load() {
        if (!CONFIG_FILE.exists()) {
            save(); // Create default config if it doesn't exist
            return;
        }
        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

            if (json.has("indicatorEnabled")) indicatorEnabled = json.get("indicatorEnabled").getAsBoolean();
            if (json.has("indicatorStyle")) indicatorStyle = json.get("indicatorStyle").getAsInt();
            if (json.has("hitboxEnabled")) hitboxEnabled = json.get("hitboxEnabled").getAsBoolean();
            if (json.has("nearbyEnabled")) nearbyEnabled = json.get("nearbyEnabled").getAsBoolean();

            if (json.has("smartCrosshair")) smartCrosshair = json.get("smartCrosshair").getAsBoolean();
            if (json.has("crosshairStyle")) crosshairStyle = json.get("crosshairStyle").getAsInt();
            if (json.has("crosshairSize")) crosshairSize = json.get("crosshairSize").getAsFloat();

            if (json.has("armorVertical")) armorVertical = json.get("armorVertical").getAsBoolean();
            if (json.has("armorX")) armorX = json.get("armorX").getAsInt();
            if (json.has("armorY")) armorY = json.get("armorY").getAsInt();
            if (json.has("armorScale")) armorScale = json.get("armorScale").getAsFloat();
            if (json.has("armorCrackThreshold")) armorCrackThreshold = json.get("armorCrackThreshold").getAsInt();

            if (json.has("radarX")) radarX = json.get("radarX").getAsInt();
            if (json.has("radarY")) radarY = json.get("radarY").getAsInt();
            if (json.has("radarScale")) radarScale = json.get("radarScale").getAsFloat();
            if (json.has("heldItemX")) heldItemX = json.get("heldItemX").getAsInt();
            if (json.has("heldItemY")) heldItemY = json.get("heldItemY").getAsInt();
            if (json.has("heldItemScale")) heldItemScale = json.get("heldItemScale").getAsFloat();
            if (json.has("killFeedX")) killFeedX = json.get("killFeedX").getAsInt();
            if (json.has("killFeedY")) killFeedY = json.get("killFeedY").getAsInt();
            if (json.has("potionHudEnabled")) potionHudEnabled = json.get("potionHudEnabled").getAsBoolean();
            if (json.has("potionHudX")) potionHudX = json.get("potionHudX").getAsInt();
            if (json.has("potionHudY")) potionHudY = json.get("potionHudY").getAsInt();

            if (json.has("taggedPlayerName")) taggedPlayerName = json.get("taggedPlayerName").getAsString();
            if (json.has("taggedFriendName")) taggedFriendName = json.get("taggedFriendName").getAsString();
            if (json.has("targetMode")) targetMode = json.get("targetMode").getAsInt();
            if (json.has("autoTrack")) autoTrack = json.get("autoTrack").getAsBoolean();
            
            if (json.has("dragonAuraEnabled")) dragonAuraEnabled = json.get("dragonAuraEnabled").getAsBoolean();
            if (json.has("dragonColor")) dragonColor = json.get("dragonColor").getAsInt();

            if (json.has("killBannerEnabled")) killBannerEnabled = json.get("killBannerEnabled").getAsBoolean();
            if (json.has("bannerColorTheme")) bannerColorTheme = json.get("bannerColorTheme").getAsInt();
            if (json.has("soulAnimationEnabled")) soulAnimationEnabled = json.get("soulAnimationEnabled").getAsBoolean();

            if (json.has("fpsBoostEnabled")) fpsBoostEnabled = json.get("fpsBoostEnabled").getAsBoolean();
            if (json.has("smoothGameEnabled")) smoothGameEnabled = json.get("smoothGameEnabled").getAsBoolean();
            if (json.has("deviceCooler")) deviceCooler = json.get("deviceCooler").getAsBoolean();
            if (json.has("cottonSensitivity")) cottonSensitivity = json.get("cottonSensitivity").getAsInt();

            if (json.has("evalActive")) evalActive = json.get("evalActive").getAsBoolean();
            if (json.has("evalMode")) evalMode = json.get("evalMode").getAsString();
            if (json.has("evalTotalMatches")) evalTotalMatches = json.get("evalTotalMatches").getAsInt();
            if (json.has("evalCurrentMatches")) evalCurrentMatches = json.get("evalCurrentMatches").getAsInt();
            if (json.has("evalKills")) evalKills = json.get("evalKills").getAsInt();
            if (json.has("evalDeaths")) evalDeaths = json.get("evalDeaths").getAsInt();

        } catch (Exception e) {
            System.out.println("Failed to load TPvP ModConfig!");
            e.printStackTrace();
        }
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            JsonObject json = new JsonObject();
            
            json.addProperty("indicatorEnabled", indicatorEnabled);
            json.addProperty("indicatorStyle", indicatorStyle);
            json.addProperty("hitboxEnabled", hitboxEnabled);
            json.addProperty("nearbyEnabled", nearbyEnabled);

            json.addProperty("smartCrosshair", smartCrosshair);
            json.addProperty("crosshairStyle", crosshairStyle);
            json.addProperty("crosshairSize", crosshairSize);

            json.addProperty("armorVertical", armorVertical);
            json.addProperty("armorX", armorX);
            json.addProperty("armorY", armorY);
            json.addProperty("armorScale", armorScale);
            json.addProperty("armorCrackThreshold", armorCrackThreshold);

            json.addProperty("radarX", radarX);
            json.addProperty("radarY", radarY);
            json.addProperty("radarScale", radarScale);
            json.addProperty("heldItemX", heldItemX);
            json.addProperty("heldItemY", heldItemY);
            json.addProperty("heldItemScale", heldItemScale);
            json.addProperty("killFeedX", killFeedX);
            json.addProperty("killFeedY", killFeedY);
            json.addProperty("potionHudEnabled", potionHudEnabled);
            json.addProperty("potionHudX", potionHudX);
            json.addProperty("potionHudY", potionHudY);

            json.addProperty("taggedPlayerName", taggedPlayerName);
            json.addProperty("taggedFriendName", taggedFriendName);
            json.addProperty("targetMode", targetMode);
            json.addProperty("autoTrack", autoTrack);
            
            json.addProperty("dragonAuraEnabled", dragonAuraEnabled);
            json.addProperty("dragonColor", dragonColor);

            json.addProperty("killBannerEnabled", killBannerEnabled);
            json.addProperty("bannerColorTheme", bannerColorTheme);
            json.addProperty("soulAnimationEnabled", soulAnimationEnabled);

            json.addProperty("fpsBoostEnabled", fpsBoostEnabled);
            json.addProperty("smoothGameEnabled", smoothGameEnabled);
            json.addProperty("deviceCooler", deviceCooler);
            json.addProperty("cottonSensitivity", cottonSensitivity);

            json.addProperty("evalActive", evalActive);
            json.addProperty("evalMode", evalMode);
            json.addProperty("evalTotalMatches", evalTotalMatches);
            json.addProperty("evalCurrentMatches", evalCurrentMatches);
            json.addProperty("evalKills", evalKills);
            json.addProperty("evalDeaths", evalDeaths);

            GSON.toJson(json, writer);
        } catch (Exception e) {
            System.out.println("Failed to save TPvP ModConfig!");
            e.printStackTrace();
        }
    }
        }
