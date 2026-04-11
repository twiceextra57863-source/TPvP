package com.tpvp.config;

public class ModConfig {
    public static boolean indicatorEnabled = true;
    public static int indicatorStyle = 1; 
    public static boolean hitboxEnabled = true;
    public static boolean nearbyEnabled = true;

    // Crosshair System
    public static boolean smartCrosshair = true;
    public static int crosshairStyle = 0; // 0=Plus, 1=Dot, 2=Angle
    public static float crosshairSize = 1.0f;

    // Armor HUD System
    public static boolean armorVertical = true; // True=Vertical, False=Horizontal
    public static int armorX = 20;
    public static int armorY = 100;
    public static float armorScale = 1.0f;

    // Radar HUD
    public static int radarX = 10;
    public static int radarY = 30;
    public static float radarScale = 1.0f;

    public static int heldItemX = 50;
    public static int heldItemY = 50;
    public static float heldItemScale = 1.0f;

    // Target System
    public static String taggedPlayerName = "";

    public static void load() {}
    public static void save() {}
}
