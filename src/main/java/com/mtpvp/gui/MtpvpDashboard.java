package com.mtpvp.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class MtpvpDashboard extends Screen {
    private final Screen parent;

    // --- ALL FEATURES SAVED HERE (GLOBAL VARIABLES) ---
    public static boolean headEnabled = true;
    public static boolean showDistance = true;
    public static boolean showAdvancedInfo = true;
    public static int styleIndex = 1; // 0=Hearts, 1=Bar, 2=Pro Face

    // --- GANG WAR TARGET SYSTEM VARIABLES ---
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
        int y = this.height / 6; // Thoda upar se start taaki saare buttons aa jayein

        // --- SECTION 1: INDICATOR SETTINGS ---

        // 1. Head Display Toggle
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Head Display: " + (headEnabled ? "§aON" : "§cOFF")), button -> {
            headEnabled = !headEnabled;
            button.setMessage(Text.literal("Head Display: " + (headEnabled ? "§aON" : "§cOFF")));
        }).dimensions(x, y, 200, 20).build());

        // 2. Indicator Style Cycle (Hearts, Bar, Face)
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Indicator Style: " + getStyleName()), button -> {
            styleIndex = (styleIndex + 1) % 3;
            button.setMessage(Text.literal("Indicator Style: " + getStyleName()));
        }).dimensions(x, y + 25, 200, 20).build());

        // 3. Distance Toggle
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Show Distance: " + (showDistance ? "§aON" : "§cOFF")), button -> {
            showDistance = !showDistance;
            button.setMessage(Text.literal("Show Distance: " + (showDistance ? "§aON" : "§cOFF")));
        }).dimensions(x, y + 50, 200, 20).build());

        // --- SECTION 2: GANG WAR TARGETING ---

        // 4. Target Style Preset ( { }, « », [ ], etc.)
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Target Brackets: " + getTargetStyleName()), button -> {
            targetStyle = (targetStyle + 1) % 5;
            button.setMessage(Text.literal("Target Brackets: " + getTargetStyleName()));
        }).dimensions(x, y + 85, 200, 20).build());

        // 5. Open Target Selector Menu (Player List)
        this.addDrawableChild(ButtonWidget.builder(Text.literal("§6§lSELECT TARGET FROM LIST"), button -> {
            if (this.client != null) {
                this.client.setScreen(new TargetSelectorScreen());
            }
        }).dimensions(x, y + 110, 200, 20).build());

        // 6. Auto-Target Low HP Players Toggle
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Auto-Target (Low HP): " + (autoTargetLowHp ? "§aON" : "§cOFF")), button -> {
            autoTargetLowHp = !autoTargetLowHp;
            button.setMessage(Text.literal("Auto-Target (Low HP): " + (autoTargetLowHp ? "§aON" : "§cOFF")));
        }).dimensions(x, y + 135, 200, 20).build());

        // --- SECTION 3: ADVANCED ---

        // 7. Armor & Items Toggle
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Armor & Advanced Info: " + (showAdvancedInfo ? "§aON" : "§cOFF")), button -> {
            showAdvancedInfo = !showAdvancedInfo;
            button.setMessage(Text.literal("Armor & Advanced Info: " + (showAdvancedInfo ? "§aON" : "§cOFF")));
        }).dimensions(x, y + 160, 200, 20).build());

        // 8. Done / Back Button
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
            default -> "Default";
        };
    }

    private String getTargetStyleName() {
        return switch (targetStyle) {
            case 0 -> "§4{ Classic }";
            case 1 -> "§b« Warrior »";
            case 2 -> "§c[ Square ]";
            case 3 -> "§e> Arrow <";
            case 4 -> "§6★ Star ★";
            default -> "Preset 1";
        };
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Background Shadow
        this.renderBackground(context, mouseX, mouseY, delta);
        
        // Titles and Category Labels
        context.drawCenteredTextWithShadow(this.textRenderer, "§b§lMTPVP CLIENT SETTINGS", this.width / 2, 10, 0xFFFFFF);
        
        context.drawCenteredTextWithShadow(this.textRenderer, "§7--- INDICATOR SETTINGS ---", this.width / 2, height / 6 - 15, 0xAAAAAA);
        context.drawCenteredTextWithShadow(this.textRenderer, "§7--- GANG WAR TARGETING ---", this.width / 2, height / 6 + 75, 0xAAAAAA);
        
        // Status Information at the bottom
        if (!targetPlayerName.isEmpty()) {
            context.drawCenteredTextWithShadow(this.textRenderer, "§fCurrent Target: §e" + targetPlayerName, this.width / 2, height - 25, 0xFFFFFF);
        } else {
            context.drawCenteredTextWithShadow(this.textRenderer, "§7No Target Selected (Press R in-game)", this.width / 2, height - 25, 0x888888);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false; // Menu kholne par game pause nahi hoga (PvP ke liye best)
    }
}
