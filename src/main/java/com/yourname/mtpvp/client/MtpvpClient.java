package com.yourname.mtpvp.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class MtpvpClient implements ClientModInitializer {
    
    @Override
    public void onInitializeClient() {
        System.out.println("MTPVP Client initialized!");
    }
}
