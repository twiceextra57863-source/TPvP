package com.yourname.mtpvp.client.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class MtpvpDashboardScreen extends Screen {
    private final Screen parent;
    
    public MtpvpDashboardScreen(Screen parent) {
        super(Text.literal("MTPVP Dashboard"));
        this.parent = parent;
    }
    
    @Override
    protected void init() {
        super.init();
        
        // Left side features
        int startY = height / 4;
        int buttonWidth = 100;
        int buttonHeight = 20;
        
        // Indicator Button
        ButtonWidget indicatorBtn = ButtonWidget.builder(Text.literal("Indicator"), 
            button -> showFeatureMessage("Indicator"))
            .dimensions(20, startY, buttonWidth, buttonHeight)
            .build();
        this.addDrawableChild(indicatorBtn);
        
        // Basic Button
        ButtonWidget basicBtn = ButtonWidget.builder(Text.literal("Basic"), 
            button -> showFeatureMessage("Basic"))
            .dimensions(20, startY + 30, buttonWidth, buttonHeight)
            .build();
        this.addDrawableChild(basicBtn);
        
        // Advance Button
        ButtonWidget advanceBtn = ButtonWidget.builder(Text.literal("Advance"), 
            button -> showFeatureMessage("Advance"))
            .dimensions(20, startY + 60, buttonWidth, buttonHeight)
            .build();
        this.addDrawableChild(advanceBtn);
        
        // Epic Button
        ButtonWidget epicBtn = ButtonWidget.builder(Text.literal("Epic"), 
            button -> showFeatureMessage("Epic"))
            .dimensions(20, startY + 90, buttonWidth, buttonHeight)
            .build();
        this.addDrawableChild(epicBtn);
        
        // Close button
        ButtonWidget closeBtn = ButtonWidget.builder(Text.literal("Close"), button -> close())
            .dimensions(width / 2 - 50, height - 30, 100, 20)
            .build();
        this.addDrawableChild(closeBtn);
        
        // Add title text widget
        TextWidget titleText = new TextWidget(width / 2 - 50, 20, 
            Text.literal("MTPVP Features").formatted(Formatting.BOLD, Formatting.GOLD), this.textRenderer);
        titleText.setWidth(100);
        this.addDrawableChild(titleText);
    }
    
    private void showFeatureMessage(String feature) {
        // You can implement actual feature functionality here
        // For now, just show a message
        if (client != null && client.player != null) {
            client.player.sendMessage(Text.literal("§a[§6MTPVP§a] §f" + feature + " feature clicked!"), false);
        }
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        
        // Draw the dashboard background
        context.fill(10, 10, width - 10, height - 10, 0xCC000000);
        context.drawBorder(10, 10, width - 20, height - 20, 0xFFAA00);
        
        // Draw title
        context.drawText(textRenderer, "MTPVP Dashboard", width / 2 - 60, 25, 0xFFFFAA, false);
        
        super.render(context, mouseX, mouseY, delta);
    }
    
    @Override
    public void close() {
        if (client != null) {
            client.setScreen(parent);
        }
    }
}
