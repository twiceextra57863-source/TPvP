package com.yourname.mtpvp.client.event;

import com.yourname.mtpvp.client.render.HeartIndicatorFeatureRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.LivingEntity;

public class FeatureRendererRegistration {
    
    public static void register() {
        LivingEntityFeatureRendererRegistrationCallback.EVENT.register(
            (entityType, entityRenderer, registrationHelper, context) -> {
                if (entityRenderer instanceof LivingEntityRenderer) {
                    registrationHelper.register(new HeartIndicatorFeatureRenderer(
                        (LivingEntityRenderer<LivingEntity, ?>) entityRenderer
                    ));
                }
            }
        );
    }
}
