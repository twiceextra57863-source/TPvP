package com.tpvp.gui;

import com.tpvp.config.ModConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class TPvPDashboardScreen extends Screen {
    
    // --- STATE VARIABLES ---
    private final Screen parent;
    public String currentTab = "📊 Tier Tracker"; 
    
    private final long initTime; 
    private long clickTime = 0;  

    // Window Layout Dimensions
    public final int winW = 500;
    public final int winH = 280;
    public final int sideW = 130;

    // --- CONSTRUCTOR ---
    public TPvPDashboardScreen(Screen parent) { 
        super(Text.literal("TPvP Dashboard")); 
        this.parent = parent; 
        this.initTime = System.currentTimeMillis();
    }
    
    public TPvPDashboardScreen() { 
        this(null); 
    }

    // --- GETTERS (To safely pass to sub-renderers) ---
    public net.minecraft.client.MinecraftClient getMinecraftClient() { 
        return this.client; 
    }
    
    public net.minecraft.client.font.TextRenderer getTextRenderer() { 
        return this.textRenderer; 
    }

    // ---------------------------------------------------------
    // --- MAIN RENDER LOOP ---
    // ---------------------------------------------------------
    @Override
    public void render(DrawContext context, int mx, int my, float delta) {
        long now = System.currentTimeMillis();
        
        // 1. DYNAMIC TIDAL WAVE BACKGROUND & GLITTER
        context.fillGradient(0, 0, this.width, this.height, 0xDD000000, 0xEE0A0A20); 
        
        float time = (now % 4000) / 4000.0f; // 4s Ocean Loop
        for (int i = 0; i < this.width; i += 20) {
            int waveY = this.height - 30 + (int)(Math.sin((i / 50.0) + (time * Math.PI * 2)) * 15);
            context.fill(i, waveY, i + 20, this.height, 0x4400FFCC); // Cyan Tidal Wave
        }
        
        // Glitter (Stars)
        for(int i = 0; i < 15; i++) {
            int gx = (int)((Math.sin(i * 99 + time * 10) * 0.5 + 0.5) * this.width);
            int gy = (int)((Math.cos(i * 33 - time * 8) * 0.5 + 0.5) * this.height);
            context.fill(gx, gy, gx+2, gy+2, 0x88FFFFFF);
        }
        
        int winX = (this.width - winW) / 2;
        int winY = (this.height - winH) / 2;

        // 2. POP-IN ANIMATION
        float intro = Math.min(1.0f, (now - initTime) / 300.0f);
        float scale = 0.8f + (0.2f * (float)Math.sin(intro * Math.PI / 2)); 
        
        context.getMatrices().push();
        context.getMatrices().translate(this.width / 2f, this.height / 2f, 0);
        context.getMatrices().scale(scale, scale, 1.0f);
        context.getMatrices().translate(-this.width / 2f, -this.height / 2f, 0);

        // 3. REBORN CYBER-NEON THEME
        context.fill(winX - 2, winY - 2, winX + winW + 2, winY + winH + 2, 0xFF00FFCC); // Neon Cyan Border
        context.fillGradient(winX, winY, winX + winW, winY + winH, 0xEE0A0A10, 0xFF050508); // Obsidian Blue Glass
        context.fill(winX, winY, winX + sideW, winY + winH, 0xAA020205); // Sidebar Darker
        
        // Sliding Glass Shine
        int shineX = winX + (int) ((now / 3) % (winW * 2)) - winW;
        context.fillGradient(shineX, winY, shineX + 40, winY + winH, 0x00FFFFFF, 0x22FFFFFF); 

        context.drawCenteredTextWithShadow(this.getTextRenderer(), "§b§lTPvP REBORN", winX + sideW / 2, winY + 15, 0xFFFFFF);

        // 4. DRAW SIDEBAR TABS (ALL 6 TABS)
        drawTab(context, "📊 Tier Tracker", winX, winY + 40, mx, my); 
        drawTab(context, "🚀 Performance", winX, winY + 65, mx, my);
        drawTab(context, "⚔ Combat", winX, winY + 90, mx, my);
        drawTab(context, "🎯 Crosshair", winX, winY + 115, mx, my);
        drawTab(context, "💀 Targets", winX, winY + 140, mx, my);
        drawTab(context, "✨ Effects", winX, winY + 165, mx, my);

        int setX = winX + sideW + 20;
        int setY = winY + 40;

        // 5. DELEGATE TAB RENDERING
        if (currentTab.equals("📊 Tier Tracker")) {
            TierTabRenderer.render(this, context, setX, setY, mx, my, winW, winH);
        } 
        else if (currentTab.equals("🚀 Performance")) {
            PerformanceTabRenderer.render(this, context, setX, setY, mx, my);
        } 
        else if (currentTab.equals("⚔ Combat")) {
            CombatTabRenderer.render(this, context, setX, setY, mx, my);
        } 
        else if (currentTab.equals("✨ Effects")) {
            EffectsTabRenderer.render(this, context, setX, setY, mx, my);
        } 
        else if (currentTab.equals("🎯 Crosshair")) {
            CrosshairTabRenderer.render(this, context, setX, setY, mx, my, winY);
        } 
        else if (currentTab.equals("💀 Targets")) {
            TargetsTabRenderer.render(this, context, setX, winY, mx, my);
        }
        
        context.getMatrices().pop();
        super.render(context, mx, my, delta);
    }

    // ---------------------------------------------------------
    // --- HELPER RENDERING METHODS ---
    // ---------------------------------------------------------
    private void drawTab(DrawContext context, String name, int x, int y, int mx, int my) {
        boolean sel = currentTab.equals(name);
        boolean hov = mx >= x && mx <= x + sideW && my >= y && my <= y + 20;
        
        // Bounce offset
        int offset = (hov && !sel) ? 2 : 0; 
        
        if (sel) { 
            context.fill(x, y + offset, x + sideW, y + 20 + offset, 0x4400FFCC); 
            context.fill(x, y + offset, x + 3, y + 20 + offset, 0xFF00FFCC); 
        } else if (hov) {
            context.fill(x + offset, y, x + sideW, y + 20, 0x22FFFFFF);
        }
        context.drawTextWithShadow(this.getTextRenderer(), name, x + 15 + offset, y + 6 + offset, sel ? 0x00FFCC : 0xAAAAAA);
    }

    public void drawToggle(DrawContext context, String label, int x, int y, boolean value) {
        context.drawTextWithShadow(this.getTextRenderer(), label, x, y + 2, 0xFFFFFF);
        int sx = x + 120;
        
        context.fill(sx, y, sx + 30, y + 12, value ? 0xFF00AA55 : 0xFF333333); 
        if (value) {
            context.fill(sx + 18, y + 1, sx + 29, y + 11, 0xFFFFFFFF); 
        } else {
            context.fill(sx + 1, y + 1, sx + 12, y + 11, 0xFFAAAAAA); 
        }
    }

    // ---------------------------------------------------------
    // --- MOUSE INPUT LOGIC (CLICKS) ---
    // ---------------------------------------------------------
    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        clickTime = System.currentTimeMillis(); 
        
        int winX = (this.width - winW) / 2;
        int winY = (this.height - winH) / 2;
        int setX = winX + sideW + 20;
        int setY = winY + 40;
        
        // Handle Sidebar Tab Clicks
        if (mx >= winX && mx <= winX + sideW) {
            if (my >= winY+40 && my <= winY+60) { currentTab = "📊 Tier Tracker"; TierTabRenderer.onOpen(); return true; }
            else if (my >= winY+65 && my <= winY+85) { currentTab = "🚀 Performance"; return true; }
            else if (my >= winY+90 && my <= winY+110) { currentTab = "⚔ Combat"; return true; }
            else if (my >= winY+115 && my <= winY+135) { currentTab = "🎯 Crosshair"; return true; }
            else if (my >= winY+140 && my <= winY+160) { currentTab = "💀 Targets"; return true; }
            else if (my >= winY+165 && my <= winY+185) { currentTab = "✨ Effects"; return true; }
        }

        // Delegate setting clicks
        if (currentTab.equals("📊 Tier Tracker")) { if (TierTabRenderer.mouseClicked(mx, my, setX, setY)) return true; }
        else if (currentTab.equals("🚀 Performance")) { if (PerformanceTabRenderer.mouseClicked(mx, my, setX, setY)) return true; }
        else if (currentTab.equals("⚔ Combat")) { if (CombatTabRenderer.mouseClicked(mx, my, setX, setY)) return true; }
        else if (currentTab.equals("✨ Effects")) { if (EffectsTabRenderer.mouseClicked(mx, my, setX, setY)) return true; }
        else if (currentTab.equals("🎯 Crosshair")) { if (CrosshairTabRenderer.mouseClicked(mx, my, setX, setY)) return true; }
        else if (currentTab.equals("💀 Targets")) { if (TargetsTabRenderer.mouseClicked(this, mx, my, setX, winY)) return true; }

        return super.mouseClicked(mx, my, button);
    }
    
    // ---------------------------------------------------------
    // --- DRAG, SCROLL AND CLOSE ---
    // ---------------------------------------------------------
    @Override 
    public boolean mouseDragged(double mx, double my, int b, double dx, double dY) { 
        int wy = (this.height - winH) / 2; 
        
        if (currentTab.equals("💀 Targets") && TargetsTabRenderer.mouseDragged(this, mx, my, wy)) return true; 
        
        int setX = (this.width - winW) / 2 + sideW + 20;
        int setY = wy + 40;
        if (currentTab.equals("🚀 Performance") && PerformanceTabRenderer.mouseDragged(mx, my, setX, setY)) return true;
        
        return super.mouseDragged(mx, my, b, dx, dY); 
    }
    
    @Override 
    public boolean mouseReleased(double mx, double my, int b) { 
        TargetsTabRenderer.isDraggingScroll = false; 
        PerformanceTabRenderer.isDraggingSlider = false;
        return super.mouseReleased(mx, my, b); 
    }
    
    @Override 
    public boolean mouseScrolled(double mx, double my, double h, double s) { 
        if (currentTab.equals("💀 Targets")) { 
            TargetsTabRenderer.mouseScrolled(s); 
            return true; 
        } 
        return super.mouseScrolled(mx, my, h, s); 
    }
    
    @Override 
    public void close() { 
        ModConfig.save(); 
        if (this.client != null) {
            this.client.setScreen(this.parent); 
        }
    }
}
