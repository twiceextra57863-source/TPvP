package com.tpvp.gui;

import com.tpvp.config.ModConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class TPvPDashboardScreen extends Screen {
    private final Screen parent;
    public String currentTab = "🚀 Performance"; 
    
    private final long initTime; 
    private long clickTime = 0;  
    
    // Target System Trackers
    private boolean showingFriends = false; 
    private int scrollOffset = 0;
    private boolean isDraggingScroll = false;

    // Dimensions
    public final int winW = 480;
    public final int winH = 280;
    public final int sideW = 130;

    public TPvPDashboardScreen(Screen parent) { 
        super(Text.literal("TPvP Dashboard")); 
        this.parent = parent; 
        this.initTime = System.currentTimeMillis();
    }
    
    public TPvPDashboardScreen() { 
        this(null); 
    }

    public net.minecraft.client.MinecraftClient getMinecraftClient() { return this.client; }
    public net.minecraft.client.font.TextRenderer getTextRenderer() { return this.textRenderer; }

    @Override
    public void render(DrawContext context, int mx, int my, float delta) {
        long now = System.currentTimeMillis();
        
        // ---------------------------------------------------------
        // 1. DYNAMIC TIDAL WAVE BACKGROUND & GLITTER
        // ---------------------------------------------------------
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

        // ---------------------------------------------------------
        // 2. POP-IN ANIMATION FOR MENU
        // ---------------------------------------------------------
        float intro = Math.min(1.0f, (now - initTime) / 300.0f);
        float scale = 0.8f + (0.2f * (float)Math.sin(intro * Math.PI / 2)); // Bouncy entry
        
        context.getMatrices().push();
        context.getMatrices().translate(this.width / 2f, this.height / 2f, 0);
        context.getMatrices().scale(scale, scale, 1.0f);
        context.getMatrices().translate(-this.width / 2f, -this.height / 2f, 0);

        // ---------------------------------------------------------
        // 3. REBORN CYBER-NEON THEME BOX
        // ---------------------------------------------------------
        context.fill(winX - 2, winY - 2, winX + winW + 2, winY + winH + 2, 0xFF00FFCC); // Neon Cyan Border
        context.fillGradient(winX, winY, winX + winW, winY + winH, 0xEE0A0A10, 0xFF050508); // Obsidian Blue Glass
        context.fill(winX, winY, winX + sideW, winY + winH, 0xAA020205); // Sidebar Darker
        
        // Sliding Glass Shine
        int shineX = winX + (int) ((now / 3) % (winW * 2)) - winW;
        context.fillGradient(shineX, winY, shineX + 40, winY + winH, 0x00FFFFFF, 0x22FFFFFF); 

        context.drawCenteredTextWithShadow(this.getTextRenderer(), "§b§lTPvP REBORN", winX + sideW / 2, winY + 15, 0xFFFFFF);

        // ---------------------------------------------------------
        // 4. DRAW TABS LIST
        // ---------------------------------------------------------
        drawTab(context, "🚀 Performance", winX, winY + 40, mx, my);
        drawTab(context, "⚔ Combat", winX, winY + 65, mx, my);
        drawTab(context, "🎯 Crosshair", winX, winY + 90, mx, my);
        drawTab(context, "💀 Targets", winX, winY + 115, mx, my);
        drawTab(context, "✨ Effects", winX, winY + 140, mx, my);
        drawTab(context, "🛠 Edit HUD", winX, winY + 165, mx, my);

        int setX = winX + sideW + 20;
        int setY = winY + 40;

        // ---------------------------------------------------------
        // 5. RENDER TAB CONTENTS (NO MODULAR DELEGATES, EVERYTHING HERE!)
        // ---------------------------------------------------------
        
        // --- PERFORMANCE TAB ---
        if (currentTab.equals("🚀 Performance")) {
            drawToggle(context, "Anti-Lag (FPS Boost)", setX, setY, ModConfig.fpsBoostEnabled);
            context.drawTextWithShadow(this.getTextRenderer(), "§7Hides distant players, reduces explosion & water", setX, setY + 16, 0xAAAAAA);
            context.drawTextWithShadow(this.getTextRenderer(), "§7particles by 75%. Essential for big PvP servers!", setX, setY + 28, 0xAAAAAA);

            drawToggle(context, "Cotton Smooth Game", setX, setY + 50, ModConfig.smoothGameEnabled);
            context.drawTextWithShadow(this.getTextRenderer(), "§7Removes screen stutters and camera shake.", setX, setY + 66, 0xAAAAAA);
            context.drawTextWithShadow(this.getTextRenderer(), "§7Feels like butter, heavily improves PvP tracking!", setX, setY + 78, 0xAAAAAA);
            
            context.fill(setX, setY + 110, setX + 250, setY + 111, 0x44FFFFFF);
            context.drawCenteredTextWithShadow(this.getTextRenderer(), "§eNotice: Turning these ON will instantly", setX + 125, setY + 125, 0xFFFFFF);
            context.drawCenteredTextWithShadow(this.getTextRenderer(), "§emake your gameplay feel like a 144Hz PC!", setX + 125, setY + 140, 0xFFFFFF);
        } 
        
        // --- COMBAT TAB ---
        else if (currentTab.equals("⚔ Combat")) {
            drawToggle(context, "Hitboxes", setX, setY, ModConfig.hitboxEnabled);
            drawToggle(context, "3D Indicator", setX + 150, setY, ModConfig.indicatorEnabled);
            drawToggle(context, "Armor Align (Vert)", setX, setY + 30, ModConfig.armorVertical);
            
            context.drawTextWithShadow(this.getTextRenderer(), "Indicator Style: §e" + ModConfig.indicatorStyle, setX + 150, setY + 30, 0xFFFFFF);
            context.fill(setX + 150, setY + 40, setX + 250, setY + 55, 0xFF550000);
            context.drawCenteredTextWithShadow(this.getTextRenderer(), "Change Style", setX + 200, setY + 44, 0xFFFFFF);

            context.drawTextWithShadow(this.getTextRenderer(), "Armor Crack: §c" + ModConfig.armorCrackThreshold + "%", setX, setY + 60, 0xFFFFFF);
            context.fill(setX, setY + 70, setX + 100, setY + 85, 0xFF550000);
            context.drawCenteredTextWithShadow(this.getTextRenderer(), "Add +10%", setX + 50, setY + 74, 0xFFFFFF);
        } 
        
        // --- EFFECTS TAB ---
        else if (currentTab.equals("✨ Effects")) {
            drawToggle(context, "MOBA Kill Banners", setX, setY, ModConfig.killBannerEnabled);
            drawToggle(context, "JJK Curse Execution", setX + 150, setY, ModConfig.soulAnimationEnabled);
            
            context.drawTextWithShadow(this.getTextRenderer(), "§7Get an epic screen banner when YOU kill players!", setX, setY + 25, 0xAAAAAA);
            context.drawTextWithShadow(this.getTextRenderer(), "§7A terrifying 3D monster will emerge and eat dead players.", setX, setY + 40, 0xAAAAAA);
            
            String[] colors = {"Blood Red", "Royal Gold", "Toxic Green"};
            context.drawTextWithShadow(this.getTextRenderer(), "Banner Theme: §e" + colors[ModConfig.bannerColorTheme], setX, setY + 65, 0xFFFFFF);
            context.fill(setX + 150, setY + 61, setX + 220, setY + 77, 0xFF550000);
            context.drawCenteredTextWithShadow(this.getTextRenderer(), "Change", setX + 185, setY + 65, 0xFFFFFF);
        } 
        
        // --- CROSSHAIR TAB ---
        else if (currentTab.equals("🎯 Crosshair")) {
            drawToggle(context, "Smart Crosshair", setX, setY, ModConfig.smartCrosshair);
            
            context.drawTextWithShadow(this.getTextRenderer(), "Style: §e" + ModConfig.crosshairStyle, setX, setY + 30, 0xFFFFFF);
            context.fill(setX, setY + 40, setX + 80, setY + 55, 0xFF550000);
            context.drawCenteredTextWithShadow(this.getTextRenderer(), "Change", setX + 40, setY + 44, 0xFFFFFF);
            
            context.drawTextWithShadow(this.getTextRenderer(), "Size: " + ModConfig.crosshairSize, setX + 100, setY + 30, 0xFFFFFF);
            context.fill(setX + 100, setY + 40, setX + 140, setY + 55, 0xFF550000);
            context.drawCenteredTextWithShadow(this.getTextRenderer(), "Add", setX + 120, setY + 44, 0xFFFFFF);

            // LIVE CROSSHAIR PREVIEW
            context.fill(setX, winY + 110, setX + 300, winY + 240, 0xFF000000); 
            context.drawCenteredTextWithShadow(this.getTextRenderer(), "Live Crosshair Preview", setX + 150, winY + 120, 0xAAAAAA);
            
            int cx = setX + 150;
            int cy = winY + 180;
            float s = ModConfig.crosshairSize;
            int color = 0xFFFFFFFF; 
            
            context.getMatrices().push();
            context.getMatrices().translate(cx, cy, 0);
            context.getMatrices().scale(s, s, 1.0f);
            
            if (ModConfig.crosshairStyle == 0) { 
                context.fill(-1, -6, 1, -2, color); context.fill(-1, 2, 1, 6, color);
                context.fill(-6, -1, -2, 1, color); context.fill(2, -1, 6, 1, color);
                context.fill(0, 0, 1, 1, color);
            } else if (ModConfig.crosshairStyle == 1) { 
                context.fill(-2, -1, 2, 1, color); context.fill(-1, -2, 1, 2, color);
                context.fill(-1, -1, 1, 1, 0x00000000); 
            } else if (ModConfig.crosshairStyle == 2) { 
                context.fill(-6, -4, -3, -3, color); context.fill(-4, -6, -3, -3, color);
                context.fill(3, 3, 6, 4, color); context.fill(3, 3, 4, 6, color);
            }
            context.getMatrices().pop();
        } 
        
        // --- TARGETS TAB ---
        else if (currentTab.equals("💀 Targets")) {
            // Friend / Enemy Toggle Button
            context.fill(setX, winY + 10, setX + 120, winY + 25, showingFriends ? 0xFF00AA00 : 0xFFAA0000);
            context.drawCenteredTextWithShadow(this.getTextRenderer(), showingFriends ? "List: FRIENDS" : "List: ENEMIES", setX + 60, winY + 14, 0xFFFFFF);
            
            // Auto-Track Low HP Toggle
            drawToggle(context, "Auto-Track Low HP", setX + 130, winY + 10, ModConfig.autoTrack);

            // Dragon Color Selector
            String[] colors = {"Ruby Red", "Void Purple", "Frost Blue"};
            context.drawTextWithShadow(this.getTextRenderer(), "Dragon: §e" + colors[ModConfig.dragonColor], setX, winY + 30, 0xFFFFFF);
            context.fill(setX + 100, winY + 26, setX + 150, winY + 38, 0xFF550000);
            context.drawCenteredTextWithShadow(this.getTextRenderer(), "Change", setX + 125, winY + 29, 0xFFFFFF);

            // Scrolling Player List Rendering
            if (this.client != null && this.client.getNetworkHandler() != null) {
                List<PlayerListEntry> players = new ArrayList<>(this.client.getNetworkHandler().getPlayerList());
                int listY = winY + 45;
                
                for (int i = 0; i < players.size(); i++) {
                    if (i >= scrollOffset && i < scrollOffset + 3) { 
                        PlayerListEntry p = players.get(i);
                        String pName = p.getProfile().getName();
                        
                        boolean isTagged = showingFriends ? ModConfig.taggedFriendName.equals(pName) : ModConfig.taggedPlayerName.equals(pName);
                        int rowColor = isTagged ? (showingFriends ? 0xAA00FF00 : 0xAAFF2222) : 0x55000000;
                        
                        context.fill(setX, listY, setX + 280, listY + 50, rowColor);
                        Identifier skin = p.getSkinTextures().texture();
                        
                        // Full 2D Body Map (Exactly mapped)
                        context.getMatrices().push();
                        context.getMatrices().translate(setX + 10, listY + 5, 0);
                        context.getMatrices().scale(1.2f, 1.2f, 1.0f);
                        context.drawTexture(RenderLayer::getGuiTextured, skin, 4, 0, 8f, 8f, 8, 8, 64, 64); // Head
                        context.drawTexture(RenderLayer::getGuiTextured, skin, 4, 8, 20f, 20f, 8, 12, 64, 64); // Body
                        context.drawTexture(RenderLayer::getGuiTextured, skin, 0, 8, 44f, 20f, 4, 12, 64, 64); // R-Arm
                        context.drawTexture(RenderLayer::getGuiTextured, skin, 12, 8, 36f, 52f, 4, 12, 64, 64); // L-Arm
                        context.drawTexture(RenderLayer::getGuiTextured, skin, 4, 20, 0f, 20f, 4, 12, 64, 64); // R-Leg
                        context.drawTexture(RenderLayer::getGuiTextured, skin, 8, 20, 20f, 52f, 4, 12, 64, 64); // L-Leg
                        context.getMatrices().pop();
                        
                        context.drawTextWithShadow(this.getTextRenderer(), isTagged ? "§l" + pName : "§f" + pName, setX + 50, listY + 20, 0xFFFFFF);
                        listY += 55;
                    }
                }

                // Scrollbar Render
                if (players.size() > 3) {
                    context.fill(setX + 290, winY + 45, setX + 295, winY + 210, 0x55000000); 
                    int thumbH = Math.max(20, 165 / (players.size() - 2));
                    int thumbY = winY + 45 + (scrollOffset * (165 - thumbH) / Math.max(1, players.size() - 3));
                    context.fill(setX + 290, thumbY, setX + 295, thumbY + thumbH, showingFriends ? 0xFF00FF00 : 0xFFFF2222);
                }
            }
        }
        
        context.getMatrices().pop();
        super.render(context, mx, my, delta);
    }

    // ---------------------------------------------------------
    // --- HELPER METHODS ---
    // ---------------------------------------------------------
    private void drawTab(DrawContext context, String name, int x, int y, int mx, int my) {
        boolean sel = currentTab.equals(name);
        boolean hov = mx >= x && mx <= x + sideW && my >= y && my <= y + 20;
        int offset = (hov && !sel) ? 2 : 0; // Bouncy hover
        
        if (sel) { 
            context.fill(x, y, x + sideW, y + 20, 0x5500FFCC); 
            context.fill(x, y, x + 3, y + 20, 0xFF00FFCC); 
        } else if (hov) {
            context.fill(x + offset, y, x + sideW, y + 20, 0x22FFFFFF);
        }
        context.drawTextWithShadow(this.getTextRenderer(), name, x + 15 + offset, y + 6, sel ? 0x00FFCC : 0xAAAAAA);
    }

    public void drawToggle(DrawContext context, String label, int x, int y, boolean value) {
        context.drawTextWithShadow(this.getTextRenderer(), label, x, y + 2, 0xFFFFFF);
        int sx = x + 120;
        context.fill(sx, y, sx + 30, y + 12, value ? 0xFF00AA55 : 0xFF333333); 
        if (value) context.fill(sx + 18, y + 1, sx + 29, y + 11, 0xFFFFFFFF); 
        else context.fill(sx + 1, y + 1, sx + 12, y + 11, 0xFFAAAAAA); 
    }

    // ---------------------------------------------------------
    // --- MOUSE CONTROLS (CLICKS & SCROLL) ---
    // ---------------------------------------------------------
    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        clickTime = System.currentTimeMillis(); 
        int winX = (this.width - winW) / 2, winY = (this.height - winH) / 2;
        int setX = winX + sideW + 20, setY = winY + 40;
        
        // Tab Selection
        if (mx >= winX && mx <= winX + sideW) {
            if (my >= winY+40 && my <= winY+60) currentTab = "🚀 Performance";
            else if (my >= winY+65 && my <= winY+85) currentTab = "⚔ Combat";
            else if (my >= winY+90 && my <= winY+110) currentTab = "🎯 Crosshair";
            else if (my >= winY+115 && my <= winY+135) currentTab = "💀 Targets";
            else if (my >= winY+140 && my <= winY+160) currentTab = "✨ Effects";
            else if (my >= winY+165 && my <= winY+185) this.client.setScreen(new EditHudScreen(this));
            return true;
        }

        if (currentTab.equals("🚀 Performance")) {
            if (mx >= setX+120 && mx <= setX+150 && my >= setY && my <= setY+12) { ModConfig.fpsBoostEnabled = !ModConfig.fpsBoostEnabled; return true; }
            if (mx >= setX+120 && mx <= setX+150 && my >= setY+50 && my <= setY+62) { ModConfig.smoothGameEnabled = !ModConfig.smoothGameEnabled; return true; }
        } 
        else if (currentTab.equals("⚔ Combat")) {
            if (mx >= setX+120 && mx <= setX+150 && my >= setY && my <= setY+12) { ModConfig.hitboxEnabled = !ModConfig.hitboxEnabled; return true; }
            if (mx >= setX+270 && mx <= setX+300 && my >= setY && my <= setY+12) { ModConfig.indicatorEnabled = !ModConfig.indicatorEnabled; return true; }
            if (mx >= setX+120 && mx <= setX+150 && my >= setY+30 && my <= setY+42) { ModConfig.armorVertical = !ModConfig.armorVertical; return true; }
            if (mx >= setX+150 && mx <= setX+250 && my >= setY+40 && my <= setY+55) { ModConfig.indicatorStyle = (ModConfig.indicatorStyle + 1) % 3; return true; }
            if (mx >= setX && mx <= setX+100 && my >= setY+70 && my <= setY+85) { ModConfig.armorCrackThreshold += 10; if(ModConfig.armorCrackThreshold > 50) ModConfig.armorCrackThreshold = 10; return true; }
        } 
        else if (currentTab.equals("✨ Effects")) {
            if (mx >= setX+120 && mx <= setX+150 && my >= setY && my <= setY+12) { ModConfig.killBannerEnabled = !ModConfig.killBannerEnabled; return true; }
            if (mx >= setX+270 && mx <= setX+300 && my >= setY && my <= setY+12) { ModConfig.soulAnimationEnabled = !ModConfig.soulAnimationEnabled; return true; }
            if (mx >= setX+150 && mx <= setX+220 && my >= setY+61 && my <= setY+77) { ModConfig.bannerColorTheme = (ModConfig.bannerColorTheme + 1) % 3; return true; }
        } 
        else if (currentTab.equals("🎯 Crosshair")) {
            if (mx >= setX+120 && mx <= setX+150 && my >= setY && my <= setY+12) { ModConfig.smartCrosshair = !ModConfig.smartCrosshair; return true; }
            if (mx >= setX && mx <= setX+80 && my >= setY+40 && my <= setY+55) { ModConfig.crosshairStyle = (ModConfig.crosshairStyle + 1) % 3; return true; }
            if (mx >= setX+100 && mx <= setX+140 && my >= setY+40 && my <= setY+55) { ModConfig.crosshairSize += 0.5f; if (ModConfig.crosshairSize > 3.0f) ModConfig.crosshairSize = 0.5f; return true; }
        } 
        else if (currentTab.equals("💀 Targets")) {
            if (mx >= setX && mx <= setX + 120 && my >= winY + 10 && my <= winY + 25) { showingFriends = !showingFriends; return true; }
            if (mx >= setX+240 && mx <= setX+270 && my >= winY+10 && my <= winY+22) { ModConfig.autoTrack = !ModConfig.autoTrack; return true; }
            if (mx >= setX+100 && mx <= setX+150 && my >= winY+26 && my <= winY+38) { ModConfig.dragonColor = (ModConfig.dragonColor + 1) % 3; return true; }
            if (mx >= setX + 290 && mx <= setX + 295 && my >= winY + 45 && my <= winY + 210) { isDraggingScroll = true; return true; }

            if (this.client != null && this.client.getNetworkHandler() != null) {
                List<PlayerListEntry> players = new ArrayList<>(this.client.getNetworkHandler().getPlayerList());
                int listY = winY + 45;
                for (int i = 0; i < players.size(); i++) {
                    if (i >= scrollOffset && i < scrollOffset + 3) {
                        if (mx >= setX && mx <= setX + 280 && my >= listY && my <= listY + 50) {
                            String name = players.get(i).getProfile().getName();
                            if (showingFriends) {
                                if (ModConfig.taggedFriendName.equals(name)) ModConfig.taggedFriendName = ""; 
                                else ModConfig.taggedFriendName = name;
                            } else {
                                if (ModConfig.taggedPlayerName.equals(name)) ModConfig.taggedPlayerName = ""; 
                                else { ModConfig.taggedPlayerName = name; ModConfig.autoTrack = false; }
                            }
                            return true;
                        }
                        listY += 55;
                    }
                }
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override 
    public boolean mouseDragged(double mx, double my, int b, double dx, double dY) { 
        if (isDraggingScroll && currentTab.equals("💀 Targets") && this.client != null && this.client.getNetworkHandler() != null) {
            int players = this.client.getNetworkHandler().getPlayerList().size();
            if (players > 3) {
                int winY = (this.height - winH) / 2;
                float percentage = (float) (my - (winY + 45)) / 165f;
                scrollOffset = Math.round(Math.max(0, Math.min(1, percentage)) * (players - 3));
            }
            return true;
        }
        return super.mouseDragged(mx, my, b, dx, dY); 
    }
    
    @Override public boolean mouseReleased(double mx, double my, int b) { isDraggingScroll = false; return super.mouseReleased(mx, my, b); }
    @Override public boolean mouseScrolled(double mx, double my, double h, double s) { if (currentTab.equals("💀 Targets")) { scrollOffset = Math.max(0, scrollOffset - (int) s); return true; } return super.mouseScrolled(mx, my, h, s); }
    @Override public void close() { ModConfig.save(); if (this.client != null) this.client.setScreen(this.parent); }
}
