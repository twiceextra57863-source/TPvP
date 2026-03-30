package com.example.heartindicator.config;

import com.example.heartindicator.HeartIndicatorMod;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

public class ModConfig {
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("heartindicator.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private boolean enabled = true; // default: on

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        save();
    }

    public void save() {
        try (FileWriter writer = new FileWriter(CONFIG_PATH.toFile())) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            HeartIndicatorMod.LOGGER.error("Failed to save config", e);
        }
    }

    public static ModConfig load() {
        if (CONFIG_PATH.toFile().exists()) {
            try (FileReader reader = new FileReader(CONFIG_PATH.toFile())) {
                return GSON.fromJson(reader, ModConfig.class);
            } catch (IOException e) {
                HeartIndicatorMod.LOGGER.error("Failed to load config, using default", e);
            }
        }
        ModConfig config = new ModConfig();
        config.save();
        return config;
    }
}
