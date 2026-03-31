package com.tpvp;

import com.tpvp.config.ModConfig;
import com.tpvp.hud.Indicator3D;
import com.tpvp.hud.NearbyPlayersHud;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public class TPvPClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModConfig.load();
        
        // 3D Hit Indicator register
        Indicator3D.register();

        // 2D Nearby Players Radar HUD register
        HudRenderCallback.EVENT.register(new NearbyPlayersHud());
    }
}
