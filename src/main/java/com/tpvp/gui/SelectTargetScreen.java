package com.tpvp.gui;

import com.tpvp.config.ModConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

public class SelectTargetScreen extends Screen {
    private final Screen parent;
    private int page = 0;

    public SelectTargetScreen(Screen parent) {
        super(Text.literal("Select Target Player"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.clearChildren();
        
        // Back Button
        this.addDrawableChild(new ButtonWidget(this.width / 2 - 50, this.height - 30, 100, 20, Text.literal("Done"), button -> {
            this.client.setScreen(parent);
        }, ButtonWidget.DEFAULT_NARRATION_SUPPLIER));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, "§l🎯 Tag a Player for Target Lock", this.width / 2, 20, 0xFFFFFF);

        if (this.client.world == null) return;

        List<AbstractClientPlayerEntity> players = this.client.world.getPlayers();
        
        int startX = this.width / 2 - 150;
        int startY = 50;
        int count = 0;

        for (AbstractClientPlayerEntity player : players) {
            if (player == this.client.player) continue; // Khud ko ignore karo

            String pName = player.getName().getString();
            boolean isTagged = ModConfig.taggedPlayerName.equals(pName);

            // Card Background
            int cardColor = isTagged ? 0x6600FF00 : 0x4D000000; // Green agar selected hai
            context.fill(startX, startY, startX + 300, startY + 50, cardColor);
            context.drawBorder(startX, startY, 300, 50, isTagged ? 0xFF00FF00 : 0x55FFFFFF);

            // Epic Logic: Draw Full Body Flat Skin
            Identifier skin = player.getSkinTextures().texture();
            int bX = startX + 10;
            int bY = startY + 5;
            // Head (8x8 -> 16x16)
            context.drawTexture(RenderLayer::getGuiTextured, skin, bX + 8, bY, 8f, 8f, 16, 16, 64, 64);
            // Torso (8x12 -> 16x24)
            context.drawTexture(RenderLayer::getGuiTextured, skin, bX + 8, bY + 16, 20f, 20f, 16, 24, 64, 64);
            // Left Arm (4x12 -> 8x24)
            context.drawTexture(RenderLayer::getGuiTextured, skin, bX, bY + 16, 32f, 48f, 8, 24, 64, 64);
            // Right Arm (4x12 -> 8x24)
            context.drawTexture(RenderLayer::getGuiTextured, skin, bX + 24, bY + 16, 40f, 16f, 8, 24, 64, 64);
            // Left Leg (4x12 -> 8x24)
            context.drawTexture(RenderLayer::getGuiTextured, skin, bX + 8, bY + 40, 16f, 48f, 8, 24, 64, 64);
            // Right Leg (4x12 -> 8x24)
            context.drawTexture(RenderLayer::getGuiTextured, skin, bX + 16, bY + 40, 0f, 16f, 8, 24, 64, 64);

            // Text info
            context.drawTextWithShadow(this.textRenderer, "§l" + pName, startX + 50, startY + 15, 0xFFFFFF);
            context.drawTextWithShadow(this.textRenderer, "HP: §c" + (int)player.getHealth() + " ♥", startX + 50, startY + 30, 0xFFFFFF);

            // Select Checkbox button
            String btnText = isTagged ? "§a[ Locked ]" : "§7[ Tag ]";
            int btnX = startX + 220;
            int btnY = startY + 15;
            context.fill(btnX, btnY, btnX + 70, btnY + 20, 0x4D000000);
            context.drawBorder(btnX, btnY, 70, 20, 0x55FFFFFF);
            context.drawCenteredTextWithShadow(this.textRenderer, btnText, btnX + 35, btnY + 6, 0xFFFFFF);

            // Simple click detection for the card
            if (mouseX >= btnX && mouseX <= btnX + 70 && mouseY >= btnY && mouseY <= btnY + 20) {
                context.fill(btnX, btnY, btnX + 70, btnY + 20, 0x33FFFFFF); // Hover glow
            }

            startY += 55;
            count++;
            if (count >= 4) break; // Max 4 dikhayega page pe (Scroll baad me add kar sakte hain)
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && this.client.world != null) {
            int startX = this.width / 2 - 150;
            int startY = 50;
            int count = 0;
            for (AbstractClientPlayerEntity player : this.client.world.getPlayers()) {
                if (player == this.client.player) continue;
                int btnX = startX + 220;
                int btnY = startY + 15;
                if (mouseX >= btnX && mouseX <= btnX + 70 && mouseY >= btnY && mouseY <= btnY + 20) {
                    // Tag/Untag Logic
                    if (ModConfig.taggedPlayerName.equals(player.getName().getString())) {
                        ModConfig.taggedPlayerName = ""; // Untag
                    } else {
                        ModConfig.taggedPlayerName = player.getName().getString(); // Tag
                        ModConfig.targetMode = 0; // Auto Manual pe switch ho jayega
                    }
                    return true;
                }
                startY += 55;
                count++;
                if (count >= 4) break;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
