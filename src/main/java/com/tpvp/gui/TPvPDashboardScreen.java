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
            int bgColor = hovered || activeTab ? 0x88002233 : 0x66000000; 
            int borderColor = hovered || activeTab ? 0xFF00AAFF : 0x55FFFFFF; 

            context.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, bgColor);
            context.drawBorder(this.getX(), this.getY(), this.width, this.height, borderColor);

            // Draw Item Icon if provided
            int textOffsetX = 0;
            if (iconItem != null) {
                context.drawItem(iconItem, this.getX() + 6, this.getY() + (this.height - 16) / 2);
                textOffsetX = 20; 
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

        // ==========================================
        // SIDEBAR TABS (WITH ICONS)
        // ==========================================
        this.addDrawableChild(new EpicIconButton(10, 50, sidebarWidth - 20, 25, "Combat", new ItemStack(Items.DIAMOND_SWORD), true, button -> { 
            currentTab = "Combat"; 
            this.init(); 
        }));
        this.addDrawableChild(new EpicIconButton(10, 80, sidebarWidth - 20, 25, "Radar", new ItemStack(Items.COMPASS), true, button -> { 
            currentTab = "Radar"; 
            this.init(); 
        }));
        this.addDrawableChild(new EpicIconButton(10, 110, sidebarWidth - 20, 25, "Target Lock", new ItemStack(Items.CROSSBOW), true, button -> { 
            currentTab = "Target Lock"; 
            this.init(); 
        }));
        this.addDrawableChild(new EpicIconButton(10, 140, sidebarWidth - 20, 25, "HUD Layouts", new ItemStack(Items.DIAMOND_CHESTPLATE), true, button -> { 
            currentTab = "HUD Layouts"; 
            this.init(); 
        }));
        this.addDrawableChild(new EpicIconButton(10, 170, sidebarWidth - 20, 25, "Crosshairs", new ItemStack(Items.SPYGLASS), true, button -> { 
            currentTab = "Crosshairs"; 
            this.init(); 
        }));

        int rightStartX = sidebarWidth + 20;
        
        // ==========================================
        // 1. COMBAT TAB
        // ==========================================
        if (currentTab.equals("Combat")) {
            this.addDrawableChild(new EpicIconButton(rightStartX, 50, 200, 25, "3D Indicator: " + (ModConfig.indicatorEnabled ? "§aON" : "§cOFF"), null, false, button -> { 
                ModConfig.indicatorEnabled = !ModConfig.indicatorEnabled; 
                this.init(); 
            }));
            
            String[] styles = {"Hearts", "Modern Bar", "Hits & Head"};
            this.addDrawableChild(new EpicIconButton(rightStartX, 85, 200, 25, "Style: §b" + styles[ModConfig.indicatorStyle], null, false, button -> { 
                ModConfig.indicatorStyle = (ModConfig.indicatorStyle + 1) % 3; 
                this.init(); 
            }));
        } 
        
        // ==========================================
        // 2. RADAR TAB
        // ==========================================
        else if (currentTab.equals("Radar")) {
            this.addDrawableChild(new EpicIconButton(rightStartX, 50, 200, 25, "Nearby Players: " + (ModConfig.nearbyEnabled ? "§aON" : "§cOFF"), null, false, button -> { 
                ModConfig.nearbyEnabled = !ModConfig.nearbyEnabled; 
                this.init(); 
            }));
            
            this.addDrawableChild(new EpicIconButton(rightStartX, 85, 200, 25, "§bEdit Radar Position", new ItemStack(Items.PAINTING), false, button -> { 
                this.client.setScreen(new EditHudScreen(this)); 
            }));
        }
        
        // ==========================================
        // 3. TARGET LOCK TAB
        // ==========================================
        else if (currentTab.equals("Target Lock")) {
            this.addDrawableChild(new EpicIconButton(rightStartX, 50, 140, 20, "Status: " + (ModConfig.targetEnabled ? "§aON" : "§cOFF"), null, false, button -> { 
                ModConfig.targetEnabled = !ModConfig.targetEnabled; 
                this.init(); 
            }));
            this.addDrawableChild(new EpicIconButton(rightStartX + 150, 50, 140, 20, "Mode: " + (ModConfig.targetMode == 0 ? "§eManual" : "§cAuto HP"), null, false, button -> { 
                ModConfig.targetMode = (ModConfig.targetMode + 1) % 2; 
                this.init(); 
            }));
            
            String[] colors = {"§6Gold", "§cRed", "§bDiamond", "§aEmerald"};
            this.addDrawableChild(new EpicIconButton(rightStartX, 75, 140, 20, "Color: " + colors[ModConfig.crownColor], null, false, button -> { 
                ModConfig.crownColor = (ModConfig.crownColor + 1) % 4; 
                this.init(); 
            }));
            this.addDrawableChild(new EpicIconButton(rightStartX + 150, 75, 140, 20, "Style: " + (ModConfig.crownStyle==0?"Crown":"Diamond"), null, false, button -> { 
                ModConfig.crownStyle = (ModConfig.crownStyle + 1) % 2; 
                this.init(); 
            }));
            
            this.addDrawableChild(new EpicIconButton(rightStartX, 100, 140, 20, "Auto Range: §e" + ModConfig.autoRange + "m", null, false, button -> { 
                ModConfig.autoRange = ModConfig.autoRange >= 50 ? 10 : ModConfig.autoRange + 10; 
                this.init(); 
            }));
            this.addDrawableChild(new EpicIconButton(rightStartX + 150, 100, 140, 20, "Show Target HP: " + (ModConfig.showTargetHealth ? "§aON" : "§cOFF"), null, false, button -> { 
                ModConfig.showTargetHealth = !ModConfig.showTargetHealth; 
                this.init(); 
            }));

            this.addDrawableChild(new EpicIconButton(rightStartX, 130, 290, 25, "§l🔍 Select & Tag Player", new ItemStack(Items.PLAYER_HEAD), false, button -> { 
                this.client.setScreen(new SelectTargetScreen(this)); 
            }));
            
            String targetStatus = ModConfig.taggedPlayerName.isEmpty() ? "§7None" : "§a" + ModConfig.taggedPlayerName;
            if (ModConfig.targetMode == 1) targetStatus = "§eAuto Searching...";
            this.addDrawableChild(new EpicIconButton(rightStartX, 160, 290, 20, "Current Locked: " + targetStatus, null, false, button -> {}));
        }
        
        // ==========================================
        // 4. HUD LAYOUTS TAB
        // ==========================================
        else if (currentTab.equals("HUD Layouts")) {
            this.addDrawableChild(new EpicIconButton(rightStartX, 40, 140, 20, "Armor HUD: " + (ModConfig.armorHudEnabled?"§aON":"§cOFF"), null, false, button -> { 
                ModConfig.armorHudEnabled = !ModConfig.armorHudEnabled; 
                this.init(); 
            }));
            String[] aStyles = {"Percentage", "Status Bar", "Numbers"};
            this.addDrawableChild(new EpicIconButton(rightStartX + 150, 40, 140, 20, "Design: §b" + aStyles[ModConfig.armorHudStyle], null, false, button -> { 
                ModConfig.armorHudStyle = (ModConfig.armorHudStyle + 1) % 3; 
                this.init(); 
            }));
            
            this.addDrawableChild(new EpicIconButton(rightStartX, 65, 140, 20, "Layout: " + (ModConfig.armorHudHorizontal?"§eHoriz ↔":"§dVert ↕"), null, false, button -> { 
                ModConfig.armorHudHorizontal = !ModConfig.armorHudHorizontal; 
                this.init(); 
            }));
            this.addDrawableChild(new EpicIconButton(rightStartX + 150, 65, 140, 20, "Held Item: " + (ModConfig.heldItemEnabled?"§aON":"§cOFF"), null, false, button -> { 
                ModConfig.heldItemEnabled = !ModConfig.heldItemEnabled; 
                this.init(); 
            }));

            this.addDrawableChild(new EpicIconButton(rightStartX, 90, 140, 20, "HUD BG: " + (ModConfig.armorBgEnabled?"§aON":"§cOFF"), null, false, button -> { 
                ModConfig.armorBgEnabled = !ModConfig.armorBgEnabled; 
                this.init(); 
            }));
            this.addDrawableChild(new EpicIconButton(rightStartX + 150, 90, 140, 20, "BG Opacity: §e" + (int)(ModConfig.armorBgOpacity*100) + "%", null, false, button -> { 
                ModConfig.armorBgOpacity += 0.2f; 
                if(ModConfig.armorBgOpacity > 1.0f) ModConfig.armorBgOpacity = 0.2f; 
                this.init(); 
            }));

            this.addDrawableChild(new EpicIconButton(rightStartX, 125, 290, 25, "§bEdit All HUD Positions", new ItemStack(Items.PAINTING), false, button -> { 
                this.client.setScreen(new EditHudScreen(this)); 
            }));
        }
        
        // ==========================================
        // 5. CROSSHAIRS TAB
        // ==========================================
        else if (currentTab.equals("Crosshairs")) {
            this.addDrawableChild(new EpicIconButton(rightStartX, 50, 200, 25, "Custom Crosshair: " + (ModConfig.crosshairEnabled ? "§aON" : "§cOFF"), null, false, button -> { 
                ModConfig.crosshairEnabled = !ModConfig.crosshairEnabled; 
                this.init(); 
            }));

            String[] styles = {"Perfect Plus", "Pro Dot", "Hollow Circle", "T-Shape", "Square + Dot"};
            this.addDrawableChild(new EpicIconButton(rightStartX, 85, 200, 25, "Style: §b" + styles[ModConfig.crosshairStyle], null, false, button -> { 
                ModConfig.crosshairStyle = (ModConfig.crosshairStyle + 1) % 5; 
                this.init(); 
            }));

            String[] colors = {"§fWhite", "§aGreen", "§cRed", "§bCyan", "§8Black"};
            this.addDrawableChild(new EpicIconButton(rightStartX, 120, 200, 25, "Color: " + colors[ModConfig.crosshairColor], null, false, button -> { 
                ModConfig.crosshairColor = (ModConfig.crosshairColor + 1) % 5; 
                this.init(); 
            }));
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fillGradient(0, 0, this.width, this.height, 0xDD050505, 0xDD111111);
        context.fill(0, 0, sidebarWidth, this.height, 0x66000000);
        context.drawBorder(sidebarWidth, 0, 1, this.height, 0x4400AAFF); 

        context.drawCenteredTextWithShadow(this.textRenderer, "§b§lTPvP §f§lCLIENT", sidebarWidth / 2, 20, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer, "§7Category: §f" + currentTab, sidebarWidth + 20, 20, 0xFFFFFF);

        super.render(context, mouseX, mouseY, delta);
    }
                       }
