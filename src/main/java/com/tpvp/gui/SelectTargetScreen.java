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

    public SelectTargetScreen(Screen parent) {
        super(Text.literal("Select Target Player"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.clearChildren();
        
        // --- COMPILATION BUG FIX: Use ButtonWidget.builder for 1.21.2+ ---
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Done"), button -> {
            this.client.setScreen(parent);
        }).dimensions(this.width / 2 - 50, this.height - 30, 100, 20).build());
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
            if (player == this.client.player) continue;

            String pName = player.getName().getString();
            boolean isTagged = ModConfig.taggedPlayerName.equals(pName);

            int cardColor = isTagged ? 0x6600FF00 : 0x4D000000; 
            context.fill(startX, startY, startX + 300, startY + 50, cardColor);
            context.drawBorder(startX, startY, 300, 50, isTagged ? 0xFF00FF00 : 0x55FFFFFF);

            // Full Body 2D Skin Render
            Identifier skin = player.getSkinTextures().texture();
            int bX = startX + 10;
            int bY = startY + 5;
            context.drawTexture(RenderLayer::getGuiTextured, skin, bX + 8, bY, 8f, 8f, 16, 16, 64, 64);
            context.drawTexture(RenderLayer::getGuiTextured, skin, bX + 8, bY + 16, 20f, 20f, 16, 24, 64, 64);
            context.drawTexture(RenderLayer::getGuiTextured, skin, bX, bY + 16, 32f, 48f, 8, 24, 64, 64);
            context.drawTexture(RenderLayer::getGuiTextured, skin, bX + 24, bY + 16, 40f, 16f, 8, 24, 64, 64);
            context.drawTexture(RenderLayer::getGuiTextured, skin, bX + 8, bY + 40, 16f, 48f, 8, 24, 64, 64);
            context.drawTexture(RenderLayer::getGuiTextured, skin, bX + 16, bY + 40, 0f, 16f, 8, 24, 64, 64);

            context.drawTextWithShadow(this.textRenderer, "§l" + pName, startX + 50, startY + 15, 0xFFFFFF);
            context.drawTextWithShadow(this.textRenderer, "HP: §c" + (int)player.getHealth() + " ♥", startX + 50, startY + 30, 0xFFFFFF);

            String btnText = isTagged ? "§a[ Locked ]" : "§7[ Tag ]";
            int btnX = startX + 220;
            int btnY = startY + 15;
            context.fill(btnX, btnY, btnX + 70, btnY + 20, 0x4D000000);
            context.drawBorder(btnX, btnY, 70, 20, 0x55FFFFFF);
            context.drawCenteredTextWithShadow(this.textRenderer, btnText, btnX + 35, btnY + 6, 0xFFFFFF);

            if (mouseX >= btnX && mouseX <= btnX + 70 && mouseY >= btnY && mouseY <= btnY + 20) {
                context.fill(btnX, btnY, btnX + 70, btnY + 20, 0x33FFFFFF); 
            }

            startY += 55;
            count++;
            if (count >= 4) break; 
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
                    if (ModConfig.taggedPlayerName.equals(player.getName().getString())) {
                        ModConfig.taggedPlayerName = ""; 
                    } else {
                        ModConfig.taggedPlayerName = player.getName().getString(); 
                        ModConfig.targetMode = 0; 
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
