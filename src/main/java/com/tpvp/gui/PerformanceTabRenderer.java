package com.tpvp.gui;

import com.tpvp.config.ModConfig;
import net.minecraft.client.gui.DrawContext;

public class PerformanceTabRenderer {

    public static boolean isDraggingSlider = false;

    public static void render(TPvPDashboardScreen screen, DrawContext context, int setX, int setY, int mx, int my) {
        
        // 1. HYPER ENGINE
        screen.drawToggle(context, "Hyper-Engine (FPS Boost)", setX, setY, ModConfig.fpsBoostEnabled);
        context.drawTextWithShadow(screen.getTextRenderer(), "§7Culls distant blocks & entities. Drops 85% of", setX, setY + 18, 0xAAAAAA);
        context.drawTextWithShadow(screen.getTextRenderer(), "§7particles (TNT/Pots) to prevent PvP stuttering.", setX, setY + 30, 0xAAAAAA);

        // 2. KINETIC SMOOTH CAMERA
        int smoothY = setY + 65;
        screen.drawToggle(context, "Kinetic Smooth Camera", setX, smoothY, ModConfig.smoothGameEnabled);
        
        context.drawTextWithShadow(screen.getTextRenderer(), "Sensitivity: §e" + ModConfig.cottonSensitivity + "%", setX + 150, smoothY, 0xFFFFFF);
        
        int sliderX = setX + 150;
        int sliderY = smoothY + 15;
        int sliderW = 100;
        context.fill(sliderX, sliderY, sliderX + sliderW, sliderY + 10, 0xFF550000); 
        int thumbX = sliderX + (int)((ModConfig.cottonSensitivity / 300.0f) * (sliderW - 10));
        context.fill(thumbX, sliderY, thumbX + 10, sliderY + 10, 0xFF00FFCC); 
        
        context.drawTextWithShadow(screen.getTextRenderer(), "§7Adjust this multiplier for buttery smooth aiming.", setX, smoothY + 32, 0xAAAAAA);
        
        // 3. MOTION BLUR (Cinematic Effect)
        int blurY = setY + 125;
        screen.drawToggle(context, "Native Motion Blur", setX, blurY, ModConfig.motionBlurEnabled);
        
        context.drawTextWithShadow(screen.getTextRenderer(), "§7Creates a trailing 'Living Legend' cinematic blur.", setX, blurY + 18, 0xAAAAAA);
        context.drawTextWithShadow(screen.getTextRenderer(), "§7Works on any FPS without using expensive shaders!", setX, blurY + 30, 0xAAAAAA);
    }

    public static boolean mouseClicked(double mx, double my, int setX, int setY) {
        int smoothY = setY + 65;
        int blurY = setY + 125;
        
        if (mx >= setX+120 && mx <= setX+150 && my >= setY && my <= setY+12) { ModConfig.fpsBoostEnabled = !ModConfig.fpsBoostEnabled; return true; }
        if (mx >= setX+120 && mx <= setX+150 && my >= smoothY && my <= smoothY+12) { ModConfig.smoothGameEnabled = !ModConfig.smoothGameEnabled; return true; }
        if (mx >= setX+120 && mx <= setX+150 && my >= blurY && my <= blurY+12) { ModConfig.motionBlurEnabled = !ModConfig.motionBlurEnabled; return true; }
        
        int sliderX = setX + 150, sliderY = smoothY + 15;
        if (mx >= sliderX && mx <= sliderX + 100 && my >= sliderY && my <= sliderY + 10) { isDraggingSlider = true; return true; }
        
        return false;
    }

    public static boolean mouseDragged(double mx, double my, int setX, int setY) {
        if (isDraggingSlider) {
            int sliderX = setX + 150;
            float pct = (float) (mx - sliderX) / 90.0f;
            pct = Math.max(0.0f, Math.min(1.0f, pct)); 
            ModConfig.cottonSensitivity = (int) (pct * 300.0f);
            return true;
        }
        return false;
    }

    public static boolean mouseScrolled(double mx, double my, int setX, int setY, double scroll) {
        int smoothY = setY + 65;
        int sliderX = setX + 150, sliderY = smoothY + 15;
        if (mx >= sliderX - 20 && mx <= sliderX + 120 && my >= sliderY - 10 && my <= sliderY + 20) {
            ModConfig.cottonSensitivity += (int) (scroll * 10); 
            if (ModConfig.cottonSensitivity < 0) ModConfig.cottonSensitivity = 0;
            if (ModConfig.cottonSensitivity > 300) ModConfig.cottonSensitivity = 300;
            return true;
        }
        return false;
    }
}
