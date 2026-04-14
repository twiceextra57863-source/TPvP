package com.tpvp.gui;

import com.tpvp.hud.PvPStatsManager;
import com.tpvp.hud.RenderUtils3D;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.joml.Matrix4f;

public class TierTabRenderer {
    
    public static int viewState = 0; // 0 = Main Circle, 1 = Modes List, 2 = Mode Details
    public static String selectedMode = "";
    private static long openTime = 0;

    // Call this when tab is clicked to reset animations
    public static void onOpen() { openTime = System.currentTimeMillis(); viewState = 0; }

    public static void render(TPvPDashboardScreen screen, DrawContext context, int setX, int setY, int mx, int my, int winW, int winH) {
        long elapsed = System.currentTimeMillis() - openTime;
        float animProgress = Math.min(1.0f, elapsed / 1500.0f); // 1.5s animation
        animProgress = 1.0f - (float)Math.pow(1.0f - animProgress, 3); // Smooth ease out

        Matrix4f mat = context.getMatrices().peek().getPositionMatrix();
        VertexConsumer buf = screen.getMinecraftClient().getBufferBuilders().getEntityVertexConsumers().getBuffer(RenderLayer.getGui());

        // ---------------------------------------------------------
        // STATE 0: THE MAIN CIRCULAR TIER TRACKER
        // ---------------------------------------------------------
        if (viewState == 0) {
            float skillPct = PvPStatsManager.calculateSkillPercentage("Overall");
            float currentShowPct = skillPct * animProgress; // Circle fills up slowly!

            int cx = setX + 160;
            int cy = setY + 70;
            float radius = 50f;

            // Background Circle (Dark Gray)
            RenderUtils3D.drawThickArc(mat, buf, cx, cy, radius, 8f, 0, 360, 0x55FFFFFF, 15728880);
            
            // Foreground Circle (Dynamic Color based on Tier)
            int ringColor = skillPct > 80 ? 0xFFFFD700 : (skillPct > 50 ? 0xFF00FFCC : 0xFFFF3333);
            RenderUtils3D.drawThickArc(mat, buf, cx, cy, radius, 8f, 0, 360 * (currentShowPct / 100f), ringColor, 15728880);

            // Center Text
            context.getMatrices().push();
            context.getMatrices().scale(1.5f, 1.5f, 1.0f);
            String pctText = String.format("%.1f%%", currentShowPct);
            context.drawCenteredTextWithShadow(screen.getTextRenderer(), pctText, (int)(cx / 1.5f), (int)(cy / 1.5f) - 5, ringColor);
            context.getMatrices().pop();
            
            String tierText = PvPStatsManager.getTierFromPercent(skillPct);
            context.drawCenteredTextWithShadow(screen.getTextRenderer(), "Overall Tier: " + tierText, cx, cy + 20, 0xFFFFFF);

            // "Select Mode" Button
            context.fill(setX + 110, setY + 150, setX + 210, setY + 175, 0xFF00FFCC);
            context.drawCenteredTextWithShadow(screen.getTextRenderer(), "§lVIEW MODES", cx, setY + 158, 0x000000);
        }
        // ---------------------------------------------------------
        // STATE 1: MODES SELECTION GRID
        // ---------------------------------------------------------
        else if (viewState == 1) {
            context.drawTextWithShadow(screen.getTextRenderer(), "§lSELECT A PVP MODE", setX, setY, 0xFFFFFF);
            
            // Draw Mode Cards
            drawModeCard(screen, context, setX, setY + 20, mx, my, "Crystal PvP", new ItemStack(Items.END_CRYSTAL));
            drawModeCard(screen, context, setX + 160, setY + 20, mx, my, "Nodebuff (Pots)", new ItemStack(Items.SPLASH_POTION));
            drawModeCard(screen, context, setX, setY + 80, mx, my, "Axe PvP", new ItemStack(Items.NETHERITE_AXE));
            drawModeCard(screen, context, setX + 160, setY + 80, mx, my, "UHC / Classic", new ItemStack(Items.COBWEB));

            // Back Button
            context.fill(setX, setY + 150, setX + 80, setY + 170, 0xAAFF3333);
            context.drawCenteredTextWithShadow(screen.getTextRenderer(), "← Back", setX + 40, setY + 156, 0xFFFFFF);
        }
        // ---------------------------------------------------------
        // STATE 2: DEEP STATS & RIVALRY MATCH HISTORY
        // ---------------------------------------------------------
        else if (viewState == 2) {
            context.drawTextWithShadow(screen.getTextRenderer(), "§l" + selectedMode.toUpperCase() + " STATS", setX, setY, 0xFFFFFF);
            
            // Stats Box
            context.fill(setX, setY + 15, setX + 150, setY + 130, 0x55000000);
            float pMode = PvPStatsManager.calculateSkillPercentage(selectedMode);
            context.drawTextWithShadow(screen.getTextRenderer(), "Tier: " + PvPStatsManager.getTierFromPercent(pMode), setX + 10, setY + 25, 0xFFFFFF);
            context.drawTextWithShadow(screen.getTextRenderer(), "Kills: §a" + PvPStatsManager.modeKills.getOrDefault(selectedMode, 0), setX + 10, setY + 45, 0xFFFFFF);
            context.drawTextWithShadow(screen.getTextRenderer(), "Deaths: §c" + PvPStatsManager.modeDeaths.getOrDefault(selectedMode, 0), setX + 10, setY + 60, 0xFFFFFF);
            context.drawTextWithShadow(screen.getTextRenderer(), "Accuracy: §e" + String.format("%.1f%%", pMode), setX + 10, setY + 80, 0xFFFFFF);

            // Match History Box (Right Side)
            context.drawTextWithShadow(screen.getTextRenderer(), "§c⚔ Match History ⚔", setX + 160, setY, 0xFFFFFF);
            context.fill(setX + 160, setY + 15, setX + 310, setY + 130, 0x55000000);
            
            int histY = setY + 20;
            if (PvPStatsManager.matchHistory.isEmpty()) {
                context.drawTextWithShadow(screen.getTextRenderer(), "§7No matches played yet.", setX + 170, histY, 0xFFFFFF);
            } else {
                for (int i = 0; i < Math.min(4, PvPStatsManager.matchHistory.size()); i++) {
                    PvPStatsManager.MatchRecord r = PvPStatsManager.matchHistory.get(i);
                    String res = r.won ? "§a[WIN]" : "§c[LOSS]";
                    context.drawTextWithShadow(screen.getTextRenderer(), res + " §fvs " + r.opponent, setX + 165, histY, 0xFFFFFF);
                    histY += 20;
                }
            }

            // Back Button
            context.fill(setX, setY + 150, setX + 80, setY + 170, 0xAAFF3333);
            context.drawCenteredTextWithShadow(screen.getTextRenderer(), "← Back", setX + 40, setY + 156, 0xFFFFFF);
        }
    }

    private static void drawModeCard(TPvPDashboardScreen screen, DrawContext context, int x, int y, int mx, int my, String name, ItemStack icon) {
        boolean hov = mx >= x && mx <= x + 140 && my >= y && my <= y + 50;
        context.fill(x, y, x + 140, y + 50, hov ? 0xAA00FFCC : 0x55000000);
        
        context.getMatrices().push();
        context.getMatrices().translate(x + 10, y + 10, 0);
        context.getMatrices().scale(2.0f, 2.0f, 1.0f);
        context.drawItem(icon, 0, 0);
        context.getMatrices().pop();
        
        context.drawTextWithShadow(screen.getTextRenderer(), name, x + 50, y + 20, 0xFFFFFF);
    }

    public static boolean mouseClicked(double mx, double my, int setX, int setY) {
        if (viewState == 0) {
            // Click "View Modes"
            if (mx >= setX + 110 && mx <= setX + 210 && my >= setY + 150 && my <= setY + 175) {
                viewState = 1; openTime = System.currentTimeMillis(); return true;
            }
        } else if (viewState == 1) {
            // Click Back
            if (mx >= setX && mx <= setX + 80 && my >= setY + 150 && my <= setY + 170) {
                viewState = 0; openTime = System.currentTimeMillis(); return true;
            }
            // Click Mode Cards
            if (checkCardClick(mx, my, setX, setY + 20, "Crystal PvP")) return true;
            if (checkCardClick(mx, my, setX + 160, setY + 20, "Nodebuff (Pots)")) return true;
            if (checkCardClick(mx, my, setX, setY + 80, "Axe PvP")) return true;
            if (checkCardClick(mx, my, setX + 160, setY + 80, "UHC / Classic")) return true;
            
        } else if (viewState == 2) {
            // Click Back
            if (mx >= setX && mx <= setX + 80 && my >= setY + 150 && my <= setY + 170) {
                viewState = 1; openTime = System.currentTimeMillis(); return true;
            }
        }
        return false;
    }

    private static boolean checkCardClick(double mx, double my, int x, int y, String mode) {
        if (mx >= x && mx <= x + 140 && my >= y && my <= y + 50) {
            selectedMode = mode;
            viewState = 2; 
            openTime = System.currentTimeMillis();
            return true;
        }
        return false;
    }
}
