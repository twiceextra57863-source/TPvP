package com.yourname.mtpvp;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MtpvpMod implements ModInitializer {
    public static final String MOD_ID = "mtpvp";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    
    @Override
    public void onInitialize() {
        LOGGER.info("MTPVP Mod Initialized!");
    }
    
    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }
}
