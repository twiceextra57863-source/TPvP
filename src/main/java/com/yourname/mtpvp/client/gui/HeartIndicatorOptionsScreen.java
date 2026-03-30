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
        
        // Title with gradient effect
        TextWidget titleText = new TextWidget(centerX - 120, 25, 
            Text.literal("❤️ HEART INDICATOR SETTINGS ❤️").formatted(Formatting.BOLD, Formatting.GOLD), 
            this.textRenderer);
        titleText.setWidth(240);
        this.addDrawableChild(titleText);
        
        // Design 1: Vanilla Hearts
        ButtonWidget vanillaBtn = createDesignButton(
            centerX - 110, startY, buttonWidth, buttonHeight,
            "❤️ Vanilla Hearts",
            "Classic Minecraft heart display",
            0xFF5555,
            HeartIndicatorRenderer.DesignType.VANILLA
        );
        this.addDrawableChild(vanillaBtn);
        
        // Design 2: Status Bar
        ButtonWidget statusBarBtn = createDesignButton(
            centerX - 110, startY + 55, buttonWidth, buttonHeight,
            "📊 Status Bar",
            "Health bar with percentage [====]",
            0x55FF55,
            HeartIndicatorRenderer.DesignType.STATUS_BAR
        );
        this.addDrawableChild(statusBarBtn);
        
        // Design 3: Player Head + HTK
        ButtonWidget headBtn = createDesignButton(
            centerX - 110, startY + 110, buttonWidth, buttonHeight,
            "👤 Player Head + HTK",
            "Shows hits needed to kill with your weapon",
            0x55AAFF,
            HeartIndicatorRenderer.DesignType.PLAYER_HEAD
        );
        this.addDrawableChild(headBtn);
        
        // Disable Indicator Button
        ButtonWidget disableBtn = ButtonWidget.builder(
            Text.literal("❌ DISABLE INDICATOR").formatted(Formatting.BOLD, Formatting.DARK_RED),
            button -> {
                selectedDesign = HeartIndicatorRenderer.DesignType.DISABLED;
                updateSelectionHighlight(vanillaBtn, statusBarBtn, headBtn, disableBtn);
            })
            .dimensions(centerX - 110, startY + 175, buttonWidth, buttonHeight)
            .build();
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
        updateSelectionHighlight(vanillaBtn, statusBarBtn, headBtn, disableBtn);
    }
    
    private ButtonWidget createDesignButton(int x, int y, int width, int height, 
                                            String title, String description, 
                                            int color, HeartIndicatorRenderer.DesignType design) {
        return ButtonWidget.builder(
            Text.literal(title).formatted(Formatting.BOLD),
            button -> {
                selectedDesign = design;
                updatePreview(design);
            })
            .dimensions(x, y, width, height)
            .build();
    }
    
    private void updateSelectionHighlight(ButtonWidget vanilla, ButtonWidget status, 
                                          ButtonWidget head, ButtonWidget disable) {
        // Reset all buttons
        setButtonHighlight(vanilla, false);
        setButtonHighlight(status, false);
        setButtonHighlight(head, false);
        setButtonHighlight(disable, false);
        
        // Highlight selected
        if (selectedDesign == HeartIndicatorRenderer.DesignType.VANILLA) {
            setButtonHighlight(vanilla, true);
        } else if (selectedDesign == HeartIndicatorRenderer.DesignType.STATUS_BAR) {
            setButtonHighlight(status, true);
        } else if (selectedDesign == HeartIndicatorRenderer.DesignType.PLAYER_HEAD) {
            setButtonHighlight(head, true);
        } else if (selectedDesign == HeartIndicatorRenderer.DesignType.DISABLED) {
            setButtonHighlight(disable, true);
        }
    }
    
    private void setButtonHighlight(ButtonWidget button, boolean highlight) {
        if (highlight) {
            button.setMessage(Text.literal("▶ " + button.getMessage().getString() + " ◀").formatted(Formatting.GREEN));
        } else {
            String text = button.getMessage().getString().replace("▶ ", "").replace(" ◀", "");
            button.setMessage(Text.literal(text));
        }
    }
    
    private void updatePreview(HeartIndicatorRenderer.DesignType design) {
        // Update preview text
        TextWidget preview = (TextWidget) this.children().stream()
            .filter(child -> child instanceof TextWidget && 
                   ((TextWidget) child).getMessage().getString().contains("Preview:"))
            .findFirst().orElse(null);
        if (preview != null) {
            preview.setMessage(Text.literal("Preview: " + getPreviewText()).formatted(Formatting.ITALIC, Formatting.GRAY));
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
