package com.yourname.mtpvp.client;

import net.fabricmc.api.ClientModInitializer;

public class MtpvpClient implements ClientModInitializer {
    
    @Override
    public void onInitializeClient() {
        System.out.println("§a[§6MTPVP§a] §fMod initialized with LivingEntityRendererMixin!");
    }
}
