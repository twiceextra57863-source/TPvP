package com.example.mod;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class DashboardScreen extends Screen {
    public DashboardScreen() {
        super(Text.literal("Mod Dashboard"));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2 - 100;
        
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Style: Vanilla Hearts"), b -> ModSettings.currentStyle = 0)
            .dimensions(centerX, 60, 200, 20).build());
            
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Style: Head & Hits"), b -> ModSettings.currentStyle = 1)
            .dimensions(centerX, 90, 200, 20).build());
            
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Style: Status Bar"), b -> ModSettings.currentStyle = 2)
            .dimensions(centerX, 120, 200, 20).build());
            
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> this.close())
            .dimensions(centerX, 160, 200, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }
}

