package com.tpvp.hud;

import com.tpvp.config.ModConfig;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.texture.Sprite;
import net.minecraft.entity.effect.StatusEffectInstance;

import java.util.Collection;

public class PotionHud implements HudRenderCallback {

    @Override
    public void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        if (!ModConfig.potionHudEnabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        // Get active potion effects
        Collection<StatusEffectInstance> effects = client.player.getStatusEffects();
        if (effects.isEmpty()) return;

        int startX = ModConfig.potionHudX;
        int startY = ModConfig.potionHudY;
        
        long time = System.currentTimeMillis();

        for (StatusEffectInstance effect : effects) {
            int durationTicks = effect.getDuration();
            int seconds = durationTicks / 20;

            // --- CINEMATIC BLAST / STRETCH LOGIC (When dying out) ---
            float scaleX = 1.0f, scaleY = 1.0f;
            float alpha = 1.0f;
            int shakeX = 0, shakeY = 0;

            if (seconds <= 3) {
                // Stretching and Blasting Out in the last 3 seconds
                float deathProgress = (3.0f - (durationTicks / 20.0f)); // 0.0 to 3.0
                
                scaleX = 1.0f + (deathProgress * 1.5f); // Stretch Horizontally
                scaleY = 1.0f - (deathProgress * 0.2f); // Squish Vertically
                alpha = Math.max(0f, 1.0f - (deathProgress / 3.0f)); // Fade Out
                
                // Intense Shake before blast
                shakeX = (int) (Math.sin(time / 20.0) * (deathProgress * 5));
                shakeY = (int) (Math.cos(time / 20.0) * (deathProgress * 5));
            } else if (seconds <= 10) {
                // Gentle pulse for warning (10 sec left)
                scaleX = scaleY = 1.0f + (float) Math.sin(time / 100.0) * 0.05f;
                shakeX = (int) (Math.sin(time / 50.0) * 1);
            }

            if (alpha <= 0.01f) continue; // Fully blasted away

            context.getMatrices().push();
            context.getMatrices().translate(startX + shakeX, startY + shakeY, 0);
            context.getMatrices().scale(scaleX, scaleY, 1.0f);

            int aColor = ((int)(alpha * 255) << 24);

            // 1. Sleek Glass Background Box
            context.fill(0, 0, 120, 24, aColor | 0x111111); // Dark inner
            context.fill(-1, -1, 121, 0, aColor | 0x555555); // Top Border
            context.fill(-1, 24, 121, 25, aColor | 0x222222); // Bottom Border

            // 2. FIRE SPIRAL VFX (Circling the Icon)
            int iconCx = 12, iconCy = 12;
            int radius = 10;
            for (int i = 0; i < 3; i++) {
                // 3 Dots circling like a flame helix
                float rot = (time % 2000) / 2000.0f * (float) Math.PI * 2;
                float offset = i * ((float) Math.PI * 2 / 3);
                
                int px = iconCx + (int)(Math.cos(rot + offset) * radius);
                int py = iconCy + (int)(Math.sin(rot + offset) * radius);
                
                // Flame colors (Orange/Yellow/Red)
                int flameColor = aColor | (i == 0 ? 0xFF5500 : (i == 1 ? 0xFFFF00 : 0xFF0000));
                context.fill(px - 1, py - 1, px + 1, py + 1, flameColor);
            }

            // ----------------------------------------------------
            // 3. RENDER CRASH-PROOF POTION ICON
            // ----------------------------------------------------
            Sprite sprite = client.getStatusEffectSpriteManager().getSprite(effect.getEffectType());
            if (sprite != null) {
                // FLAWLESS 1.21.4 FIX: Draws the exact UI Sprite using the GUI textured atlas!
                context.drawSprite(iconCx - 9, iconCy - 9, 0, 18, 18, sprite);
            }

            // ----------------------------------------------------
            // 4. TEXTS (Name and Timer)
            // ----------------------------------------------------
            String name = effect.getEffectType().value().getName().getString();
            // Shorten name if too long to prevent overflowing out of the box
            if (name.length() > 12) name = name.substring(0, 10) + "..";
            
            String timeStr = String.format("%d:%02d", seconds / 60, seconds % 60);
            if (seconds <= 10) timeStr = "§c" + timeStr; // Red warning text
            
            int lvl = effect.getAmplifier() + 1;
            String lvlStr = lvl > 1 ? " " + lvl : "";

            context.drawTextWithShadow(client.textRenderer, name + lvlStr, 30, 4, aColor | 0xFFFFFF);
            context.drawTextWithShadow(client.textRenderer, timeStr, 30, 14, aColor | 0xAAAAAA);

            context.getMatrices().pop();

            // Stack vertically
            startY += (int)(28 * scaleY); 
        }
    }
}
