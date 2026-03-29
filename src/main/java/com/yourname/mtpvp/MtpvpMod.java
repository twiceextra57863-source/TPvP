package com.mtpvp;

import com.mtpvp.config.MtpvpConfig;
import com.mtpvp.renderer.HealthRenderer;
import net.fabricmc.api.ClientModInitializer;

public class MtpvpMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MtpvpConfig.load(); // Load settings first
        HealthRenderer.init();
    }
}
