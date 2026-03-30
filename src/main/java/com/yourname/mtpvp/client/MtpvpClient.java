package com.yourname.mtpvp.client;

import com.yourname.mtpvp.client.event.HeartIndicatorHandler;
import net.fabricmc.api.ClientModInitializer;

public class MtpvpClient implements ClientModInitializer {
    
    @Override
    public void onInitializeClient() {
        HeartIndicatorHandler.register();
        System.out.println("§a[§6MTPVP§a] §fMod initialized successfully!");
    }
}
