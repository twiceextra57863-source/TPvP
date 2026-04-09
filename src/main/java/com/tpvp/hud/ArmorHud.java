package com.tpvp.hud;

import com.tpvp.config.ModConfig;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ArmorHud implements HudRenderCallback {
    @Override
    public void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        if (!ModConfig.armorHudEnabled) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        // Armor items fetch karna (Helmet to Boots)
        List<ItemStack> armorList = new ArrayList<>();
        client.player.getArmorItems().forEach(armorList::add);
        Collections.reverse(armorList); 

        MatrixStack matrices = context.getMatrices();
        float time = client.player.age + tickCounter.getTickDelta(true);

        matrices.push();
        // Z=1000 forces the HUD to render ABOVE the chat and other HUD elements
        matrices.translate(ModConfig.armorHudX, ModConfig.armorHudY, 1000);
        matrices.scale(ModConfig.armorHudScale, ModConfig.armorHudScale, 1.0f);

        int spacingX = ModConfig.armorHudHorizontal ? 45 : 0;
        int spacingY = ModConfig.armorHudHorizontal ? 0 : 25;
        
        // Calculate dynamic background size
        int totalW = ModConfig.armorHudHorizontal ? 180 : 50;
        int totalH = ModConfig.armorHudHorizontal ? 30 : 110;

        // GLASS BACKGROUND
        if (ModConfig.armorBgEnabled) {
            int alpha = (int)(ModConfig.armorBgOpacity * 255);
            int bgColor = (alpha << 24) | 0x111111;
            context.fill(-5, -5, totalW, totalH, bgColor);
            context.drawBorder(-5, -5, totalW + 5, totalH + 5, ModConfig.armorBorderColor | (alpha << 24));
            context.fill(-5, -5, totalW, -4, 0x33FFFFFF); // Top shine
        }

        int lx = 0, ly = 0;
        for (int i = 0; i < armorList.size(); i++) {
            ItemStack armor = armorList.get(i);

            // CONNECTED HELD ITEM (Drawn next to Leggings/Index 2)
            if (i == 2 && ModConfig.heldItemEnabled) {
                ItemStack hand = client.player.getMainHandStack();
                if (!hand.isEmpty()) {
                    context.drawItem(hand, lx - 22, ly);
                    context.drawBorder(lx - 24, ly - 2, 20, 20, 0x44FFFFFF);
                }
            }

            if (!armor.isEmpty()) {
                int maxDur = armor.getMaxDamage();
                int curDur = maxDur - armor.getDamage();
                boolean isDanger = maxDur > 0 && curDur <= 15;

                matrices.push();
                // SHAKE ALERT ANIMATION
                if (isDanger) {
                    float shake = MathHelper.sin(time * 4f) * 1.2f;
                    matrices.translate(shake, 0, 0);
                }

                context.drawItem(armor, lx, ly);

                if (maxDur > 0) {
                    float pct = (float) curDur / maxDur;
                    int color = pct < 0.2f ? 0xFFFF3333 : (pct < 0.5f ? 0xFFFFAA00 : 0xFF00FF00);

                    if (ModConfig.armorHudStyle == 0) { // Percent
                        context.drawTextWithShadow(client.textRenderer, (int)(pct*100)+"%", lx+18, ly+4, color);
                    } else if (ModConfig.armorHudStyle == 1) { // Bar
                        context.fill(lx+18, ly+12, lx+38, ly+15, 0xFF000000);
                        context.fill(lx+18, ly+12, lx+18+(int)(20*pct), ly+15, color);
                    } else { // Number
                        context.drawTextWithShadow(client.textRenderer, String.valueOf(curDur), lx+18, ly+4, color);
                    }

                    // Blinking ⚠ Alert
                    if (isDanger && (time % 10 < 5)) {
                        context.drawTextWithShadow(client.textRenderer, "⚠", lx+2, ly+2, 0xFFFF0000);
                    }
                }
                matrices.pop();
            }
            lx += spacingX; ly += spacingY;
        }
        matrices.pop();
    }
}
