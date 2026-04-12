package com.tpvp.hud;

import com.tpvp.config.ModConfig;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;

public class KillBannerHud implements HudRenderCallback {
    
    public static int killStreak = 0;
    public static long lastKillTime = 0;
    
    public static String killerName = "";
    public static Identifier killerSkin = null;
    public static String victimName = "";
    public static Identifier victimSkin = null;

    // Trigger this from Indicator3D
    public static void addKill(String kName, Identifier kSkin, String vName, Identifier vSkin) {
        long now = System.currentTimeMillis();
        if (now - lastKillTime < 15000) killStreak++; // 15 seconds combo time
        else killStreak = 1;
        
        killerName = kName;
        killerSkin = kSkin;
        victimName = vName;
        victimSkin = vSkin;
        lastKillTime = now;
    }

    @Override
    public void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        if (!ModConfig.killBannerEnabled || killStreak == 0) return;

        long elapsed = System.currentTimeMillis() - lastKillTime;
        if (elapsed > 4000) return; // Show for 4 seconds

        MinecraftClient client = MinecraftClient.getInstance();
        int screenW = client.getWindow().getScaledWidth();
        
        // Custom Slide & Pop Animation
        float popIn = Math.min(1.0f, elapsed / 300.0f); 
        float fadeOut = elapsed > 3500 ? (4000 - elapsed) / 500.0f : 1.0f; 
        
        int cy = (int) (50 + (20 * (1.0f - popIn))); 
        int cx = screenW / 2;

        String title = "";
        String subtitle = "Eliminated";
        int color = 0xFFFFFF;

        // CUSTOM EPIC TEXTS
        switch (killStreak) {
            case 1: title = "INITIAL STRIKE"; color = 0xFFFF3333; break; 
            case 2: title = "CHAIN KILL"; color = 0xFFFFAA00; subtitle = "Double Down!"; break; 
            case 3: title = "MASSACRE"; color = 0xFFFF33FF; subtitle = "Unstoppable Force!"; break; 
            case 4: title = "EXTERMINATION"; color = 0xFF33FFFF; subtitle = "Pure Carnage!"; break; 
            default: title = "GOD OF WAR!!"; color = 0xFFFF0000; subtitle = "Legendary!"; break; 
        }

        int aColor = ((int)(fadeOut * 255) << 24);
        int finalColor = (color & 0x00FFFFFF) | aColor;
        int subColor = (0xAAAAAA & 0x00FFFFFF) | aColor;
        int whiteAlpha = 0xFFFFFF | aColor;

        context.getMatrices().push();
        
        // 1. Dynamic Wide Background
        float bgWidth = 180.0f * popIn;
        context.fill((int)(cx - bgWidth), cy - 20, (int)(cx + bgWidth), cy + 25, aColor | 0x110000); // Dark Red-Black BG
        context.fill((int)(cx - bgWidth), cy - 20, (int)(cx + bgWidth), cy - 18, finalColor); // Top border
        context.fill((int)(cx - bgWidth), cy + 23, (int)(cx + bgWidth), cy + 25, finalColor); // Bottom border

        // 2. Epic Texts
        context.getMatrices().push();
        context.getMatrices().translate(cx, cy - 15, 0);
        context.getMatrices().scale(1.5f, 1.5f, 1.0f);
        context.drawTextWithShadow(client.textRenderer, title, -client.textRenderer.getWidth(title) / 2, 0, finalColor);
        context.getMatrices().pop();
        
        context.drawTextWithShadow(client.textRenderer, subtitle, cx - client.textRenderer.getWidth(subtitle) / 2, cy + 10, subColor);

        // 3. Render Killer & Victim Heads & Names
        if (killerSkin != null && victimSkin != null) {
            // Killer (Left Side)
            context.drawTexture(RenderLayer::getGuiTextured, killerSkin, cx - 140, cy - 10, 8f, 8f, 20, 20, 64, 64);
            context.drawTextWithShadow(client.textRenderer, killerName, cx - 115, cy - 5, whiteAlpha);
            
            // VS Text
            context.drawTextWithShadow(client.textRenderer, "⚔", cx - 70, cy - 5, finalColor);

            // Victim (Right Side)
            context.drawTexture(RenderLayer::getGuiTextured, victimSkin, cx + 120, cy - 10, 8f, 8f, 20, 20, 64, 64);
            context.drawTextWithShadow(client.textRenderer, victimName, cx + 75 - client.textRenderer.getWidth(victimName), cy - 5, whiteAlpha);
        }

        context.getMatrices().pop();
    }
}
