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
    
    public HeartIndicatorOptionsScreen(Screen parent) {
        super(Text.literal("Heart Indicator Options"));
        this.parent = parent;
    }
    
    @Override
    protected void init() {
        super.init();
        
        int centerX = width / 2;
        int startY = height / 3;
        int buttonWidth = 200;
        int buttonHeight = 30;
        
        // Title
        TextWidget titleText = new TextWidget(centerX - 100, 30, 
            Text.literal("Choose Heart Indicator Style").formatted(Formatting.BOLD, Formatting.GOLD), this.textRenderer);
        titleText.setWidth(200);
        this.addDrawableChild(titleText);
        
        // Design 1: Vanilla Hearts
        ButtonWidget vanillaBtn = ButtonWidget.builder(
            Text.literal("❤️ Design 1: Vanilla Hearts").formatted(Formatting.RED), 
            button -> selectDesign(HeartIndicatorRenderer.DesignType.VANILLA))
            .dimensions(centerX - 100, startY, buttonWidth, buttonHeight)
            .build();
        this.addDrawableChild(vanillaBtn);
        
        // Design 2: Status Bar
        ButtonWidget statusBarBtn = ButtonWidget.builder(
            Text.literal("📊 Design 2: Status Bar [====]").formatted(Formatting.GREEN), 
            button -> selectDesign(HeartIndicatorRenderer.DesignType.STATUS_BAR))
            .dimensions(centerX - 100, startY + 40, buttonWidth, buttonHeight)
            .build();
        this.addDrawableChild(statusBarBtn);
        
        // Design 3: Player Head with Hits to Kill
        ButtonWidget headBtn = ButtonWidget.builder(
            Text.literal("👤 Design 3: Player Head + HTK").formatted(Formatting.BLUE), 
            button -> selectDesign(HeartIndicatorRenderer.DesignType.PLAYER_HEAD))
            .dimensions(centerX - 100, startY + 80, buttonWidth, buttonHeight)
            .build();
        this.addDrawableChild(headBtn);
        
        // Disable Indicator Button
        ButtonWidget disableBtn = ButtonWidget.builder(
            Text.literal("❌ Disable Heart Indicator").formatted(Formatting.DARK_RED), 
            button -> selectDesign(HeartIndicatorRenderer.DesignType.DISABLED))
            .dimensions(centerX - 100, startY + 130, buttonWidth, buttonHeight)
            .build();
        this.addDrawableChild(disableBtn);
        
        // Back button
        ButtonWidget backBtn = ButtonWidget.builder(Text.literal("← Back"), button -> close())
            .dimensions(centerX - 50, height - 40, 100, 20)
            .build();
        this.addDrawableChild(backBtn);
        
        // Info text
        TextWidget infoText = new TextWidget(centerX - 150, height - 80, 
            Text.literal("Design 3 shows HTK based on your current weapon").formatted(Formatting.GRAY, Formatting.ITALIC), 
            this.textRenderer);
        infoText.setWidth(300);
        this.addDrawableChild(infoText);
    }
    
    private void selectDesign(HeartIndicatorRenderer.DesignType design) {
        HeartIndicatorRenderer.setDesignType(design);
        if (client != null && client.player != null) {
            String designName = design == HeartIndicatorRenderer.DesignType.VANILLA ? "Vanilla Hearts" :
                               design == HeartIndicatorRenderer.DesignType.STATUS_BAR ? "Status Bar" :
                               design == HeartIndicatorRenderer.DesignType.PLAYER_HEAD ? "Player Head + HTK" : "Disabled";
            client.player.sendMessage(Text.literal("§a[§6MTPVP§a] §fHeart indicator set to: §e" + designName), false);
        }
        close();
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        context.fill(10, 10, width - 10, height - 10, 0xCC000000);
        context.drawBorder(10, 10, width - 20, height - 20, 0xFFAA00);
        super.render(context, mouseX, mouseY, delta);
    }
    
    @Override
    public void close() {
        if (client != null) {
            client.setScreen(parent);
        }
    }
}
