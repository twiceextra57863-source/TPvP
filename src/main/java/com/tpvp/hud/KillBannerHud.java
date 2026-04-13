package com.tpvp.hud;

import com.tpvp.config.ModConfig;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
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
    private static final Random random = new Random();

    public static void addKill(String kName, Identifier kSkin, String vName, Identifier vSkin, boolean isFriend) {
        long now = System.currentTimeMillis();
        if (now - lastKillTime < 15000 && killerName.equals(kName)) killStreak++; 
        else killStreak = 1;
        killerName = kName; killerSkin = kSkin; victimName = vName; victimSkin = vSkin;
        lastKillTime = now;
    }

    @Override
    public void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        if (!ModConfig.killBannerEnabled || killStreak == 0) return;

        long elapsed = System.currentTimeMillis() - lastKillTime;
        if (elapsed > 5000) return; // 5 Seconds duration

        MinecraftClient client = MinecraftClient.getInstance();
        int screenW = client.getWindow().getScaledWidth();
        
        // --- SLIDING ANIMATION LOGIC ---
        float slideProgress = 1.0f;
        if (elapsed < 500) slideProgress = elapsed / 500.0f; // Slide In
        else if (elapsed > 4500) slideProgress = (5000 - elapsed) / 500.0f; // Slide Out
        
        // Easing for smooth slide
        slideProgress = 1.0f - (float) Math.pow(1.0f - slideProgress, 3); 

        int cardW = 160;
        int cardH = 30;
        int baseX = ModConfig.killFeedX;
        int baseY = ModConfig.killFeedY;
        boolean isRightSide = baseX > screenW / 2;

        // Auto direction: Slide from outside the screen
        int renderX = isRightSide ? (int)(screenW + cardW - (screenW - baseX + cardW) * slideProgress) 
                                  : (int)(-cardW + (baseX + cardW) * slideProgress);

        // Colors
        int themeColor = ModConfig.bannerColorTheme == 0 ? 0xFFFF2222 : (ModConfig.bannerColorTheme == 1 ? 0xFFFFAA00 : 0xFF22FF22);
        String streakText = killStreak > 1 ? " x" + killStreak : "";
        
        context.getMatrices().push();
        context.getMatrices().translate(renderX, baseY, 0);

        // 1. Dark Cyber Background
        context.fill(0, 0, cardW, cardH, 0xDD0A0A0A);
        
        // 2. ELECTRIC ANIMATION BORDER
        long tick = System.currentTimeMillis() / 50;
        int elecx1 = (int)(Math.sin(tick) * 2), elecy1 = (int)(Math.cos(tick) * 2);
        int elecx2 = (int)(Math.cos(tick * 1.5) * 2), elecy2 = (int)(Math.sin(tick * 1.5) * 2);
        
        // Electric jagged lines
        context.fill(-1 + elecx1, -1 + elecy1, cardW + 1 + elecx2, 1, themeColor); // Top
        context.fill(-1 + elecx2, cardH - 1, cardW + 1 + elecx1, cardH + 1 + elecy2, themeColor); // Bottom
        
        // 3. PERFECT 2D FACES (8x8 UV to 20x20 size)
        if (killerSkin != null && victimSkin != null) {
            context.drawTexture(RenderLayer::getGuiTextured, killerSkin, 5, 5, 8f, 8f, 20, 20, 64, 64);
            context.drawTexture(RenderLayer::getGuiTextured, victimSkin, cardW - 25, 5, 8f, 8f, 20, 20, 64, 64);
            
            // Texts
            context.getMatrices().push();
            context.getMatrices().scale(0.8f, 0.8f, 1.0f);
            context.drawTextWithShadow(client.textRenderer, killerName, 35, 10, 0xFFFFFF);
            context.drawTextWithShadow(client.textRenderer, victimName, (int)((cardW - 30) / 0.8f) - client.textRenderer.getWidth(victimName), 10, 0xAAAAAA);
            context.getMatrices().pop();
            
            // Center Sword/Streak
            context.drawCenteredTextWithShadow(client.textRenderer, "⚔" + streakText, cardW / 2, 10, themeColor);
        }
        
        context.getMatrices().pop();
    }
}
