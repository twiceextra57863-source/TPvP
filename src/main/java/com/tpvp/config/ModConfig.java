package com.tpvp.config;

public class ModConfig {
    public static boolean indicatorEnabled = true;
    public static int indicatorStyle = 1; 

    public static boolean nearbyEnabled = true;
    public static int hudX = 10;
    public static int hudY = 30;
    public static float hudScale = 1.0f;

    public static boolean targetEnabled = true;
    public static int targetMode = 0; 
    public static int crownColor = 0; 
    public static int crownStyle = 0; 
    public static boolean showTargetHealth = true; 
    public static int autoRange = 30; 
    public static float crownScale = 1.0f;
    public static String taggedPlayerName = ""; 

    public static boolean armorHudEnabled = true;
    public static int armorHudX = 10;
    public static int armorHudY = 150;
    public static float armorHudScale = 1.0f;
    public static boolean armorHudHorizontal = false; 
    public static int armorHudStyle = 0; 
    public static boolean armorBgEnabled = true;
    public static float armorBgOpacity = 0.5f; 
    public static boolean heldItemEnabled = true;
    public static int heldItemX = 100;
    public static int heldItemY = 150;
    public static float heldItemScale = 1.0f;

    // --- NEW: CROSSHAIR SETTINGS ---
    public static boolean crosshairEnabled = true;
    public static int crosshairStyle = 0; // 0=Perfect Plus, 1=Dot, 2=Hollow Circle, 3=T-Shape, 4=Square+Dot
    public static int crosshairColor = 0; // 0=White, 1=Green, 2=Red, 3=Cyan, 4=Black

    public static void load() { }
    public static void save() { }
}
