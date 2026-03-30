package com.example.heartindicator.config;

import com.example.heartindicator.HeartIndicatorMod;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Path;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("heartindicator.json");
    private static ConfigData data = new ConfigData();

    public static boolean isEnabled() {
        return data.enabled;
    }

    public static void setEnabled(boolean enabled) {
        data.enabled = enabled;
        save();
    }

    public static void load() {
        if (CONFIG_PATH.toFile().exists()) {
            try (Reader reader = new FileReader(CONFIG_PATH.toFile())) {
                data = GSON.fromJson(reader, ConfigData.class);
            } catch (IOException e) {
                HeartIndicatorMod.LOGGER.error("Failed to load config", e);
            }
        } else {
            save(); // create default config
        }
    }

    public static void save() {
        try (Writer writer = new FileWriter(CONFIG_PATH.toFile())) {
            GSON.toJson(data, writer);
        } catch (IOException e) {
            HeartIndicatorMod.LOGGER.error("Failed to save config", e);
        }
    }

    private static class ConfigData {
        boolean enabled = true; // default: enabled
    }
}
