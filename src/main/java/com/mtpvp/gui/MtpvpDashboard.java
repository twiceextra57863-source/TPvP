package com.mtpvp.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class MtpvpDashboard extends Screen {
    private final Screen parent;

    // --- Static Settings (Used by Mixins) ---
    public static boolean headEnabled = true; // World Indicators (Hearts/Bar)
    public static boolean hudEnabled = true;  // Top Target HUD
    public static int styleIndex = 0;         // 0: 10 Hearts, 1: Level Bar, 2: Pro Face + Hits

    private final String[] styles = {
        "❤ 10 Hearts Style", 
        "📊 Level Bar Style", 
        "⚔ Pro Skin + Hits"
    };

    public MtpvpDashboard(Screen parent) {
        super(Text.literal("MTPVP DASHBOARD"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = width / 2;
        int centerY = height / 2;
        int btnWidth = 160;
        int btnHeight = 20;
        int startY = centerY - 50;

        // 1. HEAD INDICATOR TOGGLE
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Head Display: " + (headEnabled ? "§aENABLED" : "§cDISABLED")), 
            (button) -> {
                headEnabled = !headEnabled;
                button.setMessage(Text.literal("Head Display: " + (headEnabled ? "§aENABLED" : "§cDISABLED")));
            }
        ).dimensions(centerX - 40, startY, btnWidth, btnHeight).build());

        // 2. TARGET HUD TOGGLE
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Target HUD: " + (hudEnabled ? "§aENABLED" : "§cDISABLED")), 
            (button) -> {
                hudEnabled = !hudEnabled;
                button.setMessage(Text.literal("Target HUD: " + (hudEnabled ? "§aENABLED" : "§cDISABLED")));
            }
        ).dimensions(centerX - 40, startY + 25, btnWidth, btnHeight).build());

        // 3. STYLE SELECTOR (The one you asked for)
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Style: " + styles[styleIndex]), 
            (button) -> {
                styleIndex = (styleIndex + 1) % styles.length;
                button.setMessage(Text.literal("Style: " + styles[styleIndex]));
            }
        ).dimensions(centerX - 40, startY + 50, btnWidth, btnHeight).build());

        // 4. SAVE & CLOSE
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("§bSAVE SETTINGS"), 
            (button) -> this.client.setScreen(this.parent)
        ).dimensions(centerX - 40, startY + 85, btnWidth, btnHeight).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Dark background overlay
        context.fill(0, 0, width, height, 0xAA000000);

        // Dashboard container coordinates
        int x1 = width / 2 - 160, y1 = height / 2 - 110;
        int x2 = width / 2 + 160, y2 = height / 2 + 105;

        // --- UI BOX DESIGN ---
        context.fill(x1, y1, x2, y2, 0xFF121212);           // Main BG
        context.fill(x1, y1, x1 + 90, y2, 0xFF1A1A1A);      // Sidebar BG
        context.fill(x1, y1, x2, y1 + 2, 0xFF00AAFF);      // Neon Top Border Glow

        // --- SIDEBAR TEXT ---
        context.drawText(this.textRenderer, "§b§lMTPVP", x1 + 20, y1 + 15, 0xFFFFFF, true);
        
        int sideY = y1 + 50;
        context.drawText(this.textRenderer, "> Combat", x1 + 15, sideY, 0xFFFFFF, false);
        context.drawText(this.textRenderer, "  Visuals", x1 + 15, sideY + 20, 0x777777, false);
        context.drawText(this.textRenderer, "  Settings", x1 + 15, sideY + 40, 0x777777, false);

        // --- HEADER & DIVIDER ---
        context.drawText(this.textRenderer, "Indicator & HUD Configuration", x1 + 100, y1 + 15, 0xFFFFFF, false);
        context.fill(x1 + 100, y1 + 30, x2 - 10, y1 + 31, 0xFF333333); // Divider line

        // --- FOOTER HINT ---
        String hint = "Styles will update in real-time above players.";
        context.drawText(this.textRenderer, hint, x1 + 100, y2 - 15, 0x555555, false);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false; // Game shouldn't pause in multiplayer
    }
}
