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
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class PerformanceMixin {

    // ---------------------------------------------------------
    // 1. ENTITY CULLING (FPS BOOST)
    // ---------------------------------------------------------
    @Mixin(EntityRenderDispatcher.class)
    public static class EntityRenderMixin {
        @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
        private void optimizeEntities(Entity entity, net.minecraft.client.render.Frustum frustum, double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
            if (ModConfig.fpsBoostEnabled && entity instanceof LivingEntity) {
                MinecraftClient client = MinecraftClient.getInstance();
                if (client.player != null && entity != client.player) {
                    if (entity.distanceTo(client.player) > 32.0) {
                        cir.setReturnValue(false); // Culls entity to save rendering resources
                    }
                }
            }
        }

        @Inject(method = "renderFire", at = @At("HEAD"), cancellable = true)
        private void optimizeFire(CallbackInfo ci) {
            if (ModConfig.fpsBoostEnabled) ci.cancel(); // Stops rendering fire on other entities
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
                cir.setReturnValue(null); // Removes 75% of non-essential particles
            }
        }
    }

    // ---------------------------------------------------------
    // 3. COTTON CAMERA SMOOTHNESS & MOTION BLUR (1.21.4 FIX)
    // ---------------------------------------------------------
    @Mixin(GameRenderer.class)
    public static class SmoothCameraMixin {
        @Inject(method = "render", at = @At("HEAD"))
        private void smoothCamera(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
            MinecraftClient client = MinecraftClient.getInstance();
            GameRenderer renderer = (GameRenderer)(Object)this;

            if (ModConfig.smoothGameEnabled && client.options != null) {
                client.options.smoothCameraEnabled = true; 

                // FIX: Minecraft 1.21.4 uses 'getPostProcessor' differently, but 'loadPostProcessor' was renamed
                // In modern fabric, it's called 'loadPostProcessor' mapping, but sometimes it throws.
                // Best fallback is to safely check using modern mapping.
                Identifier blurShader = Identifier.ofVanilla("shaders/post/phosphor.json");
                if (client.getCameraEntity() != null) {
                    try {
                        // We use the direct modern Yarn mapped method for applying Shaders
                        renderer.loadPostProcessor(blurShader);
                    } catch (Exception e) {
                        // Failsafe in case device doesn't support the blur shader
                    }
                }
            } else if (!ModConfig.smoothGameEnabled && client.options != null) {
                if (client.options.smoothCameraEnabled) client.options.smoothCameraEnabled = false; 
                
                try {
                    renderer.disablePostProcessor();
                } catch (Exception e) {}
            }
        }
    }

    // ---------------------------------------------------------
    // 4. DEVICE COOLER (ANTI-HEAT FOR MOBILES) - 1.21.4 FIX
    // ---------------------------------------------------------
    @Mixin(MinecraftClient.class)
    public static class BackgroundFPSMixin {
        // FIX: The method name in 1.21.4 mappings is "getFramerateLimit" returning an int
        @Inject(method = "getFramerateLimit", at = @At("HEAD"), cancellable = true)
        private void backgroundCooler(CallbackInfoReturnable<Integer> cir) {
            MinecraftClient client = (MinecraftClient)(Object)this;
            if (ModConfig.deviceCooler && !client.isWindowFocused()) {
                cir.setReturnValue(10); // Throttle FPS to 10 to cool the device when multitasking
            }
        }
    }

    // ---------------------------------------------------------
    // 5. COTTON CAMERA SENSITIVITY FIX (SLIDER ATTACHED)
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
