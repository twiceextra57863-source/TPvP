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
    public static boolean wasFriend = false; // NAYA: Friend Revenge System

    // Called from DeadSoulRenderer.java
    public static void addKill(String kName, Identifier kSkin, String vName, Identifier vSkin, boolean isFriend) {
        long now = System.currentTimeMillis();
        if (now - lastKillTime < 15000) killStreak++; // 15 seconds combo time
        else killStreak = 1;
        
        killerName = kName;
        killerSkin = kSkin;
        victimName = vName;
        victimSkin = vSkin;
        wasFriend = isFriend;
        lastKillTime = now;
    }

    @Override
    public void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        if (!ModConfig.killBannerEnabled || killStreak == 0) return;

        long elapsed = System.currentTimeMillis() - lastKillTime;
        if (elapsed > 4000) return; // Show for 4 seconds

        MinecraftClient client = MinecraftClient.getInstance();
        int screenW = client.getWindow().getScaledWidth();
        
        // Dynamic Slide-in from Top
        float popIn = Math.min(1.0f, elapsed / 300.0f); 
        float fadeOut = elapsed > 3500 ? (4000 - elapsed) / 500.0f : 1.0f; 
        
        int cy = (int) (60 + (30 * (1.0f - popIn))); // Bada drop
        int cx = screenW / 2;

        String title = "";
        String subtitle = killerName + " eliminated " + victimName;
        int color = 0xFFFFFF;

        // --- DYNAMIC TEXTS ---
        if (wasFriend) {
            title = "FRIEND DOWN!";
            color = 0xFFFF0000; // Blood Red
            subtitle = "Avenge " + victimName + " immediately!";
        } else {
            switch (killStreak) {
                case 1: title = "FIRST BLOOD"; color = 0xFFFF3333; break; 
                case 2: title = "DOUBLE KILL"; color = 0xFFFFAA00; subtitle = "Two down!"; break; 
                case 3: title = "TRIPLE KILL"; color = 0xFFFF33FF; subtitle = "Unstoppable Force!"; break; 
                case 4: title = "MANIAC!"; color = 0xFF33FFFF; subtitle = "Pure Carnage!"; break; 
                default: title = "SAVAGE!!"; color = 0xFFFF0000; subtitle = "GODLIKE!"; break; 
            }
        }

        int aColor = ((int)(fadeOut * 255) << 24);
        int finalColor = (color & 0x00FFFFFF) | aColor;
        int subColor = (0xAAAAAA & 0x00FFFFFF) | aColor;
        int whiteAlpha = 0xFFFFFF | aColor;

        context.getMatrices().push();
        
        // ----------------------------------------------------
        // 1. TORN PAPER BACKGROUND EFFECT (Jagged Quads)
        // ----------------------------------------------------
        float bgW = 200.0f * popIn;
        // Base dark parchment
        context.fill((int)(cx - bgW), cy - 25, (int)(cx + bgW), cy + 30, aColor | 0x221100); 
        
        // Jagged edges (Torn Paper Feel)
        int segments = 20;
        float segW = (bgW * 2) / segments;
        for (int i = 0; i < segments; i++) {
            float startX = (cx - bgW) + (i * segW);
            float endX = startX + segW;
            int yJitterTop = (i % 2 == 0) ? -28 : -23;
            int yJitterBot = (i % 3 == 0) ? 33 : 28;
            
            context.fill((int)startX, cy + yJitterTop, (int)endX, cy - 25, aColor | 0x442200); // Top torn
            context.fill((int)startX, cy + 30, (int)endX, cy + yJitterBot, aColor | 0x442200); // Bottom torn
        }
        
        // Top/Bottom Color Strikes
        context.fill((int)(cx - bgW), cy - 25, (int)(cx + bgW), cy - 23, finalColor); 
        context.fill((int)(cx - bgW), cy + 28, (int)(cx + bgW), cy + 30, finalColor); 

        // ----------------------------------------------------
        // 2. EPIC TEXT RENDERING
        // ----------------------------------------------------
        context.getMatrices().push();
        context.getMatrices().translate(cx, cy - 15, 0);
        context.getMatrices().scale(1.8f, 1.8f, 1.0f); // Bigger Title
        context.drawTextWithShadow(client.textRenderer, title, -client.textRenderer.getWidth(title) / 2, 0, finalColor);
        context.getMatrices().pop();
        
        context.drawTextWithShadow(client.textRenderer, subtitle, cx - client.textRenderer.getWidth(subtitle) / 2, cy + 12, subColor);

        // ----------------------------------------------------
        // 3. KILLER & VICTIM 2D FACES (Left and Right)
        // ----------------------------------------------------
        if (killerSkin != null && victimSkin != null) {
            // Killer Face
            context.drawTexture(RenderLayer::getGuiTextured, killerSkin, cx - 160, cy - 12, 8f, 8f, 24, 24, 64, 64);
            context.drawTextWithShadow(client.textRenderer, killerName, cx - 130, cy - 4, whiteAlpha);
            
            // VS Text
            context.drawTextWithShadow(client.textRenderer, "⚔", cx - 80, cy - 4, finalColor);

            // Victim Face
            context.drawTexture(RenderLayer::getGuiTextured, victimSkin, cx + 136, cy - 12, 8f, 8f, 24, 24, 64, 64);
            context.drawTextWithShadow(client.textRenderer, victimName, cx + 130 - client.textRenderer.getWidth(victimName), cy - 4, whiteAlpha);
        }

        context.getMatrices().pop();
    }
}
