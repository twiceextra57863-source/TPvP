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
    public static boolean wasFriend = false; 

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
        if (elapsed > 5000) return; // 5 Seconds duration

        MinecraftClient client = MinecraftClient.getInstance();
        int screenW = client.getWindow().getScaledWidth();
        
        // SLIDING ANIMATION LOGIC
        float slideProgress = 1.0f;
        if (elapsed < 500) slideProgress = elapsed / 500.0f; // Slide In
        else if (elapsed > 4500) slideProgress = (5000 - elapsed) / 500.0f; // Slide Out
        
        // Easing for smooth cinematic slide
        slideProgress = 1.0f - (float) Math.pow(1.0f - slideProgress, 3); 

        int cardW = 160;
        int cardH = 34;
        int baseX = ModConfig.killFeedX;
        int baseY = ModConfig.killFeedY;
        boolean isRightSide = baseX > screenW / 2;

        int renderX = isRightSide ? (int)(screenW + cardW - (screenW - baseX + cardW) * slideProgress) 
                                  : (int)(-cardW + (baseX + cardW) * slideProgress);

        String title = "";
        String streakText = killStreak > 1 ? " x" + killStreak : "";
        int color = 0xFFFFFF;

        // EPIC TEXT & COLOR THEMES
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

        // Apply Banner Theme setting
        if (!wasFriend) {
            color = ModConfig.bannerColorTheme == 0 ? 0xFFFF2222 : (ModConfig.bannerColorTheme == 1 ? 0xFFFFD700 : 0xFF00FF22);
        }

        context.getMatrices().push();
        context.getMatrices().translate(renderX, baseY, 0);

        // 1. CYBER DARK BACKGROUND
        context.fill(0, 0, cardW, cardH, 0xDD0A0A0A);
        
        // 2. ELECTRIC ANIMATION BORDER (VFX)
        long tick = System.currentTimeMillis() / 50;
        int elecx1 = (int)(Math.sin(tick) * 2), elecy1 = (int)(Math.cos(tick) * 2);
        int elecx2 = (int)(Math.cos(tick * 1.5) * 2), elecy2 = (int)(Math.sin(tick * 1.5) * 2);
        
        // Animated Lightning Edge
        context.fill(-1 + elecx1, -1 + elecy1, cardW + 1 + elecx2, 1, color); // Top edge
        context.fill(-1 + elecx2, cardH - 1, cardW + 1 + elecx1, cardH + 1 + elecy2, color); // Bottom edge
        
        // 3. PERFECT 2D HEADS (RADAR MAPPING LOGIC!)
        if (killerSkin != null && victimSkin != null) {
            // Draw Killer Head (Texture UV 8,8, size 8x8 stretched to 20x20)
            context.drawTexture(RenderLayer::getGuiTextured, killerSkin, 6, 7, 8f, 8f, 20, 20, 64, 64);
            
            // Draw Victim Head
            context.drawTexture(RenderLayer::getGuiTextured, victimSkin, cardW - 26, 7, 8f, 8f, 20, 20, 64, 64);
            
            // 4. TEXT RENDERING
            context.getMatrices().push();
            context.getMatrices().scale(0.85f, 0.85f, 1.0f);
            
            // Killer Name
            context.drawTextWithShadow(client.textRenderer, killerName, 36, 12, color);
            
            // Victim Name (Right Aligned)
            int vW = client.textRenderer.getWidth(victimName);
            context.drawTextWithShadow(client.textRenderer, victimName, (int)((cardW - 32) / 0.85f) - vW, 12, 0xAAAAAA);
            context.getMatrices().pop();
            
            // Center Sword Icon & Streak
            context.drawCenteredTextWithShadow(client.textRenderer, "⚔" + streakText, cardW / 2, 7, 0xAAAAAA);
            
            // Epic Title below Sword
            context.getMatrices().push();
            context.getMatrices().scale(0.7f, 0.7f, 1.0f);
            context.drawCenteredTextWithShadow(client.textRenderer, title, (int)((cardW / 2) / 0.7f), 24, color);
            context.getMatrices().pop();
        }
        
        context.getMatrices().pop();
    }
}
