package com.mtpvp.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class MtpvpConfig {
    public static int healthMode = 0;
    public static boolean enabled = true;

    private static final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("mtpvp.json").toFile();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(new ConfigData(healthMode, enabled), writer);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void load() {
        if (!CONFIG_FILE.exists()) { save(); return; }
        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            ConfigData data = GSON.fromJson(reader, ConfigData.class);
            if (data != null) {
                healthMode = data.healthMode;
                enabled = data.enabled;
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private record ConfigData(int healthMode, boolean enabled) {}
}
