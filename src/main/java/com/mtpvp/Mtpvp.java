package com.mtpvp;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Mtpvp implements ModInitializer {
    public static final String MOD_ID = "mtpvp";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Mtpvp Client Initialized for 1.21.4!");
    }
}
