package com.yourname.mtpvp.client.event;

import com.yourname.mtpvp.client.render.HeartIndicatorFeatureRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

public class FeatureRendererRegistration {
    
    public static void register() {
        LivingEntityFeatureRendererRegistrationCallback.EVENT.register(
            (entityType, entityRenderer, registrationHelper, context) -> {
                // Register for all living entities, but we'll filter in the renderer
                if (entityRenderer instanceof LivingEntityRenderer<?, ?> renderer) {
                    registrationHelper.register(new HeartIndicatorFeatureRenderer<>(renderer));
                }
            }
        );
    }
}
