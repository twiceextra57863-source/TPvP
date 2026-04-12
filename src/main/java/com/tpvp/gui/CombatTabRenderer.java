package com.tpvp.gui;

import com.tpvp.config.ModConfig;
import net.minecraft.client.gui.DrawContext;

public class CombatTabRenderer {
    public static void render(TPvPDashboardScreen screen, DrawContext context, int setX, int setY, int mx, int my) {
        screen.drawToggle(context, "Hitboxes", setX, setY, ModConfig.hitboxEnabled);
        screen.drawToggle(context, "3D Indicator", setX + 150, setY, ModConfig.indicatorEnabled);
        screen.drawToggle(context, "Armor Align (Vert)", setX, setY + 30, ModConfig.armorVertical);
        
        context.drawTextWithShadow(screen.getTextRenderer(), "Indicator Style: §e" + ModConfig.indicatorStyle, setX + 150, setY + 30, 0xFFFFFF);
        context.fill(setX + 150, setY + 40, setX + 250, setY + 55, 0xFF550000);
        context.drawCenteredTextWithShadow(screen.getTextRenderer(), "Change Style", setX + 200, setY + 44, 0xFFFFFF);

        context.drawTextWithShadow(screen.getTextRenderer(), "Armor Crack: §c" + ModConfig.armorCrackThreshold + "%", setX, setY + 60, 0xFFFFFF);
        context.fill(setX, setY + 70, setX + 100, setY + 85, 0xFF550000);
        context.drawCenteredTextWithShadow(screen.getTextRenderer(), "Add +10%", setX + 50, setY + 74, 0xFFFFFF);
    }

    public static boolean mouseClicked(double mx, double my, int setX, int setY) {
        if (mx >= setX+110 && mx <= setX+140 && my >= setY && my <= setY+12) { ModConfig.hitboxEnabled = !ModConfig.hitboxEnabled; return true; }
        if (mx >= setX+260 && mx <= setX+290 && my >= setY && my <= setY+12) { ModConfig.indicatorEnabled = !ModConfig.indicatorEnabled; return true; }
        if (mx >= setX+110 && mx <= setX+140 && my >= setY+30 && my <= setY+42) { ModConfig.armorVertical = !ModConfig.armorVertical; return true; }
        if (mx >= setX+150 && mx <= setX+250 && my >= setY+40 && my <= setY+55) { ModConfig.indicatorStyle = (ModConfig.indicatorStyle + 1) % 3; return true; }
        if (mx >= setX && mx <= setX+100 && my >= setY+70 && my <= setY+85) { 
            ModConfig.armorCrackThreshold += 10; 
            if(ModConfig.armorCrackThreshold > 50) ModConfig.armorCrackThreshold = 10; 
            return true;
        }
        return false;
    }
}
