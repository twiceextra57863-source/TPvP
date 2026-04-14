package com.tpvp.config;

public class ModConfig {

    // ---------------------------------------------------------
    // --- COMBAT & INDICATOR SETTINGS ---
    // ---------------------------------------------------------
    public static boolean indicatorEnabled = true;
    public static int indicatorStyle = 1; // 0 = Hearts, 1 = Health Bar, 2 = Face + Hits
    public static boolean hitboxEnabled = true;
    
    // ---------------------------------------------------------
    // --- CROSSHAIR SETTINGS ---
    // ---------------------------------------------------------
    public static boolean smartCrosshair = true;
    public static int crosshairStyle = 0; // 0 = Pro Plus, 1 = Hollow Dot, 2 = Angle Chevron
    public static float crosshairSize = 1.0f;

    // ---------------------------------------------------------
    // --- ARMOR HUD SETTINGS ---
    // ---------------------------------------------------------
    public static boolean armorVertical = true;
    public static int armorX = 20;
    public static int armorY = 100;
    public static float armorScale = 1.0f;
    public static int armorCrackThreshold = 20; // % at which armor cracks/glows red

    // ---------------------------------------------------------
    // --- RADAR HUD SETTINGS ---
    // ---------------------------------------------------------
    public static boolean nearbyEnabled = true;
    public static int radarX = 10;
    public static int radarY = 30;
    public static float radarScale = 1.0f;

    // ---------------------------------------------------------
    // --- CUSTOM ITEM BOX SETTINGS ---
    // ---------------------------------------------------------
    public static int heldItemX = 50;
    public static int heldItemY = 50;
    public static float heldItemScale = 1.0f;

    // ---------------------------------------------------------
    // --- KILL FEED NOTIFICATION SETTINGS ---
    // ---------------------------------------------------------
    public static int killFeedX = 10;
    public static int killFeedY = 50;

    // ---------------------------------------------------------
    // --- POTION HUD (NEW SYSTEM) ---
    // ---------------------------------------------------------
    public static boolean potionHudEnabled = true;
    public static int potionHudX = 10;
    public static int potionHudY = 150;

    // ---------------------------------------------------------
    // --- TARGET TRACKING SYSTEM ---
    // ---------------------------------------------------------
    public static String taggedPlayerName = "";
    public static String taggedFriendName = ""; 
    public static int targetMode = 0; 
    public static boolean autoTrack = false; // Auto tracks lowest HP enemy
    
    public static boolean dragonAuraEnabled = true;
    public static int dragonColor = 0; // 0 = Ruby Red, 1 = Void Purple, 2 = Frost Blue
    
    // ---------------------------------------------------------
    // --- EFFECTS & ANIMATIONS SETTINGS ---
    // ---------------------------------------------------------
    public static boolean killBannerEnabled = true;
    public static int bannerColorTheme = 0; // 0 = Blood Red, 1 = Royal Gold, 2 = Toxic Green
    public static boolean soulAnimationEnabled = true; // The Abyssal Void & JJK Monsters

    // ---------------------------------------------------------
    // --- PERFORMANCE & OPTIMIZATION SETTINGS ---
    // ---------------------------------------------------------
    public static boolean fpsBoostEnabled = false; // Prevents fire rendering, limits particles, culls distant entities
    public static boolean smoothGameEnabled = false; // Cinematic Camera interpolation for butter smooth movement
    public static boolean deviceCooler = true; // Locks game to 10 FPS when unfocused/multitasking to prevent overheating
    public static int cottonSensitivity = 100; // Multiplier (10% to 300%) for Smooth Game mode

    // ---------------------------------------------------------
    // --- SAVE & LOAD LOGIC ---
    // ---------------------------------------------------------
    public static void load() {
        // Future JSON Loading logic goes here
    }

    public static void save() {
        // Future JSON Saving logic goes here
    }
}
