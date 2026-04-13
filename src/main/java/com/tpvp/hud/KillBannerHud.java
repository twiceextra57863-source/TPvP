package com.tpvp.hud;

import com.tpvp.config.ModConfig;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

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
        if (elapsed > 5000) return;

        MinecraftClient client = MinecraftClient.getInstance();
        int screenW = client.getWindow().getScaledWidth();
        
        // --- SMOOTH SLIDE ANIMATION ---
        float slideProgress = 1.0f;
        if (elapsed < 500) slideProgress = elapsed / 500.0f; // Slide In
        else if (elapsed > 4500) slideProgress = (5000 - elapsed) / 500.0f; // Slide Out
        slideProgress = 1.0f - (float) Math.pow(1.0f - slideProgress, 3); // Cubic Easing

        // Make the banner wider for names to fit perfectly
        int cardW = 200; 
        int cardH = 36;
        int baseX = ModConfig.killFeedX;
        int baseY = ModConfig.killFeedY;

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

        if (!wasFriend) {
            if (ModConfig.bannerColorTheme == 0) color = 0xFFFF2222;
            else if (ModConfig.bannerColorTheme == 1) color = 0xFFFFD700;
            else if (ModConfig.bannerColorTheme == 2) color = 0xFF00FF22;
        }

        context.getMatrices().push();
        context.getMatrices().translate(cx, baseY, 0);

        int aColor = ((int)(slideProgress * 255) << 24);
        int finalColor = (color & 0x00FFFFFF) | aColor;
        
        // ----------------------------------------------------
        // 1. STYLISH BACKGROUND (Angled/Slanted Box) & CLOUD PARTICLES
        // ----------------------------------------------------
        Matrix4f mat = context.getMatrices().peek().getPositionMatrix();
        net.minecraft.client.render.VertexConsumerProvider.Immediate imm = client.getBufferBuilders().getEntityVertexConsumers();
        net.minecraft.client.render.VertexConsumer bgBuffer = imm.getBuffer(RenderLayer.getGui());

        // Burning/Cloud Particles flowing upwards behind banner
        long tick = System.currentTimeMillis();
        for (int p = 0; p < 8; p++) {
            float pLife = ((tick + p * 120) % 1000) / 1000.0f; // 0 to 1
            float px = (float)(Math.sin(tick * 0.005 + p) * cardW) + (cardW / 2f);
            float py = cardH - (pLife * cardH * 1.5f); // Flies upward
            float pSize = 4f * (1.0f - pLife); // Shrinks as it goes up
            int pColor = (color & 0x00FFFFFF) | ((int)((1.0f - pLife) * 150 * slideProgress) << 24); 
            
            RenderUtils3D.drawColorQuad(mat, bgBuffer, px, py, pSize, pSize, pColor, 15728880);
        }
        
        // Dark Cyber Background (Rhombus/Slanted Edges)
        int bgC = aColor | 0x0A0A0A;
        RenderUtils3D.drawQuad(mat, bgBuffer, 10, 0, 0, cardW, 0, 0, cardW - 10, cardH, 0, 0, cardH, 0, 
            ((bgC>>16)&255)/255f, ((bgC>>8)&255)/255f, (bgC&255)/255f, ((bgC>>24)&255)/255f, 15728880);

        // ----------------------------------------------------
        // 2. TRUE LIGHTNING BORDERS (Zigzag Lines)
        // ----------------------------------------------------
        net.minecraft.client.render.VertexConsumer lineBuf = imm.getBuffer(RenderLayer.getLines());
        float r = ((color>>16)&255)/255f, g = ((color>>8)&255)/255f, b = (color&255)/255f;
        
        float lx = 10;
        for (int i = 0; i < 8; i++) {
            float nextX = lx + (cardW - 10) / 8.0f;
            float sparkY = (float)(Math.sin(tick * 0.05 + i) * 3);
            // Top Lightning
            RenderUtils3D.drawLine(mat, lineBuf, lx, sparkY, 0, nextX, (float)(Math.cos(tick * 0.05 + i + 1) * 3), 0, r, g, b, slideProgress);
            // Bottom Lightning
            RenderUtils3D.drawLine(mat, lineBuf, lx - 10, cardH + sparkY, 0, nextX - 10, cardH + (float)(Math.cos(tick * 0.05 + i + 1) * 3), 0, r, g, b, slideProgress);
            lx = nextX;
        }

        // ----------------------------------------------------
        // 3. MINECRAFT SWORD (Pointing to Victim)
        // ----------------------------------------------------
        context.getMatrices().push();
        context.getMatrices().translate(cardW / 2f, cardH / 2f - 2, 0); // Center of banner
        context.getMatrices().scale(1.2f, 1.2f, 1.0f); // Size of sword
        
        // Sword tilts towards the Victim (Right side)
        context.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(45f)); 
        ItemStack sword = new ItemStack(killStreak >= 3 ? Items.DIAMOND_SWORD : Items.IRON_SWORD);
        
        // Sword drop shadow/glow
        context.fill(-8, -8, 8, 8, aColor | 0x440000); 
        context.drawItem(sword, -8, -8);
        context.getMatrices().pop();

        // Streak Count below sword
        if (!streakText.isEmpty()) {
            context.getMatrices().push();
            context.getMatrices().scale(0.7f, 0.7f, 1.0f);
            context.drawCenteredTextWithShadow(client.textRenderer, streakText, (int)((cardW / 2) / 0.7f), (int)((cardH / 2 + 10) / 0.7f), finalColor);
            context.getMatrices().pop();
        }

        // ----------------------------------------------------
        // 4. FLAWLESS 2D FACES & AUTO-TRIMMED NAMES
        // ----------------------------------------------------
        if (killerSkin != null && victimSkin != null) {
            
            // --- KILLER (Left) ---
            context.fill(4, 7, 26, 29, finalColor); // Gold/Green Border
            context.getMatrices().push();
            context.getMatrices().translate(5, 8, 0);
            context.getMatrices().scale(2.5f, 2.5f, 1.0f); 
            context.drawTexture(RenderLayer::getGuiTextured, killerSkin, 0, 0, 8f, 8f, 8, 8, 64, 64);
            context.getMatrices().pop();
            
            // --- VICTIM (Right) ---
            context.fill(cardW - 26, 7, cardW - 4, 29, aColor | 0xFF0000); // Red Border
            context.getMatrices().push();
            context.getMatrices().translate(cardW - 25, 8, 0);
            context.getMatrices().scale(2.5f, 2.5f, 1.0f);
            context.drawTexture(RenderLayer::getGuiTextured, victimSkin, 0, 0, 8f, 8f, 8, 8, 64, 64);
            context.getMatrices().pop();
            
            // --- TEXT RENDERING (COLLAPSE FIX) ---
            context.getMatrices().push();
            context.getMatrices().scale(0.85f, 0.85f, 1.0f);
            
            // Safe Name Length (Max 12 chars)
            String safeKiller = killerName.length() > 12 ? killerName.substring(0, 10) + ".." : killerName;
            String safeVictim = victimName.length() > 12 ? victimName.substring(0, 10) + ".." : victimName;

            context.drawTextWithShadow(client.textRenderer, safeKiller, 35, 16, finalColor);
            int vW = client.textRenderer.getWidth(safeVictim);
            context.drawTextWithShadow(client.textRenderer, safeVictim, (int)((cardW - 30) / 0.85f) - vW, 16, aColor | 0xAAAAAA);
            context.getMatrices().pop();
            
            // Epic Title on Top
            context.getMatrices().push();
            context.getMatrices().scale(0.8f, 0.8f, 1.0f);
            context.drawCenteredTextWithShadow(client.textRenderer, title, (int)((cardW / 2) / 0.8f), -8, finalColor);
            context.getMatrices().pop();
        }

        context.getMatrices().pop();
        imm.draw(); // Flush particle and lines buffers!
    }
}
