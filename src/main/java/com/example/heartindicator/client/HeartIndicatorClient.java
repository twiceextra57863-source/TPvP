package com.example.heartindicator.client;

import com.example.heartindicator.config.ModConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.ingame.GameMenuScreen;

public class HeartIndicatorClient implements ClientModInitializer {
    public static boolean isEnabled() {
        return ModConfig.isEnabled();
    }

    public static void toggle() {
        ModConfig.setEnabled(!ModConfig.isEnabled());
    }

    @Override
    public void onInitializeClient() {
        // No additional setup needed – the mixins will use isEnabled()
        ModConfig.load(); // ensure config is loaded on client start
    }
}
