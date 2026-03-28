package com.mtpvp.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class MtpvpDashboard extends Screen {
    private final Screen parent;
    public static boolean heartEnabled = true;
    public static int styleIndex = 0;
    private final String[] styles = {"Hearts", "Hits", "Full Info"};

    public MtpvpDashboard(Screen parent) {
        super(Text.literal("MTPVP"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int x = width / 2 - 40;
        int y = height / 2 - 50;

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Indicators: " + (heartEnabled ? "ON" : "OFF")), (b) -> {
            heartEnabled = !heartEnabled;
            b.setMessage(Text.literal("Indicators: " + (heartEnabled ? "ON" : "OFF")));
        }).dimensions(x, y, 160, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Mode: " + styles[styleIndex]), (b) -> {
            styleIndex = (styleIndex + 1) % styles.length;
            b.setMessage(Text.literal("Mode: " + styles[styleIndex]));
        }).dimensions(x, y + 25, 160, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("DONE"), (b) -> this.client.setScreen(parent))
            .dimensions(x, y + 60, 160, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0x99000000);
        int x1 = width / 2 - 150, y1 = height / 2 - 100, x2 = width / 2 + 150, y2 = height / 2 + 100;
        
        context.fill(x1, y1, x2, y2, 0xFF111111); // Dark Web Style
        context.fill(x1, y1, x2, y1 + 2, 0xFF00AAFF); // Glow top
        
        context.drawText(this.textRenderer, "MTPVP | SETTINGS", x1 + 10, y1 + 10, 0x00AAFF, false);
        super.render(context, mouseX, mouseY, delta);
    }
}
