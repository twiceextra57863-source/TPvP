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
import net.minecraft.util.math.Vec3d; // FIX: Added missing Vec3d import!
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class PerformanceMixin {

    // =========================================================
    // 1. GHOST ENTITY CULLING & NO-FIRE (Massive FPS in Team Fights)
    // =========================================================
    @Mixin(net.minecraft.client.render.entity.EntityRenderDispatcher.class)
    public static class EntityRenderMixin {
        
        @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
        private void optimizeEntities(Entity entity, net.minecraft.client.render.Frustum frustum, double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
            if (ModConfig.fpsBoostEnabled && entity instanceof LivingEntity) {
                MinecraftClient client = MinecraftClient.getInstance();
                
                if (client.player != null && entity != client.player) {
                    double dist = entity.distanceTo(client.player);
                    
                    // If target is far away, immediately drop it
                    if (dist > 32.0) {
                        cir.setReturnValue(false); 
                    } 
                    // If there are too many players around, aggressively cull those that are not in direct line of sight
                    else if (dist > 10.0 && client.world != null && client.world.getPlayers().size() > 15) {
                        Vec3d lookVec = client.player.getRotationVec(1.0F);
                        Vec3d dirToTarget = entity.getPos().subtract(client.player.getPos()).normalize();
                        
                        // Dot Product Math: Hides entities behind the player's camera to save extreme GPU power
                        if (lookVec.dotProduct(dirToTarget) < 0.2) { 
                            cir.setReturnValue(false);
                        }
                    }
                }
            }
        }
        
        @Inject(method = "renderFire", at = @At("HEAD"), cancellable = true)
        private void removeOtherPlayersFire(CallbackInfo ci) {
            if (ModConfig.fpsBoostEnabled) ci.cancel(); // Stops rendering laggy fire on others!
        }
    }

    // =========================================================
    // 2. BLOCK ENTITY CULLING (Hides Chests/Signs out of sight)
    // =========================================================
    @Mixin(BlockEntityRenderDispatcher.class)
    public static class BlockEntityCullingMixin {
        @Inject(method = "render(Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V", at = @At("HEAD"), cancellable = true)
        private <E extends BlockEntity> void cullBlockEntities(E blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
            if (ModConfig.fpsBoostEnabled) {
                MinecraftClient client = MinecraftClient.getInstance();
                if (client.player != null) {
                    // Cull heavy block models beyond 24 blocks (576 squared)
                    if (client.player.squaredDistanceTo(blockEntity.getPos().toCenterPos()) > 576.0) { 
                        ci.cancel();
                    }
                }
            }
        }
    }

    // =========================================================
    // 3. WEATHER / FOG / WORLD OPTIMIZATIONS
    // =========================================================
    @Mixin(WorldRenderer.class)
    public static class WorldOptimizeMixin {
        @Inject(method = "renderWeather", at = @At("HEAD"), cancellable = true)
        private void stopRainLag(CallbackInfo ci) {
            // Completely kills rain/snow geometry rendering
            if (ModConfig.fpsBoostEnabled) ci.cancel(); 
        }
    }

    // =========================================================
    // 4. TNT BLAST & PARTICLE THROTTLER
    // =========================================================
    @Mixin(ParticleManager.class)
    public static class ParticleMixin {
        private int particleCount = 0;

        @Inject(method = "addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)Lnet/minecraft/client/particle/Particle;", at = @At("HEAD"), cancellable = true)
        private void reduceLagParticles(CallbackInfoReturnable<?> cir) {
            if (ModConfig.fpsBoostEnabled) {
                particleCount++;
                // Strict throttling: Only renders 1 out of every 5 particles (80% drop)
                // This completely prevents FPS drops during massive TNT/Crystal explosions!
                if (particleCount % 5 != 0) {
                    cir.setReturnValue(null); 
                }
            }
        }
    }

    // =========================================================
    // 5. BACKGROUND FPS THROTTLE (Anti-Heat) & SMOOTH CAMERA
    // =========================================================
    @Mixin(GameRenderer.class)
    public static class SmoothCameraMixin {
        @Inject(method = "render", at = @At("HEAD"))
        private void smoothAndCool(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
            MinecraftClient client = MinecraftClient.getInstance();

            // Device Cooler: Cools down phone/CPU when switching to other apps
            if (ModConfig.deviceCooler && !client.isWindowFocused()) {
                try { 
                    Thread.sleep(100); 
                } catch (InterruptedException e) { 
                    Thread.currentThread().interrupt(); 
                }
            }

            // Cinematic Camera Enabler
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
                // Multiplies raw mouse inputs smoothly
                double multiplier = ModConfig.cottonSensitivity / 100.0;
                this.cursorDeltaX *= multiplier;
                this.cursorDeltaY *= multiplier;
            }
        }
    }
}
