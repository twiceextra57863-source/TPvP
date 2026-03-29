package com.example.mod;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExampleMod implements ModInitializer {
    public static final String MOD_ID = "health_indicator_mod";
    public static final Logger LOGGER = LoggerFactory.getLogger("HealthIndicator");

    @Override
    public void onInitialize() {
        LOGGER.info("Health Indicator Mod [Release] initialized successfully!");
    }
}

