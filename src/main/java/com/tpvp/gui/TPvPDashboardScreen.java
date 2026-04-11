package com.tpvp.gui;

import com.tpvp.config.ModConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TPvPDashboardScreen extends Screen {
    private final Screen parent;
    private String currentTab = "Combat";
    
    // Scrollbar Logic
    private int scrollOffset = 0;
    private boolean isDraggingScroll = false;

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
    protected void init() {
        this.clearChildren();
        int winX = (this.width - winW) / 2;
        int winY = (this.height - winH) / 2;
        int setX = winX + sideW + 15;

        this.addDrawableChild(ButtonWidget.builder(Text.literal("⚔ Combat"), b -> { currentTab = "Combat"; this.init(); }).dimensions(winX+5, winY+40, sideW-10, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("🎯 Crosshair"), b -> { currentTab = "Crosshair"; this.init(); }).dimensions(winX+5, winY+65, sideW-10, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("💀 Targets"), b -> { currentTab = "Targets"; this.init(); }).dimensions(winX+5, winY+90, sideW-10, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("🛠 Edit HUD"), b -> this.client.setScreen(new EditHudScreen(this))).dimensions(winX+5, winY+115, sideW-10, 20).build());

        if (currentTab.equals("Combat")) {
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Armor Align: " + (ModConfig.armorVertical ? "Vertical" : "Horizontal")), b -> { ModConfig.armorVertical = !ModConfig.armorVertical; this.init(); }).dimensions(setX, winY+40, 140, 20).build());
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Hitboxes: " + (ModConfig.hitboxEnabled ? "ON" : "OFF")), b -> { ModConfig.hitboxEnabled = !ModConfig.hitboxEnabled; this.init(); }).dimensions(setX+150, winY+40, 140, 20).build());
            
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Indicator: " + (ModConfig.indicatorEnabled ? "ON" : "OFF")), b -> { ModConfig.indicatorEnabled = !ModConfig.indicatorEnabled; this.init(); }).dimensions(setX, winY+70, 140, 20).build());
            String[] styles = {"Heart Style", "Bar Style", "Head + Hits"};
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Style: " + styles[ModConfig.indicatorStyle]), b -> { ModConfig.indicatorStyle = (ModConfig.indicatorStyle + 1) % 3; this.init(); }).dimensions(setX+150, winY+70, 140, 20).build());
        
        } else if (currentTab.equals("Crosshair")) {
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Smart Crosshair: " + (ModConfig.smartCrosshair ? "ON" : "OFF")), b -> { ModConfig.smartCrosshair = !ModConfig.smartCrosshair; this.init(); }).dimensions(setX, winY+40, 140, 20).build());
            String[] crossStyles = {"Pro Plus", "Hollow Dot", "Pro Angle"};
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Style: " + crossStyles[ModConfig.crosshairStyle]), b -> { ModConfig.crosshairStyle = (ModConfig.crosshairStyle + 1) % 3; this.init(); }).dimensions(setX+150, winY+40, 140, 20).build());
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Size: " + String.format("%.1fx", ModConfig.crosshairSize)), b -> { ModConfig.crosshairSize += 0.5f; if(ModConfig.crosshairSize > 3.0f) ModConfig.crosshairSize = 0.5f; this.init(); }).dimensions(setX, winY+70, 140, 20).build());
        
        } else if (currentTab.equals("Targets")) {
            // Auto Track Toggle
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Auto-Track Low HP: " + (ModConfig.autoTrack ? "§aON" : "§cOFF")), b -> { ModConfig.autoTrack = !ModConfig.autoTrack; this.init(); }).dimensions(setX, winY+10, 160, 20).build());
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0xCC050000); 

        int winX = (this.width - winW) / 2;
        int winY = (this.height - winH) / 2;

        // RED GLASS THEME
        context.fill(winX - 2, winY - 2, winX + winW + 2, winY + winH + 2, 0xFFFF3333); // Red Border
        context.fillGradient(winX, winY, winX + winW, winY + winH, 0xDD330000, 0xEE110000); // Dark Red Glass Inside
        context.fillGradient(winX, winY, winX + winW, winY + 60, 0x44FFFFFF, 0x00FFFFFF); // Glass Shine Reflection
        context.fill(winX, winY, winX + sideW, winY + winH, 0xAA220000); // Sidebar Darker
        
        context.drawCenteredTextWithShadow(this.textRenderer, "§c§lTPvP CLIENT", winX + sideW / 2, winY + 15, 0xFFFFFF);
        
        int setX = winX + sideW + 15;

        // TARGETS MENU
        if (currentTab.equals("Targets")) {
            if (this.client != null && this.client.getNetworkHandler() != null) {
                Collection<PlayerListEntry> allPlayers = this.client.getNetworkHandler().getPlayerList();
                List<PlayerListEntry> players = new ArrayList<>(allPlayers);
                
                int listY = winY + 40;
                int maxVisible = 4; // Because body is big

                for (int i = 0; i < players.size(); i++) {
                    if (i >= scrollOffset && i < scrollOffset + maxVisible) {
                        PlayerListEntry p = players.get(i);
                        String pName = p.getProfile().getName();
                        boolean isTagged = ModConfig.taggedPlayerName.equals(pName) && !ModConfig.autoTrack;
                        
                        context.fill(setX, listY, setX + 280, listY + 50, isTagged ? 0xAAFF3333 : 0x55000000);
                        
                        // FULL 2D BODY RENDERER (Skin Mapping)
                        Identifier skin = p.getSkinTextures().texture();
                        int bx = setX + 10, by = listY + 5;
                        context.getMatrices().push();
                        context.getMatrices().translate(bx, by, 0);
                        context.getMatrices().scale(1.2f, 1.2f, 1.0f);
                        // Head
                        context.drawTexture(RenderLayer::getGuiTextured, skin, 4, 0, 8f, 8f, 8, 8, 64, 64);
                        // Body
                        context.drawTexture(RenderLayer::getGuiTextured, skin, 4, 8, 20f, 20f, 8, 12, 64, 64);
                        // Arms
                        context.drawTexture(RenderLayer::getGuiTextured, skin, 0, 8, 44f, 20f, 4, 12, 64, 64);
                        context.drawTexture(RenderLayer::getGuiTextured, skin, 12, 8, 32f, 52f, 4, 12, 64, 64);
                        // Legs
                        context.drawTexture(RenderLayer::getGuiTextured, skin, 4, 20, 0f, 20f, 4, 12, 64, 64);
                        context.drawTexture(RenderLayer::getGuiTextured, skin, 8, 20, 16f, 52f, 4, 12, 64, 64);
                        context.getMatrices().pop();
                        
                        context.drawTextWithShadow(this.textRenderer, isTagged ? "§c§l" + pName : "§f" + pName, setX + 50, listY + 20, 0xFFFFFF);
                        
                        listY += 55;
                    }
                }

                // Drag Scrollbar
                if (players.size() > maxVisible) {
                    context.fill(setX + 290, winY + 40, setX + 295, winY + 245, 0x55000000); 
                    int trackHeight = 205;
                    int thumbH = Math.max(20, trackHeight / (players.size() - maxVisible + 1));
                    int maxScroll = Math.max(1, players.size() - maxVisible);
                    int thumbY = winY + 40 + (scrollOffset * (trackHeight - thumbH) / maxScroll);
                    context.fill(setX + 290, thumbY, setX + 295, thumbY + thumbH, 0xFFFF3333); 
                }
            }
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        int winX = (this.width - winW) / 2;
        int winY = (this.height - winH) / 2;
        int setX = winX + sideW + 15;

        if (currentTab.equals("Targets")) {
            // Scrollbar Track Click
            if (mx >= setX + 290 && mx <= setX + 295 && my >= winY + 40 && my <= winY + 245) {
                isDraggingScroll = true;
                return true;
            }

            if (this.client != null && this.client.getNetworkHandler() != null) {
                List<PlayerListEntry> players = new ArrayList<>(this.client.getNetworkHandler().getPlayerList());
                int listY = winY + 40;
                for (int i = 0; i < players.size(); i++) {
                    if (i >= scrollOffset && i < scrollOffset + 4) {
                        if (mx >= setX && mx <= setX + 280 && my >= listY && my <= listY + 50) {
                            String name = players.get(i).getProfile().getName();
                            if (ModConfig.taggedPlayerName.equals(name)) ModConfig.taggedPlayerName = ""; 
                            else { ModConfig.taggedPlayerName = name; ModConfig.autoTrack = false; }
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
            int maxVisible = 4;
            if (players > maxVisible) {
                int winY = (this.height - winH) / 2;
                int trackHeight = 205;
                float percentage = (float) (my - (winY + 40)) / trackHeight;
                percentage = Math.max(0, Math.min(1, percentage));
                scrollOffset = Math.round(percentage * (players - maxVisible));
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
    public void close() { ModConfig.save(); if (this.client != null) this.client.setScreen(this.parent); }
                            }
