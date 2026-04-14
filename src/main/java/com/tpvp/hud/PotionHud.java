package com.tpvp.hud;

import com.tpvp.config.ModConfig;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.texture.Sprite;
import net.minecraft.entity.effect.StatusEffectInstance;
import org.joml.Matrix4f;

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
            Matrix4f pM = context.getMatrices().peek().getPositionMatrix();
            net.minecraft.client.render.VertexConsumerProvider.Immediate imm = client.getBufferBuilders().getEntityVertexConsumers();
            net.minecraft.client.render.VertexConsumer bgBuf = imm.getBuffer(RenderLayer.getGui());

            // 1. Sleek Glass Background Box (Drawn via RenderUtils3D)
            RenderUtils3D.drawColorQuad(pM, bgBuf, 0f, 0f, 120f, 24f, aColor | 0x111111, 15728880); // Dark inner
            RenderUtils3D.drawColorQuad(pM, bgBuf, -1f, -1f, 122f, 1f, aColor | 0x555555, 15728880); // Top Border
            RenderUtils3D.drawColorQuad(pM, bgBuf, -1f, 24f, 122f, 1f, aColor | 0x222222, 15728880); // Bottom Border

            // 2. FIRE SPIRAL VFX (Circling the Icon)
            float iconCx = 12f, iconCy = 12f;
            float radius = 10f;
            for (int i = 0; i < 3; i++) {
                float rot = (time % 2000) / 2000.0f * (float) Math.PI * 2;
                float offset = i * ((float) Math.PI * 2 / 3);
                
                float px = iconCx + (float) Math.cos(rot + offset) * radius;
                float py = iconCy + (float) Math.sin(rot + offset) * radius;
                
                int flameColor = aColor | (i == 0 ? 0xFF5500 : (i == 1 ? 0xFFFF00 : 0xFF0000));
                RenderUtils3D.drawColorQuad(pM, bgBuf, px - 1f, py - 1f, 2f, 2f, flameColor, 15728880);
            }

            // ----------------------------------------------------
            // 3. FLAWLESS CRASH-PROOF POTION ICON RENDERER (Using RenderUtils3D)
            // ----------------------------------------------------
            Sprite sprite = client.getStatusEffectSpriteManager().getSprite(effect.getEffectType());
            if (sprite != null) {
                // Gets the exact transparent sprite layer
                net.minecraft.client.render.VertexConsumer iconBuf = imm.getBuffer(RenderLayer.getTextSeeThrough(sprite.getAtlasId()));
                
                // Directly maps the sprite UV to our screen using math instead of Minecraft's broken methods!
                RenderUtils3D.drawTextureQuad(
                    pM, iconBuf, 
                    iconCx - 9f, iconCy - 9f, 18f, 18f, // Screen X, Y, Width, Height
                    sprite.getMinU(), sprite.getMinV(), sprite.getMaxU(), sprite.getMaxV(), // Texture UVs
                    1f, 1f, 1f, alpha, 15728880 // Colors and Light
                );
            }
            imm.draw(); // Flush the buffers to render the quads & sprites to screen

            // ----------------------------------------------------
            // 4. TEXTS (Name and Timer)
            // ----------------------------------------------------
            String name = effect.getEffectType().value().getName().getString();
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
