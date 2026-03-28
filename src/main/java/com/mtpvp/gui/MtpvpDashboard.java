package com.mtpvp.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class MtpvpDashboard extends Screen {
    private final Screen parent;
    public static boolean heartEnabled = true;
    public static int styleIndex = 0;
    private final String[] styles = {"Classic Hearts", "Hits Needed", "Full Info Tag"};

    public MtpvpDashboard(Screen parent) {
        super(Text.literal("MTPVP DASHBOARD"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = width / 2;
        int centerY = height / 2;

        // Main Web-Style Buttons
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Indicator: " + (heartEnabled ? "ON" : "OFF")), 
            (b) -> {
                heartEnabled = !heartEnabled;
                b.setMessage(Text.literal("Indicator: " + (heartEnabled ? "ON" : "OFF")));
            }
        ).dimensions(centerX - 50, centerY - 40, 160, 20).build());

        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Style: " + styles[styleIndex]), 
            (b) -> {
                styleIndex = (styleIndex + 1) % styles.length;
                b.setMessage(Text.literal("Style: " + styles[styleIndex]));
            }
        ).dimensions(centerX - 50, centerY - 15, 160, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("SAVE & EXIT"), (b) -> this.client.setScreen(parent))
            .dimensions(centerX - 50, centerY + 20, 160, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0xAA000000); // Overlay

        int x1 = width / 2 - 160, y1 = height / 2 - 100;
        int x2 = width / 2 + 160, y2 = height / 2 + 100;

        // Container
        context.fill(x1, y1, x2, y2, 0xFF121212); 
        context.fill(x1, y1, x1 + 90, y2, 0xFF1A1A1A); // Sidebar
        context.fill(x1, y1, x2, y1 + 2, 0xFF00AAFF); // Neon Top

        context.drawText(this.textRenderer, "MTPVP", x1 + 20, y1 + 15, 0xFF00AAFF, true);
        context.drawText(this.textRenderer, "PvP Visuals", x1 + 105, y1 + 15, 0xFFFFFF, false);
        context.fill(x1 + 100, y1 + 30, x2 - 10, y1 + 31, 0xFF333333);

        super.render(context, mouseX, mouseY, delta);
    }
}
