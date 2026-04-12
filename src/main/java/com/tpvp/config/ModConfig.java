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
    public static int armorX = 20;
    public static int armorY = 100;
    public static float armorScale = 1.0f;
    public static int armorCrackThreshold = 20;

    public static int radarX = 10, radarY = 30;
    public static float radarScale = 1.0f;
    public static int heldItemX = 50, heldItemY = 50;
    public static float heldItemScale = 1.0f;

    // TARGET SYSTEM
    public static String taggedPlayerName = "";
    public static String taggedFriendName = ""; 
    public static boolean autoTrack = false;
    
    // DRAGON SETTINGS
    public static boolean dragonAuraEnabled = true;
    public static int dragonColor = 0; // 0=Ruby Red, 1=Void Purple, 2=Frost Blue

    public static boolean killBannerEnabled = true;
    public static boolean soulAnimationEnabled = true;

    public static void load() {}
    public static void save() {}
}
