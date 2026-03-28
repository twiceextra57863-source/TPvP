package com.mtpvp.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class MtpvpDashboard extends Screen {
    private final Screen parent;
    
    // Toggles
    public static boolean headEnabled = true;
    public static boolean hudEnabled = true;
    
    // Styles
    public static int styleIndex = 0; // 0: Hearts, 1: Hits, 2: HP Only
    private final String[] styles = {"❤ Hearts", "⚔ Hits", "📊 HP Only"};

    public MtpvpDashboard(Screen parent) {
        super(Text.literal("MTPVP DASHBOARD"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int x = width / 2 - 75;
        int y = height / 2 - 60;

        // --- HEAD INDICATOR CONTROLS ---
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Head Display: " + (headEnabled ? "ON" : "OFF")), (b) -> {
            headEnabled = !headEnabled;
            b.setMessage(Text.literal("Head Display: " + (headEnabled ? "ON" : "OFF")));
        }).dimensions(x, y, 150, 20).build());

        // --- TARGET HUD CONTROLS ---
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Target HUD: " + (hudEnabled ? "ON" : "OFF")), (b) -> {
            hudEnabled = !hudEnabled;
            b.setMessage(Text.literal("Target HUD: " + (hudEnabled ? "ON" : "OFF")));
        }).dimensions(x, y + 25, 150, 20).build());

        // --- GLOBAL STYLE SELECTOR ---
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Style: " + styles[styleIndex]), (b) -> {
            styleIndex = (styleIndex + 1) % styles.length;
            b.setMessage(Text.literal("Style: " + styles[styleIndex]));
        }).dimensions(x, y + 50, 150, 20).build());

        // CLOSE
        this.addDrawableChild(ButtonWidget.builder(Text.literal("SAVE"), (b) -> this.client.setScreen(parent))
            .dimensions(x, y + 85, 150, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0xAA000000);
        int x1 = width / 2 - 160, y1 = height / 2 - 110, x2 = width / 2 + 160, y2 = height / 2 + 100;
        context.fill(x1, y1, x2, y2, 0xFF121212);
        context.fill(x1, y1, x1 + 90, y2, 0xFF1A1A1A);
        context.fill(x1, y1, x2, y1 + 2, 0xFF00AAFF);
        context.drawText(this.textRenderer, "MTPVP SETTINGS", x1 + 105, y1 + 15, 0xFFFFFF, false);
        super.render(context, mouseX, mouseY, delta);
    }
}
