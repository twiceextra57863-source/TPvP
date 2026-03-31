package com.tpvp.gui;

import com.tpvp.config.ModConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.math.MatrixStack;
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

            // Card Background
            int cardColor = isTagged ? 0x6600FF00 : 0x4D000000; 
            context.fill(startX, startY, startX + 300, startY + 50, cardColor);
            context.drawBorder(startX, startY, 300, 50, isTagged ? 0xFF00FF00 : 0x55FFFFFF);

            // --- EPIC 64x64 FULL BODY SKIN RENDERER ---
            Identifier skin = player.getSkinTextures().texture();
            MatrixStack matrices = context.getMatrices();
            matrices.push();
            
            // Card ke andar positioning aur size scaling
            matrices.translate(startX + 10, startY + 2, 0);
            matrices.scale(1.4f, 1.4f, 1.0f); // Perfect size for 50px card

            // LAYER 1: BASE SKIN (Body)
            // Head
            context.drawTexture(RenderLayer::getGuiTextured, skin, 4, 0, 8f, 8f, 8, 8, 64, 64);
            // Torso
            context.drawTexture(RenderLayer::getGuiTextured, skin, 4, 8, 20f, 20f, 8, 12, 64, 64);
            // Right Arm (Screen left)
            context.drawTexture(RenderLayer::getGuiTextured, skin, 0, 8, 44f, 20f, 4, 12, 64, 64);
            // Left Arm (Screen right)
            context.drawTexture(RenderLayer::getGuiTextured, skin, 12, 8, 36f, 52f, 4, 12, 64, 64);
            // Right Leg
            context.drawTexture(RenderLayer::getGuiTextured, skin, 4, 20, 4f, 20f, 4, 12, 64, 64);
            // Left Leg
            context.drawTexture(RenderLayer::getGuiTextured, skin, 8, 20, 20f, 52f, 4, 12, 64, 64);

            // LAYER 2: OVERLAY SKIN (Clothes/Hat/Jacket) - 3D Skin depth feel
            // Hat
            context.drawTexture(RenderLayer::getGuiTextured, skin, 4, 0, 40f, 8f, 8, 8, 64, 64);
            // Jacket
            context.drawTexture(RenderLayer::getGuiTextured, skin, 4, 8, 20f, 36f, 8, 12, 64, 64);
            // Right Sleeve
            context.drawTexture(RenderLayer::getGuiTextured, skin, 0, 8, 44f, 36f, 4, 12, 64, 64);
            // Left Sleeve
            context.drawTexture(RenderLayer::getGuiTextured, skin, 12, 8, 52f, 52f, 4, 12, 64, 64);
            // Right Pants
            context.drawTexture(RenderLayer::getGuiTextured, skin, 4, 20, 4f, 36f, 4, 12, 64, 64);
            // Left Pants
            context.drawTexture(RenderLayer::getGuiTextured, skin, 8, 20, 4f, 52f, 4, 12, 64, 64);

            matrices.pop();
            // ------------------------------------------

            context.drawTextWithShadow(this.textRenderer, "§l" + pName, startX + 50, startY + 15, 0xFFFFFF);
            context.drawTextWithShadow(this.textRenderer, "HP: §c" + String.format("%.1f ♥", player.getHealth()), startX + 50, startY + 30, 0xFFFFFF);

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
