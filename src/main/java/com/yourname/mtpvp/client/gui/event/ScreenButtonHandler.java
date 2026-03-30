package com.yourname.mtpvp.client.event;

import com.yourname.mtpvp.client.gui.MtpvpDashboardScreen;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class ScreenButtonHandler {
    
    public static void register() {
        // Add button to Title Screen
        ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof TitleScreen) {
                ScreenEvents.afterInit(screen).register(initScreen -> {
                    int centerX = initScreen.width / 2;
                    int buttonY = initScreen.height / 4 + 96 + 48;
                    
                    ButtonWidget mtpvpButton = ButtonWidget.builder(
                        Text.literal("Mtpvp"),
                        button -> client.setScreen(new MtpvpDashboardScreen(initScreen))
                    ).dimensions(centerX - 100, buttonY, 200, 20).build();
                    
                    initScreen.addDrawableChild(mtpvpButton);
                });
            }
            
            // Add button to Escape Menu
            if (screen instanceof GameMenuScreen) {
                ScreenEvents.afterInit(screen).register(initScreen -> {
                    int centerX = initScreen.width / 2;
                    int buttonY = initScreen.height / 4 + 96 + 48;
                    
                    ButtonWidget mtpvpButton = ButtonWidget.builder(
                        Text.literal("Mtpvp"),
                        button -> client.setScreen(new MtpvpDashboardScreen(initScreen))
                    ).dimensions(centerX - 100, buttonY, 200, 20).build();
                    
                    initScreen.addDrawableChild(mtpvpButton);
                });
            }
        });
    }
}
