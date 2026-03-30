package com.yourname.mtpvp.client;

import com.yourname.mtpvp.client.event.FeatureRendererRegistration;
import net.fabricmc.api.ClientModInitializer;

public class MtpvpClient implements ClientModInitializer {
    
    @Override
    public void onInitializeClient() {
        FeatureRendererRegistration.register();
        System.out.println("§a[§6MTPVP§a] §fHeart indicator feature renderer registered!");
    }
}
