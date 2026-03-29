package com.mtpvp.gui;

import com.mtpvp.config.MtpvpConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class MtpvpDashboard extends Screen {
    private final Screen parent;

    public MtpvpDashboard(Screen parent) {
        super(Text.literal("MTPVP DASHBOARD"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int x = 110;
        // Style Buttons
        addBtn("Style: Progress Bar", 0, x, 60);
        addBtn("Style: Vanilla Hearts", 1, x, 90);
        addBtn("Style: Head & Hits", 2, x, 120);

        // Save
        this.addDrawableChild(ButtonWidget.builder(Text.literal("§a✔ SAVE & APPLY"), (btn) -> this.close())
            .dimensions(this.width - 110, this.height - 30, 100, 20).build());
    }

    private void addBtn(String label, int mode, int x, int y) {
        this.addDrawableChild(ButtonWidget.builder(Text.literal(label), (btn) -> MtpvpConfig.healthMode = mode)
            .dimensions(x, y, 160, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        // Sidebar background
        context.fill(0, 0, 100, this.height, 0xDD000000);
        context.fill(100, 0, 101, this.height, 0xFF00FFFF);
        
        context.drawCenteredTextWithShadow(textRenderer, "§bMTPVP", 50, 20, 0xFFFFFF);
        context.drawTextWithShadow(textRenderer, "§e> Indicators", 10, 65, 0xFFFFFF);
        
        // Active Indicator highlight
        context.drawTextWithShadow(textRenderer, "Status: §aRunning", 110, 20, 0xFFFFFF);
        context.drawTextWithShadow(textRenderer, "Mode: §bStyle " + MtpvpConfig.healthMode, 110, 35, 0xFFFFFF);

        super.render(context, mouseX, mouseY, delta);
    }
}
