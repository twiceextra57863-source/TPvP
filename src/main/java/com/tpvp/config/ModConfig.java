package com.tpvp.config;

public class ModConfig {
    public static boolean indicatorEnabled = true;
    public static int indicatorStyle = 1; 
    public static boolean hitboxEnabled = true;
    public static boolean nearbyEnabled = true;

    public static boolean smartCrosshair = true;
    public static int crosshairStyle = 0; 
    public static float crosshairSize = 1.0f;

    public static boolean armorVertical = true;
    public static int armorX = 20, armorY = 100;
    public static float armorScale = 1.0f;
    public static int armorCrackThreshold = 20;

    public static int radarX = 10, radarY = 30;
    public static float radarScale = 1.0f;
    public static int heldItemX = 50, heldItemY = 50;
    public static float heldItemScale = 1.0f;
    public static int killFeedX = 10, killFeedY = 50;
    public static boolean potionHudEnabled = true;
    public static int potionHudX = 10, potionHudY = 150;

    public static String taggedPlayerName = "";
    public static String taggedFriendName = ""; 
    public static int targetMode = 0; 
    public static boolean autoTrack = false;
    
    public static boolean dragonAuraEnabled = true;
    public static int dragonColor = 0; 
    
    public static boolean killBannerEnabled = true;
    public static int bannerColorTheme = 0; 
    public static boolean soulAnimationEnabled = true;

    // --- AUTO-ENABLED HYPER PERFORMANCE ENGINE ---
    public static boolean fpsBoostEnabled = true; // DEFAULT ON
    public static boolean smoothGameEnabled = true; // DEFAULT ON
    public static boolean deviceCooler = true; // DEFAULT ON
    public static int cottonSensitivity = 100; 

    // Ranked Eval
    public static boolean evalActive = false;
    public static String evalMode = "Crystal PvP";
    public static int evalTotalMatches = 10;
    public static int evalCurrentMatches = 0;
    public static int evalKills = 0;
    public static int evalDeaths = 0;

    public static void load() {}
    public static void save() {}
}
