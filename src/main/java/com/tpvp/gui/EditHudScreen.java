package com.tpvp.gui;

import com.tpvp.config.ModConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class EditHudScreen extends Screen {
    private final Screen parent;
    private int draggingId = -1;
    private double dragOffsetX = 0;
    private double dragOffsetY = 0;

    public EditHudScreen(Screen parent) {
        super(Text.literal("Edit HUD"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        this.addDrawableChild(ButtonWidget.builder(Text.literal("§a✔ Save & Return"), button -> {
            this.close();
        }).dimensions(this.width / 2 - 50, this.height - 30, 100, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        
        // Premium Grid System
        int gridSize = 20;
        for (int i = 0; i < this.width; i += gridSize) context.fill(i, 0, i + 1, this.height, 0x1AFFFFFF);
        for (int i = 0; i < this.height; i += gridSize) context.fill(0, i, this.width, i + 1, 0x1AFFFFFF);

        context.drawCenteredTextWithShadow(this.textRenderer, "§eDrag elements to move. Scroll wheel to scale.", this.width / 2, 20, 0xFFFFFF);

        // 1. Held Item Mockup
        float itemScale = ModConfig.heldItemScale;
        int itemColor = (draggingId == 1 || getHoveredId(mouseX, mouseY) == 1) ? 0x6600FF00 : 0x66FFFFFF;
        context.fill(ModConfig.heldItemX, ModConfig.heldItemY, ModConfig.heldItemX + (int)(60 * itemScale), ModConfig.heldItemY + (int)(60 * itemScale), itemColor);
        context.getMatrices().push();
        context.getMatrices().translate(ModConfig.heldItemX, ModConfig.heldItemY, 0);
        context.getMatrices().scale(itemScale, itemScale, 1.0f);
        context.drawTextWithShadow(this.textRenderer, "Custom Box", 2, 2, 0xFFFFFF);
        context.getMatrices().pop();

        // 2. Armor HUD Mockup
        float armorScale = ModConfig.armorScale;
        int armorColor = (draggingId == 2 || getHoveredId(mouseX, mouseY) == 2) ? 0x6600FF00 : 0x66FFFFFF;
        context.fill(ModConfig.armorX, ModConfig.armorY, ModConfig.armorX + (int)(60 * armorScale), ModConfig.armorY + (int)(80 * armorScale), armorColor);
        context.getMatrices().push();
        context.getMatrices().translate(ModConfig.armorX, ModConfig.armorY, 0);
        context.getMatrices().scale(armorScale, armorScale, 1.0f);
        context.drawTextWithShadow(this.textRenderer, "Armor HUD", 2, 2, 0xFFFFFF);
        context.getMatrices().pop();

        // 3. Radar HUD Mockup
        float radarScale = ModConfig.radarScale;
        int radarColor = (draggingId == 3 || getHoveredId(mouseX, mouseY) == 3) ? 0x6600FF00 : 0x66FFFFFF;
        context.fill(ModConfig.radarX, ModConfig.radarY, ModConfig.radarX + (int)(130 * radarScale), ModConfig.radarY + (int)(80 * radarScale), radarColor);
        context.getMatrices().push();
        context.getMatrices().translate(ModConfig.radarX, ModConfig.radarY, 0);
        context.getMatrices().scale(radarScale, radarScale, 1.0f);
        context.drawTextWithShadow(this.textRenderer, "Radar HUD", 2, 2, 0xFFFFFF);
        context.getMatrices().pop();

        super.render(context, mouseX, mouseY, delta);
    }

    private int getHoveredId(double mx, double my) {
        if (mx >= ModConfig.heldItemX && mx <= ModConfig.heldItemX + (60 * ModConfig.heldItemScale) && my >= ModConfig.heldItemY && my <= ModConfig.heldItemY + (60 * ModConfig.heldItemScale)) return 1;
        if (mx >= ModConfig.armorX && mx <= ModConfig.armorX + (60 * ModConfig.armorScale) && my >= ModConfig.armorY && my <= ModConfig.armorY + (80 * ModConfig.armorScale)) return 2;
        if (mx >= ModConfig.radarX && mx <= ModConfig.radarX + (130 * ModConfig.radarScale) && my >= ModConfig.radarY && my <= ModConfig.radarY + (80 * ModConfig.radarScale)) return 3;
        return -1;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        draggingId = getHoveredId(mx, my);
        if (draggingId == 1) { dragOffsetX = mx - ModConfig.heldItemX; dragOffsetY = my - ModConfig.heldItemY; return true; }
        if (draggingId == 2) { dragOffsetX = mx - ModConfig.armorX; dragOffsetY = my - ModConfig.armorY; return true; }
        if (draggingId == 3) { dragOffsetX = mx - ModConfig.radarX; dragOffsetY = my - ModConfig.radarY; return true; }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dX, double dY) {
        if (draggingId == 1) { ModConfig.heldItemX = (int)(mx - dragOffsetX); ModConfig.heldItemY = (int)(my - dragOffsetY); return true; }
        if (draggingId == 2) { ModConfig.armorX = (int)(mx - dragOffsetX); ModConfig.armorY = (int)(my - dragOffsetY); return true; }
        if (draggingId == 3) { ModConfig.radarX = (int)(mx - dragOffsetX); ModConfig.radarY = (int)(my - dragOffsetY); return true; }
        return super.mouseDragged(mx, my, button, dX, dY);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        draggingId = -1; 
        ModConfig.save(); 
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double hAmount, double scroll) {
        int id = getHoveredId(mx, my);
        if (id == 1) ModConfig.heldItemScale = Math.max(0.5f, Math.min(2.0f, ModConfig.heldItemScale + (float)(scroll * 0.1)));
        if (id == 2) ModConfig.armorScale = Math.max(0.5f, Math.min(2.0f, ModConfig.armorScale + (float)(scroll * 0.1)));
        if (id == 3) ModConfig.radarScale = Math.max(0.5f, Math.min(2.0f, ModConfig.radarScale + (float)(scroll * 0.1)));
        return super.mouseScrolled(mx, my, hAmount, scroll);
    }

    @Override
    public void close() {
        ModConfig.save(); 
        if (this.client != null) this.client.setScreen(this.parent);
    }
}
