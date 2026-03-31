package com.tpvp.gui;

import com.tpvp.config.ModConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class TPvPDashboardScreen extends Screen {
    private String currentTab = "Combat";

    public TPvPDashboardScreen() {
        super(Text.literal("TPvP Dashboard"));
    }

    @Override
    protected void init() {
        int sidebarWidth = 120;
        
        // Left Side Tabs (Categories)
        this.addDrawableChild(ButtonWidget.builder(Text.literal("⚔ Combat"), button -> currentTab = "Combat")
                .dimensions(10, 40, sidebarWidth - 20, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("🛡 Render"), button -> currentTab = "Render")
                .dimensions(10, 65, sidebarWidth - 20, 20).build());

        // Right Side Settings (Only visible when Combat is selected)
        // Enable/Disable Indicator
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Indicator: " + (ModConfig.indicatorEnabled ? "ON" : "OFF")), button -> {
            ModConfig.indicatorEnabled = !ModConfig.indicatorEnabled;
            button.setMessage(Text.literal("Indicator: " + (ModConfig.indicatorEnabled ? "ON" : "OFF")));
        }).dimensions(sidebarWidth + 20, 40, 150, 20).build());

        // Change Style
        String[] styles = {"Heart Style", "Bar Style", "Head + Hits Style"};
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Style: " + styles[ModConfig.indicatorStyle]), button -> {
            ModConfig.indicatorStyle = (ModConfig.indicatorStyle + 1) % 3;
            button.setMessage(Text.literal("Style: " + styles[ModConfig.indicatorStyle]));
        }).dimensions(sidebarWidth + 20, 65, 150, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta); // Blur background

        int sidebarWidth = 120;

        // Left Sidebar Background
        context.fill(0, 0, sidebarWidth, this.height, 0x99000000);
        // Right Main Area Background
        context.fill(sidebarWidth, 0, this.width, this.height, 0x66000000);

        // Title Line
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("§lTPvP Client Config"), sidebarWidth / 2, 15, 0xFFFFFF);
        
        // Tab Title
        context.drawTextWithShadow(this.textRenderer, Text.literal("Current Category: §e" + currentTab), sidebarWidth + 20, 15, 0xFFFFFF);

        super.render(context, mouseX, mouseY, delta);
    }
                     }
