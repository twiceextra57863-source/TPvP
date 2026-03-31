package com.tpvp.gui;

import com.tpvp.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class TPvPDashboardScreen extends Screen {
    private String currentTab = "Combat";
    private final int sidebarWidth = 140;

    public TPvPDashboardScreen() {
        super(Text.literal("TPvP Dashboard"));
    }

    // NAYA FIX: Custom Inner Class banayi hai Button ke liye taaki protected access ka error na aaye
    private class EpicButton extends ButtonWidget {
        public EpicButton(int x, int y, int width, int height, String message, PressAction onPress) {
            super(x, y, width, height, Text.literal(message), onPress, ButtonWidget.DEFAULT_NARRATION_SUPPLIER);
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            // 30% Opacity Default, Hover hone par thoda light
            int bgColor = this.isHovered() ? 0x4D333333 : 0x4D000000; 
            // Hover hone par border glow karega
            int borderColor = this.isHovered() ? 0xAA00FF00 : 0x55FFFFFF;
            
            // 30% opacity flat background draw karo
            context.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, bgColor);
            // Sleek border draw karo
            context.drawBorder(this.getX(), this.getY(), this.width, this.height, borderColor);
            
            // 100% Opacity Solid Text
            int textColor = this.isHovered() ? 0x00FF00 : 0xFFFFFF;
            context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, this.getMessage(), 
                    this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, textColor);
        }
    }

    @Override
    protected void init() {
        this.clearChildren();

        // ----- LEFT TABS (Modern Buttons) -----
        this.addDrawableChild(new EpicButton(10, 50, sidebarWidth - 20, 25, "⚔ Combat", button -> {
            currentTab = "Combat";
            this.init();
        }));
        
        this.addDrawableChild(new EpicButton(10, 85, sidebarWidth - 20, 25, "📡 Radar", button -> {
            currentTab = "Radar";
            this.init();
        }));

        // ----- RIGHT SETTINGS -----
        int rightStartX = sidebarWidth + 30;
        
        if (currentTab.equals("Combat")) {
            this.addDrawableChild(new EpicButton(rightStartX, 50, 200, 25, "3D Indicator: " + (ModConfig.indicatorEnabled ? "§aON" : "§cOFF"), button -> {
                ModConfig.indicatorEnabled = !ModConfig.indicatorEnabled;
                this.init(); // UI refresh
            }));

            String[] styles = {"Heart Style", "Bar Style", "Head + Hits Style"};
            this.addDrawableChild(new EpicButton(rightStartX, 85, 200, 25, "Style: §e" + styles[ModConfig.indicatorStyle], button -> {
                ModConfig.indicatorStyle = (ModConfig.indicatorStyle + 1) % 3;
                this.init();
            }));
        } 
        else if (currentTab.equals("Radar")) {
            this.addDrawableChild(new EpicButton(rightStartX, 50, 200, 25, "Nearby Players: " + (ModConfig.nearbyEnabled ? "§aON" : "§cOFF"), button -> {
                ModConfig.nearbyEnabled = !ModConfig.nearbyEnabled;
                this.init();
            }));

            this.addDrawableChild(new EpicButton(rightStartX, 85, 200, 25, "§bEdit HUD Position", button -> {
                this.client.setScreen(new EditHudScreen(this)); 
            }));
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Vanilla background ko thoda blur type effect dega
        this.renderBackground(context, mouseX, mouseY, delta);

        // Modern 30% Opacity Backgrounds (Hex: 4D = 30% alpha)
        context.fill(0, 0, sidebarWidth, this.height, 0x4D000000); // Sidebar
        context.fill(sidebarWidth, 0, this.width, this.height, 0x33000000); // Main Area

        // Separator Line
        context.fill(sidebarWidth, 0, sidebarWidth + 1, this.height, 0x55FFFFFF);

        // Title and Category Text (100% Opacity)
        context.drawCenteredTextWithShadow(this.textRenderer, "§b§lTPvP Client", sidebarWidth / 2, 20, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer, "§lCategory: §e" + currentTab, sidebarWidth + 30, 20, 0xFFFFFF);

        super.render(context, mouseX, mouseY, delta);
    }
}
