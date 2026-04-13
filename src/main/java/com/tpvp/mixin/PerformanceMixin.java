package com.tpvp.mixin;

import com.tpvp.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class PerformanceMixin {

    // 1. ENTITY CULLING (FPS BOOST)
    @Mixin(EntityRenderDispatcher.class)
    public static class EntityRenderMixin {
        @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
        private void optimizeEntities(Entity entity, net.minecraft.client.render.Frustum frustum, double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
            if (ModConfig.fpsBoostEnabled && entity instanceof LivingEntity) {
                MinecraftClient client = MinecraftClient.getInstance();
                if (client.player != null && entity != client.player) {
                    if (entity.distanceTo(client.player) > 32.0) {
                        cir.setReturnValue(false);
                    }
                }
            }
        }
    }

    // 2. PARTICLES & EXPLOSION LAG FIX (FPS BOOST)
    @Mixin(ParticleManager.class)
    public static class ParticleMixin {
        @Inject(method = "addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)Lnet/minecraft/client/particle/Particle;", at = @At("HEAD"), cancellable = true)
        private void reduceLagParticles(CallbackInfoReturnable<?> cir) {
            if (ModConfig.fpsBoostEnabled) {
                if (Math.random() > 0.25) {
                    cir.setReturnValue(null);
                }
            }
        }
    }

    // 3. SMOOTH GAME (BUTTER CAMERA - COTTON FEEL)
    @Mixin(GameRenderer.class)
    public static class SmoothCameraMixin {
        
        // FIX FOR 1.21.4 CRASH:
        // Mojang updated the 'render' method parameters in GameRenderer.
        // It now takes a RenderTickCounter object instead of raw float tickDelta.
        @Inject(method = "render", at = @At("HEAD"))
        private void smoothCamera(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
            MinecraftClient client = MinecraftClient.getInstance();
            
            if (ModConfig.smoothGameEnabled && client.options != null) {
                client.options.smoothCameraEnabled = true; 
            } else if (!ModConfig.smoothGameEnabled && client.options != null && client.options.smoothCameraEnabled) {
                client.options.smoothCameraEnabled = false; 
            }
        }
    }
}
