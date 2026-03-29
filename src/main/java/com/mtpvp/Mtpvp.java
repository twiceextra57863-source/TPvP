package com.mtpvp;

import com.mtpvp.gui.MtpvpDashboard;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class MtpvpClient implements ClientModInitializer {
    public static KeyBinding targetKey;

    @Override
    public void onInitializeClient() {
        // 'R' Keybind for Target
        targetKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.mtpvp.target",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "MTPVP Client"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (targetKey.wasPressed()) {
                if (client.targetedEntity instanceof PlayerEntity target) {
                    MtpvpDashboard.targetPlayerName = target.getGameProfile().getName();
                    client.player.sendMessage(Text.literal("§c§l[MTPVP] §fTarget Locked: §e" + MtpvpDashboard.targetPlayerName), true);
                } else {
                    MtpvpDashboard.targetPlayerName = "";
                    client.player.sendMessage(Text.literal("§7§l[MTPVP] §fTarget Cleared"), true);
                }
            }
        });
    }
}
