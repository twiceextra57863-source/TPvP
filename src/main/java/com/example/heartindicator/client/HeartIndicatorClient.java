package com.example.heartindicator.client;

import com.example.heartindicator.HeartIndicatorMod;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class HeartIndicatorClient implements ClientModInitializer {
    public static boolean showHeartIndicator = true;
    private static KeyBinding toggleKey;

    @Override
    public void onInitializeClient() {
        HeartIndicatorMod.LOGGER.info("Heart Indicator Client Initialized!");
        
        // Register toggle key (H key)
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.heartindicator.toggle",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            "category.heartindicator"
        ));
        
        // Handle toggle key press
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (toggleKey.wasPressed()) {
                showHeartIndicator = !showHeartIndicator;
                HeartIndicatorMod.LOGGER.info("Heart indicator toggled: " + showHeartIndicator);
            }
        });
    }
    
    public static boolean isShowing() {
        return showHeartIndicator;
    }
}
