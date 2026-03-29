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
        int startX = 120;
        
        // Mode 0: Progress Bar Button
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Style: Progress Bar"), (button) -> {
            MtpvpConfig.healthMode = 0;
        }).dimensions(startX, 50, 160, 20).build());

        // Mode 1: Vanilla Hearts Button
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Style: Vanilla Hearts"), (button) -> {
            MtpvpConfig.healthMode = 1;
        }).dimensions(startX, 80, 160, 20).build());

        // Mode 2: Head + Hits Button
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Style: Head + Hits"), (button) -> {
            MtpvpConfig.healthMode = 2;
        }).dimensions(startX, 110, 160, 20).build());

        // Save & Close Button
        this.addDrawableChild(ButtonWidget.builder(Text.literal("💾 SAVE & APPLY"), (button) -> {
            MtpvpConfig.save();
            this.close();
        }).dimensions(this.width - 110, this.height - 30, 100, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        
        // Dynamic Sidebar Rendering
        context.fill(0, 0, 100, this.height, 0xCC000000); // Black Sidebar
        context.fill(100, 0, 102, this.height, 0xFF00AAFF); // Cyan Accent Line
        
        context.drawCenteredTextWithShadow(this.textRenderer, "MTPVP CLIENT", 50, 20, 0x00FF00);
        context.drawTextWithShadow(this.textRenderer, "● Indicators", 10, 55, 0x00FF00);
        context.drawTextWithShadow(this.textRenderer, "  Combat", 10, 75, 0xAAAAAA);
        context.drawTextWithShadow(this.textRenderer, "  Movement", 10, 95, 0xAAAAAA);

        // Highlight Active Mode
        String current = "Current: " + (MtpvpConfig.healthMode == 0 ? "Bar" : MtpvpConfig.healthMode == 1 ? "Hearts" : "Hits");
        context.drawTextWithShadow(this.textRenderer, "Status: §a" + current, 120, 20, 0xFFFFFF);

        super.render(context, mouseX, mouseY, delta);
    }
}
