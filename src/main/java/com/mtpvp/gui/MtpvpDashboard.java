package com.mtpvp.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class MtpvpDashboard extends Screen {
    private final Screen parent;

    // --- ALL FEATURES SAVED HERE ---
    public static boolean headEnabled = true;
    public static boolean showDistance = true;
    public static boolean showAdvancedInfo = true;
    public static int styleIndex = 1; // 0=Hearts, 1=Bar, 2=Pro

    public MtpvpDashboard(Screen parent) {
        super(Text.literal("MTPVP Dashboard"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int x = this.width / 2 - 100;
        int y = this.height / 4;

        // 1. Toggle Indicator ON/OFF
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Head Display: " + (headEnabled ? "§aON" : "§cOFF")), button -> {
            headEnabled = !headEnabled;
            button.setMessage(Text.literal("Head Display: " + (headEnabled ? "§aON" : "§cOFF")));
        }).dimensions(x, y, 200, 20).build());

        // 2. Cycle Styles
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Style: " + getStyleName()), button -> {
            styleIndex = (styleIndex + 1) % 3;
            button.setMessage(Text.literal("Style: " + getStyleName()));
        }).dimensions(x, y + 25, 200, 20).build());

        // 3. Distance Toggle
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Distance: " + (showDistance ? "§aON" : "§cOFF")), button -> {
            showDistance = !showDistance;
            button.setMessage(Text.literal("Distance: " + (showDistance ? "§aON" : "§cOFF")));
        }).dimensions(x, y + 50, 200, 20).build());

        // 4. Advanced Info (Armor/Item) Toggle
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Armor & Items: " + (showAdvancedInfo ? "§aON" : "§cOFF")), button -> {
            showAdvancedInfo = !showAdvancedInfo;
            button.setMessage(Text.literal("Armor & Items: " + (showAdvancedInfo ? "§aON" : "§cOFF")));
        }).dimensions(x, y + 75, 200, 20).build());

        // 5. Back Button
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Done"), button -> {
            this.client.setScreen(this.parent);
        }).dimensions(x, y + 110, 200, 20).build());
    }

    private String getStyleName() {
        return switch (styleIndex) {
            case 0 -> "10 Hearts";
            case 1 -> "Smooth Bar";
            case 2 -> "Pro Face";
            default -> "Unknown";
        };
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, "§b§lMTPVP CLIENT SETTINGS", this.width / 2, 20, 0xFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }
}
