package com.tpvp.hud;

import com.tpvp.config.ModConfig;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

public class KillBannerHud implements HudRenderCallback {
    
    // Kill streak tracking
    public static int killStreak = 0;
    public static long lastKillTime = 0;

    public static void addKill() {
        long now = System.currentTimeMillis();
        // Agar 10 second ke andar dusra kill kiya toh streak badhegi, warna reset
        if (now - lastKillTime < 10000) killStreak++;
        else killStreak = 1;
        lastKillTime = now;
    }

    @Override
    public void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        if (!ModConfig.killBannerEnabled || killStreak == 0) return;

        long elapsed = System.currentTimeMillis() - lastKillTime;
        if (elapsed > 3000) return; // 3 second baad banner gayab

        MinecraftClient client = MinecraftClient.getInstance();
        int screenW = client.getWindow().getScaledWidth();
        
        // Animation Logic (Pop up & Slide down)
        float progress = Math.min(1.0f, elapsed / 300.0f); // 0.3s me pop hoga
        float scale = 1.0f + (float) Math.sin(progress * Math.PI) * 0.5f; // Bouncing pop effect
        float alpha = elapsed > 2500 ? (3000 - elapsed) / 500.0f : 1.0f; // Aakhiri 0.5s me fade out
        
        int yPos = (int) (30 * progress); // Upar se neeche aayega
        
        String text = "";
        String subText = "You killed an enemy!";
        int color = 0xFFFFFF;

        switch (killStreak) {
            case 1: text = "FIRST BLOOD"; color = 0xFFAAAA; break; // Light Red
            case 2: text = "DOUBLE KILL"; color = 0xFFFF55; break; // Yellow
            case 3: text = "TRIPLE KILL"; color = 0xFF55FF; break; // Purple
            case 4: text = "MANIAC!"; color = 0x55FFFF; subText = "Unstoppable!"; break; // Aqua
            default: text = "SAVAGE!!"; color = 0xFF0000; subText = "GODLIKE!"; break; // Red
        }

        context.getMatrices().push();
        context.getMatrices().translate(screenW / 2.0f, yPos, 0);
        context.getMatrices().scale(scale, scale, 1.0f);

        int aColor = ((int)(alpha * 255) << 24);
        
        // Banner Background (Dark Gradient)
        int textW = client.textRenderer.getWidth(text) * 2;
        context.fill(-textW/2 - 20, -10, textW/2 + 20, 25, aColor | 0x000000);
        context.fillGradient(-textW/2 - 20, 25, textW/2 + 20, 27, aColor | color, aColor | 0x000000); // Glowing Underline

        // Epic Text
        context.getMatrices().push();
        context.getMatrices().scale(2.0f, 2.0f, 1.0f); // Title bada hoga
        context.drawTextWithShadow(client.textRenderer, text, -client.textRenderer.getWidth(text) / 2, -4, aColor | color);
        context.getMatrices().pop();

        context.drawTextWithShadow(client.textRenderer, subText, -client.textRenderer.getWidth(subText) / 2, 14, aColor | 0xAAAAAA);

        context.getMatrices().pop();
    }
}
