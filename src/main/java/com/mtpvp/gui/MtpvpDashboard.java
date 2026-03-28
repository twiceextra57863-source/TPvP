package com.mtpvp.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class MtpvpDashboard extends Screen {
    private final Screen parent;
    public static boolean heartEnabled = true;
    public static int styleIndex = 0;
    private final String[] styles = {"Hearts Style", "Hits Style", "HP Bar Style"};

    public MtpvpDashboard(Screen parent) {
        super(Text.literal("MTPVP DASHBOARD"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int contentX = width / 2 - 50;
        int contentY = height / 2 - 60;

        // Button 1: Toggle
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Status: " + (heartEnabled ? "ENABLED" : "DISABLED")), 
            (b) -> {
                heartEnabled = !heartEnabled;
                b.setMessage(Text.literal("Status: " + (heartEnabled ? "ENABLED" : "DISABLED")));
            }
        ).dimensions(contentX, contentY + 20, 140, 20).build());

        // Button 2: Style
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Mode: " + styles[styleIndex]), 
            (b) -> {
                styleIndex = (styleIndex + 1) % styles.length;
                b.setMessage(Text.literal("Mode: " + styles[styleIndex]));
            }
        ).dimensions(contentX, contentY + 50, 140, 20).build());

        // Button 3: Exit
        this.addDrawableChild(ButtonWidget.builder(Text.literal("DONE"), (b) -> this.client.setScreen(parent))
            .dimensions(contentX, contentY + 90, 140, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Darkened background
        context.fill(0, 0, width, height, 0xAA000000);

        int x1 = width / 2 - 160, y1 = height / 2 - 100;
        int x2 = width / 2 + 160, y2 = height / 2 + 100;

        // Main Web-Panel Design
        context.fill(x1, y1, x2, y2, 0xFF121212); // Deep Black BG
        context.fill(x1, y1, x1 + 90, y2, 0xFF1E1E1E); // Side Sidebar
        context.fill(x1, y1, x2, y1 + 2, 0xFF00AAFF); // Neon Blue Top border

        // Sidebar Text
        context.drawText(this.textRenderer, "MTPVP", x1 + 20, y1 + 15, 0x00AAFF, true);
        context.drawText(this.textRenderer, "v1.0", x1 + 35, y2 - 15, 0x555555, false);

        // Header Title
        context.drawText(this.textRenderer, "CONTROL PANEL / COMBAT", x1 + 105, y1 + 15, 0xFFFFFF, false);
        context.fill(x1 + 100, y1 + 30, x2 - 10, y1 + 31, 0xFF333333); // Header line

        super.render(context, mouseX, mouseY, delta);
    }
}
