package com.tpvp.gui;

import com.tpvp.config.ModConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class EditHudScreen extends Screen {
    
    // Parent screen ko save karne ke liye (Ye tumhara error fix karega)
    private final Screen parent;
    
    // Dragging logic ke variables
    private int draggingId = -1;
    private double dragOffsetX = 0;
    private double dragOffsetY = 0;

    // Modified Constructor (Ab ye parent screen accept karega)
    public EditHudScreen(Screen parent) {
        super(Text.literal("Edit HUD Elements"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        // Bottom Center me 'Save & Exit' Button
        this.addDrawableChild(ButtonWidget.builder(Text.literal("§a✔ Save & Return"), button -> {
            this.close();
        }).dimensions(this.width / 2 - 105, this.height - 30, 100, 20).build());

        // Bottom Center me 'Reset Defaults' Button
        this.addDrawableChild(ButtonWidget.builder(Text.literal("§c✖ Reset Defaults"), button -> {
            ModConfig.heldItemX = 50;
            ModConfig.heldItemY = 50;
            ModConfig.heldItemScale = 1.0f;
            ModConfig.save();
        }).dimensions(this.width / 2 + 5, this.height - 30, 100, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // 1. Dark transparent background
        this.renderBackground(context, mouseX, mouseY, delta);

        // 2. Pro Grid Lines for alignment (Premium feel)
        int gridSize = 20;
        for (int i = 0; i < this.width; i += gridSize) {
            context.fill(i, 0, i + 1, this.height, 0x1AFFFFFF); // Vertical lines
        }
        for (int i = 0; i < this.height; i += gridSize) {
            context.fill(0, i, this.width, i + 1, 0x1AFFFFFF); // Horizontal lines
        }

        // 3. Top Instructions Text
        context.fill(0, 0, this.width, 40, 0x66000000); // Top bar background
        context.drawCenteredTextWithShadow(this.textRenderer, "§lHUD Editor Mode", this.width / 2, 10, 0xFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, "§eClick & Drag to move elements. Scroll wheel to scale them.", this.width / 2, 25, 0xAAAAAA);

        // 4. Render the Mock HUD Element (Held Item / Stats Box)
        float scale = ModConfig.heldItemScale;
        int boxWidth = (int) (60 * scale);
        int boxHeight = (int) (60 * scale);
        int x = ModConfig.heldItemX;
        int y = ModConfig.heldItemY;

        // Background Box with borders based on hover state
        int borderColor = (draggingId == 1 || getHoveredId(mouseX, mouseY) == 1) ? 0xFF00FF00 : 0xFFFFFFFF; // Green border if hovering/dragging
        context.fill(x - 1, y - 1, x + boxWidth + 1, y + boxHeight + 1, borderColor);
        context.fill(x, y, x + boxWidth, y + boxHeight, 0x99000000); // Inner Dark Box

        // Mock Data inside the box
        context.getMatrices().push();
        context.getMatrices().translate(x + 5 * scale, y + 5 * scale, 0);
        context.getMatrices().scale(scale, scale, 1.0f);
        context.drawTextWithShadow(this.textRenderer, "§b§lITEM", 0, 0, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer, "Scale: " + String.format("%.1fx", scale), 0, 15, 0xAAAAAA);
        context.drawTextWithShadow(this.textRenderer, "X: " + x, 0, 30, 0xAAAAAA);
        context.drawTextWithShadow(this.textRenderer, "Y: " + y, 0, 40, 0xAAAAAA);
        context.getMatrices().pop();

        super.render(context, mouseX, mouseY, delta);
    }

    // Helper method to check which element the mouse is hovering over
    private int getHoveredId(double mx, double my) {
        float scale = ModConfig.heldItemScale;
        int w = (int) (60 * scale);
        int h = (int) (60 * scale);
        int x = ModConfig.heldItemX;
        int y = ModConfig.heldItemY;

        if (mx >= x && mx <= x + w && my >= y && my <= y + h) {
            return 1; // ID 1 = Our Held Item Box
        }
        return -1; // Nothing hovered
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        draggingId = getHoveredId(mouseX, mouseY);
        
        if (draggingId == 1) {
            dragOffsetX = mouseX - ModConfig.heldItemX;
            dragOffsetY = mouseY - ModConfig.heldItemY;
            return true;
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (draggingId == 1) {
            // Screen ke bahar jane se rokne ke liye bounds check
            int newX = (int) (mouseX - dragOffsetX);
            int newY = (int) (mouseY - dragOffsetY);
            
            // Limit to screen edges
            ModConfig.heldItemX = Math.max(0, Math.min(newX, this.width - (int)(60 * ModConfig.heldItemScale)));
            ModConfig.heldItemY = Math.max(0, Math.min(newY, this.height - (int)(60 * ModConfig.heldItemScale)));
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (draggingId != -1) {
            draggingId = -1;
            ModConfig.save(); // Save configuration on release
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double scroll) {
        int hovered = getHoveredId(mouseX, mouseY);
        if (hovered == 1) {
            // Scroll to scale (Min 0.5x, Max 3.0x)
            float newScale = ModConfig.heldItemScale + (float) (scroll * 0.1);
            ModConfig.heldItemScale = Math.max(0.5f, Math.min(3.0f, newScale));
            ModConfig.save();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, scroll);
    }

    // Escape dabane par pichle screen par wapas jane ka system
    @Override
    public void close() {
        ModConfig.save();
        if (this.client != null) {
            this.client.setScreen(this.parent); // Purane menu ko open kar dega
        }
    }
}
