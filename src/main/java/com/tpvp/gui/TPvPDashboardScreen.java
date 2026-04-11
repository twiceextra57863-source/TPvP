package com.tpvp.gui;

import com.tpvp.config.ModConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

public class TPvPDashboardScreen extends Screen {
    private final Screen parent; // Escape menu par wapas jane ke liye
    private String currentTab = "Combat";

    // Menu ke dimensions (Floating Window)
    private final int windowWidth = 420;
    private final int windowHeight = 240;
    private final int sidebarWidth = 130;

    public TPvPDashboardScreen(Screen parent) {
        super(Text.literal("TPvP Dashboard"));
        this.parent = parent;
    }

    public TPvPDashboardScreen() {
        this(null); // Agar direct Title Screen se khule
    }

    @Override
    protected void init() {
        this.clearChildren();

        // Floating window ke coordinates (Screen ke center me)
        int winX = (this.width - windowWidth) / 2;
        int winY = (this.height - windowHeight) / 2;
        
        int settingsX = winX + sidebarWidth + 15;
        int settingsY = winY + 40;

        // SETTINGS BUTTONS (Sirf Right Side me show honge)
        if (currentTab.equals("Combat")) {
            this.addDrawableChild(ButtonWidget.builder(Text.literal("3D Indicator: " + (ModConfig.indicatorEnabled ? "§aON" : "§cOFF")), b -> {
                ModConfig.indicatorEnabled = !ModConfig.indicatorEnabled; this.init();
            }).dimensions(settingsX, settingsY, 120, 20).build());

            String[] styles = {"Heart Style", "Bar Style", "Head + Hits"};
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Style: §e" + styles[ModConfig.indicatorStyle]), b -> {
                ModConfig.indicatorStyle = (ModConfig.indicatorStyle + 1) % 3; this.init();
            }).dimensions(settingsX + 130, settingsY, 120, 20).build());

            this.addDrawableChild(ButtonWidget.builder(Text.literal("Smart Crosshair: " + (ModConfig.smartCrosshair ? "§aON" : "§cOFF")), b -> {
                ModConfig.smartCrosshair = !ModConfig.smartCrosshair; this.init();
            }).dimensions(settingsX, settingsY + 30, 120, 20).build());

            this.addDrawableChild(ButtonWidget.builder(Text.literal("PvP Hitboxes: " + (ModConfig.hitboxEnabled ? "§aON" : "§cOFF")), b -> {
                ModConfig.hitboxEnabled = !ModConfig.hitboxEnabled; this.init();
            }).dimensions(settingsX + 130, settingsY + 30, 120, 20).build());

        } else if (currentTab.equals("Radar")) {
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Nearby Players: " + (ModConfig.nearbyEnabled ? "§aON" : "§cOFF")), b -> {
                ModConfig.nearbyEnabled = !ModConfig.nearbyEnabled; this.init();
            }).dimensions(settingsX, settingsY, 250, 20).build());
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // 1. Dynamic Gradient Background (Puri screen par dark blur jaisa effect)
        context.fillGradient(0, 0, this.width, this.height, 0xDD000000, 0xAA001133);

        int winX = (this.width - windowWidth) / 2;
        int winY = (this.height - windowHeight) / 2;

        // 2. Window Border (Glowing Aqua Line)
        context.fill(winX - 2, winY - 2, winX + windowWidth + 2, winY + windowHeight + 2, 0xFF00FFAA);
        
        // 3. Main Window Background (Right Side)
        context.fill(winX, winY, winX + windowWidth, winY + windowHeight, 0xFF151515);
        
        // 4. Sidebar Background (Left Side - Thoda zyada dark)
        context.fill(winX, winY, winX + sidebarWidth, winY + windowHeight, 0xFF0A0A0A);
        
        // 5. Sidebar & Main Area separator line
        context.fill(winX + sidebarWidth, winY, winX + sidebarWidth + 1, winY + windowHeight, 0x55FFFFFF);

        // Header Title
        context.drawCenteredTextWithShadow(this.textRenderer, "§b§lTPvP CLIENT", winX + sidebarWidth / 2, winY + 15, 0xFFFFFF);
        
        // Settings Header
        context.drawTextWithShadow(this.textRenderer, "§l" + currentTab.toUpperCase() + " SETTINGS", winX + sidebarWidth + 20, winY + 15, 0xFFFFFF);
        context.fill(winX + sidebarWidth + 20, winY + 28, winX + windowWidth - 20, winY + 29, 0x33FFFFFF); // Underline

        // --- CUSTOM TABS RENDERING ---
        drawCustomTab(context, mouseX, mouseY, "Combat", Items.DIAMOND_SWORD.getDefaultStack(), winX, winY + 40);
        drawCustomTab(context, mouseX, mouseY, "Radar", Items.COMPASS.getDefaultStack(), winX, winY + 70);
        drawCustomTab(context, mouseX, mouseY, "Edit HUD", Items.PAINTING.getDefaultStack(), winX, winY + 100);

        // Buttons draw karna
        super.render(context, mouseX, mouseY, delta);
    }

    // Helper Method to draw premium looking tabs with Minecraft Items
    private void drawCustomTab(DrawContext context, int mx, int my, String tabName, ItemStack icon, int x, int y) {
        boolean isSelected = currentTab.equals(tabName);
        boolean isHovered = mx >= x && mx <= x + sidebarWidth && my >= y && my <= y + 25;

        // Background Highlight
        if (isSelected) {
            context.fill(x, y, x + sidebarWidth, y + 25, 0x4400FFAA); // Aqua selection highlight
            context.fill(x, y, x + 3, y + 25, 0xFF00FFAA); // Thick left accent bar
        } else if (isHovered) {
            context.fill(x, y, x + sidebarWidth, y + 25, 0x22FFFFFF); // Subtle white hover
        }

        // Draw 3D Minecraft Item
        context.drawItem(icon, x + 10, y + 4);
        
        // Draw Text
        int textColor = isSelected ? 0x00FFAA : (isHovered ? 0xFFFFFF : 0xAAAAAA);
        context.drawTextWithShadow(this.textRenderer, tabName, x + 32, y + 8, textColor);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int winX = (this.width - windowWidth) / 2;
        int winY = (this.height - windowHeight) / 2;

        // Custom Click Detection for Tabs
        if (mouseX >= winX && mouseX <= winX + sidebarWidth) {
            if (mouseY >= winY + 40 && mouseY <= winY + 65) {
                currentTab = "Combat"; this.init(); return true;
            }
            if (mouseY >= winY + 70 && mouseY <= winY + 95) {
                currentTab = "Radar"; this.init(); return true;
            }
            if (mouseY >= winY + 100 && mouseY <= winY + 125) {
                this.client.setScreen(new EditHudScreen(this)); // Open Edit HUD Screen
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void close() {
        ModConfig.save();
        if (this.client != null) {
            this.client.setScreen(this.parent); // Escape dabane par pichli screen pe jaye
        }
    }
}
