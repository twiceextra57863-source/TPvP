package com.yourname.mtpvp.client.gui;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class MtpvpButtonWidget extends ButtonWidget {
    
    public MtpvpButtonWidget(int x, int y, int width, int height, PressAction onPress) {
        super(x, y, width, height, Text.literal("Mtpvp"), onPress, DEFAULT_NARRATION_SUPPLIER);
    }
}
