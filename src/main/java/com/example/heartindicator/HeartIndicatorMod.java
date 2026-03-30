package com.example.heartindicator;

import com.example.heartindicator.config.ModConfig;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HeartIndicatorMod implements ModInitializer {
    public static final String MOD_ID = "heartindicator";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static ModConfig config;

    @Override
    public void onInitialize() {
        config = ModConfig.load();
        LOGGER.info("Heart Indicator Mod initialized. Enabled: {}", config.isEnabled());
    }

    public static ModConfig getConfig() {
        return config;
    }

    public static void saveConfig() {
        config.save();
    }
}
