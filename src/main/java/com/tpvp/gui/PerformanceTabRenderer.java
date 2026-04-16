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
        
        context.drawTextWithShadow(screen.getTextRenderer(), "§7Aggressively culls blocks, removes shadows & clouds,", setX, setY + 18, 0xAAAAAA);
        context.drawTextWithShadow(screen.getTextRenderer(), "§7stops hurt-cam shake, and drops 85% particles.", setX, setY + 30, 0xAAAAAA);

        // ---------------------------------------------------
        // 2. KINETIC SMOOTH CAMERA (Spacing increased to +65)
        // ---------------------------------------------------
        int smoothY = setY + 65;
        screen.drawToggle(context, "Kinetic Smooth Camera", setX, smoothY, ModConfig.smoothGameEnabled);
        
        // Sensitivity Section
        context.drawTextWithShadow(screen.getTextRenderer(), "Sensitivity: §e" + ModConfig.cottonSensitivity + "%", setX + 150, smoothY, 0xFFFFFF);
        
        int sliderX = setX + 150;
        int sliderY = smoothY + 15;
        int sliderW = 100;
        
        // Background Track
        context.fill(sliderX, sliderY, sliderX + sliderW, sliderY + 10, 0xFF550000); 
        
        // Thumb (Draggable Box) - Math fixed to map 0-300% on 100px bar
        int thumbX = sliderX + (int)((ModConfig.cottonSensitivity / 300.0f) * (sliderW - 10));
        context.fill(thumbX, sliderY, thumbX + 10, sliderY + 10, 0xFF00FFCC); // Cyan thumb
        
        context.drawTextWithShadow(screen.getTextRenderer(), "§7Esports-level buttery smooth aim tracking!", setX, smoothY + 32, 0xAAAAAA);
        
        // ---------------------------------------------------
        // 3. DEVICE COOLER (Spacing increased to +125)
        // ---------------------------------------------------
        int coolerY = setY + 125;
        screen.drawToggle(context, "Multitasking Cooler", setX, coolerY, ModConfig.deviceCooler);
        
        context.drawTextWithShadow(screen.getTextRenderer(), "§7Drops game to 10 FPS when listening to music or", setX, coolerY + 18, 0xAAAAAA);
        context.drawTextWithShadow(screen.getTextRenderer(), "§7multitasking to prevent phone heating & battery drain.", setX, coolerY + 30, 0xAAAAAA);
    }

    public static boolean mouseClicked(double mx, double my, int setX, int setY) {
        int smoothY = setY + 65;
        int coolerY = setY + 125;
        
        // Toggles Click Detection
        if (mx >= setX+120 && mx <= setX+150 && my >= setY && my <= setY+12) { ModConfig.fpsBoostEnabled = !ModConfig.fpsBoostEnabled; return true; }
        if (mx >= setX+120 && mx <= setX+150 && my >= smoothY && my <= smoothY+12) { ModConfig.smoothGameEnabled = !ModConfig.smoothGameEnabled; return true; }
        if (mx >= setX+120 && mx <= setX+150 && my >= coolerY && my <= coolerY+12) { ModConfig.deviceCooler = !ModConfig.deviceCooler; return true; }
        
        // Slider Click Detection
        int sliderX = setX + 150, sliderY = smoothY + 15;
        if (mx >= sliderX && mx <= sliderX + 100 && my >= sliderY && my <= sliderY + 10) { 
            isDraggingSlider = true; 
            return true; 
        }
        
        return false;
    }

    public static boolean mouseDragged(double mx, double my, int setX, int setY) {
        if (isDraggingSlider) {
            int sliderX = setX + 150;
            // Maps mouse X position to 0.0 - 1.0
            float pct = (float) (mx - sliderX) / 90.0f; // 90 to account for thumb width
            pct = Math.max(0.0f, Math.min(1.0f, pct)); 
            
            // Scales to 0 - 300%
            ModConfig.cottonSensitivity = (int) (pct * 300.0f);
            return true;
        }
        return false;
    }

    // --- NAYA: SCROLL LOGIC FOR SENSITIVITY SLIDER ---
    public static boolean mouseScrolled(double mx, double my, int setX, int setY, double scroll) {
        int smoothY = setY + 65;
        int sliderX = setX + 150, sliderY = smoothY + 15;
        
        // Agar mouse slider area me hai, toh scroll karne pe sensitivity +- 10 hogi
        if (mx >= sliderX - 20 && mx <= sliderX + 120 && my >= sliderY - 10 && my <= sliderY + 20) {
            ModConfig.cottonSensitivity += (int) (scroll * 10); // Scroll UP = +10, DOWN = -10
            
            // Clamping bounds
            if (ModConfig.cottonSensitivity < 0) ModConfig.cottonSensitivity = 0;
            if (ModConfig.cottonSensitivity > 300) ModConfig.cottonSensitivity = 300;
            return true;
        }
        return false;
    }
}
