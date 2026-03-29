package com.mtpvp.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class MtpvpDashboard extends Screen {
    private final Screen parent;

    // --- GLOBAL PERSISTENT SETTINGS ---
    public static boolean headEnabled = true;
    public static boolean showDistance = true;
    public static boolean showAdvancedInfo = true;
    public static int styleIndex = 1; // 0=Hearts, 1=Bar, 2=Pro Face

    // --- GANG WAR TARGET SYSTEM ---
    public static String targetPlayerName = ""; 
    public static boolean autoTargetLowHp = false;
    public static int targetStyle = 0; // 0 to 4 (Presets)

    public MtpvpDashboard(Screen parent) {
        super(Text.literal("MTPVP Dashboard"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int x = this.width / 2 - 100;
        int y = this.height / 6; // Thoda upar se start taaki saare buttons fit ho jayein

        // --- SECTION 1: INDICATOR SETTINGS ---
        
        // 1. Head Display (Master Toggle)
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Head Display: " + (headEnabled ? "§aON" : "§cOFF")), button -> {
            headEnabled = !headEnabled;
            button.setMessage(Text.literal("Head Display: " + (headEnabled ? "§aON" : "§cOFF")));
        }).dimensions(x, y, 200, 20).build());

        // 2. Cycle Indicator Style
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Indicator Style: " + getStyleName()), button -> {
            styleIndex = (styleIndex + 1) % 3;
            button.setMessage(Text.literal("Indicator Style: " + getStyleName()));
        }).dimensions(x, y + 25, 200, 20).build());

        // 3. Distance Toggle
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Show Distance: " + (showDistance ? "§aON" : "§cOFF")), button -> {
            showDistance = !showDistance;
            button.setMessage(Text.literal("Show Distance: " + (showDistance ? "§aON" : "§cOFF")));
        }).dimensions(x, y + 50, 200, 20).build());

        // --- SECTION 2: GANG WAR TARGETING (HEAVY FEATURES) ---

        // 4. Target Bracket Presets
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Target Brackets: " + getTargetStyleName()), button -> {
            targetStyle = (targetStyle + 1) % 5;
            button.setMessage(Text.literal("Target Brackets: " + getTargetStyleName()));
        }).dimensions(x, y + 85, 200, 20).build());

        // 5. Open Target Selector (Player List Menu)
        this.addDrawableChild(ButtonWidget.builder(Text.literal("§6§lOPEN TARGET SELECTOR"), button -> {
            if (this.client != null) {
                this.client.setScreen(new TargetSelectorScreen());
            }
        }).dimensions(x, y + 110, 200, 20).build());

        // 6. Auto-Target Low HP Toggle
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Auto-Target (Low HP): " + (autoTargetLowHp ? "§aON" : "§cOFF")), button -> {
            autoTargetLowHp = !autoTargetLowHp;
            button.setMessage(Text.literal("Auto-Target (Low HP): " + (autoTargetLowHp ? "§aON" : "§cOFF")));
        }).dimensions(x, y + 135, 200, 20).build());

        // --- SECTION 3: ADVANCED ---

        // 7. Armor & Items Toggle
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Armor & Items: " + (showAdvancedInfo ? "§aON" : "§cOFF")), button -> {
            showAdvancedInfo = !showAdvancedInfo;
            button.setMessage(Text.literal("Armor & Items: " + (showAdvancedInfo ? "§aON" : "§cOFF")));
        }).dimensions(x, y + 160, 200, 20).build());

        // 8. Back / Done Button
        this.addDrawableChild(ButtonWidget.builder(Text.literal("§fSave & Close"), button -> {
            if (this.client != null) {
                this.client.setScreen(this.parent);
            }
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
        // Darkened background for readability
        this.renderBackground(context, mouseX, mouseY, delta);
        
        // Header
        context.drawCenteredTextWithShadow(this.textRenderer, "§b§lMTPVP CLIENT SETTINGS", this.width / 2, 10, 0xFFFFFF);
        
        // Category Dividers
        context.drawCenteredTextWithShadow(this.textRenderer, "§7--- INDICATOR SETTINGS ---", this.width / 2, height / 6 - 15, 0xAAAAAA);
        context.drawCenteredTextWithShadow(this.textRenderer, "§7--- GANG WAR TARGETING ---", this.width / 2, height / 6 + 75, 0xAAAAAA);
        
        // Bottom Status Info
        if (!targetPlayerName.isEmpty()) {
            context.drawCenteredTextWithShadow(this.textRenderer, "§fTargeting: §e" + targetPlayerName, this.width / 2, height - 25, 0xFFFFFF);
        } else {
            context.drawCenteredTextWithShadow(this.textRenderer, "§7No Target (Look at player & press R)", this.width / 2, height - 25, 0x888888);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false; // Best for PvP servers
    }
}
