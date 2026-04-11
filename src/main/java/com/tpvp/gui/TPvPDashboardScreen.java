package com.tpvp.gui;

import com.tpvp.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;

import java.util.Collection;

public class TPvPDashboardScreen extends Screen {
    private final Screen parent;
    private String currentTab = "Combat";
    private int scrollOffset = 0;

    private final int winW = 440;
    private final int winH = 260;
    private final int sideW = 120;

    public TPvPDashboardScreen(Screen parent) {
        super(Text.literal("TPvP Dashboard"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.clearChildren();
        int winX = (this.width - winW) / 2;
        int winY = (this.height - winH) / 2;
        int setX = winX + sideW + 15;

        // TABS
        this.addDrawableChild(ButtonWidget.builder(Text.literal("⚔ Combat"), b -> { currentTab = "Combat"; this.init(); }).dimensions(winX+5, winY+40, sideW-10, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("🎯 Crosshair"), b -> { currentTab = "Crosshair"; this.init(); }).dimensions(winX+5, winY+65, sideW-10, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("💀 Targets"), b -> { currentTab = "Targets"; this.init(); }).dimensions(winX+5, winY+90, sideW-10, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("🛠 Edit HUD"), b -> this.client.setScreen(new EditHudScreen(this))).dimensions(winX+5, winY+115, sideW-10, 20).build());

        if (currentTab.equals("Combat")) {
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Armor Align: " + (ModConfig.armorVertical ? "Vertical" : "Horizontal")), b -> { ModConfig.armorVertical = !ModConfig.armorVertical; this.init(); }).dimensions(setX, winY+40, 140, 20).build());
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Hitboxes: " + (ModConfig.hitboxEnabled ? "ON" : "OFF")), b -> { ModConfig.hitboxEnabled = !ModConfig.hitboxEnabled; this.init(); }).dimensions(setX+150, winY+40, 140, 20).build());
        
        } else if (currentTab.equals("Crosshair")) {
            String[] styles = {"Pro Plus", "Hollow Dot", "Pro Angle"};
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Style: " + styles[ModConfig.crosshairStyle]), b -> { ModConfig.crosshairStyle = (ModConfig.crosshairStyle + 1) % 3; this.init(); }).dimensions(setX, winY+40, 140, 20).build());
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Size: " + String.format("%.1fx", ModConfig.crosshairSize)), b -> { 
                ModConfig.crosshairSize += 0.5f; if(ModConfig.crosshairSize > 3.0f) ModConfig.crosshairSize = 0.5f; this.init(); 
            }).dimensions(setX+150, winY+40, 140, 20).build());
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // FIX FOR TITLE SCREEN OPACITY BUG: Solid background instead of gradient
        context.fill(0, 0, this.width, this.height, 0xEE111111); 

        int winX = (this.width - winW) / 2;
        int winY = (this.height - winH) / 2;

        context.fill(winX - 2, winY - 2, winX + winW + 2, winY + winH + 2, 0xFF00FFAA); // Border
        context.fill(winX, winY, winX + winW, winY + winH, 0xFF151515); // Main
        context.fill(winX, winY, winX + sideW, winY + winH, 0xFF0A0A0A); // Sidebar
        
        context.drawCenteredTextWithShadow(this.textRenderer, "§b§lTPvP CLIENT", winX + sideW / 2, winY + 15, 0xFFFFFF);
        
        int setX = winX + sideW + 15;

        // LIVE CROSSHAIR PREVIEW
        if (currentTab.equals("Crosshair")) {
            context.fill(setX, winY+80, setX + 290, winY + 220, 0xFF000000); // Black Preview Box
            context.drawCenteredTextWithShadow(this.textRenderer, "Live Preview", setX + 145, winY + 85, 0xAAAAAA);
            
            int cx = setX + 145;
            int cy = winY + 150;
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

        // TARGETS TAB (Scrollable Player List)
        if (currentTab.equals("Targets")) {
            context.drawTextWithShadow(this.textRenderer, "Select a player to Target", setX, winY + 15, 0xFFFFFF);
            if (this.client != null && this.client.getNetworkHandler() != null) {
                Collection<PlayerListEntry> players = this.client.getNetworkHandler().getPlayerList();
                int listY = winY + 40;
                int count = 0;
                for (PlayerListEntry p : players) {
                    if (count >= scrollOffset && count < scrollOffset + 6) { // Max 6 on screen
                        String pName = p.getProfile().getName();
                        boolean isTagged = ModConfig.taggedPlayerName.equals(pName);
                        
                        // Background row
                        context.fill(setX, listY, setX + 280, listY + 25, isTagged ? 0x6600FF00 : 0x33FFFFFF);
                        
                        // Player Head
                        context.drawTexture(net.minecraft.client.render.RenderLayer::getGuiTextured, p.getSkinTextures().texture(), setX + 5, listY + 4, 8f, 8f, 16, 16, 64, 64);
                        
                        context.drawTextWithShadow(this.textRenderer, pName, setX + 30, listY + 8, 0xFFFFFF);
                        
                        // Click logic for rows handled in mouseClicked
                        listY += 30;
                    }
                    count++;
                }
            } else {
                context.drawTextWithShadow(this.textRenderer, "§cJoin a server to see players.", setX, winY + 50, 0xFFFFFF);
            }
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        int winX = (this.width - winW) / 2;
        int winY = (this.height - winH) / 2;
        int setX = winX + sideW + 15;

        // Target Selection Logic
        if (currentTab.equals("Targets") && this.client != null && this.client.getNetworkHandler() != null) {
            Collection<PlayerListEntry> players = this.client.getNetworkHandler().getPlayerList();
            int listY = winY + 40;
            int count = 0;
            for (PlayerListEntry p : players) {
                if (count >= scrollOffset && count < scrollOffset + 6) {
                    if (mx >= setX && mx <= setX + 280 && my >= listY && my <= listY + 25) {
                        String name = p.getProfile().getName();
                        if (ModConfig.taggedPlayerName.equals(name)) ModConfig.taggedPlayerName = ""; // Untag
                        else ModConfig.taggedPlayerName = name; // Tag
                        return true;
                    }
                    listY += 30;
                }
                count++;
            }
        }
        return super.mouseClicked(mx, my, button);
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
