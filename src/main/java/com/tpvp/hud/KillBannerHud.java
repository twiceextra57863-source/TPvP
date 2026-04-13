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
        if (elapsed > 4000) return;

        MinecraftClient client = MinecraftClient.getInstance();
        int screenW = client.getWindow().getScaledWidth();
        
        float popIn = Math.min(1.0f, elapsed / 300.0f); 
        float fadeOut = elapsed > 3500 ? (4000 - elapsed) / 500.0f : 1.0f; 
        
        int cy = (int) (60 + (30 * (1.0f - popIn))); 
        int cx = screenW / 2;

        String title = "";
        String subtitle = killerName + " eliminated " + victimName;
        int color = 0xFFFFFF;

        // 6 UNIQUE CUSTOM TEXTS!
        if (wasFriend) {
            title = "FRIEND FALLEN!"; color = 0xFFFF0000; subtitle = "Avenge " + victimName + " immediately!";
        } else {
            switch (killStreak) {
                case 1: title = "FIRST STRIKE"; color = 0xFFFFAA00; break; // Gold
                case 2: title = "TWIN TAKEDOWN"; color = 0xFFFF5555; subtitle = "Double Down!"; break; // Red
                case 3: title = "TRINITY SMASH"; color = 0xFFFF33FF; subtitle = "Unstoppable Force!"; break; // Purple
                case 4: title = "QUADRA CRUSH"; color = 0xFF33FFFF; subtitle = "Total Annihilation!"; break; // Aqua
                case 5: title = "PENTA WIPE"; color = 0xFFFF0000; subtitle = "Unbelievable!!"; break; // Dark Red
                default: title = "OBLITERATION!"; color = 0xFFFFD700; subtitle = "LEGENDARY!!"; break; // Deep Gold
            }
        }

        int aColor = ((int)(fadeOut * 255) << 24);
        int finalColor = (color & 0x00FFFFFF) | aColor;
        int whiteAlpha = 0xFFFFFF | aColor;

        context.getMatrices().push();
        
        // WIDE MOBA STYLE BANNER BACKGROUND
        float bgW = 160.0f * popIn;
        // Base Red Gradient
        context.fillGradient((int)(cx - bgW), cy - 20, (int)(cx + bgW), cy + 20, aColor | 0x880000, aColor | 0x330000); 
        // Golden Borders
        context.fill((int)(cx - bgW), cy - 22, (int)(cx + bgW), cy - 20, aColor | 0xFFD700); 
        context.fill((int)(cx - bgW), cy + 20, (int)(cx + bgW), cy + 22, aColor | 0xFFD700); 

        // EPIC TEXT
        context.getMatrices().push();
        context.getMatrices().translate(cx, cy - 14, 0);
        context.getMatrices().scale(1.5f, 1.5f, 1.0f);
        context.drawTextWithShadow(client.textRenderer, title, -client.textRenderer.getWidth(title) / 2, 0, finalColor);
        context.getMatrices().pop();
        
        context.drawTextWithShadow(client.textRenderer, subtitle, cx - client.textRenderer.getWidth(subtitle) / 2, cy + 8, whiteAlpha);

        // RENDER PLAYER FACES ON EDGES
        if (killerSkin != null && victimSkin != null) {
            // Killer Face (Left)
            context.fill(cx - 162, cy - 18, cx - 128, cy + 18, aColor | 0xFFD700); // Gold Border
            context.drawTexture(RenderLayer::getGuiTextured, killerSkin, cx - 160, cy - 16, 8f, 8f, 32, 32, 64, 64);
            
            // VS Text
            context.drawTextWithShadow(client.textRenderer, "⚔", cx - 90, cy - 4, finalColor);

            // Victim Face (Right)
            context.fill(cx + 128, cy - 18, cx + 162, cy + 18, aColor | 0xFF0000); // Red Border
            context.drawTexture(RenderLayer::getGuiTextured, victimSkin, cx + 130, cy - 16, 8f, 8f, 32, 32, 64, 64);
        }

        context.getMatrices().pop();
    }
}
