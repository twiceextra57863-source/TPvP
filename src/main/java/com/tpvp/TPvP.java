package com.tpvp;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TPvP implements ModInitializer {
    public static final String MOD_ID = "tpvp";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("TPvP Mod Initialized!");
    }
}
