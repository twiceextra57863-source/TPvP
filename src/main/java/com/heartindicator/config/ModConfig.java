package com.heartindicator.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.*;

public class ModConfig {

    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("heartindicator.json");

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // ─── Settings ───────────────────────────────────────────────────────────────
    public boolean indicatorEnabled = true;
    public IconStyle iconStyle      = IconStyle.MODERN;
    public DetectionRange range     = DetectionRange.MEDIUM;
    public boolean showBackground   = true;
    public boolean animateHearts    = true;
    public boolean showPercentage   = false;

    // ─── Enums ──────────────────────────────────────────────────────────────────
    public enum IconStyle {
        CLASSIC("Classic ❤"),
        PIXEL("Pixel ♥"),
        MODERN("Modern ◆");

        public final String label;
        IconStyle(String label) { this.label = label; }

        public IconStyle next() {
            IconStyle[] v = values();
            return v[(ordinal() + 1) % v.length];
        }
    }

    public enum DetectionRange {
        SHORT(16,  "Short  (16 blocks)"),
        MEDIUM(32, "Medium (32 blocks)"),
        LONG(64,   "Long   (64 blocks)");

        public final int blocks;
        public final String label;
        DetectionRange(int blocks, String label) {
            this.blocks = blocks;
            this.label  = label;
        }

        public DetectionRange next() {
            DetectionRange[] v = values();
            return v[(ordinal() + 1) % v.length];
        }
    }

    // ─── Singleton ───────────────────────────────────────────────────────────────
    private static ModConfig INSTANCE;

    public static ModConfig get() {
        if (INSTANCE == null) load();
        return INSTANCE;
    }

    // ─── IO ──────────────────────────────────────────────────────────────────────
    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader r = Files.newBufferedReader(CONFIG_PATH)) {
                INSTANCE = GSON.fromJson(r, ModConfig.class);
                if (INSTANCE == null) INSTANCE = new ModConfig();
            } catch (Exception e) {
                INSTANCE = new ModConfig();
            }
        } else {
            INSTANCE = new ModConfig();
            save();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer w = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(INSTANCE, w);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
