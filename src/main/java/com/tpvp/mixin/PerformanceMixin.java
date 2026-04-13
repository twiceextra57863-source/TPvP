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
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class PerformanceMixin {

    // ---------------------------------------------------------
    // 1. ENTITY CULLING & NO-FIRE (FPS BOOST)
    // ---------------------------------------------------------
    @Mixin(EntityRenderDispatcher.class)
    public static class EntityRenderMixin {
        
        @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
        private void optimizeEntities(Entity entity, net.minecraft.client.render.Frustum frustum, double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
            if (ModConfig.fpsBoostEnabled && entity instanceof LivingEntity) {
                MinecraftClient client = MinecraftClient.getInstance();
                if (client.player != null && entity != client.player) {
                    if (entity.distanceTo(client.player) > 32.0) {
                        cir.setReturnValue(false); // Hides players far away
                    }
                }
            }
        }

        // --- CRASH FIXED: Parameter-less Inject ---
        // By not capturing the specific 1.21.4 parameters, we prevent any mapping crashes!
        // This also completely disables Fire rendering when FPS Boost is ON (Massive PvP Advantage!)
        @Inject(method = "renderFire", at = @At("HEAD"), cancellable = true)
        private void optimizeFire(CallbackInfo ci) {
            if (ModConfig.fpsBoostEnabled) {
                ci.cancel(); 
            }
        }
    }

    // ---------------------------------------------------------
    // 2. PARTICLES LAG FIX
    // ---------------------------------------------------------
    @Mixin(ParticleManager.class)
    public static class ParticleMixin {
        @Inject(method = "addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)Lnet/minecraft/client/particle/Particle;", at = @At("HEAD"), cancellable = true)
        private void reduceLagParticles(CallbackInfoReturnable<?> cir) {
            if (ModConfig.fpsBoostEnabled && Math.random() > 0.25) {
                cir.setReturnValue(null); // Removes 75% of useless particles
            }
        }
    }

    // ---------------------------------------------------------
    // 3. COTTON CAMERA SMOOTHNESS (Anti-Stutter)
    // ---------------------------------------------------------
    @Mixin(GameRenderer.class)
    public static class SmoothCameraMixin {
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

    // ---------------------------------------------------------
    // 4. DEVICE COOLER (ANTI-HEAT FOR MOBILES & POJAV LAUNCHER)
    // ---------------------------------------------------------
    @Mixin(MinecraftClient.class)
    public static class BackgroundFPSMixin {
        @Inject(method = "getFramerateLimit", at = @At("HEAD"), cancellable = true)
        private void backgroundCooler(CallbackInfoReturnable<Integer> cir) {
            MinecraftClient client = (MinecraftClient)(Object)this;
            if (ModConfig.deviceCooler && !client.isWindowFocused()) {
                cir.setReturnValue(10); // Drops FPS to 10 when multitasking to prevent phone heat
            }
        }
    }

    // ---------------------------------------------------------
    // 5. COTTON CAMERA SENSITIVITY FIX
    // ---------------------------------------------------------
    @Mixin(net.minecraft.client.Mouse.class)
    public static class MouseSensitivityMixin {
        @ModifyVariable(method = "updateMouse", at = @At("STORE"), ordinal = 0)
        private double applyCottonSensitivityX(double cursorDeltaX) {
            if (ModConfig.smoothGameEnabled) return cursorDeltaX * (ModConfig.cottonSensitivity / 100.0);
            return cursorDeltaX;
        }
        
        @ModifyVariable(method = "updateMouse", at = @At("STORE"), ordinal = 1)
        private double applyCottonSensitivityY(double cursorDeltaY) {
            if (ModConfig.smoothGameEnabled) return cursorDeltaY * (ModConfig.cottonSensitivity / 100.0);
            return cursorDeltaY;
        }
    }
}
