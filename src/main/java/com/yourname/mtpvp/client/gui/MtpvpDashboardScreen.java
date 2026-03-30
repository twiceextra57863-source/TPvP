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
        
        int startY = height / 4;
        int buttonWidth = 150;
        int buttonHeight = 25;
        int centerX = width / 2;
        
        // Title
        TextWidget titleText = new TextWidget(centerX - 100, 20, 
            Text.literal("MTPVP Features").formatted(Formatting.BOLD, Formatting.GOLD), this.textRenderer);
        titleText.setWidth(200);
        this.addDrawableChild(titleText);
        
        // Indicator Button with sub-options
        ButtonWidget indicatorBtn = ButtonWidget.builder(Text.literal("► Heart Indicator"), 
            button -> showHeartIndicatorOptions())
            .dimensions(centerX - 75, startY, buttonWidth, buttonHeight)
            .build();
        this.addDrawableChild(indicatorBtn);
        
        // Basic Button
        ButtonWidget basicBtn = ButtonWidget.builder(Text.literal("Basic Features"), 
            button -> showFeatureMessage("Basic"))
            .dimensions(centerX - 75, startY + 35, buttonWidth, buttonHeight)
            .build();
        this.addDrawableChild(basicBtn);
        
        // Advance Button
        ButtonWidget advanceBtn = ButtonWidget.builder(Text.literal("Advance Features"), 
            button -> showFeatureMessage("Advance"))
            .dimensions(centerX - 75, startY + 70, buttonWidth, buttonHeight)
            .build();
        this.addDrawableChild(advanceBtn);
        
        // Epic Button
        ButtonWidget epicBtn = ButtonWidget.builder(Text.literal("Epic Features"), 
            button -> showFeatureMessage("Epic"))
            .dimensions(centerX - 75, startY + 105, buttonWidth, buttonHeight)
            .build();
        this.addDrawableChild(epicBtn);
        
        // Close button
        ButtonWidget closeBtn = ButtonWidget.builder(Text.literal("Close"), button -> close())
            .dimensions(centerX - 50, height - 40, 100, 20)
            .build();
        this.addDrawableChild(closeBtn);
    }
    
    private void showHeartIndicatorOptions() {
        if (client != null) {
            client.setScreen(new HeartIndicatorOptionsScreen(this));
        }
    }
    
    private void showFeatureMessage(String feature) {
        if (client != null && client.player != null) {
            client.player.sendMessage(Text.literal("§a[§6MTPVP§a] §f" + feature + " feature will be implemented soon!"), false);
        }
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        context.fill(10, 10, width - 10, height - 10, 0xCC000000);
        context.drawBorder(10, 10, width - 20, height - 20, 0xFFAA00);
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
