package com.mtpvp.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import java.util.ArrayList;
import java.util.List;

public class TargetSelectorScreen extends Screen {
    public TargetSelectorScreen() { 
        super(Text.of("Target Menu")); 
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        int x = width / 2 - 100, y = 40;
        
        context.drawCenteredTextWithShadow(textRenderer, "§6§lGANG WAR TARGET SELECTOR", width/2, 15, 0xFFFFFF);
        
        List<PlayerListEntry> players = new ArrayList<>(MinecraftClient.getInstance().getNetworkHandler().getPlayerList());
        for (PlayerListEntry p : players) {
            String name = p.getProfile().getName();
            boolean isSel = name.equals(MtpvpDashboard.targetPlayerName);
            
            // Selection Background
            int bgColor = isSel ? 0x88FF0000 : 0x44000000;
            context.fill(x - 5, y - 2, x + 150, y + 12, bgColor);
            
            // --- FIX FOR 1.21.4 drawTexture Error ---
            // Yahan hum RenderLayer.getGuiTextured use karenge taaki Identifier compatibility error na aaye
            context.drawTexture(RenderLayer::getGuiTextured, p.getSkinTextures().texture(), x, y, 8, 8, 8, 8, 64, 64);
            
            // Player Name Display
            context.drawTextWithShadow(textRenderer, name, x + 15, y, isSel ? 0xFF5555 : 0xFFFFFF);
            
            y += 15;
            if (y > height - 40) {
                context.drawTextWithShadow(textRenderer, "...and more", x + 15, y, 0xAAAAAA);
                break; 
            }
        }
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = width / 2 - 100, y = 40;
        List<PlayerListEntry> players = new ArrayList<>(MinecraftClient.getInstance().getNetworkHandler().getPlayerList());
        for (PlayerListEntry p : players) {
            if (mouseX >= x && mouseX <= x + 150 && mouseY >= y && mouseY <= y + 12) {
                MtpvpDashboard.targetPlayerName = p.getProfile().getName();
                // Target set hone par screen close kar do taaki PvP turant start ho sake
                this.client.setScreen(null); 
                return true;
            }
            y += 15;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void close() {
        // Dashboard par wapis bhejo agar user escape dabaye
        this.client.setScreen(new MtpvpDashboard(null));
    }
}
