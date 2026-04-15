package com.tpvp.gui;

import com.tpvp.config.ModConfig;
import com.tpvp.hud.PvPStatsManager;
import com.tpvp.hud.RenderUtils3D;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.joml.Matrix4f;

public class TierTabRenderer {
    
    public static int viewState = 0; // 0=Circle, 1=Modes, 2=Stats, 3=Eval Setup, 4=Eval Results
    public static String selectedMode = "Crystal PvP";
    private static long openTime = 0;
    
    public static int evalTargetMatches = 10;
    public static int modeScrollIndex = 0;

    public static void onOpen() { 
        openTime = System.currentTimeMillis(); 
        if (ModConfig.evalActive && ModConfig.evalCurrentMatches >= ModConfig.evalTotalMatches) {
            viewState = 4; // Show Results Screen!
            ModConfig.evalActive = false;
        } else {
            viewState = 0; 
        }
    }

    // Helper to return the correct Minecraft item for each PvP mode
    public static ItemStack getIconForMode(String mode) {
        switch(mode) {
            case "Crystal PvP": return new ItemStack(Items.END_CRYSTAL);
            case "Nodebuff": return new ItemStack(Items.SPLASH_POTION);
            case "Axe PvP": return new ItemStack(Items.NETHERITE_AXE);
            case "UHC / Classic": return new ItemStack(Items.COBWEB);
            case "Cart PvP": return new ItemStack(Items.MINECART); // Minecart added!
            case "Mace PvP": return new ItemStack(Items.MACE); // Mace added! (Assuming 1.21)
            case "Nethpot": return new ItemStack(Items.NETHERITE_CHESTPLATE);
            case "Spear/Trident": return new ItemStack(Items.TRIDENT);
            case "Beast PvP": return new ItemStack(Items.TOTEM_OF_UNDYING);
            case "Iron Pots": return new ItemStack(Items.IRON_CHESTPLATE);
            case "Dia SMP": return new ItemStack(Items.DIAMOND_CHESTPLATE);
            default: return new ItemStack(Items.DIAMOND_SWORD);
        }
    }

    public static void render(TPvPDashboardScreen screen, DrawContext context, int setX, int setY, int mx, int my, int winW, int winH) {
        long elapsed = System.currentTimeMillis() - openTime;
        float animProgress = Math.min(1.0f, elapsed / 1500.0f); 
        animProgress = 1.0f - (float)Math.pow(1.0f - animProgress, 4); 

        Matrix4f mat = context.getMatrices().peek().getPositionMatrix();
        VertexConsumer buf = screen.getMinecraftClient().getBufferBuilders().getEntityVertexConsumers().getBuffer(RenderLayer.getGui());

        // ---------------------------------------------------------
        // STATE 0: CIRCULAR TIER TRACKER
        // ---------------------------------------------------------
        if (viewState == 0) {
            float skillPct = PvPStatsManager.calculateSkillPercentage("Overall");
            float currentShowPct = 100f - ((100f - skillPct) * animProgress); 
            if (skillPct == 0 && animProgress > 0.99f) currentShowPct = 0f;

            int cx = setX + 160;
            int cy = setY + 60;
            float radius = 55f;

            RenderUtils3D.drawThickArc(mat, buf, cx, cy, radius, 8f, 0, 360, 0x55FFFFFF, 15728880); 
            
            int ringColor = currentShowPct > 80 ? 0xFFFFD700 : (currentShowPct > 50 ? 0xFF00FFCC : 0xFFFF3333);
            RenderUtils3D.drawThickArc(mat, buf, cx, cy, radius, 8f, 0, 360 * (currentShowPct / 100f), ringColor, 15728880);

            context.getMatrices().push(); context.getMatrices().scale(1.5f, 1.5f, 1.0f);
            String pctText = skillPct == 0 ? "N/A" : String.format("%.1f%%", currentShowPct);
            context.drawCenteredTextWithShadow(screen.getTextRenderer(), pctText, (int)(cx / 1.5f), (int)(cy / 1.5f) - 5, ringColor);
            context.getMatrices().pop();
            
            context.drawCenteredTextWithShadow(screen.getTextRenderer(), "Overall Tier: " + PvPStatsManager.getTierFromPercent(skillPct), cx, cy + 20, 0xFFFFFF);

            if (ModConfig.evalActive) {
                context.drawCenteredTextWithShadow(screen.getTextRenderer(), "§a[EVALUATION ACTIVE: " + ModConfig.evalCurrentMatches + "/" + ModConfig.evalTotalMatches + "]", cx, cy - 70, 0xFFFFFF);
            }

            context.fill(setX + 40, setY + 150, setX + 140, setY + 175, 0xFF00FFCC);
            context.drawCenteredTextWithShadow(screen.getTextRenderer(), "§lVIEW MODES", setX + 90, setY + 158, 0x000000);

            context.fill(setX + 180, setY + 150, setX + 280, setY + 175, 0xFFFF3333);
            context.drawCenteredTextWithShadow(screen.getTextRenderer(), "§lSTART EVAL", setX + 230, setY + 158, 0xFFFFFF);
        }
        // ---------------------------------------------------------
        // STATE 1: MODES GRID (Now with Dynamic Minecraft Items!)
        // ---------------------------------------------------------
        else if (viewState == 1) {
            context.drawTextWithShadow(screen.getTextRenderer(), "§lSELECT A PVP MODE", setX, setY - 10, 0xFFFFFF);
            
            // Loop through 4 visible cards
            int bX = setX, bY = setY + 10;
            for (int i = 0; i < 4; i++) {
                int index = modeScrollIndex + i;
                if (index < PvPStatsManager.ALL_MODES.length) {
                    String mode = PvPStatsManager.ALL_MODES[index];
                    ItemStack icon = getIconForMode(mode); // Fetch correct item
                    
                    boolean hov = mx >= bX && mx <= bX + 140 && my >= bY && my <= bY + 30;
                    context.fill(bX, bY, bX + 140, bY + 30, hov ? 0xAA00FFCC : 0x55000000);
                    
                    context.getMatrices().push();
                    context.getMatrices().translate(bX + 5, bY + 6, 0);
                    context.getMatrices().scale(1.2f, 1.2f, 1.0f); // Render item
                    context.drawItem(icon, 0, 0);
                    context.getMatrices().pop();
                    
                    context.drawTextWithShadow(screen.getTextRenderer(), mode, bX + 35, bY + 10, 0xFFFFFF);
                    bY += 35;
                }
            }

            context.fill(setX + 160, setY + 10, setX + 200, setY + 30, 0xAAFF3333); context.drawCenteredTextWithShadow(screen.getTextRenderer(), "UP", setX + 180, setY + 16, 0xFFFFFF);
            context.fill(setX + 160, setY + 40, setX + 200, setY + 60, 0xAAFF3333); context.drawCenteredTextWithShadow(screen.getTextRenderer(), "DOWN", setX + 180, setY + 46, 0xFFFFFF);

            context.fill(setX + 220, setY + 150, setX + 300, setY + 170, 0xAAFF3333);
            context.drawCenteredTextWithShadow(screen.getTextRenderer(), "← Back", setX + 260, setY + 156, 0xFFFFFF);
        }
        // ---------------------------------------------------------
        // STATE 2: MODE STATS & HISTORY
        // ---------------------------------------------------------
        else if (viewState == 2) {
            context.drawTextWithShadow(screen.getTextRenderer(), "§l" + selectedMode.toUpperCase() + " STATS", setX, setY - 10, 0xFFFFFF);
            
            context.fill(setX, setY + 10, setX + 120, setY + 130, 0x55000000);
            float pMode = PvPStatsManager.calculateSkillPercentage(selectedMode);
            
            context.drawTextWithShadow(screen.getTextRenderer(), "Tier: " + PvPStatsManager.getTierFromPercent(pMode), setX + 5, setY + 20, 0xFFFFFF);
            context.drawTextWithShadow(screen.getTextRenderer(), "Kills: §a" + PvPStatsManager.modeKills.getOrDefault(selectedMode, 0), setX + 5, setY + 40, 0xFFFFFF);
            context.drawTextWithShadow(screen.getTextRenderer(), "Deaths: §c" + PvPStatsManager.modeDeaths.getOrDefault(selectedMode, 0), setX + 5, setY + 55, 0xFFFFFF);
            context.drawTextWithShadow(screen.getTextRenderer(), "Win: §e" + String.format("%.1f%%", pMode), setX + 5, setY + 75, 0xFFFFFF);

            context.drawTextWithShadow(screen.getTextRenderer(), "§c⚔ Match History ⚔", setX + 130, setY - 10, 0xFFFFFF);
            context.fill(setX + 130, setY + 10, setX + 320, setY + 130, 0x55000000);
            
            int histY = setY + 15;
            if (PvPStatsManager.matchHistory.isEmpty()) {
                context.drawTextWithShadow(screen.getTextRenderer(), "§7No matches played yet.", setX + 140, histY, 0xFFFFFF);
            } else {
                int count = 0;
                for (PvPStatsManager.MatchRecord r : PvPStatsManager.matchHistory) {
                    if (r.mode.equals(selectedMode) || selectedMode.equals("Overall")) {
                        String res = r.won ? "§a[W]" : "§c[L]";
                        String text = res + " " + r.killer + " ⚔ " + r.victim;
                        if (text.length() > 30) text = text.substring(0, 28) + "..";
                        context.drawTextWithShadow(screen.getTextRenderer(), text, setX + 135, histY, 0xFFFFFF);
                        histY += 18;
                        count++;
                        if (count >= 6) break;
                    }
                }
            }

            context.fill(setX, setY + 150, setX + 80, setY + 170, 0xAAFF3333);
            context.drawCenteredTextWithShadow(screen.getTextRenderer(), "← Back", setX + 40, setY + 156, 0xFFFFFF);
        }
        // ---------------------------------------------------------
        // STATE 3: RANKED EVALUATION SETUP
        // ---------------------------------------------------------
        else if (viewState == 3) {
            context.drawTextWithShadow(screen.getTextRenderer(), "§lSTART PLACEMENT MATCHES", setX, setY - 10, 0xFFFFFF);
            
            context.drawTextWithShadow(screen.getTextRenderer(), "Mode: §e" + selectedMode, setX, setY + 20, 0xFFFFFF);
            context.fill(setX + 150, setY + 15, setX + 220, setY + 30, 0xFF550000);
            context.drawCenteredTextWithShadow(screen.getTextRenderer(), "Change", setX + 185, setY + 19, 0xFFFFFF);

            context.drawTextWithShadow(screen.getTextRenderer(), "Matches to play: §b" + evalTargetMatches, setX, setY + 50, 0xFFFFFF);
            context.fill(setX + 150, setY + 45, setX + 220, setY + 60, 0xFF550000);
            context.drawCenteredTextWithShadow(screen.getTextRenderer(), "+5", setX + 185, setY + 49, 0xFFFFFF);

            context.fill(setX + 80, setY + 90, setX + 220, setY + 120, 0xFFFF2222);
            context.drawCenteredTextWithShadow(screen.getTextRenderer(), "§lBEGIN EVALUATION", setX + 150, setY + 100, 0xFFFFFF);

            context.fill(setX, setY + 150, setX + 80, setY + 170, 0xAAFF3333);
            context.drawCenteredTextWithShadow(screen.getTextRenderer(), "← Cancel", setX + 40, setY + 156, 0xFFFFFF);
        }
        // ---------------------------------------------------------
        // STATE 4: FINAL EVALUATION RESULTS!
        // ---------------------------------------------------------
        else if (viewState == 4) {
            context.drawCenteredTextWithShadow(screen.getTextRenderer(), "§6§lEVALUATION COMPLETED!", setX + 150, setY - 10, 0xFFFFFF);
            
            context.fill(setX + 50, setY + 10, setX + 250, setY + 130, 0x88000000);
            context.drawCenteredTextWithShadow(screen.getTextRenderer(), "Mode: §e" + ModConfig.evalMode, setX + 150, setY + 20, 0xFFFFFF);
            context.drawCenteredTextWithShadow(screen.getTextRenderer(), "Kills: §a" + ModConfig.evalKills + "  §f|  Deaths: §c" + ModConfig.evalDeaths, setX + 150, setY + 40, 0xFFFFFF);
            
            float pMode = PvPStatsManager.calculateSkillPercentage(ModConfig.evalMode);
            context.getMatrices().push(); context.getMatrices().scale(2.0f, 2.0f, 1.0f);
            context.drawCenteredTextWithShadow(screen.getTextRenderer(), PvPStatsManager.getTierFromPercent(pMode), (int)((setX + 150)/2.0f), (int)((setY + 70)/2.0f), 0xFFFFFF);
            context.getMatrices().pop();

            context.fill(setX + 100, setY + 150, setX + 200, setY + 170, 0xAAFF3333);
            context.drawCenteredTextWithShadow(screen.getTextRenderer(), "Finish", setX + 150, setY + 156, 0xFFFFFF);
        }
    }

    public static boolean mouseClicked(double mx, double my, int setX, int setY) {
        if (viewState == 0) {
            if (mx >= setX + 40 && mx <= setX + 140 && my >= setY + 150 && my <= setY + 175) { viewState = 1; openTime = System.currentTimeMillis(); return true; }
            if (mx >= setX + 180 && mx <= setX + 280 && my >= setY + 150 && my <= setY + 175) { viewState = 3; openTime = System.currentTimeMillis(); return true; }
        } else if (viewState == 1) {
            if (mx >= setX && mx <= setX + 80 && my >= setY + 150 && my <= setY + 170) { viewState = 0; return true; }
            if (mx >= setX + 160 && mx <= setX + 200 && my >= setY + 10 && my <= setY + 30) { if (modeScrollIndex > 0) modeScrollIndex--; return true; }
            if (mx >= setX + 160 && mx <= setX + 200 && my >= setY + 40 && my <= setY + 60) { if (modeScrollIndex < PvPStatsManager.ALL_MODES.length - 4) modeScrollIndex++; return true; }
            
            int bX = setX, bY = setY + 10;
            for (int i = 0; i < 4; i++) {
                int index = modeScrollIndex + i;
                if (index < PvPStatsManager.ALL_MODES.length) {
                    if (mx >= bX && mx <= bX + 140 && my >= bY && my <= bY + 30) {
                        selectedMode = PvPStatsManager.ALL_MODES[index];
                        viewState = 2; openTime = System.currentTimeMillis(); return true;
                    }
                    bY += 35;
                }
            }
        } else if (viewState == 2) {
            if (mx >= setX && mx <= setX + 80 && my >= setY + 150 && my <= setY + 170) { viewState = 1; openTime = System.currentTimeMillis(); return true; }
        } else if (viewState == 3) {
            if (mx >= setX && mx <= setX + 80 && my >= setY + 150 && my <= setY + 170) { viewState = 0; return true; }
            
            if (mx >= setX + 150 && mx <= setX + 220 && my >= setY + 15 && my <= setY + 30) {
                modeScrollIndex = (modeScrollIndex + 1) % PvPStatsManager.ALL_MODES.length;
                selectedMode = PvPStatsManager.ALL_MODES[modeScrollIndex];
                return true;
            }
            if (mx >= setX + 150 && mx <= setX + 220 && my >= setY + 45 && my <= setY + 60) {
                evalTargetMatches += 5; if(evalTargetMatches > 30) evalTargetMatches = 5; return true;
            }
            if (mx >= setX + 80 && mx <= setX + 220 && my >= setY + 90 && my <= setY + 120) {
                ModConfig.evalActive = true;
                ModConfig.evalMode = selectedMode;
                ModConfig.evalTotalMatches = evalTargetMatches;
                ModConfig.evalCurrentMatches = 0;
                ModConfig.evalKills = 0;
                ModConfig.evalDeaths = 0;
                ModConfig.save();
                viewState = 0; openTime = System.currentTimeMillis();
                return true;
            }
        } else if (viewState == 4) { 
            if (mx >= setX + 100 && mx <= setX + 200 && my >= setY + 150 && my <= setY + 170) {
                viewState = 0; openTime = System.currentTimeMillis(); return true; 
            }
        }
        return false;
    }
}
