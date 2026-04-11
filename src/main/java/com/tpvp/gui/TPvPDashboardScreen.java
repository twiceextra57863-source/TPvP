package com.tpvp.gui;

import com.tpvp.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class TPvPDashboardScreen extends Screen {
    private final Screen parent;
    private String currentTab = "Combat";
    private int scrollOffset = 0;

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

        // TABS
        this.addDrawableChild(ButtonWidget.builder(Text.literal("⚔ Combat"), b -> { currentTab = "Combat"; this.init(); }).dimensions(winX+5, winY+40, sideW-10, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("🎯 Crosshair"), b -> { currentTab = "Crosshair"; this.init(); }).dimensions(winX+5, winY+65, sideW-10, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("💀 Targets"), b -> { currentTab = "Targets"; this.init(); }).dimensions(winX+5, winY+90, sideW-10, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("🛠 Edit HUD"), b -> this.client.setScreen(new EditHudScreen(this))).dimensions(winX+5, winY+115, sideW-10, 20).build());

        // SETTINGS (Combat)
        if (currentTab.equals("Combat")) {
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Armor Align: " + (ModConfig.armorVertical ? "Vertical" : "Horizontal")), b -> { ModConfig.armorVertical = !ModConfig.armorVertical; this.init(); }).dimensions(setX, winY+40, 140, 20).build());
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Hitboxes: " + (ModConfig.hitboxEnabled ? "ON" : "OFF")), b -> { ModConfig.hitboxEnabled = !ModConfig.hitboxEnabled; this.init(); }).dimensions(setX+150, winY+40, 140, 20).build());
            
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Indicator: " + (ModConfig.indicatorEnabled ? "ON" : "OFF")), b -> { ModConfig.indicatorEnabled = !ModConfig.indicatorEnabled; this.init(); }).dimensions(setX, winY+70, 140, 20).build());
            String[] styles = {"Heart Style", "Bar Style", "Head + Hits"};
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Style: " + styles[ModConfig.indicatorStyle]), b -> { ModConfig.indicatorStyle = (ModConfig.indicatorStyle + 1) % 3; this.init(); }).dimensions(setX+150, winY+70, 140, 20).build());
        } 
        // SETTINGS (Crosshair)
        else if (currentTab.equals("Crosshair")) {
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Smart Crosshair: " + (ModConfig.smartCrosshair ? "ON" : "OFF")), b -> { ModConfig.smartCrosshair = !ModConfig.smartCrosshair; this.init(); }).dimensions(setX, winY+40, 140, 20).build());
            String[] crossStyles = {"Pro Plus", "Hollow Dot", "Pro Angle"};
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Style: " + crossStyles[ModConfig.crosshairStyle]), b -> { ModConfig.crosshairStyle = (ModConfig.crosshairStyle + 1) % 3; this.init(); }).dimensions(setX+150, winY+40, 140, 20).build());
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Size: " + String.format("%.1fx", ModConfig.crosshairSize)), b -> { ModConfig.crosshairSize += 0.5f; if(ModConfig.crosshairSize > 3.0f) ModConfig.crosshairSize = 0.5f; this.init(); }).dimensions(setX, winY+70, 140, 20).build());
        }
    }

    @Override
    public void render(DrawContext context, int mx, int my, float delta) {
        // 1. Dark Blur Background
        context.fill(0, 0, this.width, this.height, 0xCC000000); 

        int winX = (this.width - winW) / 2;
        int winY = (this.height - winH) / 2;

        // 2. RED GLASS SHINE THEME
        context.fill(winX - 2, winY - 2, winX + winW + 2, winY + winH + 2, 0xFFFF2222); // Red Outer Glow Border
        context.fillGradient(winX, winY, winX + winW, winY + winH, 0xDD440000, 0xEE110000); // Red Glass Inside
        context.fillGradient(winX, winY, winX + winW, winY + 50, 0x33FFFFFF, 0x00FFFFFF); // Top Glass Shine Reflection
        context.fill(winX, winY, winX + sideW, winY + winH, 0x99220000); // Sidebar Darker Red

        context.drawCenteredTextWithShadow(this.textRenderer, "§c§lTPvP CLIENT", winX + sideW / 2, winY + 15, 0xFFFFFF);
        
        int setX = winX + sideW + 15;

        // --- TARGETS MENU (Scrollable & Distance Sorted) ---
        if (currentTab.equals("Targets")) {
            context.drawTextWithShadow(this.textRenderer, "§fSelect a player to Target (Scroll to see more)", setX, winY + 15, 0xFFFFFF);
            
            if (this.client != null && this.client.world != null && this.client.player != null) {
                // Get all players and sort by distance (Closest First)
                List<AbstractClientPlayerEntity> players = this.client.world.getPlayers().stream()
                        .filter(p -> p != this.client.player)
                        .sorted(Comparator.comparingDouble(p -> p.distanceTo(this.client.player)))
                        .collect(Collectors.toList());

                int listY = winY + 35;
                int count = 0;
                int maxVisible = 5; // 5 players at a time

                for (AbstractClientPlayerEntity p : players) {
                    if (count >= scrollOffset && count < scrollOffset + maxVisible) {
                        String pName = p.getName().getString();
                        boolean isTagged = ModConfig.taggedPlayerName.equals(pName);
                        double dist = this.client.player.distanceTo(p);

                        // Row Background (Highlight Red if Tagged)
                        context.fill(setX, listY, setX + 300, listY + 35, isTagged ? 0xAAFF0000 : 0x55000000);
                        
                        // CLEAR PLAYER HEAD RENDER (Bigger size 24x24)
                        context.drawTexture(RenderLayer::getGuiTextured, p.getSkinTextures().texture(), setX + 5, listY + 5, 8f, 8f, 24, 24, 64, 64);
                        
                        // Text
                        context.drawTextWithShadow(this.textRenderer, isTagged ? "§c§l" + pName : "§f" + pName, setX + 40, listY + 8, 0xFFFFFF);
                        context.drawTextWithShadow(this.textRenderer, String.format("§7Distance: %.1fm", dist), setX + 40, listY + 20, 0xFFFFFF);
                        
                        listY += 40;
                    }
                    count++;
                }

                // Scrollbar visual
                if (players.size() > maxVisible) {
                    context.fill(setX + 305, winY + 35, setX + 310, winY + 235, 0x55000000); // Track
                    int thumbH = Math.max(20, 200 / (players.size() - maxVisible + 1));
                    int thumbY = winY + 35 + (scrollOffset * (200 - thumbH) / Math.max(1, players.size() - maxVisible));
                    context.fill(setX + 305, thumbY, setX + 310, thumbY + thumbH, 0xFFFF2222); // Thumb
                }
            } else {
                context.drawTextWithShadow(this.textRenderer, "§cJoin a server to see players.", setX, winY + 50, 0xFFFFFF);
            }
        }

        super.render(context, mx, my, delta);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        int winX = (this.width - winW) / 2;
        int winY = (this.height - winH) / 2;
        int setX = winX + sideW + 15;

        // Targets Click Logic
        if (currentTab.equals("Targets") && this.client != null && this.client.world != null) {
            List<AbstractClientPlayerEntity> players = this.client.world.getPlayers().stream()
                    .filter(p -> p != this.client.player)
                    .sorted(Comparator.comparingDouble(p -> p.distanceTo(this.client.player)))
                    .collect(Collectors.toList());

            int listY = winY + 35;
            int count = 0;
            for (AbstractClientPlayerEntity p : players) {
                if (count >= scrollOffset && count < scrollOffset + 5) {
                    if (mx >= setX && mx <= setX + 300 && my >= listY && my <= listY + 35) {
                        String name = p.getName().getString();
                        if (ModConfig.taggedPlayerName.equals(name)) ModConfig.taggedPlayerName = ""; 
                        else ModConfig.taggedPlayerName = name; 
                        return true;
                    }
                    listY += 40;
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
