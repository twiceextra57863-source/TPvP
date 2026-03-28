package com.mtpvp;

import net.fabricmc.api.ClientModInitializer; // Ye change hua
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Mtpvp implements ClientModInitializer { // ModInitializer ki jagah ClientModInitializer
    public static final String MOD_ID = "mtpvp";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() { // onInitialize ki jagah onInitializeClient
        LOGGER.info("Mtpvp Client Initialized for 1.21.4 (Fixed)!");
    }
}
