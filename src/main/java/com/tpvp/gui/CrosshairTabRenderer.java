package com.tpvp.gui;

import com.tpvp.config.ModConfig;
import net.minecraft.client.gui.DrawContext;

public class CrosshairTabRenderer {
    public static void render(TPvPDashboardScreen screen, DrawContext context, int setX, int setY, int mx, int my, int winY) {
        screen.drawToggle(context, "Smart Crosshair", setX, setY, ModConfig.smartCrosshair);
        
        context.drawTextWithShadow(screen.getTextRenderer(), "Style: §e" + ModConfig.crosshairStyle, setX, setY + 30, 0xFFFFFF);
        context.fill(setX, setY + 40, setX + 80, setY + 55, 0xFF550000);
        context.drawCenteredTextWithShadow(screen.getTextRenderer(), "Change", setX + 40, setY + 44, 0xFFFFFF);
        
        context.drawTextWithShadow(screen.getTextRenderer(), "Size: " + ModConfig.crosshairSize, setX + 100, setY + 30, 0xFFFFFF);
        context.fill(setX + 100, setY + 40, setX + 140, setY + 55, 0xFF550000);
        context.drawCenteredTextWithShadow(screen.getTextRenderer(), "Add", setX + 120, setY + 44, 0xFFFFFF);

        context.fill(setX, winY + 110, setX + 300, winY + 240, 0xFF000000); 
        context.drawCenteredTextWithShadow(screen.getTextRenderer(), "Live Crosshair Preview", setX + 150, winY + 120, 0xAAAAAA);
        
        int cx = setX + 150, cy = winY + 180;
        float s = ModConfig.crosshairSize;
        int color = 0xFFFFFFFF; 
        
        context.getMatrices().push();
        context.getMatrices().translate(cx, cy, 0);
        context.getMatrices().scale(s, s, 1.0f);
        
        if (ModConfig.crosshairStyle == 0) { 
            context.fill(-1, -6, 1, -2, color); context.fill(-1, 2, 1, 6, color);
            context.fill(-6, -1, -2, 1, color); context.fill(2, -1, 6, 1, color);
            context.fill(0, 0, 1, 1, color);
        } else if (ModConfig.crosshairStyle == 1) { 
            context.fill(-2, -1, 2, 1, color); context.fill(-1, -2, 1, 2, color);
            context.fill(-1, -1, 1, 1, 0x00000000); 
        } else if (ModConfig.crosshairStyle == 2) { 
            context.fill(-6, -4, -3, -3, color); context.fill(-4, -6, -3, -3, color);
            context.fill(3, 3, 6, 4, color); context.fill(3, 3, 4, 6, color);
        }
        context.getMatrices().pop();
    }

    public static boolean mouseClicked(double mx, double my, int setX, int setY) {
        if (mx >= setX+110 && mx <= setX+140 && my >= setY && my <= setY+12) { ModConfig.smartCrosshair = !ModConfig.smartCrosshair; return true; }
        if (mx >= setX && mx <= setX+80 && my >= setY+40 && my <= setY+55) { ModConfig.crosshairStyle = (ModConfig.crosshairStyle + 1) % 3; return true; }
        if (mx >= setX+100 && mx <= setX+140 && my >= setY+40 && my <= setY+55) { ModConfig.crosshairSize += 0.5f; if (ModConfig.crosshairSize > 3.0f) ModConfig.crosshairSize = 0.5f; return true;}
        return false;
    }
}
