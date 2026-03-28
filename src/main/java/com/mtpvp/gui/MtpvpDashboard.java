package com.mtpvp.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class MtpvpDashboard extends Screen {
    private final Screen parent;
    
    // Global Settings
    public static boolean heartEnabled = true;
    public static int styleIndex = 0; // 0: Bar, 1: Hearts, 2: Head/Hits
    private final String[] styles = {"Status Bar", "Classic Hearts", "Player Head + Hits"};

    public MtpvpDashboard(Screen parent) {
        super(Text.literal("Mtpvp Dashboard"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Toggle Indicator ON/OFF
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Indicator: " + (heartEnabled ? "ON" : "OFF")), 
            (button) -> {
                heartEnabled = !heartEnabled;
                button.setMessage(Text.literal("Indicator: " + (heartEnabled ? "ON" : "OFF")));
            }
        ).dimensions(centerX - 75, centerY - 40, 150, 20).build());

        // Cycle Style Button
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Style: " + styles[styleIndex]), 
            (button) -> {
                styleIndex = (styleIndex + 1) % styles.length;
                button.setMessage(Text.literal("Style: " + styles[styleIndex]));
            }
        ).dimensions(centerX - 75, centerY - 10, 150, 20).build());

        // Done Button
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Save & Exit"), (button) -> {
            this.client.setScreen(this.parent);
        }).dimensions(centerX - 75, centerY + 50, 150, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderInGameBackground(context);
        
        // Modern Dashboard Box (Lunar/Feather Style)
        int x1 = width / 2 - 120, y1 = height / 2 - 80;
        int x2 = width / 2 + 120, y2 = height / 2 + 90;
        
        context.fill(x1, y1, x2, y2, 0xDD000000); // Black Glass
        context.drawBorder(x1, y1, 240, 170, 0xFF00AAFF); // Cyan Border
        context.drawCenteredTextWithShadow(this.textRenderer, "MTPVP SETTINGS", width / 2, y1 + 10, 0xFFFFFF);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() { this.client.setScreen(parent); }
}
