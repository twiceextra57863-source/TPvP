package com.tpvp.hud;

import com.tpvp.config.ModConfig;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer; // 100% Native Head Drawer!
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;

import java.util.Random;

public class KillBannerHud implements HudRenderCallback {
    
    public static int killStreak = 0;
    public static long lastKillTime = 0;
    
    public static String killerName = "";
    public static Identifier killerSkin = null;
    public static String victimName = "";
    public static Identifier victimSkin = null;
    public static boolean wasFriend = false; 

    private static final Random random = new Random();

    public static void addKill(String kName, Identifier kSkin, String vName, Identifier vSkin, boolean isFriend) {
        long now = System.currentTimeMillis();
        if (now - lastKillTime < 15000 && killerName.equals(kName)) killStreak++; 
        else killStreak = 1;
        
        killerName = kName; killerSkin = kSkin;
        victimName = vName; victimSkin = vSkin;
        wasFriend = isFriend;
        lastKillTime = now;
    }

    @Override
    public void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        if (!ModConfig.killBannerEnabled || killStreak == 0) return;

        long elapsed = System.currentTimeMillis() - lastKillTime;
        if (elapsed > 5000) return; // 5 Seconds pop duration

        MinecraftClient client = MinecraftClient.getInstance();
        int screenW = client.getWindow().getScaledWidth();
        
        // --- SMOOTH SLIDE ANIMATION ---
        float slideProgress = 1.0f;
        if (elapsed < 500) slideProgress = elapsed / 500.0f; // Slide In
        else if (elapsed > 4500) slideProgress = (5000 - elapsed) / 500.0f; // Slide Out
        slideProgress = 1.0f - (float) Math.pow(1.0f - slideProgress, 3); // Cubic Easing

        int cardW = 180;
        int cardH = 34;
        int baseX = ModConfig.killFeedX;
        int baseY = ModConfig.killFeedY;

        // Auto direction logic based on screen position
        boolean isRightSide = baseX > screenW / 2;
        int cx = isRightSide ? (int)(screenW + cardW - (screenW - baseX + cardW) * slideProgress) 
                             : (int)(-cardW + (baseX + cardW) * slideProgress);

        String title = "";
        String streakText = killStreak > 1 ? " x" + killStreak : "";
        int color = 0xFFFFFF;

        // --- EPIC TEXTS ---
        if (wasFriend) {
            title = "FRIEND DOWN!"; color = 0xFFFF0000; 
        } else {
            switch (killStreak) {
                case 1: title = "FIRST STRIKE"; color = 0xFFFF5555; break; 
                case 2: title = "DOUBLE KILL"; color = 0xFFFFAA00; break; 
                case 3: title = "TRIPLE KILL"; color = 0xFFFF55FF; break; 
                case 4: title = "QUADRA CRUSH"; color = 0xFF55FFFF; break; 
                case 5: title = "PENTA WIPE"; color = 0xFFFF0000; break; 
                default: title = "GODLIKE!!"; color = 0xFFFFD700; break; 
            }
        }

        // Apply Custom Theme Colors
        if (!wasFriend) {
            color = ModConfig.bannerColorTheme == 0 ? 0xFFFF2222 : (ModConfig.bannerColorTheme == 1 ? 0xFFFFD700 : 0xFF00FF22);
        }

        context.getMatrices().push();
        context.getMatrices().translate(cx, baseY, 0);

        // 1. DARK CYBER BACKGROUND
        context.fill(0, 0, cardW, cardH, 0xEE050505);
        
        // 2. ELECTRIC / LIGHTNING BORDER PARTICLES (Top & Bottom)
        long tick = System.currentTimeMillis() / 40;
        
        // Chaotic electric offsets
        int eX1 = (int)(Math.sin(tick * 2.3) * 4);
        int eY1 = (int)(Math.cos(tick * 1.7) * 2);
        int eX2 = (int)(Math.sin(tick * 3.1) * 4);
        int eY2 = (int)(Math.cos(tick * 2.1) * 2);

        // Draw lightning lines
        context.fill(-2 + eX1, -1 + eY1, cardW + 2 + eX2, 1, color); // Top Lightning
        context.fill(-2 + eX2, cardH - 1, cardW + 2 + eX1, cardH + 1 + eY2, color); // Bottom Lightning

        // ----------------------------------------------------
        // 3. 100% PERFECT 2D HEAD RENDERING (NO RAW SKIN GLITCH)
        // ----------------------------------------------------
        if (killerSkin != null && victimSkin != null) {
            
            // Draw Killer Head (Left Side) - Using Vanilla PlayerSkinDrawer!
            context.fill(4, 4, 26, 26, color); // Glowing Border behind head
            PlayerSkinDrawer.draw(context, killerSkin, 5, 5, 20); // Exact 20x20 pixel Face
            
            // Draw Victim Head (Right Side)
            context.fill(cardW - 26, 4, cardW - 4, 26, 0xFFFF0000); // Red Border
            PlayerSkinDrawer.draw(context, victimSkin, cardW - 25, 5, 20); // Exact 20x20 pixel Face
            
            // 4. TEXT RENDERING
            context.getMatrices().push();
            context.getMatrices().scale(0.85f, 0.85f, 1.0f);
            
            // Killer Name
            context.drawTextWithShadow(client.textRenderer, killerName, 35, 12, color);
            
            // Victim Name (Right Aligned to Head)
            int vW = client.textRenderer.getWidth(victimName);
            context.drawTextWithShadow(client.textRenderer, victimName, (int)((cardW - 30) / 0.85f) - vW, 12, 0xAAAAAA);
            context.getMatrices().pop();
            
            // Center Sword & Kill Streak Number
            context.drawCenteredTextWithShadow(client.textRenderer, "⚔" + streakText, cardW / 2, 7, 0xAAAAAA);
            
            // Epic Title Banner
            context.getMatrices().push();
            context.getMatrices().scale(0.7f, 0.7f, 1.0f);
            context.drawCenteredTextWithShadow(client.textRenderer, title, (int)((cardW / 2) / 0.7f), 24, color);
            context.getMatrices().pop();
        }

        context.getMatrices().pop();
    }
}
