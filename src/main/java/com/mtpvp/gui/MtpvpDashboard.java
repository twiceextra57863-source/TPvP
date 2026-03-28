package com.mtpvp.gui;

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
        // Dark Background Overlay
        this.renderBackground(context, mouseX, mouseY, delta);
        
        // Main Panel (Lawn Chair/Lunar Style)
        int panelX = width / 2 - 150;
        int panelY = height / 2 - 100;
        context.fill(panelX, panelY, panelX + 300, panelY + 200, 0xAA000000); // Black Transparent
        context.drawBorder(panelX, panelY, 300, 200, 0xFF00AAFF); // Cyan Border

        context.drawCenteredTextWithShadow(this.textRenderer, "MTPVP SETTINGS", width / 2, panelY + 10, 0xFFFFFF);
        
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        client.setScreen(parent);
    }
}
