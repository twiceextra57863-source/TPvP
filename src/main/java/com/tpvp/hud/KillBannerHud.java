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
        if (elapsed > 5000) return; // 5 Seconds pop

        MinecraftClient client = MinecraftClient.getInstance();
        int screenW = client.getWindow().getScaledWidth();
        
        // SLIDE ANIMATION
        float popIn = Math.min(1.0f, elapsed / 300.0f); 
        float fadeOut = elapsed > 4500 ? (5000 - elapsed) / 500.0f : 1.0f; 
        
        // Easing function for smooth slide
        float slide = 1.0f - (float) Math.pow(1.0f - popIn, 3); 

        // Movable coordinates from config
        int cardW = 180;
        int cardH = 34;
        int baseX = ModConfig.killFeedX;
        int baseY = ModConfig.killFeedY;

        // Auto direction: Left side slides from left edge, Right side slides from right edge
        boolean isRightSide = baseX > screenW / 2;
        int renderX = isRightSide ? (int)(screenW + cardW - (screenW - baseX + cardW) * slide) 
                                  : (int)(-cardW + (baseX + cardW) * slide);

        String streakText = killStreak > 1 ? "x" + killStreak : "";
        int color = ModConfig.bannerColorTheme == 0 ? 0xFFFF2222 : (ModConfig.bannerColorTheme == 1 ? 0xFFFFD700 : 0xFF00FF22);
        
        int aColor = ((int)(fadeOut * 255) << 24);
        int finalColor = (color & 0x00FFFFFF) | aColor;
        int whiteAlpha = 0xFFFFFF | aColor;

        context.getMatrices().push();
        context.getMatrices().translate(renderX, baseY, 0);
        
        // --- MODERN NOTIFICATION CARD ---
        context.fill(0, 0, cardW, cardH, aColor | 0x111111); // Dark Glass Background
        context.fill(isRightSide ? cardW - 2 : 0, 0, isRightSide ? cardW : 2, cardH, finalColor); // Highlight Border

        // RENDER 2D FACES (Perfect Scale)
        if (killerSkin != null && victimSkin != null) {
            context.drawTexture(RenderLayer::getGuiTextured, killerSkin, 8, 7, 8f, 8f, 20, 20, 64, 64);
            context.drawTexture(RenderLayer::getGuiTextured, victimSkin, cardW - 28, 7, 8f, 8f, 20, 20, 64, 64);
            
            // Text Scale
            context.getMatrices().push();
            context.getMatrices().scale(0.85f, 0.85f, 1.0f);
            context.drawTextWithShadow(client.textRenderer, killerName, 36, 12, finalColor);
            
            int vW = client.textRenderer.getWidth(victimName);
            context.drawTextWithShadow(client.textRenderer, victimName, (int)((cardW - 32) / 0.85f) - vW, 12, whiteAlpha);
            context.getMatrices().pop();
            
            // Center Kill Icon
            context.drawCenteredTextWithShadow(client.textRenderer, "⚔" + streakText, cardW / 2, 12, aColor | 0xAAAAAA);
        }

        context.getMatrices().pop();
    }
}
