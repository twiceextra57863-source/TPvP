package com.tpvp.hud;

import com.tpvp.config.ModConfig;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

public class KillBannerHud implements HudRenderCallback {
    
    public static int killStreak = 0;
    public static long lastKillTime = 0;

    // Call this method whenever someone dies near the player
    public static void addKill() {
        long now = System.currentTimeMillis();
        if (now - lastKillTime < 10000) killStreak++; // Double kill, Triple kill...
        else killStreak = 1; // Reset to First Blood
        lastKillTime = now;
    }

    @Override
    public void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        if (!ModConfig.killBannerEnabled || killStreak == 0) return;

        long elapsed = System.currentTimeMillis() - lastKillTime;
        if (elapsed > 3000) return; // 3 second baad gayab

        MinecraftClient client = MinecraftClient.getInstance();
        int screenW = client.getWindow().getScaledWidth();
        
        float popIn = Math.min(1.0f, elapsed / 200.0f); // Fast slide in
        float fadeOut = elapsed > 2500 ? (3000 - elapsed) / 500.0f : 1.0f; // Smooth fade out
        
        int cy = (int) (40 + (15 * (1.0f - popIn))); // Slide down effect
        int cx = screenW / 2;

        String text = "";
        String subText = "ENEMY KILLED";
        int color = 0xFFFFFF;

        switch (killStreak) {
            case 1: text = "FIRST BLOOD"; color = 0xFFFF5555; break; 
            case 2: text = "DOUBLE KILL"; color = 0xFFFFAA00; break; 
            case 3: text = "TRIPLE KILL"; color = 0xFFFF55FF; subText = "ON FIRE!"; break; 
            case 4: text = "MANIAC!"; color = 0xFF55FFFF; subText = "UNSTOPPABLE!"; break; 
            default: text = "SAVAGE!!"; color = 0xFFFF0000; subText = "GODLIKE!"; break; 
        }

        int aColor = ((int)(fadeOut * 255) << 24);
        int finalColor = (color & 0x00FFFFFF) | aColor;
        int subColor = (0xAAAAAA & 0x00FFFFFF) | aColor;

        context.getMatrices().push();
        
        // Dynamic Glowing Thin Line
        float lineWidth = 120.0f * popIn;
        context.fill((int)(cx - lineWidth), cy + 10, (int)(cx + lineWidth), cy + 11, finalColor);
        context.fill((int)(cx - lineWidth), cy - 14, (int)(cx + lineWidth), cy + 10, aColor | 0x33000000); 

        // Title Text
        context.getMatrices().push();
        context.getMatrices().translate(cx, cy - 10, 0);
        context.getMatrices().scale(1.5f, 1.5f, 1.0f);
        context.drawTextWithShadow(client.textRenderer, text, -client.textRenderer.getWidth(text) / 2, 0, finalColor);
        context.getMatrices().pop();

        // Sub Text
        context.drawTextWithShadow(client.textRenderer, subText, cx - client.textRenderer.getWidth(subText) / 2, cy + 14, subColor);

        context.getMatrices().pop();
    }
}
