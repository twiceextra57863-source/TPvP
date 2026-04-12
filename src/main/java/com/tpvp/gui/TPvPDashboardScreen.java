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
    private String currentTab = "Combat";
    
    // Target System Variables
    private boolean showingFriends = false; 
    private int scrollOffset = 0;
    private boolean isDraggingScroll = false;

    // Window Layout
    private final int winW = 460;
    private final int winH = 260;
    private final int sideW = 120;

    public TPvPDashboardScreen(Screen parent) { 
        super(Text.literal("TPvP Dashboard")); 
        this.parent = parent; 
    }
    
    public TPvPDashboardScreen() { 
        this(null); 
    }

    @Override
    public void render(DrawContext context, int mx, int my, float delta) {
        // 1. Full Screen Dark Overlay
        context.fill(0, 0, this.width, this.height, 0xDD050000); 
        
        int winX = (this.width - winW) / 2;
        int winY = (this.height - winH) / 2;

        // 2. RUBY RED GLASS THEME
        context.fill(winX - 2, winY - 2, winX + winW + 2, winY + winH + 2, 0xFFFF2222); // Outer Glowing Red Border
        context.fillGradient(winX, winY, winX + winW, winY + winH, 0xEE440000, 0xFF110000); // Inner Red Glass Gradient
        context.fill(winX, winY, winX + sideW, winY + winH, 0xAA220000); // Sidebar Darker Red
        
        // 3. ANIMATED SHINE EFFECT
        long time = System.currentTimeMillis();
        int shineX = winX + (int) ((time / 3) % (winW * 2)) - winW;
        context.fillGradient(shineX, winY, shineX + 40, winY + winH, 0x00FFFFFF, 0x22FFFFFF); // Sliding Glass Reflection

        context.drawCenteredTextWithShadow(this.textRenderer, "§c§lTPvP CLIENT", winX + sideW / 2, winY + 15, 0xFFFFFF);

        // 4. DRAW SIDEBAR TABS
        drawTab(context, "Combat", winX, winY + 40, mx, my);
        drawTab(context, "Crosshair", winX, winY + 65, mx, my);
        drawTab(context, "Targets", winX, winY + 90, mx, my);
        drawTab(context, "✨ Effects", winX, winY + 115, mx, my);
        drawTab(context, "🛠 Edit HUD", winX, winY + 140, mx, my);

        int setX = winX + sideW + 20;
        int setY = winY + 40;

        // ---------------------------------------------------------
        // --- COMBAT SETTINGS TAB ---
        // ---------------------------------------------------------
        if (currentTab.equals("Combat")) {
            drawToggle(context, "Hitboxes", setX, setY, ModConfig.hitboxEnabled);
            drawToggle(context, "3D Indicator", setX + 150, setY, ModConfig.indicatorEnabled);
            drawToggle(context, "Armor Align (Vert)", setX, setY + 30, ModConfig.armorVertical);
            
            // Indicator Style Selector
            context.drawTextWithShadow(this.textRenderer, "Indicator Style: §e" + ModConfig.indicatorStyle, setX + 150, setY + 30, 0xFFFFFF);
            context.fill(setX + 150, setY + 40, setX + 250, setY + 55, 0xFF550000);
            context.drawCenteredTextWithShadow(this.textRenderer, "Change Style", setX + 200, setY + 44, 0xFFFFFF);

            // Armor Crack Threshold Selector
            context.drawTextWithShadow(this.textRenderer, "Armor Crack: §c" + ModConfig.armorCrackThreshold + "%", setX, setY + 60, 0xFFFFFF);
            context.fill(setX, setY + 70, setX + 100, setY + 85, 0xFF550000);
            context.drawCenteredTextWithShadow(this.textRenderer, "Add +10%", setX + 50, setY + 74, 0xFFFFFF);
        } 
        // ---------------------------------------------------------
        // --- EFFECTS SETTINGS TAB ---
        // ---------------------------------------------------------
        else if (currentTab.equals("✨ Effects")) { 
            drawToggle(context, "Epic Kill Banners", setX, setY, ModConfig.killBannerEnabled);
            drawToggle(context, "3D Soul Ritual", setX + 150, setY, ModConfig.soulAnimationEnabled);
            
            context.drawTextWithShadow(this.textRenderer, "§7Get an epic screen banner when you kill players!", setX, setY + 30, 0xAAAAAA);
            context.drawTextWithShadow(this.textRenderer, "§7Dead players ascend to heaven as 3D wavy souls.", setX, setY + 45, 0xAAAAAA);
        } 
        // ---------------------------------------------------------
        // --- CROSSHAIR SETTINGS TAB ---
        // ---------------------------------------------------------
        else if (currentTab.equals("Crosshair")) {
            drawToggle(context, "Smart Crosshair", setX, setY, ModConfig.smartCrosshair);
            
            context.drawTextWithShadow(this.textRenderer, "Style: §e" + ModConfig.crosshairStyle, setX, setY + 30, 0xFFFFFF);
            context.fill(setX, setY + 40, setX + 80, setY + 55, 0xFF550000);
            context.drawCenteredTextWithShadow(this.textRenderer, "Change", setX + 40, setY + 44, 0xFFFFFF);
            
            context.drawTextWithShadow(this.textRenderer, "Size: " + ModConfig.crosshairSize, setX + 100, setY + 30, 0xFFFFFF);
            context.fill(setX + 100, setY + 40, setX + 140, setY + 55, 0xFF550000);
            context.drawCenteredTextWithShadow(this.textRenderer, "Add", setX + 120, setY + 44, 0xFFFFFF);

            // LIVE CROSSHAIR PREVIEW
            context.fill(setX, winY + 110, setX + 300, winY + 240, 0xFF000000); 
            context.drawCenteredTextWithShadow(this.textRenderer, "Live Crosshair Preview", setX + 150, winY + 120, 0xAAAAAA);
            
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
        // ---------------------------------------------------------
        // --- TARGETS SETTINGS TAB ---
        // ---------------------------------------------------------
        else if (currentTab.equals("Targets")) {
            // FRIEND / ENEMY TOGGLE BUTTON
            context.fill(setX, winY + 10, setX + 120, winY + 25, showingFriends ? 0xFF00AA00 : 0xFFAA0000);
            context.drawCenteredTextWithShadow(this.textRenderer, showingFriends ? "List: FRIENDS" : "List: ENEMIES", setX + 60, winY + 14, 0xFFFFFF);
            
            drawToggle(context, "Dragon Aura", setX + 150, winY + 10, ModConfig.dragonAuraEnabled);

            if (this.client != null && this.client.getNetworkHandler() != null) {
                List<PlayerListEntry> players = new ArrayList<>(this.client.getNetworkHandler().getPlayerList());
                int listY = winY + 40;
                
                for (int i = 0; i < players.size(); i++) {
                    if (i >= scrollOffset && i < scrollOffset + 4) { // Show 4 players per page
                        PlayerListEntry p = players.get(i);
                        String pName = p.getProfile().getName();
                        
                        boolean isTagged = showingFriends ? ModConfig.taggedFriendName.equals(pName) : ModConfig.taggedPlayerName.equals(pName);
                        int rowColor = isTagged ? (showingFriends ? 0xAA00FF00 : 0xAAFF2222) : 0x55000000;
                        
                        context.fill(setX, listY, setX + 280, listY + 50, rowColor);
                        Identifier skin = p.getSkinTextures().texture();
                        
                        // FULL 2D BODY SKIN MAPPING
                        context.getMatrices().push();
                        context.getMatrices().translate(setX + 10, listY + 5, 0);
                        context.getMatrices().scale(1.2f, 1.2f, 1.0f);
                        context.drawTexture(RenderLayer::getGuiTextured, skin, 4, 0, 8f, 8f, 8, 8, 64, 64); // Head
                        context.drawTexture(RenderLayer::getGuiTextured, skin, 4, 8, 20f, 20f, 8, 12, 64, 64); // Body
                        context.drawTexture(RenderLayer::getGuiTextured, skin, 0, 8, 44f, 20f, 4, 12, 64, 64); // Right Arm
                        context.drawTexture(RenderLayer::getGuiTextured, skin, 12, 8, 36f, 52f, 4, 12, 64, 64); // Left Arm
                        context.drawTexture(RenderLayer::getGuiTextured, skin, 4, 20, 0f, 20f, 4, 12, 64, 64); // Right Leg
                        context.drawTexture(RenderLayer::getGuiTextured, skin, 8, 20, 20f, 52f, 4, 12, 64, 64); // Left Leg
                        context.getMatrices().pop();
                        
                        context.drawTextWithShadow(this.textRenderer, isTagged ? "§l" + pName : "§f" + pName, setX + 50, listY + 20, 0xFFFFFF);
                        listY += 55;
                    }
                }

                // SCROLLBAR RENDER
                if (players.size() > 4) {
                    context.fill(setX + 290, winY + 40, setX + 295, winY + 245, 0x55000000);
                    int thumbH = Math.max(20, 205 / (players.size() - 3));
                    int thumbY = winY + 40 + (scrollOffset * (205 - thumbH) / Math.max(1, players.size() - 4));
                    context.fill(setX + 290, thumbY, setX + 295, thumbY + thumbH, showingFriends ? 0xFF00FF00 : 0xFFFF2222);
                }
            }
        }
        super.render(context, mx, my, delta);
    }

    // --- HELPER METHODS ---

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

    private void drawToggle(DrawContext context, String label, int x, int y, boolean value) {
        context.drawTextWithShadow(this.textRenderer, label, x, y + 2, 0xFFFFFF);
        int sx = x + 110;
        context.fill(sx, y, sx + 30, y + 12, value ? 0xFFCC0000 : 0xFF333333); 
        if (value) context.fill(sx + 18, y + 1, sx + 29, y + 11, 0xFFFFFFFF); 
        else context.fill(sx + 1, y + 1, sx + 12, y + 11, 0xFFAAAAAA); 
    }

    // --- MOUSE CONTROLS ---

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        int winX = (this.width - winW) / 2, winY = (this.height - winH) / 2;
        int setX = winX + sideW + 20, setY = winY + 40;
        
        if (mx >= winX && mx <= winX + sideW) {
            if (my >= winY+40 && my <= winY+60) currentTab = "Combat";
            else if (my >= winY+65 && my <= winY+85) currentTab = "Crosshair";
            else if (my >= winY+90 && my <= winY+110) currentTab = "Targets";
            else if (my >= winY+115 && my <= winY+135) currentTab = "✨ Effects";
            else if (my >= winY+140 && my <= winY+160) this.client.setScreen(new EditHudScreen(this));
            return true;
        }

        if (currentTab.equals("Combat")) {
            if (mx >= setX+110 && mx <= setX+140 && my >= setY && my <= setY+12) ModConfig.hitboxEnabled = !ModConfig.hitboxEnabled;
            if (mx >= setX+260 && mx <= setX+290 && my >= setY && my <= setY+12) ModConfig.indicatorEnabled = !ModConfig.indicatorEnabled;
            if (mx >= setX+110 && mx <= setX+140 && my >= setY+30 && my <= setY+42) ModConfig.armorVertical = !ModConfig.armorVertical;
            if (mx >= setX+150 && mx <= setX+250 && my >= setY+40 && my <= setY+55) ModConfig.indicatorStyle = (ModConfig.indicatorStyle + 1) % 3;
            if (mx >= setX && mx <= setX+100 && my >= setY+70 && my <= setY+85) { ModConfig.armorCrackThreshold += 10; if(ModConfig.armorCrackThreshold > 50) ModConfig.armorCrackThreshold = 10; }
        } else if (currentTab.equals("✨ Effects")) {
            if (mx >= setX+110 && mx <= setX+140 && my >= setY && my <= setY+12) ModConfig.killBannerEnabled = !ModConfig.killBannerEnabled;
            if (mx >= setX+260 && mx <= setX+290 && my >= setY && my <= setY+12) ModConfig.soulAnimationEnabled = !ModConfig.soulAnimationEnabled;
        } else if (currentTab.equals("Crosshair")) {
            if (mx >= setX+110 && mx <= setX+140 && my >= setY && my <= setY+12) ModConfig.smartCrosshair = !ModConfig.smartCrosshair;
            if (mx >= setX && mx <= setX+80 && my >= setY+40 && my <= setY+55) ModConfig.crosshairStyle = (ModConfig.crosshairStyle + 1) % 3;
            if (mx >= setX+100 && mx <= setX+140 && my >= setY+40 && my <= setY+55) { ModConfig.crosshairSize += 0.5f; if (ModConfig.crosshairSize > 3.0f) ModConfig.crosshairSize = 0.5f; }
        } else if (currentTab.equals("Targets")) {
            // Friend/Enemy List Toggle Button
            if (mx >= setX && mx <= setX + 120 && my >= winY + 10 && my <= winY + 25) {
                showingFriends = !showingFriends;
                return true;
            }
            if (mx >= setX+150 && mx <= setX+250 && my >= winY+10 && my <= winY+22) ModConfig.dragonAuraEnabled = !ModConfig.dragonAuraEnabled;

            if (mx >= setX + 290 && mx <= setX + 295 && my >= winY + 40 && my <= winY + 245) { isDraggingScroll = true; return true; }

            if (this.client != null && this.client.getNetworkHandler() != null) {
                List<PlayerListEntry> players = new ArrayList<>(this.client.getNetworkHandler().getPlayerList());
                int listY = winY + 40;
                for (int i = 0; i < players.size(); i++) {
                    if (i >= scrollOffset && i < scrollOffset + 4) {
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
    public boolean mouseDragged(double mx, double my, int button, double dX, double dY) {
        if (isDraggingScroll && currentTab.equals("Targets") && this.client != null && this.client.getNetworkHandler() != null) {
            int players = this.client.getNetworkHandler().getPlayerList().size();
            if (players > 4) {
                float pct = (float) (my - ((this.height - winH) / 2) - 40) / 205f;
                scrollOffset = Math.round(Math.max(0, Math.min(1, pct)) * (players - 4));
            }
            return true;
        }
        return super.mouseDragged(mx, my, button, dX, dY);
    }
    
    @Override 
    public boolean mouseReleased(double mx, double my, int button) { 
        isDraggingScroll = false; 
        return super.mouseReleased(mx, my, button); 
    }
    
    @Override 
    public boolean mouseScrolled(double mx, double my, double h, double scroll) { 
        if (currentTab.equals("Targets")) { 
            scrollOffset = Math.max(0, scrollOffset - (int) scroll); 
            return true; 
        } 
        return super.mouseScrolled(mx, my, h, scroll); 
    }
    
    @Override 
    public void close() { 
        ModConfig.save(); 
        if (this.client != null) this.client.setScreen(this.parent); 
    }
}
