package com.tpvp.gui;

import com.tpvp.config.ModConfig;
import net.minecraft.client.gui.DrawContext;

public class PerformanceTabRenderer {
    public static void render(TPvPDashboardScreen screen, DrawContext context, int setX, int setY, int mx, int my) {
        
        screen.drawToggle(context, "Anti-Lag (FPS Boost)", setX, setY, ModConfig.fpsBoostEnabled);
        context.drawTextWithShadow(screen.getTextRenderer(), "§7Hides distant players, reduces explosion & water", setX, setY + 16, 0xAAAAAA);
        context.drawTextWithShadow(screen.getTextRenderer(), "§7particles by 75%. Essential for big PvP servers!", setX, setY + 28, 0xAAAAAA);

        screen.drawToggle(context, "Cotton Smooth Game", setX, setY + 50, ModConfig.smoothGameEnabled);
        context.drawTextWithShadow(screen.getTextRenderer(), "§7Removes screen stutters and camera shake.", setX, setY + 66, 0xAAAAAA);
        context.drawTextWithShadow(screen.getTextRenderer(), "§7Feels like butter, heavily improves PvP tracking!", setX, setY + 78, 0xAAAAAA);
        
        // Extra Visual Details
        context.fill(setX, setY + 110, setX + 250, setY + 111, 0x44FFFFFF);
        context.drawCenteredTextWithShadow(screen.getTextRenderer(), "§eNotice: Turning these ON will instantly", setX + 125, setY + 125, 0xFFFFFF);
        context.drawCenteredTextWithShadow(screen.getTextRenderer(), "§emake your gameplay feel like a 144Hz PC!", setX + 125, setY + 140, 0xFFFFFF);
    }

    public static boolean mouseClicked(double mx, double my, int setX, int setY) {
        if (mx >= setX+120 && mx <= setX+150 && my >= setY && my <= setY+12) { ModConfig.fpsBoostEnabled = !ModConfig.fpsBoostEnabled; return true; }
        if (mx >= setX+120 && mx <= setX+150 && my >= setY+50 && my <= setY+62) { ModConfig.smoothGameEnabled = !ModConfig.smoothGameEnabled; return true; }
        return false;
    }
}
