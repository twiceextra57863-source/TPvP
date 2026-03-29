package com.mtpvp.gui;

import com.mtpvp.config.MtpvpConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class MtpvpDashboard extends Screen {
    private final Screen parent;

    public MtpvpDashboard(Screen parent) {
        super(Text.literal("Mtpvp Dashboard"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        // Mode Selection Button
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Switch Health Mode"), (button) -> {
            MtpvpConfig.healthMode = (MtpvpConfig.healthMode + 1) % 3;
        }).dimensions(120, 50, 150, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        
        // Sidebar
        context.fill(0, 0, 100, this.height, 0xAA000000);
        context.drawTextWithShadow(this.textRenderer, "CATEGORIES", 10, 20, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer, "> Indicators", 10, 50, 0x00FF00);
        
        // Right Panel Info
        String modeName = switch (MtpvpConfig.healthMode) {
            case 0 -> "Progress Bar";
            case 1 -> "Vanilla Hearts";
            case 2 -> "Head + Hits To Kill";
            default -> "Unknown";
        };
        context.drawTextWithShadow(this.textRenderer, "Current Mode: " + modeName, 120, 80, 0xFFFFFF);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }
}
