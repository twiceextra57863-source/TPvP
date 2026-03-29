package com.mtpvp;

import com.mtpvp.gui.MtpvpDashboard;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import org.lwjgl.glfw.GLFW;

public class Mtpvp implements ClientModInitializer {
    public static KeyBinding targetKey;

    @Override
    public void onInitializeClient() {
        targetKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.mtpvp.target", 
                InputUtil.Type.KEYSYM, 
                GLFW.GLFW_KEY_R, 
                "category.mtpvp"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (targetKey.wasPressed()) {
                // Quick Target Logic: Look at a player and press R
                if (client.targetedEntity instanceof PlayerEntity target) {
                    MtpvpDashboard.targetPlayerName = target.getName().getString();
                    client.player.sendMessage(net.minecraft.text.Text.of("§c§l[MTPVP] §fTarget Set: §e" + MtpvpDashboard.targetPlayerName), true);
                } else {
                    MtpvpDashboard.targetPlayerName = ""; // Clear target if looking at air
                }
            }
        });
    }
}
