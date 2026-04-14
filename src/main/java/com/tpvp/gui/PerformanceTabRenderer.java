package com.tpvp.gui;

import com.tpvp.config.ModConfig;
import net.minecraft.client.gui.DrawContext;

public class PerformanceTabRenderer {

    // Slider dragging tracker
    public static boolean isDraggingSlider = false;

    public static void render(TPvPDashboardScreen screen, DrawContext context, int setX, int setY, int mx, int my) {
        
        // 1. FPS BOOST & ANTI-LAG
        screen.drawToggle(context, "Anti-Lag (FPS Boost)", setX, setY, ModConfig.fpsBoostEnabled);
        context.drawTextWithShadow(screen.getTextRenderer(), "§7Hides distant players, stops rendering fire on", setX, setY + 16, 0xAAAAAA);
        context.drawTextWithShadow(screen.getTextRenderer(), "§7others, and removes chunk-loading stutters.", setX, setY + 28, 0xAAAAAA);

        // 2. COTTON SMOOTH CAMERA
        screen.drawToggle(context, "Cotton Smooth Camera", setX, setY + 50, ModConfig.smoothGameEnabled);
        
        // --- 100% WORKING DRAG SLIDER FOR SENSITIVITY ---
        context.drawTextWithShadow(screen.getTextRenderer(), "Sensitivity: §e" + ModConfig.cottonSensitivity + "%", setX + 150, setY + 50, 0xFFFFFF);
        
        // Slider Background (Track)
        int sliderX = setX + 150;
        int sliderY = setY + 65;
        int sliderW = 100;
        context.fill(sliderX, sliderY, sliderX + sliderW, sliderY + 10, 0xFF550000); 

        // Slider Thumb (Draggable Box)
        // Convert config 0-300 to slider 0-100 pixels
        int thumbX = sliderX + (int)((ModConfig.cottonSensitivity / 300.0f) * (sliderW - 10));
        context.fill(thumbX, sliderY, thumbX + 10, sliderY + 10, 0xFF00FFCC); // Cyan thumb
        
        context.drawTextWithShadow(screen.getTextRenderer(), "§7Living Legend Butter Feel!", setX, setY + 82, 0xAAAAAA);
        
        // 3. DEVICE COOLER (Multitasking Fix)
        screen.drawToggle(context, "Device Cooler (Anti-Heat)", setX, setY + 105, ModConfig.deviceCooler);
        context.drawTextWithShadow(screen.getTextRenderer(), "§7Drops to 10 FPS when multitasking/background to", setX, setY + 120, 0xAAAAAA);
        context.drawTextWithShadow(screen.getTextRenderer(), "§7stop phone heating. A must for mobile players!", setX, setY + 132, 0xAAAAAA);
    }

    // Handles Click inside the slider
    public static boolean mouseClicked(double mx, double my, int setX, int setY) {
        if (mx >= setX+120 && mx <= setX+150 && my >= setY && my <= setY+12) { ModConfig.fpsBoostEnabled = !ModConfig.fpsBoostEnabled; return true; }
        if (mx >= setX+120 && mx <= setX+150 && my >= setY+50 && my <= setY+62) { ModConfig.smoothGameEnabled = !ModConfig.smoothGameEnabled; return true; }
        if (mx >= setX+120 && mx <= setX+150 && my >= setY+105 && my <= setY+117) { ModConfig.deviceCooler = !ModConfig.deviceCooler; return true; }
        
        // Click on Slider
        int sliderX = setX + 150;
        int sliderY = setY + 65;
        if (mx >= sliderX && mx <= sliderX + 100 && my >= sliderY && my <= sliderY + 10) {
            isDraggingSlider = true;
            return true;
        }
        return false;
    }

    // Handles Dragging the slider thumb
    public static boolean mouseDragged(double mx, double my, int setX, int setY) {
        if (isDraggingSlider) {
            int sliderX = setX + 150;
            // Get percentage of mouse position within 100 pixels
            float pct = (float) (mx - sliderX) / 100.0f;
            pct = Math.max(0.0f, Math.min(1.0f, pct)); // Clamp between 0 and 1
            
            // Map 0-1 to 0-300
            ModConfig.cottonSensitivity = (int) (pct * 300.0f);
            return true;
        }
        return false;
    }
}
