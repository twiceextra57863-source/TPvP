package com.example.heartindicator;

import com.example.heartindicator.config.ModConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HeartIndicatorMod implements ModInitializer {
    public static final String MOD_ID = "heartindicator";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Heart Indicator Mod initializing...");

        // Load config on server start (config is client-only but we still load on server for safety)
        ServerLifecycleEvents.SERVER_STARTED.register(server -> ModConfig.load());
    }
}
