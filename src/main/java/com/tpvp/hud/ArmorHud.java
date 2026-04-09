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
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        MatrixStack matrices = context.getMatrices();
        float time = client.player.age + tickCounter.getTickDelta(true);

        // ==========================================
        // 1. ARMOR HUD RENDERING
        // ==========================================
        if (ModConfig.armorHudEnabled) {
            List<ItemStack> armorList = new ArrayList<>();
            client.player.getArmorItems().forEach(armorList::add);
            Collections.reverse(armorList);

            matrices.push();
            // Z=500 FORCES IT ABOVE CHAT!
            matrices.translate(ModConfig.armorHudX, ModConfig.armorHudY, 500);
            matrices.scale(ModConfig.armorHudScale, ModConfig.armorHudScale, 1.0f);

            int spacingX = ModConfig.armorHudHorizontal ? 40 : 0;
            int spacingY = ModConfig.armorHudHorizontal ? 0 : 22;
            int totalW = ModConfig.armorHudHorizontal ? (4 * 40) : 40;
            int totalH = ModConfig.armorHudHorizontal ? 25 : (4 * 22);

            // Draw Custom Background
            if (ModConfig.armorBgEnabled) {
                int alpha = (int)(ModConfig.armorBgOpacity * 255);
                int bgColor = (alpha << 24) | 0x000000;
                context.fill(-4, -4, totalW, totalH, bgColor);
                context.drawBorder(-4, -4, totalW + 4, totalH + 4, (alpha << 24) | 0x00AAFF);
            }

            int localX = 0, localY = 0;

            for (ItemStack item : armorList) {
                if (item.isEmpty()) {
                    localX += spacingX; localY += spacingY; continue;
                }

                int maxDurability = item.getMaxDamage();
                int currentDamage = item.getDamage();
                int durabilityLeft = maxDurability - currentDamage;
                boolean isDanger = maxDurability > 0 && durabilityLeft <= 15;

                matrices.push();
                // SHAKE EFFECT FOR LOW DURABILITY
                if (isDanger) {
                    float shakeX = MathHelper.sin(time * 2f) * 1.5f;
                    float shakeY = MathHelper.cos(time * 2.5f) * 1.5f;
                    matrices.translate(shakeX, shakeY, 0);
                }

                context.drawItem(item, localX, localY);

                if (maxDurability > 0) {
                    float durPercent = (float) durabilityLeft / maxDurability;
                    int textColor = durPercent < 0.2f ? 0xFFFF3333 : (durPercent < 0.5f ? 0xFFFFAA00 : 0xFF00FF00);

                    int textX = ModConfig.armorHudHorizontal ? localX + 2 : localX + 20;
                    int textY = ModConfig.armorHudHorizontal ? localY + 18 : localY + 4;

                    if (ModConfig.armorHudStyle == 0) {
                        context.drawTextWithShadow(client.textRenderer, (int)(durPercent * 100) + "%", textX, textY, textColor);
                    } else if (ModConfig.armorHudStyle == 1) {
                        int bX = ModConfig.armorHudHorizontal ? localX - 2 : localX + 20;
                        int bY = ModConfig.armorHudHorizontal ? localY + 18 : localY + 6;
                        context.fill(bX - 1, bY - 1, bX + 21, bY + 5, 0xFF000000);
                        context.fill(bX, bY, bX + 20, bY + 4, 0xFF333333);
                        context.fill(bX, bY, bX + (int)(20 * durPercent), bY + 4, textColor);
                    } else {
                        context.drawTextWithShadow(client.textRenderer, durabilityLeft + "", textX, textY, textColor);
                    }

                    // DANGER ALERT OVERLAY (Blinking ⚠️ symbol)
                    if (isDanger && (time % 10 < 5)) { // Blinks every few frames
                        context.drawTextWithShadow(client.textRenderer, "§c§l⚠️", localX + 2, localY + 2, 0xFFFFFF);
                    }
                }
                matrices.pop();
                localX += spacingX; localY += spacingY;
            }
            matrices.pop();
        }

        // ==========================================
        // 2. HELD ITEM RENDERING
        // ==========================================
        if (ModConfig.heldItemEnabled) {
            ItemStack mainHand = client.player.getMainHandStack();
            if (!mainHand.isEmpty()) {
                matrices.push();
                matrices.translate(ModConfig.heldItemX, ModConfig.heldItemY, 500);
                matrices.scale(ModConfig.heldItemScale, ModConfig.heldItemScale, 1.0f);

                if (ModConfig.armorBgEnabled) {
                    int alpha = (int)(ModConfig.armorBgOpacity * 255);
                    context.fill(-4, -4, 24, 24, (alpha << 24) | 0x000000);
                    context.drawBorder(-4, -4, 28, 28, (alpha << 24) | 0xFF00FF); // Purple border
                }
                context.drawItem(mainHand, 2, 2);
                matrices.pop();
            }
        }
    }
}
