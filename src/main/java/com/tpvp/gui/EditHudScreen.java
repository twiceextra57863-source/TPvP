package com.tpvp.gui;

import com.tpvp.config.ModConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class EditHudScreen extends Screen {
    private final Screen parent;
    
    // Dragging Logic Trackers
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
        
        // Save & Return Button (Bottom Center)
        this.addDrawableChild(ButtonWidget.builder(Text.literal("§a✔ Save & Return"), button -> {
            this.close();
        }).dimensions(this.width / 2 - 50, this.height - 30, 100, 20).build());

        // Reset to Defaults Button (Optional Quality of Life Feature)
        this.addDrawableChild(ButtonWidget.builder(Text.literal("§c✖ Reset"), button -> {
            ModConfig.armorX = 20; ModConfig.armorY = 100; ModConfig.armorScale = 1.0f;
            ModConfig.radarX = 10; ModConfig.radarY = 30; ModConfig.radarScale = 1.0f;
            ModConfig.heldItemX = 50; ModConfig.heldItemY = 50; ModConfig.heldItemScale = 1.0f;
            ModConfig.killFeedX = 10; ModConfig.killFeedY = 50;
            ModConfig.save();
        }).dimensions(this.width / 2 + 55, this.height - 30, 60, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // 1. Dark Blur Background
        this.renderBackground(context, mouseX, mouseY, delta);
        
        // 2. PREMIUM GRID SYSTEM (For easy alignment)
        int gridSize = 20;
        for (int i = 0; i < this.width; i += gridSize) {
            context.fill(i, 0, i + 1, this.height, 0x1AFFFFFF); // Vertical Lines
        }
        for (int i = 0; i < this.height; i += gridSize) {
            context.fill(0, i, this.width, i + 1, 0x1AFFFFFF); // Horizontal Lines
        }

        // Top Instructions
        context.fill(0, 0, this.width, 35, 0xAA000000); // Top Bar Background
        context.drawCenteredTextWithShadow(this.textRenderer, "§b§lHUD EDITOR MODE", this.width / 2, 8, 0xFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, "§eDrag elements to move. Scroll wheel to scale.", this.width / 2, 20, 0xAAAAAA);

        // ---------------------------------------------------------
        // 1. RENDER ARMOR HUD MOCKUP
        // ---------------------------------------------------------
        float aS = ModConfig.armorScale;
        int armorColor = (draggingId == 1 || getHoveredId(mouseX, mouseY) == 1) ? 0x6600FFCC : 0x66FFFFFF; // Neon Cyan on hover
        context.fill(ModConfig.armorX, ModConfig.armorY, ModConfig.armorX + (int)(60 * aS), ModConfig.armorY + (int)(80 * aS), armorColor);
        
        context.getMatrices().push();
        context.getMatrices().translate(ModConfig.armorX, ModConfig.armorY, 0);
        context.getMatrices().scale(aS, aS, 1.0f);
        context.drawTextWithShadow(this.textRenderer, "Armor HUD", 2, 2, 0xFFFFFF);
        context.getMatrices().pop();

        // ---------------------------------------------------------
        // 2. RENDER RADAR HUD MOCKUP
        // ---------------------------------------------------------
        float rS = ModConfig.radarScale;
        int radarColor = (draggingId == 2 || getHoveredId(mouseX, mouseY) == 2) ? 0x6600FFCC : 0x66FFFFFF;
        context.fill(ModConfig.radarX, ModConfig.radarY, ModConfig.radarX + (int)(130 * rS), ModConfig.radarY + (int)(80 * rS), radarColor);
        
        context.getMatrices().push();
        context.getMatrices().translate(ModConfig.radarX, ModConfig.radarY, 0);
        context.getMatrices().scale(rS, rS, 1.0f);
        context.drawTextWithShadow(this.textRenderer, "Radar HUD", 2, 2, 0xFFFFFF);
        context.getMatrices().pop();

        // ---------------------------------------------------------
        // 3. RENDER CUSTOM ITEM BOX MOCKUP
        // ---------------------------------------------------------
        float iS = ModConfig.heldItemScale;
        int itemColor = (draggingId == 4 || getHoveredId(mouseX, mouseY) == 4) ? 0x6600FFCC : 0x66FFFFFF;
        context.fill(ModConfig.heldItemX, ModConfig.heldItemY, ModConfig.heldItemX + (int)(60 * iS), ModConfig.heldItemY + (int)(60 * iS), itemColor);
        
        context.getMatrices().push();
        context.getMatrices().translate(ModConfig.heldItemX, ModConfig.heldItemY, 0);
        context.getMatrices().scale(iS, iS, 1.0f);
        context.drawTextWithShadow(this.textRenderer, "Item Box", 2, 2, 0xFFFFFF);
        context.getMatrices().pop();

        // ---------------------------------------------------------
        // 4. RENDER KILL NOTIFICATION FEED MOCKUP
        // ---------------------------------------------------------
        int feedColor = (draggingId == 3 || getHoveredId(mouseX, mouseY) == 3) ? 0x6600FFCC : 0x66FFFFFF;
        int feedW = 160; // Banner is approx 160 pixels wide
        int feedH = 34;  // Banner is approx 34 pixels tall
        
        // Auto direction hint text
        String dirHint = (ModConfig.killFeedX > this.width / 2) ? "Slides from Right ←" : "Slides from Left →";

        context.fill(ModConfig.killFeedX, ModConfig.killFeedY, ModConfig.killFeedX + feedW, ModConfig.killFeedY + feedH, feedColor);
        context.drawTextWithShadow(this.textRenderer, "Kill Feed Area", ModConfig.killFeedX + 5, ModConfig.killFeedY + 5, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer, "§7" + dirHint, ModConfig.killFeedX + 5, ModConfig.killFeedY + 18, 0xAAAAAA);

        super.render(context, mouseX, mouseY, delta);
    }

    // Helper: Find which element is under the cursor
    private int getHoveredId(double mx, double my) {
        if (mx >= ModConfig.armorX && mx <= ModConfig.armorX + (60 * ModConfig.armorScale) && my >= ModConfig.armorY && my <= ModConfig.armorY + (80 * ModConfig.armorScale)) return 1;
        if (mx >= ModConfig.radarX && mx <= ModConfig.radarX + (130 * ModConfig.radarScale) && my >= ModConfig.radarY && my <= ModConfig.radarY + (80 * ModConfig.radarScale)) return 2;
        if (mx >= ModConfig.killFeedX && mx <= ModConfig.killFeedX + 160 && my >= ModConfig.killFeedY && my <= ModConfig.killFeedY + 34) return 3; // Kill Feed
        if (mx >= ModConfig.heldItemX && mx <= ModConfig.heldItemX + (60 * ModConfig.heldItemScale) && my >= ModConfig.heldItemY && my <= ModConfig.heldItemY + (60 * ModConfig.heldItemScale)) return 4;
        return -1;
    }

    // --- DRAG CONTROLS ---

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        draggingId = getHoveredId(mx, my);
        
        if (draggingId == 1) { dragOffsetX = mx - ModConfig.armorX; dragOffsetY = my - ModConfig.armorY; return true; }
        if (draggingId == 2) { dragOffsetX = mx - ModConfig.radarX; dragOffsetY = my - ModConfig.radarY; return true; }
        if (draggingId == 3) { dragOffsetX = mx - ModConfig.killFeedX; dragOffsetY = my - ModConfig.killFeedY; return true; }
        if (draggingId == 4) { dragOffsetX = mx - ModConfig.heldItemX; dragOffsetY = my - ModConfig.heldItemY; return true; }
        
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dX, double dY) {
        if (draggingId == 1) { 
            ModConfig.armorX = Math.max(0, Math.min((int)(mx - dragOffsetX), this.width - (int)(60 * ModConfig.armorScale))); 
            ModConfig.armorY = Math.max(0, Math.min((int)(my - dragOffsetY), this.height - (int)(80 * ModConfig.armorScale))); 
            return true; 
        }
        if (draggingId == 2) { 
            ModConfig.radarX = Math.max(0, Math.min((int)(mx - dragOffsetX), this.width - (int)(130 * ModConfig.radarScale))); 
            ModConfig.radarY = Math.max(0, Math.min((int)(my - dragOffsetY), this.height - (int)(80 * ModConfig.radarScale))); 
            return true; 
        }
        if (draggingId == 3) { // Kill Feed Bounding Box
            ModConfig.killFeedX = Math.max(0, Math.min((int)(mx - dragOffsetX), this.width - 160)); 
            ModConfig.killFeedY = Math.max(0, Math.min((int)(my - dragOffsetY), this.height - 34)); 
            return true; 
        }
        if (draggingId == 4) { 
            ModConfig.heldItemX = Math.max(0, Math.min((int)(mx - dragOffsetX), this.width - (int)(60 * ModConfig.heldItemScale))); 
            ModConfig.heldItemY = Math.max(0, Math.min((int)(my - dragOffsetY), this.height - (int)(60 * ModConfig.heldItemScale))); 
            return true; 
        }
        
        return super.mouseDragged(mx, my, button, dX, dY);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        if (draggingId != -1) {
            draggingId = -1; 
            ModConfig.save(); // Save coordinates when drag stops
            return true;
        }
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double horizontalAmount, double scroll) {
        int id = getHoveredId(mx, my);
        
        // Scale elements by scrolling mouse wheel (Kill feed scale is fixed so it's not affected)
        if (id == 1) ModConfig.armorScale = Math.max(0.5f, Math.min(2.0f, ModConfig.armorScale + (float)(scroll * 0.1)));
        if (id == 2) ModConfig.radarScale = Math.max(0.5f, Math.min(2.0f, ModConfig.radarScale + (float)(scroll * 0.1)));
        if (id == 4) ModConfig.heldItemScale = Math.max(0.5f, Math.min(2.0f, ModConfig.heldItemScale + (float)(scroll * 0.1)));
        
        return super.mouseScrolled(mx, my, horizontalAmount, scroll);
    }

    @Override
    public void close() {
        ModConfig.save(); 
        if (this.client != null) {
            this.client.setScreen(this.parent); // Return to Dashboard Menu
        }
    }
}
