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
        int startY = height / 3;
        int buttonWidth = 200;
        int buttonHeight = 45;
        
        // Title
        TextWidget titleText = new TextWidget(centerX - 100, 30, 
            Text.literal("⚔️ MTPVP DASHBOARD ⚔️").formatted(Formatting.BOLD, Formatting.GOLD), 
            this.textRenderer);
        titleText.setWidth(200);
        this.addDrawableChild(titleText);
        
        // Heart Indicator Button
        ButtonWidget indicatorBtn = ButtonWidget.builder(
            Text.literal("❤️ Heart Indicator").formatted(Formatting.RED),
            button -> {
                if (client != null) {
                    client.setScreen(new HeartIndicatorOptionsScreen(this));
                }
            })
            .dimensions(centerX - 100, startY, buttonWidth, buttonHeight)
            .build();
        this.addDrawableChild(indicatorBtn);
        
        // Basic Button
        ButtonWidget basicBtn = ButtonWidget.builder(
            Text.literal("📦 Basic Features").formatted(Formatting.GREEN),
            button -> showFeatureMessage("Basic Features"))
            .dimensions(centerX - 100, startY + 55, buttonWidth, buttonHeight)
            .build();
        this.addDrawableChild(basicBtn);
        
        // Advance Button
        ButtonWidget advanceBtn = ButtonWidget.builder(
            Text.literal("⚡ Advance Features").formatted(Formatting.BLUE),
            button -> showFeatureMessage("Advance Features"))
            .dimensions(centerX - 100, startY + 110, buttonWidth, buttonHeight)
            .build();
        this.addDrawableChild(advanceBtn);
        
        // Epic Button
        ButtonWidget epicBtn = ButtonWidget.builder(
            Text.literal("✨ Epic Features").formatted(Formatting.LIGHT_PURPLE),
            button -> showFeatureMessage("Epic Features"))
            .dimensions(centerX - 100, startY + 165, buttonWidth, buttonHeight)
            .build();
        this.addDrawableChild(epicBtn);
        
        // Close Button
        ButtonWidget closeBtn = ButtonWidget.builder(
            Text.literal("✗ Close").formatted(Formatting.GRAY),
            button -> close())
            .dimensions(centerX - 50, height - 45, 100, 25)
            .build();
        this.addDrawableChild(closeBtn);
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
        
        // Border
        context.drawBorder(5, 5, width - 10, height - 10, 0xFFAA00);
        
        // Subtitle
        context.drawText(textRenderer, "Select a feature to customize", 
                        width / 2 - 100, height / 3 - 25, 0xCCCCCC, false);
        
        super.render(context, mouseX, mouseY, delta);
    }
    
    @Override
    public void close() {
        if (client != null) {
            client.setScreen(parent);
        }
    }
}
