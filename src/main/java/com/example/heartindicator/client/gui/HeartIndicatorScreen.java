package com.example.heartindicator.client.gui;

import com.example.heartindicator.HeartIndicatorMod;
import com.example.heartindicator.config.ModConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class HeartIndicatorScreen extends Screen {
    private static final Text TITLE = Text.literal("Heart Indicator Settings");
    private ButtonWidget toggleButton;

    protected HeartIndicatorScreen() {
        super(TITLE);
    }

    @Override
    protected void init() {
        super.init();
        ModConfig config = HeartIndicatorMod.getConfig();

        toggleButton = ButtonWidget.builder(
                getToggleText(config.isEnabled()),
                button -> {
                    boolean newState = !HeartIndicatorMod.getConfig().isEnabled();
                    HeartIndicatorMod.getConfig().setEnabled(newState);
                    button.setMessage(getToggleText(newState));
                })
                .dimensions(this.width / 2 - 100, this.height / 2 - 20, 200, 20)
                .build();
        this.addDrawableChild(toggleButton);

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Back"), button -> this.close())
                .dimensions(this.width / 2 - 100, this.height / 2 + 20, 200, 20)
                .build());
    }

    private Text getToggleText(boolean enabled) {
        return Text.literal("Heart Indicator: " + (enabled ? "ON" : "OFF"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, TITLE, this.width / 2, 40, 0xFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }
}
