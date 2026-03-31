package com.tpvp.gui;

import com.tpvp.config.ModConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class EditHudScreen extends Screen {
    private final Screen parent;
    private boolean dragging = false;
    private double dragOffsetX = 0;
    private double dragOffsetY = 0;

    // Dummy dimension logic based on NearbyPlayersHud
    private final int hudBaseWidth = 130;
    private final int hudBaseHeight = 14 + (5 * 16);

    public EditHudScreen(Screen parent) {
        super(Text.literal("Edit HUD Position"));
        this.parent = parent;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);

        // Instructions text
        context.drawCenteredTextWithShadow(this.textRenderer, "§e§lEdit Radar HUD", this.width / 2, 20, 0xFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, "§7Click & Drag to move • Scroll to resize • Press ESC to save", this.width / 2, 35, 0xAAAAAA);

        // Draw HUD inside matrix
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(ModConfig.hudX, ModConfig.hudY, 0);
        matrices.scale(ModConfig.hudScale, ModConfig.hudScale, 1f);

        // Bounding Box (Visualizer)
        int scaledWidth = hudBaseWidth;
        int scaledHeight = hudBaseHeight;
        context.fill(0, 0, scaledWidth, scaledHeight, 0x55FFFFFF); // Transparent White Box
        context.drawBorder(0, 0, scaledWidth, scaledHeight, 0xFF00FF00); // Green Border

        // Dummy text inside HUD to show what it looks like
        context.drawTextWithShadow(this.textRenderer, "§l📡 Nearby Players", 0, 0, 0xFFAAFF);
        context.drawTextWithShadow(this.textRenderer, "§e▶ §fPlayer_1 §7(12m) §a↑", 5, 15, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer, "§e◀ §fPlayer_2 §7(25m) §7-", 5, 31, 0xFFFFFF);

        matrices.pop();

        super.render(context, mouseX, mouseY, delta);
    }

    private boolean isMouseOverHud(double mouseX, double mouseY) {
        double scaledW = hudBaseWidth * ModConfig.hudScale;
        double scaledH = hudBaseHeight * ModConfig.hudScale;
        return mouseX >= ModConfig.hudX && mouseX <= ModConfig.hudX + scaledW &&
               mouseY >= ModConfig.hudY && mouseY <= ModConfig.hudY + scaledH;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && isMouseOverHud(mouseX, mouseY)) {
            dragging = true;
            dragOffsetX = mouseX - ModConfig.hudX;
            dragOffsetY = mouseY - ModConfig.hudY;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging) {
            ModConfig.hudX = (int) (mouseX - dragOffsetX);
            ModConfig.hudY = (int) (mouseY - dragOffsetY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            dragging = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    // 1.21.2+ Mouse Scroll Method (4 parameters)
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (isMouseOverHud(mouseX, mouseY)) {
            if (verticalAmount > 0) ModConfig.hudScale += 0.05f; // Scroll Up = Zoom In
            else if (verticalAmount < 0) ModConfig.hudScale -= 0.05f; // Scroll Down = Zoom Out
            
            // Limit scale between 0.5x and 2.0x
            ModConfig.hudScale = Math.max(0.5f, Math.min(2.0f, ModConfig.hudScale));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void close() {
        this.client.setScreen(parent); // Wapas dashboard pe le aao
    }
}
