package com.tpvp.gui;

import com.tpvp.config.ModConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class EditHudScreen extends Screen {
    private int draggingId = -1;
    private double dragOffsetX = 0;
    private double dragOffsetY = 0;

    public EditHudScreen() {
        super(Text.literal("Edit HUD"));
    }

    @Override
    public void render(DrawContext context, int mx, int my, float delta) {
        this.renderBackground(context, mx, my, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, "§eDrag HUD elements to move. Scroll to scale.", this.width / 2, 20, 0xFFFFFF);

        float hS = ModConfig.heldItemScale;
        context.fill(ModConfig.heldItemX, ModConfig.heldItemY, ModConfig.heldItemX + (int)(30 * hS), ModConfig.heldItemY + (int)(30 * hS), 0x4DFF00FF);
        context.drawTextWithShadow(this.textRenderer, "ITEM", ModConfig.heldItemX + 2, ModConfig.heldItemY + 2, 0xFFFFFF);

        super.render(context, mx, my, delta);
    }

    private int getHovered(double mx, double my) {
        float hS = ModConfig.heldItemScale;
        if (mx >= ModConfig.heldItemX && mx <= ModConfig.heldItemX + (30 * hS) && my >= ModConfig.heldItemY && my <= ModConfig.heldItemY + (30 * hS)) return 2;
        return -1;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        draggingId = getHovered(mx, my);
        if (draggingId == 2) {
            dragOffsetX = mx - ModConfig.heldItemX;
            dragOffsetY = my - ModConfig.heldItemY;
            return true;
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double deltaX, double deltaY) {
        if (draggingId == 2) {
            ModConfig.heldItemX = (int) (mx - dragOffsetX);
            ModConfig.heldItemY = (int) (my - dragOffsetY);
            return true;
        }
        return super.mouseDragged(mx, my, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingId = -1;
        ModConfig.save();
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double scroll) {
        int hovered = getHovered(mouseX, mouseY);
        if (hovered == 2) {
            ModConfig.heldItemScale = Math.max(0.5f, Math.min(2.0f, ModConfig.heldItemScale + (float)(scroll * 0.1)));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, scroll);
    }
}
