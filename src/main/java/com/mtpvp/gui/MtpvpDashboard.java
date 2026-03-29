package com.mtpvp.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class MtpvpDashboard extends Screen {
    private final Screen parent;
    public static boolean headEnabled = true, showDistance = true, showAdvancedInfo = true, autoTargetLowHp = false;
    public static int styleIndex = 1, targetStyle = 0;
    public static String targetPlayerName = "";

    public MtpvpDashboard(Screen parent) { super(Text.literal("MTPVP Dashboard")); this.parent = parent; }

    @Override
    protected void init() {
        int x = this.width / 2 - 100, y = this.height / 6;
        addDrawableChild(ButtonWidget.builder(Text.literal("Head Display: " + (headEnabled ? "§aON" : "§cOFF")), b -> headEnabled = !headEnabled).dimensions(x, y, 200, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Style: " + styleIndex), b -> styleIndex = (styleIndex + 1) % 3).dimensions(x, y + 25, 200, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Target Style: " + targetStyle), b -> targetStyle = (targetStyle + 1) % 5).dimensions(x, y + 50, 200, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("§fClose"), b -> client.setScreen(parent)).dimensions(x, y + 100, 200, 20).build());
    }

    @Override
    public void render(DrawContext c, int mx, int my, float d) { renderBackground(c, mx, my, d); super.render(c, mx, my, d); }
    @Override public boolean shouldPause() { return false; }
}
