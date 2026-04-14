package com.tpvp.gui;

import com.tpvp.config.ModConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class TPvPDashboardScreen extends Screen {
    
    // --- VARIABLES ---
    private final Screen parent;
    public String currentTab = "🚀 Performance"; 
    
    private final long initTime; 
    private long clickTime = 0;  
    
    // Window Dimensions
    public final int winW = 480;
    public final int winH = 280;
    public final int sideW = 130;

    // --- CONSTRUCTORS ---
    public TPvPDashboardScreen(Screen parent) { 
        super(Text.literal("TPvP Dashboard")); 
        this.parent = parent; 
        this.initTime = System.currentTimeMillis();
    }
    
    public TPvPDashboardScreen() { 
        this(null); 
    }

    // --- HELPER GETTERS (To avoid Protected Access errors in other classes) ---
    public net.minecraft.client.MinecraftClient getMinecraftClient() { 
        return this.client; 
    }
    
    public net.minecraft.client.font.TextRenderer getTextRenderer() { 
        return this.textRenderer; 
    }

    // ---------------------------------------------------------
    // --- RENDER (DRAWING THE UI) ---
    // ---------------------------------------------------------
    @Override
    public void render(DrawContext context, int mx, int my, float delta) {
        long now = System.currentTimeMillis();
        
        // 1. DYNAMIC TIDAL WAVE BACKGROUND & GLITTER
        context.fillGradient(0, 0, this.width, this.height, 0xDD000000, 0xEE0A0A20); 
        
        float time = (now % 4000) / 4000.0f; // 4s Ocean Wave Loop
        for (int i = 0; i < this.width; i += 20) {
            int waveY = this.height - 30 + (int)(Math.sin((i / 50.0) + (time * Math.PI * 2)) * 15);
            context.fill(i, waveY, i + 20, this.height, 0x4400FFCC); // Cyan Tidal Wave
        }
        
        // Glitter (Stars) in the background
        for(int i = 0; i < 15; i++) {
            int gx = (int)((Math.sin(i * 99 + time * 10) * 0.5 + 0.5) * this.width);
            int gy = (int)((Math.cos(i * 33 - time * 8) * 0.5 + 0.5) * this.height);
            context.fill(gx, gy, gx+2, gy+2, 0x88FFFFFF); // White stars
        }
        
        int winX = (this.width - winW) / 2;
        int winY = (this.height - winH) / 2;

        // 2. INTRO POP-IN ANIMATION
        float intro = Math.min(1.0f, (now - initTime) / 300.0f);
        float scale = 0.8f + (0.2f * (float)Math.sin(intro * Math.PI / 2)); 
        
        context.getMatrices().push();
        context.getMatrices().translate(this.width / 2f, this.height / 2f, 0);
        context.getMatrices().scale(scale, scale, 1.0f);
        context.getMatrices().translate(-this.width / 2f, -this.height / 2f, 0);

        // 3. REBORN CYBER-NEON THEME BOX
        context.fill(winX - 2, winY - 2, winX + winW + 2, winY + winH + 2, 0xFF00FFCC); // Neon Cyan Border
        context.fillGradient(winX, winY, winX + winW, winY + winH, 0xEE0A0A10, 0xFF050508); // Obsidian Blue Glass Inside
        context.fill(winX, winY, winX + sideW, winY + winH, 0xAA020205); // Sidebar Darker Background
        
        // Animated Glass Shine passing over the box
        int shineX = winX + (int) ((now / 3) % (winW * 2)) - winW;
        context.fillGradient(shineX, winY, shineX + 40, winY + winH, 0x00FFFFFF, 0x22FFFFFF); 

        // Mod Title
        context.drawCenteredTextWithShadow(this.getTextRenderer(), "§b§lTPvP REBORN", winX + sideW / 2, winY + 15, 0xFFFFFF);

        // 4. DRAW SIDEBAR TABS (Bounce Animation on Hover)
        drawTab(context, "🚀 Performance", winX, winY + 40, mx, my);
        drawTab(context, "⚔ Combat", winX, winY + 65, mx, my);
        drawTab(context, "🎯 Crosshair", winX, winY + 90, mx, my);
        drawTab(context, "💀 Targets", winX, winY + 115, mx, my);
        drawTab(context, "✨ Effects", winX, winY + 140, mx, my);
        drawTab(context, "🛠 Edit HUD", winX, winY + 165, mx, my);

        // 5. DELEGATE RENDERING TO INDIVIDUAL TAB CLASSES
        int setX = winX + sideW + 20;
        int setY = winY + 40;

        if (currentTab.equals("🚀 Performance")) {
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
        
        context.getMatrices().pop(); // Pop Intro Animation scale
        super.render(context, mx, my, delta);
    }

    // ---------------------------------------------------------
    // --- HELPER: TAB DRAWER (With Hover Bounce) ---
    // ---------------------------------------------------------
    private void drawTab(DrawContext context, String name, int x, int y, int mx, int my) {
        boolean isSelected = currentTab.equals(name);
        boolean isHovered = mx >= x && mx <= x + sideW && my >= y && my <= y + 20;
        
        // Slightly move text to right if hovering (Bouncy feel)
        int offset = (isHovered && !isSelected) ? 2 : 0; 
        
        if (isSelected) { 
            context.fill(x, y + offset, x + sideW, y + 20 + offset, 0x4400FFCC); // Transparent Cyan BG
            context.fill(x, y + offset, x + 3, y + 20 + offset, 0xFF00FFCC); // Solid Cyan Marker Line
        } else if (isHovered) {
            context.fill(x + offset, y, x + sideW, y + 20, 0x22FFFFFF); // White hover BG
        }
        
        // Draw Text
        context.drawTextWithShadow(this.getTextRenderer(), name, x + 15 + offset, y + 6 + offset, isSelected ? 0x00FFCC : 0xAAAAAA);
    }

    // ---------------------------------------------------------
    // --- HELPER: APPLE/ANDROID STYLE TOGGLE SWITCH ---
    // ---------------------------------------------------------
    public void drawToggle(DrawContext context, String label, int x, int y, boolean value) {
        context.drawTextWithShadow(this.getTextRenderer(), label, x, y + 2, 0xFFFFFF);
        
        int sx = x + 120; // Switch background
        context.fill(sx, y, sx + 30, y + 12, value ? 0xFF00AA55 : 0xFF333333); // Green if ON, Gray if OFF
        
        // Sliding Knob
        if (value) {
            context.fill(sx + 18, y + 1, sx + 29, y + 11, 0xFFFFFFFF); // Knob ON (Right side)
        } else {
            context.fill(sx + 1, y + 1, sx + 12, y + 11, 0xFFAAAAAA); // Knob OFF (Left side)
        }
    }

    // ---------------------------------------------------------
    // --- MOUSE INPUT LOGIC (CLICKS) ---
    // ---------------------------------------------------------
    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        this.clickTime = System.currentTimeMillis(); 
        
        int winX = (this.width - winW) / 2;
        int winY = (this.height - winH) / 2;
        int setX = winX + sideW + 20;
        int setY = winY + 40;
        
        // 1. Sidebar Tab Switching Clicks
        if (mx >= winX && mx <= winX + sideW) {
            if (my >= winY+40 && my <= winY+60) { currentTab = "🚀 Performance"; return true; }
            if (my >= winY+65 && my <= winY+85) { currentTab = "⚔ Combat"; return true; }
            if (my >= winY+90 && my <= winY+110) { currentTab = "🎯 Crosshair"; return true; }
            if (my >= winY+115 && my <= winY+135) { currentTab = "💀 Targets"; return true; }
            if (my >= winY+140 && my <= winY+160) { currentTab = "✨ Effects"; return true; }
            if (my >= winY+165 && my <= winY+185) { 
                this.client.setScreen(new EditHudScreen(this)); // Switch Screen!
                return true; 
            }
        }

        // 2. Delegate settings clicks to respective Tab Renderer classes
        if (currentTab.equals("🚀 Performance")) { 
            if (PerformanceTabRenderer.mouseClicked(mx, my, setX, setY)) return true; 
        } 
        else if (currentTab.equals("⚔ Combat")) { 
            if (CombatTabRenderer.mouseClicked(mx, my, setX, setY)) return true; 
        } 
        else if (currentTab.equals("✨ Effects")) { 
            if (EffectsTabRenderer.mouseClicked(mx, my, setX, setY)) return true; 
        } 
        else if (currentTab.equals("🎯 Crosshair")) { 
            if (CrosshairTabRenderer.mouseClicked(mx, my, setX, setY)) return true; 
        } 
        else if (currentTab.equals("💀 Targets")) { 
            if (TargetsTabRenderer.mouseClicked(this, mx, my, setX, winY)) return true; 
        }

        return super.mouseClicked(mx, my, button);
    }
    
    // ---------------------------------------------------------
    // --- MOUSE DRAG & SCROLL LOGIC ---
    // ---------------------------------------------------------
    @Override 
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) { 
        int winY = (this.height - winH) / 2; 
        int setX = (this.width - winW) / 2 + sideW + 20;
        int setY = winY + 40;

        // Delegate dragged events to tabs that need them (Targets Scrollbar, Performance Slider)
        if (currentTab.equals("💀 Targets") && TargetsTabRenderer.mouseDragged(this, mx, my, winY)) {
            return true; 
        }
        if (currentTab.equals("🚀 Performance") && PerformanceTabRenderer.mouseDragged(mx, my, setX, setY)) {
            return true;
        }

        return super.mouseDragged(mx, my, button, dx, dy); 
    }
    
    @Override 
    public boolean mouseReleased(double mx, double my, int button) { 
        // Reset dragging flags globally
        TargetsTabRenderer.isDraggingScroll = false; 
        PerformanceTabRenderer.isDraggingSlider = false; 
        return super.mouseReleased(mx, my, button); 
    }
    
    @Override 
    public boolean mouseScrolled(double mx, double my, double horizontalAmount, double scrollAmount) { 
        // Delegate scroll to Targets tab
        if (currentTab.equals("💀 Targets")) { 
            TargetsTabRenderer.mouseScrolled(scrollAmount); 
            return true; 
        } 
        return super.mouseScrolled(mx, my, horizontalAmount, scrollAmount); 
    }
    
    @Override 
    public void close() { 
        // Automatically save configuration when pressing Escape
        ModConfig.save(); 
        if (this.client != null) {
            this.client.setScreen(this.parent); // Go back to Title Screen / Esc Menu
        }
    }
}
