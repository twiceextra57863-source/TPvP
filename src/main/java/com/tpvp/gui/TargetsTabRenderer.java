package com.tpvp.gui;

import com.tpvp.config.ModConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class TargetsTabRenderer {
    public static boolean showingFriends = false;
    public static int scrollOffset = 0;
    public static boolean isDraggingScroll = false;

    public static void render(TPvPDashboardScreen screen, DrawContext context, int setX, int winY, int mx, int my) {
        
        // 1. Friend / Enemy List Toggle
        context.fill(setX, winY + 10, setX + 120, winY + 25, showingFriends ? 0xFF00AA00 : 0xFFAA0000);
        context.drawCenteredTextWithShadow(screen.getTextRenderer(), showingFriends ? "List: FRIENDS" : "List: ENEMIES", setX + 60, winY + 14, 0xFFFFFF);
        
        // 2. Auto-Track Low HP Toggle
        screen.drawToggle(context, "Auto-Track Low HP", setX + 130, winY + 10, ModConfig.autoTrack);

        // 3. Dragon Color Selector
        String[] colors = {"Ruby Red", "Void Purple", "Frost Blue"};
        context.drawTextWithShadow(screen.getTextRenderer(), "Dragon: §e" + colors[ModConfig.dragonColor], setX, winY + 30, 0xFFFFFF);
        context.fill(setX + 100, winY + 26, setX + 150, winY + 38, 0xFF550000);
        context.drawCenteredTextWithShadow(screen.getTextRenderer(), "Change", setX + 125, winY + 29, 0xFFFFFF);

        // 4. Player List rendering
        if (screen.getMinecraftClient() != null && screen.getMinecraftClient().getNetworkHandler() != null) {
            List<PlayerListEntry> players = new ArrayList<>(screen.getMinecraftClient().getNetworkHandler().getPlayerList());
            int listY = winY + 45;
            
            for (int i = 0; i < players.size(); i++) {
                if (i >= scrollOffset && i < scrollOffset + 3) { // Reduced to 3 per page to make room for new buttons
                    PlayerListEntry p = players.get(i);
                    String pName = p.getProfile().getName();
                    
                    boolean isTagged = showingFriends ? ModConfig.taggedFriendName.equals(pName) : ModConfig.taggedPlayerName.equals(pName);
                    // Red highlight for tagged enemies, Green for friends
                    int rowColor = isTagged ? (showingFriends ? 0xAA00FF00 : 0xAAFF2222) : 0x55000000;
                    
                    context.fill(setX, listY, setX + 280, listY + 50, rowColor);
                    Identifier skin = p.getSkinTextures().texture();
                    
                    // Full 2D Body Map
                    context.getMatrices().push();
                    context.getMatrices().translate(setX + 10, listY + 5, 0);
                    context.getMatrices().scale(1.2f, 1.2f, 1.0f);
                    context.drawTexture(RenderLayer::getGuiTextured, skin, 4, 0, 8f, 8f, 8, 8, 64, 64); // Head
                    context.drawTexture(RenderLayer::getGuiTextured, skin, 4, 8, 20f, 20f, 8, 12, 64, 64); // Body
                    context.drawTexture(RenderLayer::getGuiTextured, skin, 0, 8, 44f, 20f, 4, 12, 64, 64); // Right Arm
                    context.drawTexture(RenderLayer::getGuiTextured, skin, 12, 8, 36f, 52f, 4, 12, 64, 64); // Left Arm
                    context.drawTexture(RenderLayer::getGuiTextured, skin, 4, 20, 0f, 20f, 4, 12, 64, 64); // Right Leg
                    context.drawTexture(RenderLayer::getGuiTextured, skin, 8, 20, 20f, 52f, 4, 12, 64, 64); // Left Leg
                    context.getMatrices().pop();
                    
                    context.drawTextWithShadow(screen.getTextRenderer(), isTagged ? "§l" + pName : "§f" + pName, setX + 50, listY + 20, 0xFFFFFF);
                    listY += 55;
                }
            }

            // Scrollbar
            if (players.size() > 3) {
                context.fill(setX + 290, winY + 45, setX + 295, winY + 210, 0x55000000); 
                int thumbH = Math.max(20, 165 / (players.size() - 2));
                int thumbY = winY + 45 + (scrollOffset * (165 - thumbH) / Math.max(1, players.size() - 3));
                context.fill(setX + 290, thumbY, setX + 295, thumbY + thumbH, showingFriends ? 0xFF00FF00 : 0xFFFF2222);
            }
        }
    }

    public static boolean mouseClicked(TPvPDashboardScreen screen, double mx, double my, int setX, int winY) {
        // Toggle Friends/Enemies
        if (mx >= setX && mx <= setX + 120 && my >= winY + 10 && my <= winY + 25) { showingFriends = !showingFriends; return true; }
        
        // Auto-Track Low HP
        if (mx >= setX+240 && mx <= setX+270 && my >= winY+10 && my <= winY+22) { ModConfig.autoTrack = !ModConfig.autoTrack; return true; }
        
        // Dragon Color Theme
        if (mx >= setX+100 && mx <= setX+150 && my >= winY+26 && my <= winY+38) { ModConfig.dragonColor = (ModConfig.dragonColor + 1) % 3; return true; }

        // Track Click
        if (mx >= setX + 290 && mx <= setX + 295 && my >= winY + 45 && my <= winY + 210) { isDraggingScroll = true; return true; }

        // Player Tag Selection
        if (screen.getMinecraftClient() != null && screen.getMinecraftClient().getNetworkHandler() != null) {
            List<PlayerListEntry> players = new ArrayList<>(screen.getMinecraftClient().getNetworkHandler().getPlayerList());
            int listY = winY + 45;
            for (int i = 0; i < players.size(); i++) {
                if (i >= scrollOffset && i < scrollOffset + 3) {
                    if (mx >= setX && mx <= setX + 280 && my >= listY && my <= listY + 50) {
                        String name = players.get(i).getProfile().getName();
                        if (showingFriends) {
                            if (ModConfig.taggedFriendName.equals(name)) ModConfig.taggedFriendName = ""; 
                            else ModConfig.taggedFriendName = name;
                        } else {
                            if (ModConfig.taggedPlayerName.equals(name)) ModConfig.taggedPlayerName = ""; 
                            else { ModConfig.taggedPlayerName = name; ModConfig.autoTrack = false; }
                        }
                        return true;
                    }
                    listY += 55;
                }
            }
        }
        return false;
    }

    public static boolean mouseDragged(TPvPDashboardScreen screen, double mx, double my, int winY) {
        if (isDraggingScroll && screen.getMinecraftClient() != null && screen.getMinecraftClient().getNetworkHandler() != null) {
            int players = screen.getMinecraftClient().getNetworkHandler().getPlayerList().size();
            if (players > 3) {
                float percentage = (float) (my - (winY + 45)) / 165f;
                scrollOffset = Math.round(Math.max(0, Math.min(1, percentage)) * (players - 3));
            }
            return true;
        }
        return false;
    }

    public static void mouseScrolled(double scroll) {
        scrollOffset = Math.max(0, scrollOffset - (int) scroll);
    }
}
