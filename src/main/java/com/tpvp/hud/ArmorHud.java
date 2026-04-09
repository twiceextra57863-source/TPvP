package com.tpvp.hud;

import com.tpvp.config.ModConfig;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ArmorHud implements HudRenderCallback {
    @Override
    public void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        if (!ModConfig.armorHudEnabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        // Player ki armor list nikalna aur reverse karna (Helmet upar/pehle dikhne ke liye)
        List<ItemStack> armorList = new ArrayList<>();
        client.player.getArmorItems().forEach(armorList::add);
        Collections.reverse(armorList);

        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(ModConfig.armorHudX, ModConfig.armorHudY, 0);
        matrices.scale(ModConfig.armorHudScale, ModConfig.armorHudScale, 1.0f);

        int localX = 0;
        int localY = 0;
        int spacingX = ModConfig.armorHudHorizontal ? 40 : 0;
        int spacingY = ModConfig.armorHudHorizontal ? 0 : 20;

        for (ItemStack item : armorList) {
            if (item.isEmpty()) continue;

            // Item Icon Draw Karna (Real Minecraft Item)
            context.drawItem(item, localX, localY);

            // Durability Calculations
            int maxDurability = item.getMaxDamage();
            int currentDamage = item.getDamage();
            int durabilityLeft = maxDurability - currentDamage;
            
            if (maxDurability > 0) {
                float durPercent = (float) durabilityLeft / maxDurability;
                
                // Color Code based on durability
                int textColor = 0xFF00FF00; // Green
                if (durPercent < 0.2f) textColor = 0xFFFF3333; // Red
                else if (durPercent < 0.5f) textColor = 0xFFFFAA00; // Orange

                int textX = localX + 18;
                int textY = localY + 4;

                if (ModConfig.armorHudHorizontal) {
                    textX = localX + 2; textY = localY + 18; // Text below item for horizontal
                }

                // --------- STYLE 0: PERCENTAGE ---------
                if (ModConfig.armorHudStyle == 0) {
                    String text = (int)(durPercent * 100) + "%";
                    context.drawTextWithShadow(client.textRenderer, text, textX, textY, textColor);
                } 
                // --------- STYLE 1: STATUS BAR ---------
                else if (ModConfig.armorHudStyle == 1) {
                    int barWidth = 20;
                    int barHeight = 4;
                    int curWidth = (int)(barWidth * durPercent);
                    
                    int bX = ModConfig.armorHudHorizontal ? localX - 2 : localX + 18;
                    int bY = ModConfig.armorHudHorizontal ? localY + 18 : localY + 6;

                    context.fill(bX - 1, bY - 1, bX + barWidth + 1, bY + barHeight + 1, 0xFF000000); // Border
                    context.fill(bX, bY, bX + barWidth, bY + barHeight, 0xFF333333); // Background
                    context.fill(bX, bY, bX + curWidth, bY + barHeight, textColor); // Health fill
                } 
                // --------- STYLE 2: NUMBERS ---------
                else if (ModConfig.armorHudStyle == 2) {
                    String text = durabilityLeft + "/" + maxDurability;
                    context.drawTextWithShadow(client.textRenderer, text, textX, textY, textColor);
                }
            }

            localX += spacingX;
            localY += spacingY;
        }
        matrices.pop();
    }
}
