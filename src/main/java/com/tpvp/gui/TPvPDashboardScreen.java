package com.tpvp.gui;

import com.tpvp.config.ModConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class TPvPDashboardScreen extends Screen {
    private final Screen parent;
    public String currentTab = "Combat";
    
    // Window Layout
    public final int winW = 460;
    public final int winH = 260;
    public final int sideW = 120;

    public TPvPDashboardScreen(Screen parent) { 
        super(Text.literal("TPvP Dashboard")); 
        this.parent = parent; 
    }
    
    public TPvPDashboardScreen() { this(null); }

    @Override
    public void render(DrawContext context, int mx, int my, float delta) {
        context.fill(0, 0, this.width, this.height, 0xDD050000); // Full Screen Dark Overlay
        
        int winX = (this.width - winW) / 2;
        int winY = (this.height - winH) / 2;

        // RUBY RED GLASS THEME
        context.fill(winX - 2, winY - 2, winX + winW + 2, winY + winH + 2, 0xFFFF2222); // Outer Glowing Red Border
        context.fillGradient(winX, winY, winX + winW, winY + winH, 0xEE440000, 0xFF110000); // Inner Red Glass Gradient
        context.fill(winX, winY, winX + sideW, winY + winH, 0xAA220000); // Sidebar Darker Red
        
        // ANIMATED SHINE EFFECT
        long time = System.currentTimeMillis();
        int shineX = winX + (int) ((time / 3) % (winW * 2)) - winW;
        context.fillGradient(shineX, winY, shineX + 40, winY + winH, 0x00FFFFFF, 0x22FFFFFF); 

        context.drawCenteredTextWithShadow(this.textRenderer, "§c§lTPvP CLIENT", winX + sideW / 2, winY + 15, 0xFFFFFF);

        // DRAW SIDEBAR TABS
        drawTab(context, "Combat", winX, winY + 40, mx, my);
        drawTab(context, "Crosshair", winX, winY + 65, mx, my);
        drawTab(context, "Targets", winX, winY + 90, mx, my);
        drawTab(context, "✨ Effects", winX, winY + 115, mx, my);
        drawTab(context, "🛠 Edit HUD", winX, winY + 140, mx, my);

        int setX = winX + sideW + 20;
        int setY = winY + 40;

        // DELEGATE RENDERING TO TAB MANAGERS
        if (currentTab.equals("Combat")) {
            CombatTabRenderer.render(this, context, setX, setY, mx, my);
        } else if (currentTab.equals("✨ Effects")) { 
            EffectsTabRenderer.render(this, context, setX, setY, mx, my);
        } else if (currentTab.equals("Crosshair")) {
            CrosshairTabRenderer.render(this, context, setX, setY, mx, my, winY);
        } else if (currentTab.equals("Targets")) {
            TargetsTabRenderer.render(this, context, setX, winY, mx, my);
        }
        
        super.render(context, mx, my, delta);
    }

    private void drawTab(DrawContext context, String name, int x, int y, int mx, int my) {
        boolean sel = currentTab.equals(name);
        boolean hov = mx >= x && mx <= x + sideW && my >= y && my <= y + 20;
        
        if (sel) { 
            context.fill(x, y, x + sideW, y + 20, 0x55FF0000); 
            context.fill(x, y, x + 3, y + 20, 0xFFFF0000); 
        } else if (hov) {
            context.fill(x, y, x + sideW, y + 20, 0x22FFFFFF);
        }
        context.drawTextWithShadow(this.textRenderer, name, x + 15, y + 6, sel ? 0xFF5555 : 0xAAAAAA);
    }

    // HELPER: Custom Apple/Android Style Switch Drawer
    public void drawToggle(DrawContext context, String label, int x, int y, boolean value) {
        context.drawTextWithShadow(this.textRenderer, label, x, y + 2, 0xFFFFFF);
        int sx = x + 110;
        context.fill(sx, y, sx + 30, y + 12, value ? 0xFFCC0000 : 0xFF333333); 
        if (value) context.fill(sx + 18, y + 1, sx + 29, y + 11, 0xFFFFFFFF); 
        else context.fill(sx + 1, y + 1, sx + 12, y + 11, 0xFFAAAAAA); 
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        int winX = (this.width - winW) / 2;
        int winY = (this.height - winH) / 2;
        
        // Sidebar Clicks
        if (mx >= winX && mx <= winX + sideW) {
            if (my >= winY+40 && my <= winY+60) currentTab = "Combat";
            else if (my >= winY+65 && my <= winY+85) currentTab = "Crosshair";
            else if (my >= winY+90 && my <= winY+110) currentTab = "Targets";
            else if (my >= winY+115 && my <= winY+135) currentTab = "✨ Effects";
            else if (my >= winY+140 && my <= winY+160) this.client.setScreen(new EditHudScreen(this));
            return true;
        }

        int setX = winX + sideW + 20;
        int setY = winY + 40;

        // DELEGATE CLICKS
        if (currentTab.equals("Combat")) {
            if (CombatTabRenderer.mouseClicked(mx, my, setX, setY)) return true;
        } else if (currentTab.equals("✨ Effects")) {
            if (EffectsTabRenderer.mouseClicked(mx, my, setX, setY)) return true;
        } else if (currentTab.equals("Crosshair")) {
            if (CrosshairTabRenderer.mouseClicked(mx, my, setX, setY)) return true;
        } else if (currentTab.equals("Targets")) {
            if (TargetsTabRenderer.mouseClicked(this, mx, my, setX, winY)) return true;
        }

        return super.mouseClicked(mx, my, button);
    }
    
    @Override 
    public boolean mouseDragged(double mx, double my, int button, double dX, double dY) {
        if (currentTab.equals("Targets") && TargetsTabRenderer.mouseDragged(this, mx, my, winY)) return true;
        return super.mouseDragged(mx, my, button, dX, dY);
    }
    
    @Override 
    public boolean mouseReleased(double mx, double my, int button) { 
        TargetsTabRenderer.isDraggingScroll = false; 
        return super.mouseReleased(mx, my, button); 
    }
    
    @Override 
    public boolean mouseScrolled(double mx, double my, double hAmount, double scroll) { 
        if (currentTab.equals("Targets")) { 
            TargetsTabRenderer.mouseScrolled(scroll); 
            return true; 
        } 
        return super.mouseScrolled(mx, my, hAmount, scroll); 
    }
    
    @Override 
    public void close() { 
        ModConfig.save(); 
        if (this.client != null) this.client.setScreen(this.parent); 
    }
}
