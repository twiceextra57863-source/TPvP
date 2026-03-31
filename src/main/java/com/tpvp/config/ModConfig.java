package com.tpvp.config;

public class ModConfig {
    public static boolean indicatorEnabled = true;
    public static int indicatorStyle = 1; 

    public static boolean nearbyEnabled = true;
    public static int hudX = 10;
    public static int hudY = 30;
    public static float hudScale = 1.0f;

    // --- NEW: TARGET LOCK SETTINGS (8 Settings) ---
    public static boolean targetEnabled = true;
    public static int targetMode = 0; // 0 = Manual Tag, 1 = Auto (Lowest HP)
    public static int crownColor = 0; // 0=Gold, 1=Red, 2=Diamond, 3=Emerald
    public static int crownStyle = 0; // 0=3D Crown, 1=Rotating Diamond
    public static boolean showTargetHealth = true; 
    public static int autoRange = 30; // Max range for Auto Select
    public static float crownScale = 1.0f;
    public static boolean glowEffect = true; // Highlight Target

    public static String taggedPlayerName = ""; // Jo player manually tag kiya gaya hai

    public static void load() { }
    public static void save() { }
}
