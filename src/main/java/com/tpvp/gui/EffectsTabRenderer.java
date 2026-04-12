package com.tpvp.gui;

import com.tpvp.config.ModConfig;
import net.minecraft.client.gui.DrawContext;

public class EffectsTabRenderer {
    public static void render(TPvPDashboardScreen screen, DrawContext context, int setX, int setY, int mx, int my) {
        screen.drawToggle(context, "MOBA Kill Banners", setX, setY, ModConfig.killBannerEnabled);
        screen.drawToggle(context, "3D Soul Ritual", setX + 150, setY, ModConfig.soulAnimationEnabled);
        
        context.drawTextWithShadow(screen.getTextRenderer(), "§7Get an epic screen banner when you kill players!", setX, setY + 25, 0xAAAAAA);
        context.drawTextWithShadow(screen.getTextRenderer(), "§7Dead players ascend to heaven as 3D wavy souls.", setX, setY + 40, 0xAAAAAA);
        
        // --- KILL BANNER COLOR THEME SELECTOR ---
        String[] colors = {"Blood Red", "Royal Gold", "Toxic Green"};
        context.drawTextWithShadow(screen.getTextRenderer(), "Banner Theme: §e" + colors[ModConfig.bannerColorTheme], setX, setY + 65, 0xFFFFFF);
        context.fill(setX + 150, setY + 61, setX + 220, setY + 77, 0xFF550000);
        context.drawCenteredTextWithShadow(screen.getTextRenderer(), "Change", setX + 185, setY + 65, 0xFFFFFF);
    }

    public static boolean mouseClicked(double mx, double my, int setX, int setY) {
        // Toggles
        if (mx >= setX+110 && mx <= setX+140 && my >= setY && my <= setY+12) { ModConfig.killBannerEnabled = !ModConfig.killBannerEnabled; return true; }
        if (mx >= setX+260 && mx <= setX+290 && my >= setY && my <= setY+12) { ModConfig.soulAnimationEnabled = !ModConfig.soulAnimationEnabled; return true; }
        
        // Banner Color Button
        if (mx >= setX+150 && mx <= setX+220 && my >= setY+61 && my <= setY+77) {
            ModConfig.bannerColorTheme = (ModConfig.bannerColorTheme + 1) % 3;
            return true;
        }
        
        return false;
    }
}
