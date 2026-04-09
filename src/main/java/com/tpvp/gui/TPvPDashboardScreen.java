package com.tpvp.gui;

import com.tpvp.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

public class TPvPDashboardScreen extends Screen {
    private String currentTab = "Combat";
    private final int sidebarWidth = 150;

    public TPvPDashboardScreen() {
        super(Text.literal("TPvP Dashboard"));
    }

    // --- DOMINANT EPIC BUTTON CLASS WITH ITEM ICONS ---
    private class EpicIconButton extends ButtonWidget {
        private final ItemStack iconItem;
        private final boolean isTab;

        public EpicIconButton(int x, int y, int width, int height, String message, ItemStack iconItem, boolean isTab, PressAction onPress) {
            super(x, y, width, height, Text.literal(message), onPress, ButtonWidget.DEFAULT_NARRATION_SUPPLIER);
            this.iconItem = iconItem;
            this.isTab = isTab;
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            boolean hovered = this.isHovered();
            boolean activeTab = isTab && currentTab.equals(this.getMessage().getString());
            
            // Premium Dark Theme Colors
            int bgColor = hovered || activeTab ? 0x88002233 : 0x66000000; // Deep Aqua glow if hovered/active
            int borderColor = hovered || activeTab ? 0xFF00AAFF : 0x55FFFFFF; // Neon Cyan Border

            context.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, bgColor);
            context.drawBorder(this.getX(), this.getY(), this.width, this.height, borderColor);

            // Draw Item Icon if provided (Instead of Emojis!)
            int textOffsetX = 0;
            if (iconItem != null) {
                context.drawItem(iconItem, this.getX() + 6, this.getY() + (this.height - 16) / 2);
                textOffsetX = 20; // Shift text to right
            }

            int textColor = hovered || activeTab ? 0xFF00FFFF : 0xFFDDDDDD;
            context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, this.getMessage(), 
                    this.getX() + textOffsetX + (this.width - textOffsetX) / 2 - MinecraftClient.getInstance().textRenderer.getWidth(this.getMessage()) / 2, 
                    this.getY() + (this.height - 8) / 2, textColor);
        }
    }

    @Override
    protected void init() {
        this.clearChildren();

        // ----- SIDEBAR TABS WITH MINECRAFT ITEMS -----
        this.addDrawableChild(new EpicIconButton(10, 50, sidebarWidth - 20, 25, "Combat", new ItemStack(Items.DIAMOND_SWORD), true, b -> { currentTab = "Combat"; this.init(); }));
        this.addDrawableChild(new EpicIconButton(10, 80, sidebarWidth - 20, 25, "Radar", new ItemStack(Items.COMPASS), true, b -> { currentTab = "Radar"; this.init(); }));
        this.addDrawableChild(new EpicIconButton(10, 110, sidebarWidth - 20, 25, "Target Lock", new ItemStack(Items.CROSSBOW), true, b -> { currentTab = "Target Lock"; this.init(); }));
        this.addDrawableChild(new EpicIconButton(10, 140, sidebarWidth - 20, 25, "Armor HUD", new ItemStack(Items.DIAMOND_CHESTPLATE), true, b -> { currentTab = "Armor HUD"; this.init(); }));

        // ----- RIGHT SETTINGS AREA -----
        int rightStartX = sidebarWidth + 20;
        
        if (currentTab.equals("Combat")) {
            this.addDrawableChild(new EpicIconButton(rightStartX, 50, 200, 25, "3D Indicator: " + (ModConfig.indicatorEnabled ? "§aON" : "§cOFF"), null, false, b -> { ModConfig.indicatorEnabled = !ModConfig.indicatorEnabled; this.init(); }));
            String[] styles = {"Hearts", "Modern Bar", "Hits & Head"};
            this.addDrawableChild(new EpicIconButton(rightStartX, 85, 200, 25, "Style: §b" + styles[ModConfig.indicatorStyle], null, false, b -> { ModConfig.indicatorStyle = (ModConfig.indicatorStyle + 1) % 3; this.init(); }));
        } 
        else if (currentTab.equals("Radar")) {
            this.addDrawableChild(new EpicIconButton(rightStartX, 50, 200, 25, "Nearby Players: " + (ModConfig.nearbyEnabled ? "§aON" : "§cOFF"), null, false, b -> { ModConfig.nearbyEnabled = !ModConfig.nearbyEnabled; this.init(); }));
            this.addDrawableChild(new EpicIconButton(rightStartX, 85, 200, 25, "§bEdit Layout", new ItemStack(Items.PAINTING), false, b -> { this.client.setScreen(new EditHudScreen(this)); }));
        }
        else if (currentTab.equals("Target Lock")) {
            this.addDrawableChild(new EpicIconButton(rightStartX, 50, 140, 20, "Status: " + (ModConfig.targetEnabled ? "§aON" : "§cOFF"), null, false, b -> { ModConfig.targetEnabled = !ModConfig.targetEnabled; this.init(); }));
            this.addDrawableChild(new EpicIconButton(rightStartX + 150, 50, 140, 20, "Mode: " + (ModConfig.targetMode == 0 ? "§eManual" : "§cAuto HP"), null, false, b -> { ModConfig.targetMode = (ModConfig.targetMode + 1) % 2; this.init(); }));
            String[] colors = {"§6Gold", "§cRed", "§bDiamond", "§aEmerald"};
            this.addDrawableChild(new EpicIconButton(rightStartX, 75, 140, 20, "Color: " + colors[ModConfig.crownColor], null, false, b -> { ModConfig.crownColor = (ModConfig.crownColor + 1) % 4; this.init(); }));
            this.addDrawableChild(new EpicIconButton(rightStartX + 150, 75, 140, 20, "Style: " + (ModConfig.crownStyle==0?"Crown":"Diamond"), null, false, b -> { ModConfig.crownStyle = (ModConfig.crownStyle + 1) % 2; this.init(); }));
            this.addDrawableChild(new EpicIconButton(rightStartX, 105, 290, 25, "§l🔍 Select & Tag Player", new ItemStack(Items.PLAYER_HEAD), false, b -> { this.client.setScreen(new SelectTargetScreen(this)); }));
        }
        // --- NEW ARMOR HUD TAB ---
        else if (currentTab.equals("Armor HUD")) {
            this.addDrawableChild(new EpicIconButton(rightStartX, 50, 200, 25, "Armor HUD: " + (ModConfig.armorHudEnabled ? "§aON" : "§cOFF"), null, false, b -> { ModConfig.armorHudEnabled = !ModConfig.armorHudEnabled; this.init(); }));
            String[] aStyles = {"Percentage (%)", "Status Bar", "Numbers (X/Y)"};
            this.addDrawableChild(new EpicIconButton(rightStartX, 85, 200, 25, "Design: §b" + aStyles[ModConfig.armorHudStyle], null, false, b -> { ModConfig.armorHudStyle = (ModConfig.armorHudStyle + 1) % 3; this.init(); }));
            this.addDrawableChild(new EpicIconButton(rightStartX, 120, 200, 25, "Layout: " + (ModConfig.armorHudHorizontal ? "§eHorizontal ↔" : "§dVertical ↕"), null, false, b -> { ModConfig.armorHudHorizontal = !ModConfig.armorHudHorizontal; this.init(); }));
            this.addDrawableChild(new EpicIconButton(rightStartX, 155, 200, 25, "§bEdit Layout Position", new ItemStack(Items.PAINTING), false, b -> { this.client.setScreen(new EditHudScreen(this)); }));
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Epic Dark Gradient Background
        context.fillGradient(0, 0, this.width, this.height, 0xDD050505, 0xDD111111);

        // Sidebar Background
        context.fill(0, 0, sidebarWidth, this.height, 0x66000000);
        context.drawBorder(sidebarWidth, 0, 1, this.height, 0x4400AAFF); // Neon Divider

        // Header Title
        context.drawCenteredTextWithShadow(this.textRenderer, "§b§lTPvP §f§lCLIENT", sidebarWidth / 2, 20, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer, "§7Category: §f" + currentTab, sidebarWidth + 20, 20, 0xFFFFFF);

        super.render(context, mouseX, mouseY, delta);
    }
}
