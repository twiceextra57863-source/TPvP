package com.tpvp;

import com.tpvp.config.ModConfig;
import com.tpvp.hud.Indicator3D;
import com.tpvp.hud.NearbyPlayersHud;
import com.tpvp.hud.ArmorHud;
import com.tpvp.hud.KillBannerHud;
import com.tpvp.hud.PotionHud;
import com.tpvp.hud.PvPStatsManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public class TPvPClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // --- LOAD SAVED DATA WHEN GAME STARTS ---
        ModConfig.load();
        PvPStatsManager.load();
        
        // 3D Rendering Registers
        Indicator3D.register();
        
        // 2D HUD Registers
        HudRenderCallback.EVENT.register(new NearbyPlayersHud());
        HudRenderCallback.EVENT.register(new ArmorHud());
        HudRenderCallback.EVENT.register(new KillBannerHud());
        HudRenderCallback.EVENT.register(new PotionHud()); 

        // --- SHUTDOWN HOOK: ALWAYS SAVE BEFORE GAME CLOSES ---
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            ModConfig.save();
            PvPStatsManager.save();
        }));
    }
}
