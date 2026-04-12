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
        // TOGGLE BUTTONS
        context.fill(setX, winY + 10, setX + 120, winY + 25, showingFriends ? 0xFF00AA00 : 0xFFAA0000);
        context.drawCenteredTextWithShadow(screen.textRenderer, showingFriends ? "List: FRIENDS" : "List: ENEMIES", setX + 60, winY + 14, 0xFFFFFF);
        
        screen.drawToggle(context, "Dragon Aura", setX + 150, winY + 10, ModConfig.dragonAuraEnabled);

        if (screen.client != null && screen.client.getNetworkHandler() != null) {
            List<PlayerListEntry> players = new ArrayList<>(screen.client.getNetworkHandler().getPlayerList());
            int listY = winY + 40;
            
            for (int i = 0; i < players.size(); i++) {
                if (i >= scrollOffset && i < scrollOffset + 4) { // Show 4 per page
                    PlayerListEntry p = players.get(i);
                    String pName = p.getProfile().getName();
                    
                    boolean isTagged = showingFriends ? ModConfig.taggedFriendName.equals(pName) : ModConfig.taggedPlayerName.equals(pName);
                    int rowColor = isTagged ? (showingFriends ? 0xAA00FF00 : 0xAAFF2222) : 0x55000000;
                    
                    context.fill(setX, listY, setX + 280, listY + 50, rowColor);
                    Identifier skin = p.getSkinTextures().texture();
                    
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
                    
                    context.drawTextWithShadow(screen.textRenderer, isTagged ? "§l" + pName : "§f" + pName, setX + 50, listY + 20, 0xFFFFFF);
                    listY += 55;
                }
            }

            // Scrollbar Render
            if (players.size() > 4) {
                context.fill(setX + 290, winY + 40, setX + 295, winY + 245, 0x55000000); 
                int thumbH = Math.max(20, 205 / (players.size() - 3));
                int thumbY = winY + 40 + (scrollOffset * (205 - thumbH) / Math.max(1, players.size() - 4));
                context.fill(setX + 290, thumbY, setX + 295, thumbY + thumbH, showingFriends ? 0xFF00FF00 : 0xFFFF2222);
            }
        }
    }

    public static boolean mouseClicked(TPvPDashboardScreen screen, double mx, double my, int setX, int winY) {
        // Toggle Friends
        if (mx >= setX && mx <= setX + 120 && my >= winY + 10 && my <= winY + 25) { showingFriends = !showingFriends; return true; }
        // Toggle Dragon
        if (mx >= setX+260 && mx <= setX+290 && my >= winY+10 && my <= winY+22) { ModConfig.dragonAuraEnabled = !ModConfig.dragonAuraEnabled; return true; }

        // Track Click
        if (mx >= setX + 290 && mx <= setX + 295 && my >= winY + 40 && my <= winY + 245) { isDraggingScroll = true; return true; }

        // Player Click
        if (screen.client != null && screen.client.getNetworkHandler() != null) {
            List<PlayerListEntry> players = new ArrayList<>(screen.client.getNetworkHandler().getPlayerList());
            int listY = winY + 40;
            for (int i = 0; i < players.size(); i++) {
                if (i >= scrollOffset && i < scrollOffset + 4) {
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
        if (isDraggingScroll && screen.client != null && screen.client.getNetworkHandler() != null) {
            int players = screen.client.getNetworkHandler().getPlayerList().size();
            if (players > 4) {
                float percentage = (float) (my - (winY + 40)) / 205f;
                scrollOffset = Math.round(Math.max(0, Math.min(1, percentage)) * (players - 4));
            }
            return true;
        }
        return false;
    }

    public static void mouseScrolled(double scroll) {
        scrollOffset = Math.max(0, scrollOffset - (int) scroll);
    }
}
