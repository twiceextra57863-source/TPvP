package com.tpvp.mixin;

import com.tpvp.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.render.GameRenderer;
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
    // Jab bahut saare players/mobs aaspas hon, jo players 32 blocks se door hain, unko render mat karo.
    @Mixin(EntityRenderDispatcher.class)
    public static class EntityRenderMixin {
        @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
        private void optimizeEntities(Entity entity, net.minecraft.client.render.Frustum frustum, double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
            if (ModConfig.fpsBoostEnabled && entity instanceof LivingEntity) {
                MinecraftClient client = MinecraftClient.getInstance();
                if (client.player != null && entity != client.player) {
                    // Agar player 32 blocks se door hai, usko invisible (cull) kardo = INSTANT FPS BOOST!
                    if (entity.distanceTo(client.player) > 32.0) {
                        cir.setReturnValue(false);
                    }
                }
            }
        }
    }

    // 2. PARTICLES & EXPLOSION LAG FIX (FPS BOOST)
    // Explosion, Water aur Potion particles ko 80% kam kar dega taaki game na atke.
    @Mixin(ParticleManager.class)
    public static class ParticleMixin {
        @Inject(method = "addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)Lnet/minecraft/client/particle/Particle;", at = @At("HEAD"), cancellable = true)
        private void reduceLagParticles(CallbackInfoReturnable<?> cir) {
            if (ModConfig.fpsBoostEnabled) {
                // Har 4 me se sirf 1 particle draw karo (75% Particle Lag Kam!)
                if (Math.random() > 0.25) {
                    cir.setReturnValue(null);
                }
            }
        }
    }

    // 3. SMOOTH GAME (BUTTER CAMERA - COTTON FEEL)
    // Vanilla camera movements me micro-stutters hote hain. Ye usko interpolate karke Cinematic bana dega.
    @Mixin(GameRenderer.class)
    public static class SmoothCameraMixin {
        @Inject(method = "render", at = @At("HEAD"))
        private void smoothCamera(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (ModConfig.smoothGameEnabled && client.options != null) {
                // Jab tak Smooth Game ON hai, Minecraft ka inbuilt Cinematic Camera (Smooth Camera) forcefully ON rahega.
                client.options.smoothCameraEnabled = true; 
            } else if (!ModConfig.smoothGameEnabled && client.options != null && client.options.smoothCameraEnabled) {
                client.options.smoothCameraEnabled = false; // Turn off agar disabled hai
            }
        }
    }
}
