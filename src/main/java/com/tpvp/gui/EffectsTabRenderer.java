package com.tpvp.gui;

import com.tpvp.config.ModConfig;
import net.minecraft.client.gui.DrawContext;

public class EffectsTabRenderer {
    public static void render(TPvPDashboardScreen screen, DrawContext context, int setX, int setY, int mx, int my) {
        screen.drawToggle(context, "MOBA Kill Banners", setX, setY, ModConfig.killBannerEnabled);
        screen.drawToggle(context, "3D Soul Ritual", setX + 150, setY, ModConfig.soulAnimationEnabled);
        
        context.drawTextWithShadow(screen.getTextRenderer(), "§7Get an epic screen banner when you kill players!", setX, setY + 25, 0xAAAAAA);
        context.drawTextWithShadow(screen.getTextRenderer(), "§7Dead players ascend to heaven as 3D wavy souls.", setX, setY + 40, 0xAAAAAA);
    }

    public static boolean mouseClicked(double mx, double my, int setX, int setY) {
        if (mx >= setX+110 && mx <= setX+140 && my >= setY && my <= setY+12) { ModConfig.killBannerEnabled = !ModConfig.killBannerEnabled; return true; }
        if (mx >= setX+260 && mx <= setX+290 && my >= setY && my <= setY+12) { ModConfig.soulAnimationEnabled = !ModConfig.soulAnimationEnabled; return true; }
        return false;
    }
}
