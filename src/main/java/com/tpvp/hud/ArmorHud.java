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

        List<ItemStack> armorList = new ArrayList<>();
        client.player.getArmorItems().forEach(armorList::add);
        Collections.reverse(armorList); // Helmet to Boots

        MatrixStack matrices = context.getMatrices();
        float time = client.player.age + tickCounter.getTickDelta(true);

        matrices.push();
        // Z=1000 moves it above everything including Chat
        matrices.translate(ModConfig.armorHudX, ModConfig.armorHudY, 1000);
        matrices.scale(ModConfig.armorHudScale, ModConfig.armorHudScale, 1.0f);

        int spacingX = ModConfig.armorHudHorizontal ? 40 : 0;
        int spacingY = ModConfig.armorHudHorizontal ? 0 : 22;
        int totalW = ModConfig.armorHudHorizontal ? (ModConfig.heldItemEnabled ? 200 : 160) : 40;
        int totalH = ModConfig.armorHudHorizontal ? 25 : 100;

        // --- GLASS BACKGROUND EFFECT ---
        if (ModConfig.armorBgEnabled) {
            int alpha = (int)(ModConfig.armorBgOpacity * 255);
            context.fill(-4, -4, totalW, totalH, (alpha << 24) | 0x111111);
            context.drawBorder(-4, -4, totalW + 4, totalH + 4, ModConfig.armorBorderColor | (alpha << 24));
            // Top Shine (Glass feel)
            context.fill(-4, -4, totalW, -3, 0x44FFFFFF);
        }

        int localX = 0, localY = 0;
        for (int i = 0; i < armorList.size(); i++) {
            ItemStack item = armorList.get(i);
            
            // DRAW HELD ITEM NEXT TO LEGGINGS (Index 2 in reversed list is Leggings)
            if (i == 2 && ModConfig.heldItemEnabled) {
                ItemStack mainHand = client.player.getMainHandStack();
                if (!mainHand.isEmpty()) {
                    context.drawItem(mainHand, localX - 22, localY);
                    context.drawBorder(localX - 24, localY - 2, 20, 20, 0x55FFFFFF);
                }
            }

            if (!item.isEmpty()) {
                int maxDur = item.getMaxDamage();
                int currentDur = maxDur - item.getDamage();
                boolean isDanger = maxDur > 0 && currentDur <= 15;

                matrices.push();
                if (isDanger) {
                    matrices.translate(MathHelper.sin(time * 3f), MathHelper.cos(time * 3f), 0);
                }

                context.drawItem(item, localX, localY);
                if (maxDur > 0) {
                    float pct = (float) currentDur / maxDur;
                    int color = pct < 0.2f ? 0xFFFF3333 : (pct < 0.5f ? 0xFFFFAA00 : 0xFF00FF00);
                    
                    if (ModConfig.armorHudStyle == 0) context.drawTextWithShadow(client.textRenderer, (int)(pct*100)+"%", localX+18, localY+4, color);
                    else if (ModConfig.armorHudStyle == 1) {
                        context.fill(localX+18, localY+12, localX+38, localY+15, 0xFF000000);
                        context.fill(localX+18, localY+12, localX+18+(int)(20*pct), localY+15, color);
                    } else context.drawTextWithShadow(client.textRenderer, currentDur+"", localX+18, localY+4, color);
                    
                    if (isDanger && (time % 10 < 5)) context.drawTextWithShadow(client.textRenderer, "⚠", localX+2, localY+2, 0xFFFF0000);
                }
                matrices.pop();
            }
            localX += spacingX; localY += spacingY;
        }
        matrices.pop();
    }
}
