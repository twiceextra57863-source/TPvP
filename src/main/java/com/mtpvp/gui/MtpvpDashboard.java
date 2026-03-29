package com.mtpvp.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class MtpvpDashboard extends Screen {
    private final Screen parent;

    // --- ALL FEATURES SAVED HERE (PERSISTENT) ---
    public static boolean headEnabled = true;
    public static boolean showDistance = true;
    public static boolean showAdvancedInfo = true;
    public static int styleIndex = 1; // 0=Hearts, 1=Bar, 2=Pro
    
    // --- TARGET SYSTEM FEATURES ---
    public static String targetPlayerName = ""; 
    public static boolean autoTargetLowHp = false;
    public static int targetStyle = 0; // 0 to 4 Presets

    public MtpvpDashboard(Screen parent) {
        super(Text.literal("MTPVP Dashboard"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int x = this.width / 2 - 100;
        int y = this.height / 6; // Adjusted for more buttons

        // --- INDICATOR CATEGORY ---
        
        // 1. Toggle Indicator
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Head Display: " + (headEnabled ? "§aON" : "§cOFF")), button -> {
            headEnabled = !headEnabled;
            button.setMessage(Text.literal("Head Display: " + (headEnabled ? "§aON" : "§cOFF")));
        }).dimensions(x, y, 200, 20).build());

        // 2. Style Cycle
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Indicator Style: " + getStyleName()), button -> {
            styleIndex = (styleIndex + 1) % 3;
            button.setMessage(Text.literal("Indicator Style: " + getStyleName()));
        }).dimensions(x, y + 25, 200, 20).build());

        // 3. Distance Toggle
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Show Distance: " + (showDistance ? "§aON" : "§cOFF")), button -> {
            showDistance = !showDistance;
            button.setMessage(Text.literal("Show Distance: " + (showDistance ? "§aON" : "§cOFF")));
        }).dimensions(x, y + 50, 200, 20).build());

        // --- TARGET TRACKER CATEGORY (NEW) ---

        // 4. Target Style Preset Cycle
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Target Preset: " + getTargetStyleName()), button -> {
            targetStyle = (targetStyle + 1) % 5;
            button.setMessage(Text.literal("Target Preset: " + getTargetStyleName()));
        }).dimensions(x, y + 85, 200, 20).build());

        // 5. Open Target Selector (The Player List Menu)
        this.addDrawableChild(ButtonWidget.builder(Text.literal("§6§lOPEN TARGET SELECTOR"), button -> {
            this.client.setScreen(new TargetSelectorScreen());
        }).dimensions(x, y + 110, 200, 20).build());

        // 6. Auto-Target Low HP Toggle
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Auto-Target Low HP: " + (autoTargetLowHp ? "§aON" : "§cOFF")), button -> {
            autoTargetLowHp = !autoTargetLowHp;
            button.setMessage(Text.literal("Auto-Target Low HP: " + (autoTargetLowHp ? "§aON" : "§cOFF")));
        }).dimensions(x, y + 135, 200, 20).build());

        // 7. Advanced Info Toggle
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Armor & Items: " + (showAdvancedInfo ? "§aON" : "§cOFF")), button -> {
            showAdvancedInfo = !showAdvancedInfo;
            button.setMessage(Text.literal("Armor & Items: " + (showAdvancedInfo ? "§aON" : "§cOFF")));
        }).dimensions(x, y + 160, 200, 20).build());

        // 8. Back Button
        this.addDrawableChild(ButtonWidget.builder(Text.literal("§fDone"), button -> {
            this.client.setScreen(this.parent);
        }).dimensions(x, y + 195, 200, 20).build());
    }

    private String getStyleName() {
        return switch (styleIndex) {
            case 0 -> "§c10 Hearts";
            case 1 -> "§aSmooth Bar";
            case 2 -> "§bPro Face";
            default -> "Unknown";
        };
    }

    private String getTargetStyleName() {
        return switch (targetStyle) {
            case 0 -> "§4{ Classic }";
            case 1 -> "§b« Warrior »";
            case 2 -> "§c[ Square ]";
            case 3 -> "§e> Arrow <";
            case 4 -> "§6★ Star ★";
            default -> "Default";
        };
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        
        // Category Labels
        context.drawCenteredTextWithShadow(this.textRenderer, "§b§lMTPVP CLIENT SETTINGS", this.width / 2, 10, 0xFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, "§7--- INDICATOR SETTINGS ---", this.width / 2, height / 6 - 15, 0xFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, "§7--- GANG WAR TARGETING ---", this.width / 2, height / 6 + 75, 0xFFFFFF);
        
        // Info text
        if (!targetPlayerName.isEmpty()) {
            context.drawCenteredTextWithShadow(this.textRenderer, "§fCurrent Target: §e" + targetPlayerName, this.width / 2, height - 30, 0xFFFFFF);
        }

        super.render(context, mouseX, mouseY, delta);
    }
}
