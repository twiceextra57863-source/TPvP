package com.yourname.mtpvp.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class MtpvpDashboard extends Screen {
    private final Screen parent;

    public MtpvpDashboard(Screen parent) {
        super(Text.literal("Mtpvp Dashboard"));
        this.parent = parent;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        
        // 1. Draw Sidebar Background (Left)
        context.fill(10, 10, 100, this.height - 10, 0x80000000); // Dark semi-transparent
        
        // 2. Draw Main Settings Area (Right)
        context.fill(110, 10, this.width - 10, this.height - 10, 0x50000000);

        // Title
        context.drawCenteredTextWithShadow(this.textRenderer, "MTPVP CLIENT SETTINGS", this.width / 2 + 50, 20, 0xFFFFFF);
        
        // Categories (Example)
        context.drawTextWithShadow(this.textRenderer, "> Indicators", 20, 40, 0x00FF00);
        context.drawTextWithShadow(this.textRenderer, "  Movement", 20, 60, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer, "  Combat", 20, 80, 0xFFFFFF);

        super.render(context, mouseX, mouseY, delta);
    }
    
    @Override
    public void close() {
        this.client.setScreen(parent);
    }
}
