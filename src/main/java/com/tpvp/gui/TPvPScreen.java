package com.tpvp.gui;

import com.tpvp.TPvPConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class TPvPScreen extends Screen {
    public TPvPScreen() {
        super(Text.literal("TPvP Dashboard"));
    }

    @Override
    protected void init() {
        // Left Sidebar Button
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Heart Indicator"), button -> {
            // Right side logic yahan aayegi
        }).dimensions(10, 50, 100, 20).build());

        // Toggle Button (Right Side)
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Status: " + (TPvPConfig.heartIndicatorEnabled ? "ON" : "OFF")), 
            button -> {
                TPvPConfig.heartIndicatorEnabled = !TPvPConfig.heartIndicatorEnabled;
                button.setMessage(Text.literal("Status: " + (TPvPConfig.heartIndicatorEnabled ? "ON" : "OFF")));
            }
        ).dimensions(150, 50, 100, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Dark Background Overlay
        context.fill(0, 0, this.width, this.height, 0x88000000);
        
        // Sidebar Background
        context.fill(0, 0, 120, this.height, 0xAA111111);
        
        // Header
        context.drawCenteredTextWithShadow(this.textRenderer, "TPvP CLIENT - PREMIUM", this.width / 2, 10, 0xFF55FF);
        
        super.render(context, mouseX, mouseY, delta);
    }
}
