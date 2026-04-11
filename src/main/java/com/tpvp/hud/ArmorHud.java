package com.tpvp.hud;

import com.tpvp.config.ModConfig;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.item.ItemStack;

public class ArmorHud implements HudRenderCallback {
    @Override
    public void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        context.getMatrices().push();
        context.getMatrices().translate(ModConfig.armorX, ModConfig.armorY, 0);
        context.getMatrices().scale(ModConfig.armorScale, ModConfig.armorScale, 1.0f);

        // Smart Alignment: Check karna ki HUD screen ke right side me hai ya left me
        int screenWidth = client.getWindow().getScaledWidth();
        boolean isRightSide = ModConfig.armorX > (screenWidth / 2);
        
        int yOffset = 0;
        
        for (ItemStack item : client.player.getArmorItems()) {
            if (item.isEmpty()) continue;

            // Item Icon Draw Karna
            context.drawItem(item, 0, yOffset);
            context.drawItemInSlot(client.textRenderer, item, 0, yOffset);

            // Damage Percentage Nikalna
            int maxDamage = item.getMaxDamage();
            int currentDamage = item.getDamage();
            if (maxDamage > 0) {
                int percent = 100 - (currentDamage * 100 / maxDamage);
                String text = percent + "%";
                
                int color = 0x00FF00; // Green
                if (percent < 20) color = 0xFF0000; // Red
                else if (percent < 50) color = 0xFFFF00; // Yellow

                // Smart Text System: Right side pe text item ke left me aayega
                if (isRightSide) {
                    int textWidth = client.textRenderer.getWidth(text);
                    context.drawTextWithShadow(client.textRenderer, text, -textWidth - 4, yOffset + 4, color);
                } else {
                    context.drawTextWithShadow(client.textRenderer, text, 18, yOffset + 4, color);
                }
            }
            yOffset += 18;
        }
        context.getMatrices().pop();
    }
}
