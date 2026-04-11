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

        int screenW = client.getWindow().getScaledWidth();
        int screenH = client.getWindow().getScaledHeight();
        
        boolean flipX = ModConfig.armorX > (screenW / 2);
        boolean flipY = ModConfig.armorY > (screenH - 60);

        List<ItemStack> armorList = new ArrayList<>();
        client.player.getArmorItems().forEach(armorList::add);
        Collections.reverse(armorList);

        int xOffset = 0;
        int yOffset = 0;
        // FIX: Spacing badha di gayi hai taaki overlap na ho
        int vSpace = 22; 
        int hSpace = 32; 

        for (ItemStack item : armorList) {
            if (item.isEmpty()) continue;

            int maxDmg = item.getMaxDamage();
            int curDmg = item.getDamage();
            int shakeX = 0, shakeY = 0;
            int percent = 100;
            int color = 0x00FF00;

            if (maxDmg > 0) {
                percent = 100 - (curDmg * 100 / maxDmg);
                if (percent < 15) {
                    color = 0xFF0000;
                    shakeX = (int) (Math.sin(System.currentTimeMillis() / 30.0) * 2);
                    shakeY = (int) (Math.cos(System.currentTimeMillis() / 30.0) * 2);
                } else if (percent < 50) {
                    color = 0xFFFF00;
                }
            }

            context.drawItem(item, xOffset + shakeX, yOffset + shakeY);

            if (maxDmg > 0) {
                String text = percent + "%";
                int textW = client.textRenderer.getWidth(text);
                int textX, textY;
                if (ModConfig.armorVertical) {
                    textX = flipX ? (xOffset - textW - 4) : (xOffset + 18);
                    textY = yOffset + 4;
                } else { 
                    textX = xOffset + (8 - textW / 2);
                    textY = flipY ? (yOffset - 10) : (yOffset + 18);
                }
                context.drawTextWithShadow(client.textRenderer, text, textX + shakeX, textY + shakeY, color);
            }

            if (ModConfig.armorVertical) yOffset += vSpace;
            else xOffset += hSpace;
        }
        context.getMatrices().pop();
    }
}
