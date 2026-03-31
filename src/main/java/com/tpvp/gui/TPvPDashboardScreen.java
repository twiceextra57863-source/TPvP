package com.tpvp.gui;

import com.tpvp.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class TPvPDashboardScreen extends Screen {
    private String currentTab = "Target"; // Default open tab
    private final int sidebarWidth = 140;

    public TPvPDashboardScreen() {
        super(Text.literal("TPvP Dashboard"));
    }

    private class EpicButton extends ButtonWidget {
        public EpicButton(int x, int y, int width, int height, String message, PressAction onPress) {
            super(x, y, width, height, Text.literal(message), onPress, ButtonWidget.DEFAULT_NARRATION_SUPPLIER);
        }
        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            int bgColor = this.isHovered() ? 0x4D333333 : 0x4D000000; 
            int borderColor = this.isHovered() ? 0xAA00FF00 : 0x55FFFFFF;
            context.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, bgColor);
            context.drawBorder(this.getX(), this.getY(), this.width, this.height, borderColor);
            int textColor = this.isHovered() ? 0x00FF00 : 0xFFFFFF;
            context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, this.getMessage(), 
                    this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, textColor);
        }
    }

    @Override
    protected void init() {
        this.clearChildren();

        // ----- LEFT TABS -----
        this.addDrawableChild(new EpicButton(10, 50, sidebarWidth - 20, 25, "⚔ Combat", b -> { currentTab = "Combat"; this.init(); }));
        this.addDrawableChild(new EpicButton(10, 80, sidebarWidth - 20, 25, "📡 Radar", b -> { currentTab = "Radar"; this.init(); }));
        this.addDrawableChild(new EpicButton(10, 110, sidebarWidth - 20, 25, "🎯 Target Lock", b -> { currentTab = "Target"; this.init(); }));

        int rightStartX = sidebarWidth + 20;

        // ----- TARGET LOCK TAB (8 Settings) -----
        if (currentTab.equals("Target")) {
            // Row 1
            this.addDrawableChild(new EpicButton(rightStartX, 40, 140, 20, "Target Lock: " + (ModConfig.targetEnabled ? "§aON" : "§cOFF"), b -> { ModConfig.targetEnabled = !ModConfig.targetEnabled; this.init(); }));
            this.addDrawableChild(new EpicButton(rightStartX + 150, 40, 140, 20, "Mode: " + (ModConfig.targetMode == 0 ? "§eManual Tag" : "§cAuto (Low HP)"), b -> { ModConfig.targetMode = (ModConfig.targetMode + 1) % 2; this.init(); }));
            
            // Row 2
            String[] colors = {"§6Gold", "§cRed", "§bDiamond", "§aEmerald"};
            this.addDrawableChild(new EpicButton(rightStartX, 65, 140, 20, "Color: " + colors[ModConfig.crownColor], b -> { ModConfig.crownColor = (ModConfig.crownColor + 1) % 4; this.init(); }));
            String[] styles = {"3D Crown", "Floating Diamond"};
            this.addDrawableChild(new EpicButton(rightStartX + 150, 65, 140, 20, "Style: §e" + styles[ModConfig.crownStyle], b -> { ModConfig.crownStyle = (ModConfig.crownStyle + 1) % 2; this.init(); }));
            
            // Row 3
            this.addDrawableChild(new EpicButton(rightStartX, 90, 140, 20, "Target Glow: " + (ModConfig.glowEffect ? "§aON" : "§cOFF"), b -> { ModConfig.glowEffect = !ModConfig.glowEffect; this.init(); }));
            this.addDrawableChild(new EpicButton(rightStartX + 150, 90, 140, 20, "Auto Range: §e" + ModConfig.autoRange + "m", b -> { ModConfig.autoRange = ModConfig.autoRange >= 50 ? 10 : ModConfig.autoRange + 10; this.init(); }));

            // Row 4
            this.addDrawableChild(new EpicButton(rightStartX, 115, 140, 20, "Crown Scale: §e" + String.format("%.1fx", ModConfig.crownScale), b -> { ModConfig.crownScale = ModConfig.crownScale >= 2.0f ? 0.5f : ModConfig.crownScale + 0.5f; this.init(); }));
            this.addDrawableChild(new EpicButton(rightStartX + 150, 115, 140, 20, "Show HP: " + (ModConfig.showTargetHealth ? "§aON" : "§cOFF"), b -> { ModConfig.showTargetHealth = !ModConfig.showTargetHealth; this.init(); }));

            // Big Epic Manual Tag Button
            this.addDrawableChild(new EpicButton(rightStartX, 150, 290, 25, "§l🔍 Select & Tag Player", b -> {
                this.client.setScreen(new SelectTargetScreen(this));
            }));
            
            // Current Target Status
            String targetStatus = ModConfig.taggedPlayerName.isEmpty() ? "§7None" : "§a" + ModConfig.taggedPlayerName;
            if (ModConfig.targetMode == 1) targetStatus = "§eAuto Searching...";
            this.addDrawableChild(new EpicButton(rightStartX, 180, 290, 20, "Current Locked: " + targetStatus, b -> {}));
        }
        // ... (Combat and Radar sections purane code ki tarah hi rahenge)
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        context.fill(0, 0, sidebarWidth, this.height, 0x4D000000); 
        context.fill(sidebarWidth, 0, this.width, this.height, 0x33000000); 
        context.fill(sidebarWidth, 0, sidebarWidth + 1, this.height, 0x55FFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, "§b§lTPvP Client", sidebarWidth / 2, 20, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer, "§lCategory: §e" + currentTab, sidebarWidth + 30, 20, 0xFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }
}
