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
    
    // Target Menu Scrollbar Variables
    private int scrollOffset = 0;
    private boolean isDraggingScroll = false;

    // Window Dimensions
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
        // 1. Dark Background Overlay
        context.fill(0, 0, this.width, this.height, 0xDD050000);

        int winX = (this.width - winW) / 2;
        int winY = (this.height - winH) / 2;

        // 2. RED GLASS THEME BACKGROUND
        context.fill(winX - 2, winY - 2, winX + winW + 2, winY + winH + 2, 0xFFFF2222); // Red Outer Border
        context.fillGradient(winX, winY, winX + winW, winY + winH, 0xEE440000, 0xFF110000); // Dark Red Glass
        context.fill(winX, winY, winX + sideW, winY + winH, 0xAA220000); // Darker Sidebar
        
        // 3. ANIMATED SHINE EFFECT
        long time = System.currentTimeMillis();
        int shineX = winX + (int) ((time / 3) % (winW * 2)) - winW;
        context.fillGradient(shineX, winY, shineX + 40, winY + winH, 0x00FFFFFF, 0x22FFFFFF); // Sliding Light

        // Dashboard Title
        context.drawCenteredTextWithShadow(this.textRenderer, "§c§lTPvP CLIENT", winX + sideW / 2, winY + 15, 0xFFFFFF);

        // 4. CUSTOM TABS RENDERING
        drawTab(context, "Combat", winX, winY + 40, mx, my);
        drawTab(context, "Crosshair", winX, winY + 65, mx, my);
        drawTab(context, "Targets", winX, winY + 90, mx, my);
        drawTab(context, "✨ Effects", winX, winY + 115, mx, my);
        drawTab(context, "🛠 Edit HUD", winX, winY + 140, mx, my);

        int setX = winX + sideW + 20;
        int setY = winY + 40;

        // ---------------------------------------------
        // TAB 1: COMBAT SETTINGS
        // ---------------------------------------------
        if (currentTab.equals("Combat")) {
            drawToggle(context, "Hitboxes", setX, setY, ModConfig.hitboxEnabled);
            drawToggle(context, "3D Indicator", setX + 150, setY, ModConfig.indicatorEnabled);
            drawToggle(context, "Armor Align (Vert)", setX, setY + 30, ModConfig.armorVertical);
            
            // Indicator Style Button
            context.drawTextWithShadow(this.textRenderer, "Indicator Style: §e" + ModConfig.indicatorStyle, setX + 150, setY + 30, 0xFFFFFF);
            context.fill(setX + 150, setY + 40, setX + 250, setY + 55, 0xFF550000);
            context.drawCenteredTextWithShadow(this.textRenderer, "Change Style", setX + 200, setY + 44, 0xFFFFFF);

            // Armor Crack Threshold Button
            context.drawTextWithShadow(this.textRenderer, "Armor Crack: §c" + ModConfig.armorCrackThreshold + "%", setX, setY + 60, 0xFFFFFF);
            context.fill(setX, setY + 70, setX + 100, setY + 85, 0xFF550000);
            context.drawCenteredTextWithShadow(this.textRenderer, "Add +10%", setX + 50, setY + 74, 0xFFFFFF);
        } 
        // ---------------------------------------------
        // TAB 2: EFFECTS SETTINGS
        // ---------------------------------------------
        else if (currentTab.equals("✨ Effects")) {
            drawToggle(context, "MOBA Kill Banners", setX, setY, ModConfig.killBannerEnabled);
            drawToggle(context, "Soul Death Anim", setX + 150, setY, ModConfig.soulAnimationEnabled);
            
            context.drawTextWithShadow(this.textRenderer, "§7Get an epic screen banner when you kill players!", setX, setY + 25, 0xAAAAAA);
            context.drawTextWithShadow(this.textRenderer, "§7Dead players ascend to heaven as wavy cloth souls.", setX, setY + 40, 0xAAAAAA);
        } 
        // ---------------------------------------------
        // TAB 3: CROSSHAIR SETTINGS (WITH LIVE PREVIEW)
        // ---------------------------------------------
        else if (currentTab.equals("Crosshair")) {
            drawToggle(context, "Smart Crosshair", setX, setY, ModConfig.smartCrosshair);
            
            // Style Button
            context.drawTextWithShadow(this.textRenderer, "Style: §e" + ModConfig.crosshairStyle, setX, setY + 30, 0xFFFFFF);
            context.fill(setX, setY + 40, setX + 80, setY + 55, 0xFF550000);
            context.drawCenteredTextWithShadow(this.textRenderer, "Change", setX + 40, setY + 44, 0xFFFFFF);
            
            // Size Button
            context.drawTextWithShadow(this.textRenderer, "Size: " + ModConfig.crosshairSize, setX + 100, setY + 30, 0xFFFFFF);
            context.fill(setX + 100, setY + 40, setX + 140, setY + 55, 0xFF550000);
            context.drawCenteredTextWithShadow(this.textRenderer, "Add", setX + 120, setY + 44, 0xFFFFFF);

            // LIVE CROSSHAIR PREVIEW BOX
            context.fill(setX, winY + 110, setX + 300, winY + 240, 0xFF000000); // Black Preview Area
            context.drawCenteredTextWithShadow(this.textRenderer, "Live Crosshair Preview", setX + 150, winY + 120, 0xAAAAAA);
            
            int cx = setX + 150;
            int cy = winY + 180;
            float s = ModConfig.crosshairSize;
            int color = 0xFFFFFFFF; // Preview default color
            
            context.getMatrices().push();
            context.getMatrices().translate(cx, cy, 0);
            context.getMatrices().scale(s, s, 1.0f);
            
            if (ModConfig.crosshairStyle == 0) { // Plus
                context.fill(-1, -6, 1, -2, color); 
                context.fill(-1, 2, 1, 6, color);
                context.fill(-6, -1, -2, 1, color); 
                context.fill(2, -1, 6, 1, color);
                context.fill(0, 0, 1, 1, color);
            } else if (ModConfig.crosshairStyle == 1) { // Dot
                context.fill(-2, -1, 2, 1, color); 
                context.fill(-1, -2, 1, 2, color);
                context.fill(-1, -1, 1, 1, 0x00000000); // Hollow center
            } else if (ModConfig.crosshairStyle == 2) { // Angle
                context.fill(-6, -4, -3, -3, color); 
                context.fill(-4, -6, -3, -3, color);
                context.fill(3, 3, 6, 4, color); 
                context.fill(3, 3, 4, 6, color);
            }
            context.getMatrices().pop();
        } 
        // ---------------------------------------------
        // TAB 4: TARGETS SYSTEM
        // ---------------------------------------------
        else if (currentTab.equals("Targets")) {
            drawToggle(context, "Auto-Track Low HP", setX, winY + 10, ModConfig.autoTrack);
            drawToggle(context, "Dragon Aura", setX + 150, winY + 10, ModConfig.dragonAuraEnabled);

            if (this.client != null && this.client.getNetworkHandler() != null) {
                List<PlayerListEntry> players = new ArrayList<>(this.client.getNetworkHandler().getPlayerList());
                int listY = winY + 40;
                
                for (int i = 0; i < players.size(); i++) {
                    if (i >= scrollOffset && i < scrollOffset + 4) { // Show 4 players per page
                        PlayerListEntry p = players.get(i);
                        String pName = p.getProfile().getName();
                        
                        // Check if this player is currently tagged by the user
                        boolean isTagged = ModConfig.taggedPlayerName.equals(pName) && !ModConfig.autoTrack;
                        
                        // Draw Background Highlight
                        context.fill(setX, listY, setX + 280, listY + 50, isTagged ? 0xAAFF2222 : 0x55000000);
                        
                        // FULL 2D BODY RENDERER (Skin Mapping)
                        Identifier skin = p.getSkinTextures().texture();
                        int bodyX = setX + 10;
                        int bodyY = listY + 5;
                        
                        context.getMatrices().push();
                        context.getMatrices().translate(bodyX, bodyY, 0);
                        context.getMatrices().scale(1.2f, 1.2f, 1.0f);
                        
                        context.drawTexture(RenderLayer::getGuiTextured, skin, 4, 0, 8f, 8f, 8, 8, 64, 64); // Head
                        context.drawTexture(RenderLayer::getGuiTextured, skin, 4, 8, 20f, 20f, 8, 12, 64, 64); // Body
                        context.drawTexture(RenderLayer::getGuiTextured, skin, 0, 8, 44f, 20f, 4, 12, 64, 64); // Right Arm
                        context.drawTexture(RenderLayer::getGuiTextured, skin, 12, 8, 36f, 52f, 4, 12, 64, 64); // Left Arm
                        context.drawTexture(RenderLayer::getGuiTextured, skin, 4, 20, 0f, 20f, 4, 12, 64, 64); // Right Leg
                        context.drawTexture(RenderLayer::getGuiTextured, skin, 8, 20, 20f, 52f, 4, 12, 64, 64); // Left Leg
                        
                        context.getMatrices().pop();
                        
                        // Draw Player Name
                        context.drawTextWithShadow(this.textRenderer, isTagged ? "§c§l" + pName : "§f" + pName, setX + 50, listY + 20, 0xFFFFFF);
                        
                        listY += 55; // Space for next player row
                    }
                }
                
                // Draw Drag Scrollbar
                if (players.size() > 4) {
                    context.fill(setX + 290, winY + 40, setX + 295, winY + 245, 0x55000000); // Track
                    int thumbHeight = Math.max(20, 205 / (players.size() - 3));
                    int maxScroll = Math.max(1, players.size() - 4);
                    int thumbY = winY + 40 + (scrollOffset * (205 - thumbHeight) / maxScroll);
                    context.fill(setX + 290, thumbY, setX + 295, thumbY + thumbHeight, 0xFFFF2222); // Red Thumb
                }
            }
        }
        
        super.render(context, mx, my, delta);
    }

    // --- HELPER METHODS FOR UI ---

    private void drawTab(DrawContext context, String name, int x, int y, int mx, int my) {
        boolean selected = currentTab.equals(name);
        boolean hovered = mx >= x && mx <= x + sideW && my >= y && my <= y + 20;
        
        if (selected) {
            context.fill(x, y, x + sideW, y + 20, 0x55FF0000); // Red highlight
            context.fill(x, y, x + 3, y + 20, 0xFFFF0000); // Solid red line
        } else if (hovered) {
            context.fill(x, y, x + sideW, y + 20, 0x22FFFFFF); // White hover
        }
        
        context.drawTextWithShadow(this.textRenderer, name, x + 15, y + 6, selected ? 0xFF5555 : 0xAAAAAA);
    }

    private void drawToggle(DrawContext context, String label, int x, int y, boolean value) {
        context.drawTextWithShadow(this.textRenderer, label, x, y + 2, 0xFFFFFF);
        int sx = x + 110;
        // Background Pill
        context.fill(sx, y, sx + 30, y + 12, value ? 0xFFCC0000 : 0xFF333333); 
        // Sliding Knob
        if (value) {
            context.fill(sx + 18, y + 1, sx + 29, y + 11, 0xFFFFFFFF); // Knob ON (Right)
        } else {
            context.fill(sx + 1, y + 1, sx + 12, y + 11, 0xFFAAAAAA); // Knob OFF (Left)
        }
    }

    // --- MOUSE INPUT LOGIC ---

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        int winX = (this.width - winW) / 2;
        int winY = (this.height - winH) / 2;
        
        // 1. Check Sidebar Tab Clicks
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

        // 2. Check Combat Settings Clicks
        if (currentTab.equals("Combat")) {
            if (mx >= setX+110 && mx <= setX+140 && my >= setY && my <= setY+12) ModConfig.hitboxEnabled = !ModConfig.hitboxEnabled;
            if (mx >= setX+260 && mx <= setX+290 && my >= setY && my <= setY+12) ModConfig.indicatorEnabled = !ModConfig.indicatorEnabled;
            if (mx >= setX+110 && mx <= setX+140 && my >= setY+30 && my <= setY+42) ModConfig.armorVertical = !ModConfig.armorVertical;
            if (mx >= setX+150 && mx <= setX+250 && my >= setY+40 && my <= setY+55) ModConfig.indicatorStyle = (ModConfig.indicatorStyle + 1) % 3;
            if (mx >= setX && mx <= setX+100 && my >= setY+70 && my <= setY+85) { 
                ModConfig.armorCrackThreshold += 10; 
                if(ModConfig.armorCrackThreshold > 50) ModConfig.armorCrackThreshold = 10; 
            }
        } 
        // 3. Check Effects Settings Clicks
        else if (currentTab.equals("✨ Effects")) {
            if (mx >= setX+110 && mx <= setX+140 && my >= setY && my <= setY+12) ModConfig.killBannerEnabled = !ModConfig.killBannerEnabled;
            if (mx >= setX+260 && mx <= setX+290 && my >= setY && my <= setY+12) ModConfig.soulAnimationEnabled = !ModConfig.soulAnimationEnabled;
        } 
        // 4. Check Crosshair Settings Clicks
        else if (currentTab.equals("Crosshair")) {
            if (mx >= setX+110 && mx <= setX+140 && my >= setY && my <= setY+12) ModConfig.smartCrosshair = !ModConfig.smartCrosshair;
            if (mx >= setX && mx <= setX+80 && my >= setY+40 && my <= setY+55) ModConfig.crosshairStyle = (ModConfig.crosshairStyle + 1) % 3;
            if (mx >= setX+100 && mx <= setX+140 && my >= setY+40 && my <= setY+55) { 
                ModConfig.crosshairSize += 0.5f; 
                if (ModConfig.crosshairSize > 3.0f) ModConfig.crosshairSize = 0.5f; 
            }
        } 
        // 5. Check Target System Clicks
        else if (currentTab.equals("Targets")) {
            if (mx >= setX+110 && mx <= setX+140 && my >= winY+10 && my <= winY+22) ModConfig.autoTrack = !ModConfig.autoTrack;
            if (mx >= setX+260 && mx <= setX+290 && my >= winY+10 && my <= winY+22) ModConfig.dragonAuraEnabled = !ModConfig.dragonAuraEnabled;

            // Scrollbar Click
            if (mx >= setX + 290 && mx <= setX + 295 && my >= winY + 40 && my <= winY + 245) { 
                isDraggingScroll = true; 
                return true; 
            }

            // Player List Click Logic
            if (this.client != null && this.client.getNetworkHandler() != null) {
                List<PlayerListEntry> players = new ArrayList<>(this.client.getNetworkHandler().getPlayerList());
                int listY = winY + 40;
                
                for (int i = 0; i < players.size(); i++) {
                    if (i >= scrollOffset && i < scrollOffset + 4) {
                        if (mx >= setX && mx <= setX + 280 && my >= listY && my <= listY + 50) {
                            String name = players.get(i).getProfile().getName();
                            
                            // Tag/Untag Logic
                            if (ModConfig.taggedPlayerName.equals(name)) {
                                ModConfig.taggedPlayerName = ""; 
                            } else { 
                                ModConfig.taggedPlayerName = name; 
                                ModConfig.autoTrack = false; // Disable auto-track if manually tagged
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
                int winY = (this.height - winH) / 2;
                float percentage = (float) (my - (winY + 40)) / 205f;
                percentage = Math.max(0, Math.min(1, percentage));
                scrollOffset = Math.round(percentage * (players - 4));
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
    public boolean mouseScrolled(double mx, double my, double hAmount, double scroll) {
        if (currentTab.equals("Targets")) {
            scrollOffset -= (int) scroll;
            if (scrollOffset < 0) scrollOffset = 0;
            return true;
        }
        return super.mouseScrolled(mx, my, hAmount, scroll);
    }

    @Override
    public void close() {
        ModConfig.save(); 
        if (this.client != null) {
            this.client.setScreen(this.parent); // Go back to Escape Menu / Title Screen
        }
    }
            }
