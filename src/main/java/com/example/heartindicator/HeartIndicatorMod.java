package com.example.heartindicator;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HeartIndicatorMod implements ModInitializer {
    public static final String MOD_ID = "heartindicator";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Heart Indicator Mod Initialized!");
    }
}
