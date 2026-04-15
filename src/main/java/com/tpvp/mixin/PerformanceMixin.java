package com.tpvp.mixin;

import com.tpvp.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class PerformanceMixin {

    // =========================================================
    // 1. ADVANCED ENTITY CULLING (PLAYERS, MOBS, FIRE)
    // =========================================================
    @Mixin(net.minecraft.client.render.entity.EntityRenderDispatcher.class)
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
        @Inject(method = "renderFire", at = @At("HEAD"), cancellable = true)
        private void removeOtherPlayersFire(CallbackInfo ci) {
            if (ModConfig.fpsBoostEnabled) ci.cancel(); 
        }
    }

    // =========================================================
    // 2. BLOCK ENTITY CULLING (CHESTS, BEDS, SIGNS - MASSIVE FPS)
    // =========================================================
    @Mixin(BlockEntityRenderDispatcher.class)
    public static class BlockEntityCullingMixin {
        @Inject(method = "render(Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V", at = @At("HEAD"), cancellable = true)
        private <E extends BlockEntity> void cullBlockEntities(E blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
            if (ModConfig.fpsBoostEnabled) {
                MinecraftClient client = MinecraftClient.getInstance();
                if (client.player != null) {
                    if (client.player.squaredDistanceTo(blockEntity.getPos().toCenterPos()) > 576.0) { // 24 squared
                        ci.cancel();
                    }
                }
            }
        }
    }

    // =========================================================
    // 3. WEATHER / FOG / WORLD OPTIMIZATIONS (CRASH FIXED!)
    // =========================================================
    @Mixin(WorldRenderer.class)
    public static class WorldOptimizeMixin {
        
        // FIX: Removed internal parameters! We just use simple 'CallbackInfo ci' to cancel rendering safely
        @Inject(method = "renderWeather", at = @At("HEAD"), cancellable = true)
        private void stopRainLag(CallbackInfo ci) {
            if (ModConfig.fpsBoostEnabled) ci.cancel();
        }
    }

    // =========================================================
    // 4. PARTICLES LIMITER
    // =========================================================
    @Mixin(ParticleManager.class)
    public static class ParticleMixin {
        @Inject(method = "addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)Lnet/minecraft/client/particle/Particle;", at = @At("HEAD"), cancellable = true)
        private void reduceLagParticles(CallbackInfoReturnable<?> cir) {
            if (ModConfig.fpsBoostEnabled && Math.random() > 0.15) cir.setReturnValue(null); 
        }
    }

    // =========================================================
    // 5. COTTON CAMERA & MULTITASKING COOLER
    // =========================================================
    @Mixin(GameRenderer.class)
    public static class SmoothCameraMixin {
        @Inject(method = "render", at = @At("HEAD"))
        private void smoothAndCool(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
            MinecraftClient client = MinecraftClient.getInstance();

            // Device Cooler logic
            if (ModConfig.deviceCooler && !client.isWindowFocused()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            // Cotton Camera Enable
            if (ModConfig.smoothGameEnabled && client.options != null) {
                client.options.smoothCameraEnabled = true; 
            } else if (!ModConfig.smoothGameEnabled && client.options != null) {
                if (client.options.smoothCameraEnabled) client.options.smoothCameraEnabled = false; 
            }
        }
    }

    // =========================================================
    // 6. COTTON SENSITIVITY CONTROLLER
    // =========================================================
    @Mixin(net.minecraft.client.Mouse.class)
    public static class MouseSensitivityMixin {
        @Shadow private double cursorDeltaX;
        @Shadow private double cursorDeltaY;

        @Inject(method = "updateMouse", at = @At("HEAD"))
        private void applyCottonSensitivity(CallbackInfo ci) {
            if (ModConfig.smoothGameEnabled) {
                double multiplier = ModConfig.cottonSensitivity / 100.0;
                this.cursorDeltaX *= multiplier;
                this.cursorDeltaY *= multiplier;
            }
        }
    }
}
