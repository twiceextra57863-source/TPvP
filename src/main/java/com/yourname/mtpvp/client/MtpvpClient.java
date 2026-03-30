package com.yourname.mtpvp.client;

import com.yourname.mtpvp.client.event.HeartIndicatorHandler;
import com.yourname.mtpvp.client.event.ScreenButtonHandler;
import net.fabricmc.api.ClientModInitializer;

public class MtpvpClient implements ClientModInitializer {
    
    @Override
    public void onInitializeClient() {
        ScreenButtonHandler.register();
        HeartIndicatorHandler.register();
        System.out.println("MTPVP Client initialized!");
    }
}
