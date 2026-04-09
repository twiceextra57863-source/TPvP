package com.tpvp.config;

public class ModConfig {
    // Combat Settings
    public static boolean indicatorEnabled = true;
    public static int indicatorStyle = 1; 

    // Radar Settings
    public static boolean nearbyEnabled = true;
    public static int hudX = 10;
    public static int hudY = 30;
    public static float hudScale = 1.0f;

    // Target Lock Settings
    public static boolean targetEnabled = true;
    public static int targetMode = 0; 
    public static int crownColor = 0; 
    public static int crownStyle = 0; 
    public static boolean showTargetHealth = true; 
    public static int autoRange = 30; 
    public static float crownScale = 1.0f;
    public static boolean glowEffect = false;
    public static String taggedPlayerName = ""; 

    // --- NEW: ARMOR HUD SETTINGS ---
    public static boolean armorHudEnabled = true;
    public static int armorHudX = 10;
    public static int armorHudY = 150;
    public static float armorHudScale = 1.0f;
    public static boolean armorHudHorizontal = false; // Vertical or Horizontal
    public static int armorHudStyle = 0; // 0=Percent, 1=Status Bar, 2=Numbers

    public static void load() { }
    public static void save() { }
}
