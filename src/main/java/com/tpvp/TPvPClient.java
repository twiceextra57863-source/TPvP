package com.tpvp;

import com.tpvp.config.ModConfig;
import com.tpvp.hud.IndicatorHud;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public class TPvPClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Config load karna
        ModConfig.load();
        
        // HUD render event register karna
        HudRenderCallback.EVENT.register(new IndicatorHud());
    }
}
