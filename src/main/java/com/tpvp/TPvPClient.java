package com.tpvp;

import com.tpvp.config.ModConfig;
import com.tpvp.hud.Indicator3D;
import net.fabricmc.api.ClientModInitializer;

public class TPvPClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Config load
        ModConfig.load();
        
        // Naya 3D Name Tag Indicator start karna
        Indicator3D.register();
    }
}
