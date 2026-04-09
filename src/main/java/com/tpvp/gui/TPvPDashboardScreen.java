package com.tpvp.gui;

import com.tpvp.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

/**
 * TPvP Universal Client Dashboard
 * Features: 5 Categories, Glass UI, Minecraft Item Icons
 */
public class TPvPDashboardScreen extends Screen {
    private String currentTab = "Combat";
    private final int sidebarWidth = 150;

    public TPvPDashboardScreen() {
        super(Text.literal("TPvP Dashboard"));
    }

    // --- CUSTOM EPIC BUTTON CLASS (PRO LOOK) ---
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
            
            // Neon Aqua & Dark Glass Theme
            int bgColor = (hovered || activeTab) ? 0x99002233 : 0x77000000; 
            int borderColor = (hovered || activeTab) ? 0xFF00FFFF : 0x44FFFFFF;

            // Background & Border
            context.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, bgColor);
            context.drawBorder(this.getX(), this.getY(), this.width, this.height, borderColor);
            
            // Top Shine Highlight (Glass look)
            context.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + 1, 0x33FFFFFF);

            int textOffsetX = 0;
            if (iconItem != null) {
                // Draw real Minecraft Item Icon
                context.drawItem(iconItem, this.getX() + 6, this.getY() + (this.height - 16) / 2);
                textOffsetX = 22; 
            }

            int textColor = (hovered || activeTab) ? 0xFF00FFFF : 0xFFFFFFFF;
            context.drawTextWithShadow(
                MinecraftClient.getInstance().textRenderer, 
                this.getMessage(), 
                this.getX() + textOffsetX + 5, 
                this.getY() + (this.height - 8) / 2, 
                textColor
            );
        }
    }

    @Override
    protected void init() {
        this.clearChildren();

        // ==========================================
        // LEFT SIDEBAR TABS (5 CATEGORIES)
        // ==========================================
        this.addDrawableChild(new EpicIconButton(10, 50, sidebarWidth - 20, 25, "Combat", new ItemStack(Items.DIAMOND_SWORD), true, b -> {
            currentTab = "Combat";
            this.init();
        }));
        
        this.addDrawableChild(new EpicIconButton(10, 80, sidebarWidth - 20, 25, "Radar", new ItemStack(Items.COMPASS), true, b -> {
            currentTab = "Radar";
            this.init();
        }));

        this.addDrawableChild(new EpicIconButton(10, 110, sidebarWidth - 20, 25, "Target Lock", new ItemStack(Items.CROSSBOW), true, b -> {
            currentTab = "Target Lock";
            this.init();
        }));

        this.addDrawableChild(new EpicIconButton(10, 140, sidebarWidth - 20, 25, "HUD Layouts", new ItemStack(Items.DIAMOND_CHESTPLATE), true, b -> {
            currentTab = "HUD Layouts";
            this.init();
        }));

        this.addDrawableChild(new EpicIconButton(10, 170, sidebarWidth - 20, 25, "Crosshairs", new ItemStack(Items.SPYGLASS), true, b -> {
            currentTab = "Crosshairs";
            this.init();
        }));

        // ==========================================
        // RIGHT SIDE SETTINGS AREA
        // ==========================================
        int rX = sidebarWidth + 25;
        
        if (currentTab.equals("Combat")) {
            this.addDrawableChild(new EpicIconButton(rX, 50, 200, 25, "3D Indicator: " + (ModConfig.indicatorEnabled ? "§aON" : "§cOFF"), null, false, b -> {
                ModConfig.indicatorEnabled = !ModConfig.indicatorEnabled;
                this.init();
            }));

            String[] styles = {"Hearts Style", "Modern Bar", "Hits & Head"};
            this.addDrawableChild(new EpicIconButton(rX, 85, 200, 25, "Design: §b" + styles[ModConfig.indicatorStyle], null, false, b -> {
                ModConfig.indicatorStyle = (ModConfig.indicatorStyle + 1) % 3;
                this.init();
            }));
        } 
        else if (currentTab.equals("Radar")) {
            this.addDrawableChild(new EpicIconButton(rX, 50, 200, 25, "Radar HUD: " + (ModConfig.nearbyEnabled ? "§aON" : "§cOFF"), null, false, b -> {
                ModConfig.nearbyEnabled = !ModConfig.nearbyEnabled;
                this.init();
            }));

            this.addDrawableChild(new EpicIconButton(rX, 85, 200, 25, "Edit HUD Position", new ItemStack(Items.PAINTING), false, b -> {
                this.client.setScreen(new EditHudScreen(this));
            }));
        }
        else if (currentTab.equals("Target Lock")) {
            this.addDrawableChild(new EpicIconButton(rX, 50, 140, 20, "Status: " + (ModConfig.targetEnabled ? "§aON" : "§cOFF"), null, false, b -> {
                ModConfig.targetEnabled = !ModConfig.targetEnabled; this.init();
            }));
            this.addDrawableChild(new EpicIconButton(rX + 150, 50, 140, 20, "Mode: " + (ModConfig.targetMode == 0 ? "§eManual" : "§cAuto HP"), null, false, b -> {
                ModConfig.targetMode = (ModConfig.targetMode + 1) % 2; this.init();
            }));

            String[] colors = {"§6Gold", "§cRed", "§bDiamond", "§aEmerald"};
            this.addDrawableChild(new EpicIconButton(rX, 75, 140, 20, "Crown Color: " + colors[ModConfig.crownColor], null, false, b -> {
                ModConfig.crownColor = (ModConfig.crownColor + 1) % 4; this.init();
            }));
            
            this.addDrawableChild(new EpicIconButton(rX + 150, 75, 140, 20, "Auto Range: §e" + ModConfig.autoRange + "m", null, false, b -> {
                ModConfig.autoRange = (ModConfig.autoRange >= 50) ? 10 : ModConfig.autoRange + 10; this.init();
            }));

            this.addDrawableChild(new EpicIconButton(rX, 105, 290, 25, "§l🔍 SELECT PLAYER TO TAG", new ItemStack(Items.PLAYER_HEAD), false, b -> {
                this.client.setScreen(new SelectTargetScreen(this));
            }));

            String status = ModConfig.taggedPlayerName.isEmpty() ? "§7None" : "§a" + ModConfig.taggedPlayerName;
            this.addDrawableChild(new EpicIconButton(rX, 135, 290, 20, "Locked On: " + status, null, false, b -> {}));
        }
        else if (currentTab.equals("HUD Layouts")) {
            this.addDrawableChild(new EpicIconButton(rX, 40, 140, 20, "Armor HUD: " + (ModConfig.armorHudEnabled?"§aON":"§cOFF"), null, false, b -> { ModConfig.armorHudEnabled = !ModConfig.armorHudEnabled; this.init(); }));
            String[] aStyles = {"Percentage", "Status Bar", "Numbers"};
            this.addDrawableChild(new EpicIconButton(rX + 150, 40, 140, 20, "Design: §b" + aStyles[ModConfig.armorHudStyle], null, false, b -> { ModConfig.armorHudStyle = (ModConfig.armorHudStyle + 1) % 3; this.init(); }));
            
            this.addDrawableChild(new EpicIconButton(rX, 65, 140, 20, "Layout: " + (ModConfig.armorHudHorizontal?"§eHoriz ↔":"§dVert ↕"), null, false, b -> { ModConfig.armorHudHorizontal = !ModConfig.armorHudHorizontal; this.init(); }));
            this.addDrawableChild(new EpicIconButton(rX + 150, 65, 140, 20, "Held Item: " + (ModConfig.heldItemEnabled?"§aON":"§cOFF"), null, false, b -> { ModConfig.heldItemEnabled = !ModConfig.heldItemEnabled; this.init(); }));

            this.addDrawableChild(new EpicIconButton(rX, 90, 140, 20, "HUD BG: " + (ModConfig.armorBgEnabled?"§aON":"§cOFF"), null, false, b -> { ModConfig.armorBgEnabled = !ModConfig.armorBgEnabled; this.init(); }));
            this.addDrawableChild(new EpicIconButton(rX + 150, 90, 140, 20, "BG Opacity: §e" + (int)(ModConfig.armorBgOpacity*100) + "%", null, false, b -> { 
                ModConfig.armorBgOpacity += 0.2f; if(ModConfig.armorBgOpacity > 1.0f) ModConfig.armorBgOpacity = 0.2f; this.init(); 
            }));

            this.addDrawableChild(new EpicIconButton(rX, 125, 290, 25, "§bEdit All HUD Positions", new ItemStack(Items.PAINTING), false, b -> { this.client.setScreen(new EditHudScreen(this)); }));
        }
        else if (currentTab.equals("Crosshairs")) {
            String[] st = {"Perfect Plus", "Pro Dot", "Hollow Circle", "T-Shape", "Square Dot"};
            this.addDrawableChild(new EpicIconButton(rX, 50, 200, 25, "Style: §b" + st[ModConfig.crosshairStyle], null, false, b -> {
                ModConfig.crosshairStyle = (ModConfig.crosshairStyle + 1) % 5; this.init();
            }));

            String[] cl = {"White", "Green", "Red", "Cyan", "Black"};
            this.addDrawableChild(new EpicIconButton(rX, 85, 200, 25, "Color: " + cl[ModConfig.crosshairColor], null, false, b -> {
                ModConfig.crosshairColor = (ModConfig.crosshairColor + 1) % 5; this.init();
            }));
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Dark Glass Gradient Background
        context.fillGradient(0, 0, this.width, this.height, 0xCC050505, 0xCC111111);
        
        // Sidebar Background
        context.fill(0, 0, sidebarWidth, this.height, 0x44000000);
        context.drawBorder(sidebarWidth, 0, 1, this.height, 0x8800FFFF); 
        
        // Dynamic Shine Overlay
        context.fillGradient(sidebarWidth, 0, sidebarWidth + 100, this.height, 0x11FFFFFF, 0x00FFFFFF);

        context.drawCenteredTextWithShadow(this.textRenderer, "§b§lTPvP §f§lCLIENT", sidebarWidth / 2, 20, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer, "§7Category: §f" + currentTab, sidebarWidth + 25, 20, 0xFFFFFF);

        super.render(context, mouseX, mouseY, delta);
    }
}
