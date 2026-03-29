package com.mtpvp.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import java.util.ArrayList;
import java.util.List;

public class TargetSelectorScreen extends Screen {
    public TargetSelectorScreen() { super(Text.of("Target Menu")); }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        int x = width / 2 - 100, y = 40;
        
        context.drawCenteredTextWithShadow(textRenderer, "§6§lSELECT TARGET PLAYER", width/2, 15, 0xFFFFFF);
        
        List<PlayerListEntry> players = new ArrayList<>(MinecraftClient.getInstance().getNetworkHandler().getPlayerList());
        for (PlayerListEntry p : players) {
            String name = p.getProfile().getName();
            boolean isSel = name.equals(MtpvpDashboard.targetPlayerName);
            
            // Draw Selection Box
            int color = isSel ? 0x6655FF55 : 0x33000000;
            context.fill(x - 5, y - 2, x + 150, y + 12, color);
            
            // Draw Head & Name
            context.drawTexture(p.getSkinTextures().texture(), x, y, 8, 8, 8, 8, 8, 8, 64, 64);
            context.drawTextWithShadow(textRenderer, name, x + 15, y, isSel ? 0x55FF55 : 0xFFFFFF);
            
            y += 15;
            if (y > height - 30) break; 
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
                MinecraftClient.getInstance().setScreen(null);
                return true;
            }
            y += 15;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
