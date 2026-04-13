package com.tpvp.mixin;

import com.tpvp.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
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
                    if (entity.distanceTo(client.player) > 32.0) cir.setReturnValue(false);
                }
            }
        }

        // --- NEW: FIRE RENDERING LAG FIX ---
        @Inject(method = "renderFire", at = @At("HEAD"), cancellable = true)
        private void optimizeFire(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Entity entity, CallbackInfo ci) {
            if (ModConfig.fpsBoostEnabled) {
                MinecraftClient client = MinecraftClient.getInstance();
                // Extinguish visual fire on OTHER entities to save massive FPS in team fights
                if (entity != client.player) ci.cancel();
            }
        }
    }

    // 2. PARTICLES LAG FIX
    @Mixin(ParticleManager.class)
    public static class ParticleMixin {
        @Inject(method = "addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)Lnet/minecraft/client/particle/Particle;", at = @At("HEAD"), cancellable = true)
        private void reduceLagParticles(CallbackInfoReturnable<?> cir) {
            if (ModConfig.fpsBoostEnabled && Math.random() > 0.25) cir.setReturnValue(null);
        }
    }

    // 3. COTTON CAMERA SMOOTHNESS
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

    // 4. DEVICE COOLER (ANTI-HEAT FOR MOBILES)
    @Mixin(MinecraftClient.class)
    public static class BackgroundFPSMixin {
        @Inject(method = "getFramerateLimit", at = @At("HEAD"), cancellable = true)
        private void backgroundCooler(CallbackInfoReturnable<Integer> cir) {
            MinecraftClient client = (MinecraftClient)(Object)this;
            // If the game window is unfocused (multitasking/checking WhatsApp)
            if (ModConfig.deviceCooler && !client.isWindowFocused()) {
                cir.setReturnValue(10); // Force 10 FPS to cool down the phone battery!
            }
        }
    }

    // 5. COTTON CAMERA SENSITIVITY FIX
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
