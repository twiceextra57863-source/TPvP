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
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class PerformanceMixin {

    // =========================================================
    // 1. ENTITY CULLING & FIRE OPTIMIZATION (FPS BOOST)
    // =========================================================
    @Mixin(EntityRenderDispatcher.class)
    public static class EntityRenderMixin {

        @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
        private void optimizeEntities(Entity entity, net.minecraft.client.render.Frustum frustum, double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
            if (ModConfig.fpsBoostEnabled && entity instanceof LivingEntity) {
                MinecraftClient client = MinecraftClient.getInstance();
                
                // Culling Logic: Stop rendering entities that are:
                // 1. Not the client player
                // 2. Further than 32 blocks away
                if (client.player != null && entity != client.player) {
                    if (entity.distanceTo(client.player) > 32.0) {
                        cir.setReturnValue(false); // Instantly frees up GPU!
                    }
                }
            }
        }

        // SAFE FIRE OPTIMIZATION
        @Inject(method = "renderFire", at = @At("HEAD"), cancellable = true)
        private void removeOtherPlayersFire(CallbackInfo ci) {
            if (ModConfig.fpsBoostEnabled) {
                // Stops rendering the ugly fire overlay on OTHER players, saving massive FPS in team fights.
                ci.cancel(); 
            }
        }
    }

    // =========================================================
    // 2. PARTICLES & EXPLOSION LAG FIX (FPS BOOST)
    // =========================================================
    @Mixin(ParticleManager.class)
    public static class ParticleMixin {

        @Inject(method = "addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)Lnet/minecraft/client/particle/Particle;", at = @At("HEAD"), cancellable = true)
        private void reduceLagParticles(CallbackInfoReturnable<?> cir) {
            // Removes 85% of useless particles (splash potions, explosions, water drops) to save FPS
            if (ModConfig.fpsBoostEnabled && Math.random() > 0.15) {
                cir.setReturnValue(null); 
            }
        }
    }

    // =========================================================
    // 3. COTTON CAMERA & DEVICE COOLER (ANTI-HEAT)
    // =========================================================
    @Mixin(GameRenderer.class)
    public static class SmoothCameraMixin {

        @Inject(method = "render", at = @At("HEAD"))
        private void smoothAndCool(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
            MinecraftClient client = MinecraftClient.getInstance();
            
            // --- DEVICE COOLER (Multitasking Fix) ---
            // If the game window is unfocused (multitasking/checking WhatsApp)
            if (ModConfig.deviceCooler && !client.isWindowFocused()) {
                try {
                    // Force the thread to sleep for 100ms. Drops game to 10 FPS.
                    // This instantly cools down the phone battery and CPU!
                    Thread.sleep(100); 
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            // --- COTTON SMOOTH CAMERA ---
            if (ModConfig.smoothGameEnabled && client.options != null) {
                client.options.smoothCameraEnabled = true; 
            } else if (!ModConfig.smoothGameEnabled && client.options != null && client.options.smoothCameraEnabled) {
                client.options.smoothCameraEnabled = false; 
            }
        }
    }

    // =========================================================
    // 4. COTTON CAMERA SENSITIVITY CONTROLLER (CRASH FIXED!)
    // =========================================================
    @Mixin(net.minecraft.client.Mouse.class)
    public static class MouseSensitivityMixin {
        
        // Use @Shadow to directly access Minecraft's raw mouse data without fragile @ModifyVariable injections
        @Shadow private double cursorDeltaX;
        @Shadow private double cursorDeltaY;

        @Inject(method = "updateMouse", at = @At("HEAD"))
        private void applyCottonSensitivity(CallbackInfo ci) {
            if (ModConfig.smoothGameEnabled) {
                // Fetch our slider value from the Dashboard (e.g., 150 = 1.5x speed)
                double multiplier = ModConfig.cottonSensitivity / 100.0;
                
                // Directly multiply the raw mouse movement before Minecraft processes it!
                this.cursorDeltaX *= multiplier;
                this.cursorDeltaY *= multiplier;
            }
        }
    }
}
