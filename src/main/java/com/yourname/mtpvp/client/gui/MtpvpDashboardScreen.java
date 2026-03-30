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
        
        int centerX = width / 2;
        int startY = height / 4; // Moved up from /3 to give more space
        
        int buttonWidth = 220;
        int buttonHeight = 45;
        
        // Title
        TextWidget titleText = new TextWidget(centerX - 120, 20, 
            Text.literal("⚔️ MTPVP DASHBOARD ⚔️").formatted(Formatting.BOLD, Formatting.GOLD), 
            this.textRenderer);
        titleText.setWidth(240);
        this.addDrawableChild(titleText);
        
        // Heart Indicator Button (First button)
        ButtonWidget indicatorBtn = ButtonWidget.builder(
            Text.literal("❤️ Heart Indicator").formatted(Formatting.RED, Formatting.BOLD),
            button -> {
                if (client != null) {
                    client.setScreen(new HeartIndicatorOptionsScreen(this));
                }
            })
            .dimensions(centerX - 110, startY, buttonWidth, buttonHeight)
            .build();
        this.addDrawableChild(indicatorBtn);
        
        // Basic Button
        ButtonWidget basicBtn = ButtonWidget.builder(
            Text.literal("📦 Basic Features").formatted(Formatting.GREEN, Formatting.BOLD),
            button -> showFeatureMessage("Basic Features"))
            .dimensions(centerX - 110, startY + 55, buttonWidth, buttonHeight)
            .build();
        this.addDrawableChild(basicBtn);
        
        // Advance Button
        ButtonWidget advanceBtn = ButtonWidget.builder(
            Text.literal("⚡ Advance Features").formatted(Formatting.BLUE, Formatting.BOLD),
            button -> showFeatureMessage("Advance Features"))
            .dimensions(centerX - 110, startY + 110, buttonWidth, buttonHeight)
            .build();
        this.addDrawableChild(advanceBtn);
        
        // Epic Button
        ButtonWidget epicBtn = ButtonWidget.builder(
            Text.literal("✨ Epic Features").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD),
            button -> showFeatureMessage("Epic Features"))
            .dimensions(centerX - 110, startY + 165, buttonWidth, buttonHeight)
            .build();
        this.addDrawableChild(epicBtn);
        
        // Close Button - Moved to top right corner
        ButtonWidget closeBtn = ButtonWidget.builder(
            Text.literal("✗").formatted(Formatting.RED, Formatting.BOLD),
            button -> close())
            .dimensions(width - 35, 10, 25, 25)
            .build();
        this.addDrawableChild(closeBtn);
        
        // Description text
        TextWidget descText = new TextWidget(centerX - 120, startY - 20, 
            Text.literal("Select a feature to customize").formatted(Formatting.ITALIC, Formatting.GRAY), 
            this.textRenderer);
        descText.setWidth(240);
        this.addDrawableChild(descText);
    }
    
    private void showFeatureMessage(String feature) {
        if (client != null && client.player != null) {
            client.player.sendMessage(Text.literal("§a[§6MTPVP§a] §f" + feature + " coming soon!"), false);
        }
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        
        // Gradient background
        for (int i = 0; i < height; i++) {
            int alpha = (int)(80 - (i / (float)height) * 60);
            context.fill(0, i, width, i + 1, 0x88000000 | (alpha << 24));
        }
        
        // Decorative border
        context.drawBorder(5, 5, width - 10, height - 10, 0xFFAA00);
        context.drawBorder(8, 8, width - 16, height - 16, 0xFFAA00);
        
        super.render(context, mouseX, mouseY, delta);
    }
    
    @Override
    public void close() {
        if (client != null) {
            client.setScreen(parent);
        }
    }
}
