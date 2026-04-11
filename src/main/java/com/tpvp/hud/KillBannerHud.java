package com.tpvp.hud;

import com.tpvp.config.ModConfig;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

public class KillBannerHud implements HudRenderCallback {
    
    public static int killStreak = 0;
    public static long lastKillTime = 0;

    public static void addKill() {
        long now = System.currentTimeMillis();
        // Agar 10 second ke andar mara toh streak badhegi
        if (now - lastKillTime < 10000) killStreak++;
        else killStreak = 1;
        lastKillTime = now;
    }

    @Override
    public void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        if (!ModConfig.killBannerEnabled || killStreak == 0) return;

        long elapsed = System.currentTimeMillis() - lastKillTime;
        if (elapsed > 3000) return; // 3 seconds baad hata do

        MinecraftClient client = MinecraftClient.getInstance();
        int screenW = client.getWindow().getScaledWidth();
        
        // Dynamic Animations
        float popIn = Math.min(1.0f, elapsed / 250.0f); // 0.25s me slide in
        float fadeOut = elapsed > 2500 ? (3000 - elapsed) / 500.0f : 1.0f; // Last 0.5s me fade out
        
        int cy = (int) (40 + (10 * (1.0f - popIn))); // Halke se upar se neeche slide hoga
        int cx = screenW / 2;

        String text = "";
        String subText = "ENEMY KILLED";
        int color = 0xFFFFFF;

        switch (killStreak) {
            case 1: text = "FIRST BLOOD"; color = 0xFFFF5555; break; // Light Red
            case 2: text = "DOUBLE KILL"; color = 0xFFFFAA00; break; // Gold/Orange
            case 3: text = "TRIPLE KILL"; color = 0xFFFF55FF; subText = "ON FIRE!"; break; // Pink/Purple
            case 4: text = "MANIAC!"; color = 0xFF55FFFF; subText = "UNSTOPPABLE!"; break; // Aqua
            default: text = "SAVAGE!!"; color = 0xFFFF0000; subText = "GODLIKE!"; break; // Deep Red
        }

        // Transparency Alpha
        int aColor = ((int)(fadeOut * 255) << 24);
        int finalColor = (color & 0x00FFFFFF) | aColor;
        int subColor = (0xAAAAAA & 0x00FFFFFF) | aColor;

        context.getMatrices().push();
        
        // 1. Sleek Thin Expanding Line (Modern MOBA look)
        float lineWidth = 120.0f * popIn;
        context.fill((int)(cx - lineWidth), cy + 10, (int)(cx + lineWidth), cy + 11, finalColor);
        context.fill((int)(cx - lineWidth), cy - 14, (int)(cx + lineWidth), cy + 10, aColor | 0x000000); // Dark transparent bg

        // 2. Main Banner Text
        context.getMatrices().push();
        context.getMatrices().translate(cx, cy - 10, 0);
        context.getMatrices().scale(1.5f, 1.5f, 1.0f);
        context.drawTextWithShadow(client.textRenderer, text, -client.textRenderer.getWidth(text) / 2, 0, finalColor);
        context.getMatrices().pop();

        // 3. Sub Text
        context.drawTextWithShadow(client.textRenderer, subText, cx - client.textRenderer.getWidth(subText) / 2, cy + 14, subColor);

        context.getMatrices().pop();
    }
}
