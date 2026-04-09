package com.tpvp.gui;

import com.tpvp.config.ModConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class EditHudScreen extends Screen {
    private final Screen parent;
    private int draggingId = -1; // -1=None, 0=Radar, 1=Armor
    private double dragOffsetX = 0;
    private double dragOffsetY = 0;

    public EditHudScreen(Screen parent) {
        super(Text.literal("Edit HUD Positions"));
        this.parent = parent;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, "§b§lUI LAYOUT EDITOR", this.width / 2, 20, 0xFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, "§7Click & Drag • Scroll to Resize • ESC to Save", this.width / 2, 35, 0xAAAAAA);

        // Visualize Radar Bounds
        float rScale = ModConfig.hudScale;
        int rW = (int)(130 * rScale); int rH = (int)(80 * rScale);
        context.fill(ModConfig.hudX, ModConfig.hudY, ModConfig.hudX + rW, ModConfig.hudY + rH, 0x4D00AAFF);
        context.drawBorder(ModConfig.hudX, ModConfig.hudY, rW, rH, 0xFF00AAFF);
        context.drawTextWithShadow(this.textRenderer, "RADAR", ModConfig.hudX + 2, ModConfig.hudY + 2, 0xFFFFFF);

        // Visualize Armor HUD Bounds
        float aScale = ModConfig.armorHudScale;
        int aW = (int)((ModConfig.armorHudHorizontal ? 160 : 40) * aScale);
        int aH = (int)((ModConfig.armorHudHorizontal ? 30 : 80) * aScale);
        context.fill(ModConfig.armorHudX, ModConfig.armorHudY, ModConfig.armorHudX + aW, ModConfig.armorHudY + aH, 0x4D00FF00);
        context.drawBorder(ModConfig.armorHudX, ModConfig.armorHudY, aW, aH, 0xFF00FF00);
        context.drawTextWithShadow(this.textRenderer, "ARMOR", ModConfig.armorHudX + 2, ModConfig.armorHudY + 2, 0xFFFFFF);

        super.render(context, mouseX, mouseY, delta);
    }

    private int getHoveredHud(double mx, double my) {
        // Armor Check
        int aW = (int)((ModConfig.armorHudHorizontal ? 160 : 40) * ModConfig.armorHudScale);
        int aH = (int)((ModConfig.armorHudHorizontal ? 30 : 80) * ModConfig.armorHudScale);
        if (mx >= ModConfig.armorHudX && mx <= ModConfig.armorHudX + aW && my >= ModConfig.armorHudY && my <= ModConfig.armorHudY + aH) return 1;
        // Radar Check
        int rW = (int)(130 * ModConfig.hudScale); int rH = (int)(80 * ModConfig.hudScale);
        if (mx >= ModConfig.hudX && mx <= ModConfig.hudX + rW && my >= ModConfig.hudY && my <= ModConfig.hudY + rH) return 0;
        return -1;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            draggingId = getHoveredHud(mouseX, mouseY);
            if (draggingId == 0) {
                dragOffsetX = mouseX - ModConfig.hudX; dragOffsetY = mouseY - ModConfig.hudY; return true;
            } else if (draggingId == 1) {
                dragOffsetX = mouseX - ModConfig.armorHudX; dragOffsetY = mouseY - ModConfig.armorHudY; return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (draggingId == 0) {
            ModConfig.hudX = (int) (mouseX - dragOffsetX); ModConfig.hudY = (int) (mouseY - dragOffsetY); return true;
        } else if (draggingId == 1) {
            ModConfig.armorHudX = (int) (mouseX - dragOffsetX); ModConfig.armorHudY = (int) (mouseY - dragOffsetY); return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingId = -1; return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double hAmount, double vAmount) {
        int hovered = getHoveredHud(mouseX, mouseY);
        if (hovered == 0) {
            ModConfig.hudScale += (vAmount > 0) ? 0.05f : -0.05f;
            ModConfig.hudScale = Math.max(0.5f, Math.min(2.0f, ModConfig.hudScale)); return true;
        } else if (hovered == 1) {
            ModConfig.armorHudScale += (vAmount > 0) ? 0.05f : -0.05f;
            ModConfig.armorHudScale = Math.max(0.5f, Math.min(2.0f, ModConfig.armorHudScale)); return true;
        }
        return super.mouseScrolled(mouseX, mouseY, hAmount, vAmount);
    }

    @Override
    public void close() { this.client.setScreen(parent); }
}
