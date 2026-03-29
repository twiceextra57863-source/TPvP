package com.mtpvp;

import com.mtpvp.config.MtpvpConfig;
import net.fabricmc.api.ClientModInitializer;

public class MtpvpMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MtpvpConfig.load();
        System.out.println("Mtpvp Client with Mixin Rendering Loaded!");
    }
}
