package com.tpvp.gui;

import com.tpvp.config.ModConfig;
import net.minecraft.client.gui.DrawContext;

public class PerformanceTabRenderer {

    public static boolean isDraggingSlider = false;

    public static void render(TPvPDashboardScreen screen, DrawContext context, int setX, int setY, int mx, int my) {
        
        // ---------------------------------------------------
        // 1. HYPER ENGINE (FPS BOOST)
        // ---------------------------------------------------
        screen.drawToggle(context, "Hyper-Engine (FPS Boost)", setX, setY, ModConfig.fpsBoostEnabled);
        context.drawTextWithShadow(screen.getTextRenderer(), "§7Aggressively culls block entities, drops 85% particles,", setX, setY + 16, 0xAAAAAA);
        context.drawTextWithShadow(screen.getTextRenderer(), "§7and disables weather/fire to drop GPU load by 80%.", setX, setY + 28, 0xAAAAAA);

        // ---------------------------------------------------
        // 2. KINETIC SMOOTH CAMERA (Spacing increased from +50 to +60)
        // ---------------------------------------------------
        int smoothY = setY + 60;
        screen.drawToggle(context, "Kinetic Smooth Camera", setX, smoothY, ModConfig.smoothGameEnabled);
        
        // Sensitivity Slider
        context.drawTextWithShadow(screen.getTextRenderer(), "Sensitivity: §e" + ModConfig.cottonSensitivity + "%", setX + 150, smoothY, 0xFFFFFF);
        
        int sliderX = setX + 150;
        int sliderY = smoothY + 15;
        int sliderW = 100;
        
        // Track
        context.fill(sliderX, sliderY, sliderX + sliderW, sliderY + 10, 0xFF550000); 
        
        // Thumb (Draggable Box)
        int thumbX = sliderX + (int)((ModConfig.cottonSensitivity / 300.0f) * (sliderW - 10));
        context.fill(thumbX, sliderY, thumbX + 10, sliderY + 10, 0xFF00FFCC); 
        
        context.drawTextWithShadow(screen.getTextRenderer(), "§7Esports-level buttery smooth aim tracking!", setX, smoothY + 32, 0xAAAAAA);
        
        // ---------------------------------------------------
        // 3. DEVICE COOLER (Spacing increased from +105 to +130)
        // ---------------------------------------------------
        int coolerY = setY + 130;
        screen.drawToggle(context, "Multitasking Cooler", setX, coolerY, ModConfig.deviceCooler);
        context.drawTextWithShadow(screen.getTextRenderer(), "§7Drops game to 10 FPS when listening to music or", setX, coolerY + 16, 0xAAAAAA);
        context.drawTextWithShadow(screen.getTextRenderer(), "§7multitasking to prevent phone heating & battery drain.", setX, coolerY + 28, 0xAAAAAA);
    }

    public static boolean mouseClicked(double mx, double my, int setX, int setY) {
        int smoothY = setY + 60;
        int coolerY = setY + 130;
        
        if (mx >= setX+150 && mx <= setX+180 && my >= setY && my <= setY+12) { ModConfig.fpsBoostEnabled = !ModConfig.fpsBoostEnabled; return true; }
        if (mx >= setX+150 && mx <= setX+180 && my >= smoothY && my <= smoothY+12) { ModConfig.smoothGameEnabled = !ModConfig.smoothGameEnabled; return true; }
        if (mx >= setX+120 && mx <= setX+150 && my >= coolerY && my <= coolerY+12) { ModConfig.deviceCooler = !ModConfig.deviceCooler; return true; }
        
        int sliderX = setX + 150, sliderY = smoothY + 15;
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
