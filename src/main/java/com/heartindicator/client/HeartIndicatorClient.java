package com.heartindicator.client;

import com.heartindicator.HeartIndicatorMod;
import com.heartindicator.config.ModConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class HeartIndicatorClient implements ClientModInitializer {

    public static KeyBinding toggleKey;

    @Override
    public void onInitializeClient() {
        ModConfig.load();

        // Register toggle keybind  (default: H)
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.heartindicator.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                "category.heartindicator"
        ));

        // Tick listener for keybind
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.wasPressed()) {
                ModConfig cfg = ModConfig.get();
                cfg.indicatorEnabled = !cfg.indicatorEnabled;
                ModConfig.save();

                String msg = cfg.indicatorEnabled
                        ? "§a[HeartIndicator] §fEnabled ✔"
                        : "§c[HeartIndicator] §fDisabled ✘";

                if (client.player != null) {
                    client.player.sendMessage(Text.of(msg), true); // action bar
                }
            }
        });

        HeartIndicatorMod.LOGGER.info("[HeartIndicator] Client initialized.");
    }
}
