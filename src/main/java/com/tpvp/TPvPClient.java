package com.tpvp;

import com.tpvp.config.ModConfig;
import com.tpvp.hud.Indicator3D;
import com.tpvp.hud.NearbyPlayersHud;
import com.tpvp.hud.ArmorHud;
import com.tpvp.hud.KillBannerHud;
import com.tpvp.hud.PotionHud; // NAYA IMPORT!
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public class TPvPClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModConfig.load();
        
        Indicator3D.register();
        
        HudRenderCallback.EVENT.register(new NearbyPlayersHud());
        HudRenderCallback.EVENT.register(new ArmorHud());
        HudRenderCallback.EVENT.register(new KillBannerHud());
        HudRenderCallback.EVENT.register(new PotionHud()); // Potion HUD Zinda ho gaya!
    }
}
