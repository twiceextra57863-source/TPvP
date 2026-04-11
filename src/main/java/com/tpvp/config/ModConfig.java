package com.tpvp.config;

public class ModConfig {
    // Combat Settings
    public static boolean indicatorEnabled = true;
    public static int indicatorStyle = 1; // 0=Hearts, 1=Bar, 2=Head+Hits
    public static boolean hitboxEnabled = true;
    public static boolean nearbyEnabled = true;

    // Crosshair Settings
    public static boolean smartCrosshair = true;
    public static int crosshairStyle = 0; // 0=Plus, 1=Dot, 2=Angle
    public static float crosshairSize = 1.0f;

    // Armor HUD Settings
    public static boolean armorVertical = true;
    public static int armorX = 20;
    public static int armorY = 100;
    public static float armorScale = 1.0f;

    // Radar Settings
    public static int radarX = 10;
    public static int radarY = 30;
    public static float radarScale = 1.0f;

    // Item Box Settings
    public static int heldItemX = 50;
    public static int heldItemY = 50;
    public static float heldItemScale = 1.0f;

    // Target System
    public static String taggedPlayerName = "";
    public static int targetMode = 0; 

    public static void load() {}
    public static void save() {}
}
