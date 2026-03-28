package com.mtpvp.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class MtpvpDashboard extends Screen {
    private final Screen parent;
    public static boolean heartEnabled = true;
    public static int styleIndex = 0;
    private final String[] styles = {"Classic Hearts", "Hits Indicator", "Tag Style"};

    public MtpvpDashboard(Screen parent) {
        super(Text.literal("Mtpvp Web Dashboard"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int xStart = width / 2 - 150;
        int yStart = height / 2 - 100;

        // Sidebar Buttons (Left Side)
        this.addDrawableChild(ButtonWidget.builder(Text.literal("General"), (b) -> {})
            .dimensions(xStart + 10, yStart + 40, 70, 20).build());
        
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Combat"), (b) -> {})
            .dimensions(xStart + 10, yStart + 65, 70, 20).build());

        // Main Toggle (Content Area - Right Side)
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Indicator: " + (heartEnabled ? "ON" : "OFF")), 
            (button) -> {
                heartEnabled = !heartEnabled;
                button.setMessage(Text.literal("Indicator: " + (heartEnabled ? "ON" : "OFF")));
            }
        ).dimensions(xStart + 100, yStart + 40, 180, 20).build());

        // Style Switcher
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Style: " + styles[styleIndex]), 
            (button) -> {
                styleIndex = (styleIndex + 1) % styles.length;
                button.setMessage(Text.literal("Style: " + styles[styleIndex]));
            }
        ).dimensions(xStart + 100, yStart + 70, 180, 20).build());

        // Exit Button
        this.addDrawableChild(ButtonWidget.builder(Text.literal("SAVE & EXIT"), (b) -> this.client.setScreen(parent))
            .dimensions(xStart + 100, yStart + 160, 180, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Blur background
        context.fill(0, 0, width, height, 0x88000000);

        int x1 = width / 2 - 160, y1 = height / 2 - 110;
        int x2 = width / 2 + 160, y2 = height / 2 + 100;

        // Main Container (Web Style)
        context.fill(x1, y1, x2, y2, 0xFF181818); // Dark Body
        context.fill(x1, y1, x1 + 90, y2, 0xFF121212); // Sidebar Background
        context.fill(x1, y1, x2, y1 + 2, 0xFF00AAFF); // Top Blue Accent Bar (Glow)

        // Sidebar Text
        context.drawText(this.textRenderer, "MTPVP", x1 + 15, y1 + 15, 0x00AAFF, true);
        
        // Header
        context.drawText(this.textRenderer, "PvP Settings > Indicators", x1 + 105, y1 + 15, 0xAAAAAA, false);

        // UI Separator Line
        context.fill(x1 + 90, y1 + 30, x2 - 10, y1 + 31, 0xFF252525);

        super.render(context, mouseX, mouseY, delta);
    }
}
