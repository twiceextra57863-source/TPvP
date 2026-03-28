package com.mtpvp.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class MtpvpDashboard extends Screen {
    private final Screen parent;
    public static boolean heartEnabled = true; // Toggle state

    public MtpvpDashboard(Screen parent) {
        super(Text.literal("Mtpvp Dashboard"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Toggle Button for Heart Indicator
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Hearts: " + (heartEnabled ? "ON" : "OFF")), 
            (button) -> {
                heartEnabled = !heartEnabled;
                button.setMessage(Text.literal("Hearts: " + (heartEnabled ? "ON" : "OFF")));
            }
        ).dimensions(centerX - 75, centerY - 20, 150, 20).build());

        // Back Button
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Done"), (button) -> {
            this.client.setScreen(this.parent);
        }).dimensions(centerX - 75, centerY + 50, 150, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Blur jaisa background effect
        this.renderInGameBackground(context);

        // Main Dashboard Box (Code-based design)
        int x1 = width / 2 - 160;
        int y1 = height / 2 - 100;
        int x2 = width / 2 + 160;
        int y2 = height / 2 + 100;

        // Draw Shadows and Glow
        context.fill(x1, y1, x2, y2, 0xCC000000); // Main Dark Panel
        context.drawBorder(x1, y1, 320, 200, 0xFF5555FF); // Blue Accent Border

        // Header
        context.fill(x1, y1, x2, y1 + 25, 0xFF111111);
        context.drawTextWithShadow(this.textRenderer, "MTPVP CLIENT | v1.0", x1 + 10, y1 + 8, 0x00AAFF);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }
}
