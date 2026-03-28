package com.mtpvp.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class MtpvpDashboard extends Screen {
    private final Screen parent;
    
    // Global Settings (Mixins use these)
    public static boolean heartEnabled = true;
    public static int styleIndex = 0;
    
    // UI Constants
    private final String[] styles = {"Hearts Style", "Hits Style", "Name + HP"};
    private final int primaryColor = 0xFF00AAFF; // Neon Blue
    private final int bgColor = 0xFF121212;      // Deep Black

    public MtpvpDashboard(Screen parent) {
        super(Text.literal("MTPVP DASHBOARD"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = width / 2;
        int centerY = height / 2;
        
        // Right Side Content Area Buttons
        int buttonX = centerX - 40; 
        int buttonY = centerY - 50;

        // 1. Toggle Button
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Indicator: " + (heartEnabled ? "ON" : "OFF")), 
            (button) -> {
                heartEnabled = !heartEnabled;
                button.setMessage(Text.literal("Indicator: " + (heartEnabled ? "ON" : "OFF")));
            }
        ).dimensions(buttonX, buttonY, 180, 20).build());

        // 2. Style Cycle Button
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Current Style: " + styles[styleIndex]), 
            (button) -> {
                styleIndex = (styleIndex + 1) % styles.length;
                button.setMessage(Text.literal("Current Style: " + styles[styleIndex]));
            }
        ).dimensions(buttonX, buttonY + 30, 180, 20).build());

        // 3. Save & Exit
        this.addDrawableChild(ButtonWidget.builder(Text.literal("SAVE SETTINGS"), (button) -> {
            this.client.setScreen(this.parent);
        }).dimensions(buttonX, buttonY + 80, 180, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Darkened Background (Blur effect jaisa look)
        context.fill(0, 0, width, height, 0xAA000000);

        // Dashboard Dimensions
        int x1 = width / 2 - 160, y1 = height / 2 - 110;
        int x2 = width / 2 + 160, y2 = height / 2 + 100;

        // --- MAIN BOX DESIGN ---
        context.fill(x1, y1, x2, y2, bgColor);           // Main Body
        context.fill(x1, y1, x1 + 90, y2, 0xFF1A1A1A);  // Sidebar Background
        context.fill(x1, y1, x2, y1 + 2, primaryColor); // Top Accent Bar

        // --- SIDEBAR TEXT ---
        context.drawText(this.textRenderer, "MTPVP", x1 + 20, y1 + 15, primaryColor, true);
        
        // Sidebar Category Items (Non-clickable just for UI)
        int sideY = y1 + 50;
        context.drawText(this.textRenderer, "> Combat", x1 + 15, sideY, 0xFFFFFFFF, false);
        context.drawText(this.textRenderer, "  Visuals", x1 + 15, sideY + 20, 0x777777, false);
        context.drawText(this.textRenderer, "  Misc", x1 + 15, sideY + 40, 0x777777, false);

        // --- HEADER ---
        context.drawText(this.textRenderer, "PvP Settings / Health Indicator", x1 + 100, y1 + 15, 0xFFFFFF, false);
        context.fill(x1 + 100, y1 + 30, x2 - 10, y1 + 31, 0xFF333333); // Divider

        // --- HINT TEXT ---
        context.drawText(this.textRenderer, "Select your preferred layout", x1 + 100, y2 - 20, 0x555555, false);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }
}
