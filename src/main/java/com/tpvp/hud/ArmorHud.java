package com.tpvp.hud;

import com.tpvp.config.ModConfig;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ArmorHud implements HudRenderCallback {
    @Override
    public void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        context.getMatrices().push();
        context.getMatrices().translate(ModConfig.armorX, ModConfig.armorY, 0);
        context.getMatrices().scale(ModConfig.armorScale, ModConfig.armorScale, 1.0f);

        boolean flipX = ModConfig.armorX > (client.getWindow().getScaledWidth() / 2);
        boolean flipY = ModConfig.armorY > (client.getWindow().getScaledHeight() - 60);

        List<ItemStack> armorList = new ArrayList<>();
        client.player.getArmorItems().forEach(armorList::add);
        Collections.reverse(armorList);

        int lowestPercent = 100;
        ItemStack worstItem = null;
        for (ItemStack item : armorList) {
            if (item.isEmpty() || item.getMaxDamage() == 0) continue;
            int percent = 100 - (item.getDamage() * 100 / item.getMaxDamage());
            if (percent <= ModConfig.armorCrackThreshold && percent < lowestPercent) {
                lowestPercent = percent;
                worstItem = item;
            }
        }

        int xOffset = 0, yOffset = 0;
        for (ItemStack item : armorList) {
            if (item.isEmpty()) continue;

            int maxDmg = item.getMaxDamage(), curDmg = item.getDamage();
            int shakeX = 0, shakeY = 0, percent = 100, color = 0x00FF00;

            if (maxDmg > 0) {
                percent = 100 - (curDmg * 100 / maxDmg);
                if (percent <= ModConfig.armorCrackThreshold) color = 0xFF0000;
                else if (percent < 50) color = 0xFFFF00;

                if (item == worstItem) {
                    shakeX = (int) (Math.sin(System.currentTimeMillis() / 20.0) * 1.5);
                    shakeY = (int) (Math.cos(System.currentTimeMillis() / 20.0) * 1.5);
                }
            }

            context.drawItem(item, xOffset + shakeX, yOffset + shakeY);

            // ANIMATED FLOWING CRACK
            if (item == worstItem) {
                float time = (System.currentTimeMillis() % 1000) / 1000.0f; // 0.0 to 1.0
                
                // Red pulsing glow
                float alpha = (float) (Math.sin(System.currentTimeMillis() / 100.0) * 0.3 + 0.3);
                context.fill(xOffset + shakeX, yOffset + shakeY, xOffset + shakeX + 16, yOffset + shakeY + 16, ((int)(alpha * 255) << 24) | 0xFF0000); 
                
                // Flowing Energy Line (Niche jati hui red scanning line)
                int flowY = (int) (time * 16);
                context.fill(xOffset + shakeX + 2, yOffset + shakeY + flowY, xOffset + shakeX + 14, yOffset + shakeY + flowY + 2, 0xCCFF0000);
                
                // Base static crack
                context.fill(xOffset + shakeX + 4, yOffset + shakeY + 6, xOffset + shakeX + 8, yOffset + shakeY + 8, 0xFFFF0000);
                context.fill(xOffset + shakeX + 8, yOffset + shakeY + 8, xOffset + shakeX + 12, yOffset + shakeY + 14, 0xFFFF0000);
            }

            if (maxDmg > 0) {
                String text = percent + "%";
                int textW = client.textRenderer.getWidth(text);
                int textX = ModConfig.armorVertical ? (flipX ? (xOffset - textW - 4) : (xOffset + 18)) : (xOffset + (8 - textW / 2));
                int textY = ModConfig.armorVertical ? (yOffset + 4) : (flipY ? (yOffset - 10) : (yOffset + 18));
                context.drawTextWithShadow(client.textRenderer, text, textX + shakeX, textY + shakeY, color);
            }

            if (ModConfig.armorVertical) yOffset += 22;
            else xOffset += 32;
        }
        context.getMatrices().pop();
    }
}
