package com.mtpvp;

import com.mtpvp.config.MtpvpConfig;
import net.fabricmc.api.ClientModInitializer;

public class MtpvpMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MtpvpConfig.load(); // Ab error nahi dega
        System.out.println("Mtpvp Client Mod Initialized Successfully!");
    }
}
