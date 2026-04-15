package com.tpvp.gui;

import com.tpvp.config.ModConfig;
import net.minecraft.client.gui.DrawContext;

public class PerformanceTabRenderer {

    public static boolean isDraggingSlider = false;

    public static void render(TPvPDashboardScreen screen, DrawContext context, int setX, int setY, int mx, int my) {
        
        // 1. HYPER ENGINE (FPS BOOST)
        screen.drawToggle(context, "Hyper-Engine (FPS Boost)", setX, setY, ModConfig.fpsBoostEnabled);
        context.drawTextWithShadow(screen.getTextRenderer(), "§7Aggressively culls block entities, drops 85% particles,", setX, setY + 16, 0xAAAAAA);
        context.drawTextWithShadow(screen.getTextRenderer(), "§7and disables weather/fire to drop GPU load by 80%.", setX, setY + 28, 0xAAAAAA);

        // 2. KINETIC SMOOTH CAMERA
        screen.drawToggle(context, "Kinetic Smooth Camera", setX, setY + 50, ModConfig.smoothGameEnabled);
        
        context.drawTextWithShadow(screen.getTextRenderer(), "Sensitivity: §e" + ModConfig.cottonSensitivity + "%", setX + 150, setY + 50, 0xFFFFFF);
        
        int sliderX = setX + 150;
        int sliderY = setY + 65;
        int sliderW = 100;
        context.fill(sliderX, sliderY, sliderX + sliderW, sliderY + 10, 0xFF550000); 

        int thumbX = sliderX + (int)((ModConfig.cottonSensitivity / 300.0f) * (sliderW - 10));
        context.fill(thumbX, sliderY, thumbX + 10, sliderY + 10, 0xFF00FFCC); 
        
        context.drawTextWithShadow(screen.getTextRenderer(), "§7Esports-level buttery smooth aim tracking!", setX, setY + 82, 0xAAAAAA);
        
        // 3. DEVICE COOLER
        screen.drawToggle(context, "Multitasking Cooler", setX, setY + 105, ModConfig.deviceCooler);
        context.drawTextWithShadow(screen.getTextRenderer(), "§7Drops game to 10 FPS when listening to music or", setX, setY + 120, 0xAAAAAA);
        context.drawTextWithShadow(screen.getTextRenderer(), "§7multitasking to prevent phone heating & battery drain.", setX, setY + 132, 0xAAAAAA);
    }

    public static boolean mouseClicked(double mx, double my, int setX, int setY) {
        if (mx >= setX+150 && mx <= setX+180 && my >= setY && my <= setY+12) { ModConfig.fpsBoostEnabled = !ModConfig.fpsBoostEnabled; return true; }
        if (mx >= setX+150 && mx <= setX+180 && my >= setY+50 && my <= setY+62) { ModConfig.smoothGameEnabled = !ModConfig.smoothGameEnabled; return true; }
        if (mx >= setX+120 && mx <= setX+150 && my >= setY+105 && my <= setY+117) { ModConfig.deviceCooler = !ModConfig.deviceCooler; return true; }
        
        int sliderX = setX + 150, sliderY = setY + 65;
        if (mx >= sliderX && mx <= sliderX + 100 && my >= sliderY && my <= sliderY + 10) { isDraggingSlider = true; return true; }
        return false;
    }

    public static boolean mouseDragged(double mx, double my, int setX, int setY) {
        if (isDraggingSlider) {
            int sliderX = setX + 150;
            float pct = (float) (mx - sliderX) / 100.0f;
            pct = Math.max(0.0f, Math.min(1.0f, pct)); 
            ModConfig.cottonSensitivity = (int) (pct * 300.0f);
            return true;
        }
        return false;
    }
}
