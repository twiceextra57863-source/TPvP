package com.tpvp.gui;

import com.tpvp.config.ModConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class EditHudScreen extends Screen {
    private final Screen parent;
    private int draggingId = -1; // 0=Radar, 1=Armor, 2=HeldItem
    private double dragOffsetX = 0, dragOffsetY = 0;

    public EditHudScreen(Screen parent) {
        super(Text.literal("Edit HUD Positions"));
        this.parent = parent;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, "§b§lUI LAYOUT EDITOR", this.width / 2, 20, 0xFFFFFF);

        // 0. Radar
        float rS = ModConfig.hudScale;
        context.fill(ModConfig.hudX, ModConfig.hudY, ModConfig.hudX + (int)(130*rS), ModConfig.hudY + (int)(80*rS), 0x4D00AAFF);
        context.drawTextWithShadow(this.textRenderer, "RADAR", ModConfig.hudX, ModConfig.hudY, 0xFFFFFF);

        // 1. Armor
        float aS = ModConfig.armorHudScale;
        int aW = (int)((ModConfig.armorHudHorizontal ? 160 : 40) * aS);
        int aH = (int)((ModConfig.armorHudHorizontal ? 30 : 90) * aS);
        context.fill(ModConfig.armorHudX, ModConfig.armorHudY, ModConfig.armorHudX + aW, ModConfig.armorHudY + aH, 0x4D00FF00);
        context.drawTextWithShadow(this.textRenderer, "ARMOR", ModConfig.armorHudX, ModConfig.armorHudY, 0xFFFFFF);

        // 2. Held Item
        float hS = ModConfig.heldItemScale;
        context.fill(ModConfig.heldItemX, ModConfig.heldItemY, ModConfig.heldItemX + (int)(30*hS), ModConfig.heldItemY + (int)(30*hS), 0x4DFF00FF);
        context.drawTextWithShadow(this.textRenderer, "ITEM", ModConfig.heldItemX, ModConfig.heldItemY, 0xFFFFFF);

        super.render(context, mouseX, mouseY, delta);
    }

    private int getHoveredHud(double mx, double my) {
        float hS = ModConfig.heldItemScale;
        if (mx >= ModConfig.heldItemX && mx <= ModConfig.heldItemX + (30*hS) && my >= ModConfig.heldItemY && my <= ModConfig.heldItemY + (30*hS)) return 2;
        
        float aS = ModConfig.armorHudScale;
        int aW = (int)((ModConfig.armorHudHorizontal ? 160 : 40) * aS);
        int aH = (int)((ModConfig.armorHudHorizontal ? 30 : 90) * aS);
        if (mx >= ModConfig.armorHudX && mx <= ModConfig.armorHudX + aW && my >= ModConfig.armorHudY && my <= ModConfig.armorHudY + aH) return 1;
        
        float rS = ModConfig.hudScale;
        if (mx >= ModConfig.hudX && mx <= ModConfig.hudX + (130*rS) && my >= ModConfig.hudY && my <= ModConfig.hudY + (80*rS)) return 0;
        return -1;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            draggingId = getHoveredHud(mx, my);
            if (draggingId == 0) { dragOffsetX = mx - ModConfig.hudX; dragOffsetY = my - ModConfig.hudY; return true; }
            if (draggingId == 1) { dragOffsetX = mx - ModConfig.armorHudX; dragOffsetY = my - ModConfig.armorHudY; return true; }
            if (draggingId == 2) { dragOffsetX = mx - ModConfig.heldItemX; dragOffsetY = my - ModConfig.heldItemY; return true; }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (draggingId == 0) { ModConfig.hudX = (int)(mx - dragOffsetX); ModConfig.hudY = (int)(my - dragOffsetY); return true; }
        if (draggingId == 1) { ModConfig.armorHudX = (int)(mx - dragOffsetX); ModConfig.armorHudY = (int)(my - dragOffsetY); return true; }
        if (draggingId == 2) { ModConfig.heldItemX = (int)(mx - dragOffsetX); ModConfig.heldItemY = (int)(my - dragOffsetY); return true; }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) { draggingId = -1; return super.mouseReleased(mx, my, button); }

    @Override
    public boolean mouseScrolled(double mx, double my, double hA, double vA) {
        int hovered = getHoveredHud(mx, my);
        float scroll = (vA > 0) ? 0.05f : -0.05f;
        if (hovered == 0) ModConfig.hudScale = Math.max(0.5f, Math.min(2f, ModConfig.hudScale + scroll));
        if (hovered == 1) ModConfig.armorHudScale = Math.max(0.5f, Math.min(2f, ModConfig.armorHudScale + scroll));
        if (hovered == 2) ModConfig.heldItemScale = Math.max(0.5f, Math.min(2f, ModConfig.heldItemScale + scroll));
        return true;
    }

    @Override
    public void close() { this.client.setScreen(parent); }
}
