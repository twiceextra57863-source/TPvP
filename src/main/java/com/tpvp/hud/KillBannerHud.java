package com.tpvp.hud;

import com.tpvp.config.ModConfig;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

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
        if (now - lastKillTime < 15000) killStreak++; 
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
        
        int cy = (int) (60 + (30 * (1.0f - popIn))); // Drops from above
        int cx = screenW / 2;

        String title = "";
        String subtitle = killerName + " eliminated " + victimName;
        int color = 0xFFFFFF;

        if (wasFriend) {
            title = "FRIEND DOWN!"; color = 0xFFFF0000; subtitle = "Avenge " + victimName + " immediately!";
        } else {
            switch (killStreak) {
                case 1: title = "INITIAL STRIKE"; color = 0xFFFF3333; break; 
                case 2: title = "CHAIN KILL"; color = 0xFFFFAA00; subtitle = "Double Down!"; break; 
                case 3: title = "MASSACRE"; color = 0xFFFF33FF; subtitle = "Unstoppable Force!"; break; 
                case 4: title = "EXTERMINATION"; color = 0xFF33FFFF; subtitle = "Pure Carnage!"; break; 
                default: title = "GOD OF WAR!!"; color = 0xFFFF0000; subtitle = "Legendary!"; break; 
            }
        }

        int aColor = ((int)(fadeOut * 255) << 24);
        int finalColor = (color & 0x00FFFFFF) | aColor;
        int subColor = (0xDDDDDD & 0x00FFFFFF) | aColor;

        context.getMatrices().push();
        
        // 1. TORN PAPER (TREASURE MAP) BACKGROUND
        float bgW = 200.0f * popIn;
        context.fill((int)(cx - bgW), cy - 25, (int)(cx + bgW), cy + 30, aColor | 0x3d2914); // Brown Parchment
        
        int segments = 20;
        float segW = (bgW * 2) / segments;
        for (int i = 0; i < segments; i++) {
            float startX = (cx - bgW) + (i * segW);
            int yTop = (i % 2 == 0) ? -29 : -23;
            int yBot = (i % 3 == 0) ? 34 : 28;
            context.fill((int)startX, cy + yTop, (int)(startX + segW), cy - 25, aColor | 0x2b1c0d); // Darker Torn Edges
            context.fill((int)startX, cy + 30, (int)(startX + segW), cy + yBot, aColor | 0x2b1c0d);
        }
        
        // 2. EPIC TEXT
        context.getMatrices().push();
        context.getMatrices().translate(cx, cy - 18, 0);
        context.getMatrices().scale(1.8f, 1.8f, 1.0f);
        context.drawTextWithShadow(client.textRenderer, title, -client.textRenderer.getWidth(title) / 2, 0, finalColor);
        context.getMatrices().pop();
        
        context.drawTextWithShadow(client.textRenderer, subtitle, cx - client.textRenderer.getWidth(subtitle) / 2, cy + 12, subColor);

        // 3. FULL 3D DOLLS RENDERING ON HUD!
        VertexConsumerProvider.Immediate immediate = client.getBufferBuilders().getEntityVertexConsumers();
        float dollRot = (System.currentTimeMillis() % 4000) / 4000.0f * 360f; // Spinning dolls

        if (killerSkin != null && victimSkin != null) {
            // Killer Doll (Left)
            context.getMatrices().push();
            context.getMatrices().translate(cx - 150, cy + 15, 50); // Z is 50 to render above HUD
            context.getMatrices().scale(30f, 30f, -30f); // 30 pixels big
            context.getMatrices().multiply(RotationAxis.POSITIVE_Y.rotationDegrees(dollRot));
            RenderUtils3D.drawDoll(context.getMatrices(), immediate, killerSkin, fadeOut, 0, 0, 0, 0);
            context.getMatrices().pop();

            context.drawTextWithShadow(client.textRenderer, "⚔", cx - 80, cy - 4, finalColor);

            // Victim Doll (Right)
            context.getMatrices().push();
            context.getMatrices().translate(cx + 150, cy + 15, 50);
            context.getMatrices().scale(30f, 30f, -30f);
            context.getMatrices().multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-dollRot));
            RenderUtils3D.drawDoll(context.getMatrices(), immediate, victimSkin, fadeOut, 0, 0, 0, 0);
            context.getMatrices().pop();
            
            immediate.draw(); // Flush the 3D buffer to the screen!
        }

        context.getMatrices().pop();
    }
}
