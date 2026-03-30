package com.yourname.mtpvp.client.gui;

import com.yourname.mtpvp.client.render.HeartIndicatorRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class HeartIndicatorOptionsScreen extends Screen {
    private final Screen parent;
    private HeartIndicatorRenderer.DesignType selectedDesign;
    private ButtonWidget vanillaBtn;
    private ButtonWidget statusBarBtn;
    private ButtonWidget headBtn;
    private ButtonWidget disableBtn;
    
    public HeartIndicatorOptionsScreen(Screen parent) {
        super(Text.literal("Heart Indicator Options"));
        this.parent = parent;
        this.selectedDesign = HeartIndicatorRenderer.currentDesign;
    }
    
    @Override
    protected void init() {
        super.init();
        
        int centerX = width / 2;
        int startY = height / 3;
        int buttonWidth = 220;
        int buttonHeight = 40;
        
        // Title
        TextWidget titleText = new TextWidget(centerX - 120, 25, 
            Text.literal("❤️ HEART INDICATOR SETTINGS ❤️").formatted(Formatting.BOLD, Formatting.GOLD), 
            this.textRenderer);
        titleText.setWidth(240);
        this.addDrawableChild(titleText);
        
        // Create Vanilla Button
        vanillaBtn = ButtonWidget.builder(
            Text.literal("❤️ Vanilla Hearts").formatted(Formatting.BOLD),
            button -> {
                selectedDesign = HeartIndicatorRenderer.DesignType.VANILLA;
                updatePreview();
                updateSelectionHighlight();
            })
            .dimensions(centerX - 110, startY, buttonWidth, buttonHeight)
            .build();
        
        // Create Status Bar Button
        statusBarBtn = ButtonWidget.builder(
            Text.literal("📊 Status Bar").formatted(Formatting.BOLD),
            button -> {
                selectedDesign = HeartIndicatorRenderer.DesignType.STATUS_BAR;
                updatePreview();
                updateSelectionHighlight();
            })
            .dimensions(centerX - 110, startY + 55, buttonWidth, buttonHeight)
            .build();
        
        // Create Player Head Button
        headBtn = ButtonWidget.builder(
            Text.literal("👤 Player Head + HTK").formatted(Formatting.BOLD),
            button -> {
                selectedDesign = HeartIndicatorRenderer.DesignType.PLAYER_HEAD;
                updatePreview();
                updateSelectionHighlight();
            })
            .dimensions(centerX - 110, startY + 110, buttonWidth, buttonHeight)
            .build();
        
        // Create Disable Button
        disableBtn = ButtonWidget.builder(
            Text.literal("❌ DISABLE INDICATOR").formatted(Formatting.BOLD, Formatting.DARK_RED),
            button -> {
                selectedDesign = HeartIndicatorRenderer.DesignType.DISABLED;
                updatePreview();
                updateSelectionHighlight();
            })
            .dimensions(centerX - 110, startY + 175, buttonWidth, buttonHeight)
            .build();
        
        // Add all buttons
        this.addDrawableChild(vanillaBtn);
        this.addDrawableChild(statusBarBtn);
        this.addDrawableChild(headBtn);
        this.addDrawableChild(disableBtn);
        
        // Save Button
        ButtonWidget saveBtn = ButtonWidget.builder(
            Text.literal("✓ SAVE & APPLY").formatted(Formatting.BOLD, Formatting.GREEN),
            button -> {
                HeartIndicatorRenderer.setDesignType(selectedDesign);
                close();
            })
            .dimensions(centerX - 80, height - 55, 160, 35)
            .build();
        this.addDrawableChild(saveBtn);
        
        // Cancel Button
        ButtonWidget cancelBtn = ButtonWidget.builder(
            Text.literal("✗ Cancel").formatted(Formatting.GRAY),
            button -> close())
            .dimensions(centerX + 90, height - 55, 80, 35)
            .build();
        this.addDrawableChild(cancelBtn);
        
        // Preview text
        TextWidget previewText = new TextWidget(centerX - 150, height - 95, 
            Text.literal("Preview: " + getPreviewText()).formatted(Formatting.ITALIC, Formatting.GRAY), 
            this.textRenderer);
        previewText.setWidth(300);
        this.addDrawableChild(previewText);
        
        // Highlight current selection
        updateSelectionHighlight();
    }
    
    private void updateSelectionHighlight() {
        // Reset all buttons
        setButtonHighlight(vanillaBtn, false);
        setButtonHighlight(statusBarBtn, false);
        setButtonHighlight(headBtn, false);
        setButtonHighlight(disableBtn, false);
        
        // Highlight selected
        if (selectedDesign == HeartIndicatorRenderer.DesignType.VANILLA) {
            setButtonHighlight(vanillaBtn, true);
        } else if (selectedDesign == HeartIndicatorRenderer.DesignType.STATUS_BAR) {
            setButtonHighlight(statusBarBtn, true);
        } else if (selectedDesign == HeartIndicatorRenderer.DesignType.PLAYER_HEAD) {
            setButtonHighlight(headBtn, true);
        } else if (selectedDesign == HeartIndicatorRenderer.DesignType.DISABLED) {
            setButtonHighlight(disableBtn, true);
        }
    }
    
    private void setButtonHighlight(ButtonWidget button, boolean highlight) {
        if (button == null) return;
        
        String originalText = button.getMessage().getString();
        // Clean the text from any existing highlights
        originalText = originalText.replace("▶ ", "").replace(" ◀", "");
        
        if (highlight) {
            button.setMessage(Text.literal("▶ " + originalText + " ◀").formatted(Formatting.GREEN));
        } else {
            button.setMessage(Text.literal(originalText));
        }
    }
    
    private void updatePreview() {
        // Find and update preview text
        for (var child : this.children()) {
            if (child instanceof TextWidget textWidget && 
                textWidget.getMessage().getString().contains("Preview:")) {
                textWidget.setMessage(Text.literal("Preview: " + getPreviewText()).formatted(Formatting.ITALIC, Formatting.GRAY));
                break;
            }
        }
    }
    
    private String getPreviewText() {
        switch (selectedDesign) {
            case VANILLA:
                return "❤️❤️❤️❤️❤️ (10 hearts)";
            case STATUS_BAR:
                return "[████████░░] 80%";
            case PLAYER_HEAD:
                return "👤 20/20 ❤ | ⚔️ HTK: 3 | ⚠️ DEATH ZONE";
            case DISABLED:
                return "Indicator disabled";
            default:
                return "Select a design";
        }
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        
        // Gradient background
        for (int i = 0; i < height; i++) {
            int alpha = (int)(100 - (i / (float)height) * 80);
            context.fill(0, i, width, i + 1, 0x88000000 | (alpha << 24));
        }
        
        // Border
        context.drawBorder(5, 5, width - 10, height - 10, 0xFFAA00);
        
        // Draw description
        context.drawText(textRenderer, "Select your preferred heart indicator style:", 
                        width / 2 - 150, height / 3 - 25, 0xCCCCCC, false);
        
        super.render(context, mouseX, mouseY, delta);
    }
    
    @Override
    public void close() {
        if (client != null) {
            client.setScreen(parent);
        }
    }
}
