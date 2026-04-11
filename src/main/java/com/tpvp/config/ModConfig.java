package com.tpvp.config;

public class ModConfig {
    // Combat Features
    public static boolean indicatorEnabled = true;
    public static int indicatorStyle = 1; 
    public static boolean smartCrosshair = true;
    public static boolean hitboxEnabled = true;

    // Radar Feature
    public static boolean nearbyEnabled = true;

    // HUD Positioning
    public static int heldItemX = 50;
    public static int heldItemY = 50;
    public static float heldItemScale = 1.0f;

    public static int armorX = 20;
    public static int armorY = 100;
    public static float armorScale = 1.0f;

    public static int radarX = 10;
    public static int radarY = 30;
    public static float radarScale = 1.0f;

    // --- YE DO VARIABLES MISSING THE (Target Screen ke liye) ---
    public static String taggedPlayerName = "";
    public static int targetMode = 0; 

    public static void load() {}
    public static void save() {}
}
