package com.tpvp.gui;

import com.tpvp.config.ModConfig;
import net.minecraft.client.gui.DrawContext;

public class PerformanceTabRenderer {
    public static void render(TPvPDashboardScreen screen, DrawContext context, int setX, int setY, int mx, int my) {
        
        // 1. FPS BOOST & ANTI-LAG
        screen.drawToggle(context, "Anti-Lag (FPS Boost)", setX, setY, ModConfig.fpsBoostEnabled);
        context.drawTextWithShadow(screen.getTextRenderer(), "§7Hides distant players, stops rendering fire on", setX, setY + 16, 0xAAAAAA);
        context.drawTextWithShadow(screen.getTextRenderer(), "§7others, and removes chunk-loading stutters.", setX, setY + 28, 0xAAAAAA);

        // 2. COTTON SMOOTH CAMERA
        screen.drawToggle(context, "Cotton Smooth Camera", setX, setY + 50, ModConfig.smoothGameEnabled);
        
        context.drawTextWithShadow(screen.getTextRenderer(), "Sensitivity: §e" + ModConfig.cottonSensitivity + "%", setX + 150, setY + 50, 0xFFFFFF);
        context.fill(setX + 150, setY + 62, setX + 250, setY + 76, 0xFF550000);
        context.drawCenteredTextWithShadow(screen.getTextRenderer(), "Add +10%", setX + 200, setY + 65, 0xFFFFFF);

        context.drawTextWithShadow(screen.getTextRenderer(), "§7Removes screen stutters. Butter smooth PvP!", setX, setY + 82, 0xAAAAAA);
        
        // 3. DEVICE COOLER (Multitasking Fix)
        screen.drawToggle(context, "Device Cooler (Anti-Heat)", setX, setY + 105, ModConfig.deviceCooler);
        context.drawTextWithShadow(screen.getTextRenderer(), "§7Drops to 10 FPS when multitasking/background to", setX, setY + 120, 0xAAAAAA);
        context.drawTextWithShadow(screen.getTextRenderer(), "§7stop phone heating. A must for mobile players!", setX, setY + 132, 0xAAAAAA);
    }

    public static boolean mouseClicked(double mx, double my, int setX, int setY) {
        if (mx >= setX+120 && mx <= setX+150 && my >= setY && my <= setY+12) { ModConfig.fpsBoostEnabled = !ModConfig.fpsBoostEnabled; return true; }
        if (mx >= setX+120 && mx <= setX+150 && my >= setY+50 && my <= setY+62) { ModConfig.smoothGameEnabled = !ModConfig.smoothGameEnabled; return true; }
        if (mx >= setX+120 && mx <= setX+150 && my >= setY+105 && my <= setY+117) { ModConfig.deviceCooler = !ModConfig.deviceCooler; return true; }
        
        // Sensitivity Button Click
        if (mx >= setX+150 && mx <= setX+250 && my >= setY+62 && my <= setY+76) {
            ModConfig.cottonSensitivity += 10;
            if (ModConfig.cottonSensitivity > 300) ModConfig.cottonSensitivity = 10;
            return true;
        }
        return false;
    }
}
