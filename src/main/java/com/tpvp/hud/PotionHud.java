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

        Collection<StatusEffectInstance> effects = client.player.getStatusEffects();
        if (effects.isEmpty()) return;

        int screenW = client.getWindow().getScaledWidth();
        int baseX = ModConfig.potionHudX;
        int baseY = ModConfig.potionHudY;
        boolean isRightSide = baseX > screenW / 2;
        
        long time = System.currentTimeMillis();
        int boxW = 140;
        int boxH = 26;
        int yOffset = 0;

        for (StatusEffectInstance effect : effects) {
            int durationTicks = effect.getDuration();
            int totalTicks = effect.getDuration(); // Approximation for slide-in if just applied (Vanilla doesn't store start time easily, so we use max duration logic if it's very high)
            int seconds = durationTicks / 20;

            float slideProgress = 1.0f;
            float alpha = 1.0f;
            int shakeX = 0, shakeY = 0;

            // --- 1. SLIDE IN ANIMATION (If just applied, e.g., duration is very high or infinite) ---
            // Fake slide-in based on tick modulo or high duration to simulate entrance
            if (durationTicks % 200 > 180) { 
                slideProgress = (200 - (durationTicks % 200)) / 20.0f; 
                slideProgress = 1.0f - (float) Math.pow(1.0f - slideProgress, 3); // Cubic Ease-out
            }

            // --- 2. BURNING ASH / FADE OUT ANIMATION (Last 3 seconds) ---
            if (seconds <= 3) {
                float deathProgress = (3.0f - (durationTicks / 20.0f)); // 0.0 to 3.0
                alpha = Math.max(0f, 1.0f - (deathProgress / 3.0f)); // Smooth Fade Out
                
                // Intense Burning Shake
                shakeX = (int) (Math.sin(time / 20.0) * (deathProgress * 2));
                shakeY = (int) (Math.cos(time / 20.0) * (deathProgress * 2));
            } else if (seconds <= 10) {
                // Gentle pulse for warning (10 sec left)
                shakeX = (int) (Math.sin(time / 50.0) * 1);
            }

            if (alpha <= 0.01f) continue; // Fully burned away

            // Auto direction: Slide from outside the screen
            int renderX = isRightSide ? (int)(screenW + boxW - (screenW - baseX + boxW) * slideProgress) 
                                      : (int)(-boxW + (baseX + boxW) * slideProgress);

            // DYNAMIC COLOR MATCHING
            int potionColor = effect.getEffectType().value().getColor();
            int aColor = ((int)(alpha * 255) << 24);
            int finalColor = (potionColor & 0x00FFFFFF) | aColor;

            context.getMatrices().push();
            context.getMatrices().translate(renderX + shakeX, baseY + yOffset + shakeY, 0);

            Matrix4f pM = context.getMatrices().peek().getPositionMatrix();
            net.minecraft.client.render.VertexConsumerProvider.Immediate imm = client.getBufferBuilders().getEntityVertexConsumers();
            net.minecraft.client.render.VertexConsumer bgBuf = imm.getBuffer(RenderLayer.getGui());

            // ----------------------------------------------------
            // 3. BURNING ASH PARTICLES (VFX) - If dying out
            // ----------------------------------------------------
            if (seconds <= 3) {
                float deathProgress = (3.0f - (durationTicks / 20.0f)); // 0.0 to 3.0
                for (int p = 0; p < 5; p++) {
                    float pLife = ((time + p * 150) % 1000) / 1000.0f; // 0 to 1
                    float px = (float)(Math.sin(time * 0.01 + p) * boxW) + (boxW / 2f);
                    float py = boxH - (pLife * boxH * 2.0f); // Ash flies upward
                    float pSize = 3f * (1.0f - pLife); // Shrinks as it burns
                    int pColor = (potionColor & 0x00FFFFFF) | ((int)((1.0f - pLife) * 200 * alpha) << 24); 
                    
                    RenderUtils3D.drawColorQuad(pM, bgBuf, px, py, pSize, pSize, pColor, 15728880);
                }
            }

            // ----------------------------------------------------
            // 4. SLEEK MODERN BACKGROUND (Cyberpunk Slanted Edges)
            // ----------------------------------------------------
            RenderUtils3D.drawColorQuad(pM, bgBuf, 0f, 0f, 3f, (float)boxH, finalColor, 15728880); // Thick Left Accent
            
            float r = 0.05f, g = 0.05f, b = 0.05f, bgAlpha = alpha * 0.8f;
            RenderUtils3D.drawQuad(pM, bgBuf, 
                3f, 0f, 0f,           
                boxW, 0f, 0f,         
                boxW - 8f, boxH, 0f,  // Slanted Right Edge
                3f, boxH, 0f,         
                r, g, b, bgAlpha, 15728880);
                
            RenderUtils3D.drawColorQuad(pM, bgBuf, 3f, 0f, boxW - 3f, 1f, finalColor, 15728880); // Top Border
            RenderUtils3D.drawColorQuad(pM, bgBuf, 3f, boxH - 1f, boxW - 11f, 1f, finalColor, 15728880); // Bottom Border

            // ----------------------------------------------------
            // 5. GLOWING PULSE AURA BEHIND ICON
            // ----------------------------------------------------
            float pulse = (float) Math.sin(time / 200.0) * 0.5f + 0.5f; 
            int pulseColor = (potionColor & 0x00FFFFFF) | ((int)(alpha * pulse * 120) << 24);
            RenderUtils3D.drawColorQuad(pM, bgBuf, 6f, 4f, 18f, 18f, pulseColor, 15728880);

            // ----------------------------------------------------
            // 6. FLAWLESS 1.21.4 CRASH-PROOF POTION ICON RENDERER
            // ----------------------------------------------------
            Sprite sprite = client.getStatusEffectSpriteManager().getSprite(effect.getEffectType());
            if (sprite != null) {
                net.minecraft.client.render.VertexConsumer iconBuf = imm.getBuffer(RenderLayer.getTextSeeThrough(sprite.getAtlasId()));
                RenderUtils3D.drawTextureQuad(
                    pM, iconBuf, 
                    7f, 5f, 16f, 16f, 
                    sprite.getMinU(), sprite.getMinV(), sprite.getMaxU(), sprite.getMaxV(), 
                    1f, 1f, 1f, alpha, 15728880
                );
            }
            imm.draw(); // Flush geometry to screen

            // ----------------------------------------------------
            // 7. TEXTS (Name, Level, and Timer)
            // ----------------------------------------------------
            String name = effect.getEffectType().value().getName().getString();
            if (name.length() > 14) name = name.substring(0, 12) + ".."; 
            
            int lvl = effect.getAmplifier() + 1;
            String lvlStr = lvl > 1 ? " " + lvl : "";
            
            String timeStr = String.format("%d:%02d", seconds / 60, seconds % 60);
            int timeColor = seconds <= 10 ? (aColor | 0xFF5555) : (aColor | 0xAAAAAA);

            context.drawTextWithShadow(client.textRenderer, name + lvlStr, 32, 4, finalColor); // Title gets Potion Color
            context.drawTextWithShadow(client.textRenderer, timeStr, 32, 14, timeColor);

            context.getMatrices().pop();

            // Stack vertically for next potion
            yOffset += (boxH + 6); 
        }
    }
}
