package com.tpvp.gui;

import com.tpvp.config.ModConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class TPvPDashboardScreen extends Screen {
    private String currentTab = "Combat";
    private final int sidebarWidth = 120;

    public TPvPDashboardScreen() {
        super(Text.literal("TPvP Dashboard"));
    }

    @Override
    protected void init() {
        this.clearChildren();

        // TABS
        this.addDrawableChild(ButtonWidget.builder(Text.literal("⚔ Combat"), button -> {
            currentTab = "Combat"; this.init();
        }).dimensions(10, 40, sidebarWidth - 20, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("📡 Radar"), button -> {
            currentTab = "Radar"; this.init();
        }).dimensions(10, 65, sidebarWidth - 20, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("🛠 Edit HUD"), button -> {
            this.client.setScreen(new EditHudScreen(this));
        }).dimensions(10, 90, sidebarWidth - 20, 20).build());

        // SETTINGS (Based on Tab)
        if (currentTab.equals("Combat")) {
            // Indicator Toggle
            this.addDrawableChild(ButtonWidget.builder(Text.literal("3D Indicator: " + (ModConfig.indicatorEnabled ? "ON" : "OFF")), button -> {
                ModConfig.indicatorEnabled = !ModConfig.indicatorEnabled;
                button.setMessage(Text.literal("3D Indicator: " + (ModConfig.indicatorEnabled ? "ON" : "OFF")));
            }).dimensions(sidebarWidth + 20, 40, 150, 20).build());

            // Indicator Style
            String[] styles = {"Heart Style", "Bar Style", "Head + Hits Style"};
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Style: " + styles[ModConfig.indicatorStyle]), button -> {
                ModConfig.indicatorStyle = (ModConfig.indicatorStyle + 1) % 3;
                button.setMessage(Text.literal("Style: " + styles[ModConfig.indicatorStyle]));
            }).dimensions(sidebarWidth + 20, 65, 150, 20).build());

            // Smart Crosshair Toggle
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Smart Crosshair: " + (ModConfig.smartCrosshair ? "ON" : "OFF")), button -> {
                ModConfig.smartCrosshair = !ModConfig.smartCrosshair;
                button.setMessage(Text.literal("Smart Crosshair: " + (ModConfig.smartCrosshair ? "ON" : "OFF")));
            }).dimensions(sidebarWidth + 20, 90, 150, 20).build());

            // PvP Hitboxes Toggle
            this.addDrawableChild(ButtonWidget.builder(Text.literal("PvP Hitboxes: " + (ModConfig.hitboxEnabled ? "ON" : "OFF")), button -> {
                ModConfig.hitboxEnabled = !ModConfig.hitboxEnabled;
                button.setMessage(Text.literal("PvP Hitboxes: " + (ModConfig.hitboxEnabled ? "ON" : "OFF")));
            }).dimensions(sidebarWidth + 20, 115, 150, 20).build());

        } else if (currentTab.equals("Radar")) {
            // Radar Toggle
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Nearby Players: " + (ModConfig.nearbyEnabled ? "ON" : "OFF")), button -> {
                ModConfig.nearbyEnabled = !ModConfig.nearbyEnabled;
                button.setMessage(Text.literal("Nearby Players: " + (ModConfig.nearbyEnabled ? "ON" : "OFF")));
            }).dimensions(sidebarWidth + 20, 40, 150, 20).build());
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        
        // Sidebar and Main Area transparency
        context.fill(0, 0, sidebarWidth, this.height, 0x99000000);
        context.fill(sidebarWidth, 0, this.width, this.height, 0x66000000);
        
        context.drawCenteredTextWithShadow(this.textRenderer, "§b§lTPvP Client", sidebarWidth / 2, 15, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer, "Category: §e" + currentTab, sidebarWidth + 20, 15, 0xFFFFFF);
        
        super.render(context, mouseX, mouseY, delta);
    }
}
