package com.example.heartindicator.client.gui;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class HeartIndicatorButton extends ButtonWidget {
    public HeartIndicatorButton(int x, int y, int width, int height, Text message, PressAction onPress) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION_SUPPLIER);
    }
}
