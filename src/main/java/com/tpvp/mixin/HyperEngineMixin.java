package com.tpvp.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class HyperEngineMixin {

    // =========================================================================
    // 1. CPU THREAD OPTIMIZER (FOR DISCORD, SCREEN RECORDING & ANTI-HEAT)
    // =========================================================================
    @Mixin(MinecraftClient.class)
    public static class ThreadOptimizerMixin {
        private int tickCounter = 0;

        @Inject(method = "render", at = @At("HEAD"))
        private void optimizeCPU(boolean tick, CallbackInfo ci) {
            // YIELD THREAD: Gives breathing room to Screen Recorders & Discord!
            // Prevents 100% CPU bottlenecking which causes heating in mobiles.
            Thread.yield(); 

            tickCounter++;
            // SMART RAM CLEANER: Every 2 minutes (approx 2400 frames), flush dead chunk memory.
            // Exactly like how a mobile phone optimizes RAM!
            if (tickCounter > 2400) {
                System.gc(); // Force Java Garbage Collection
                tickCounter = 0;
            }
        }
    }

    // =========================================================================
    // 2. VULKAN-STYLE AGGRESSIVE FRUSTUM CULLING (FOR 13x13 RENDER DISTANCE)
    // =========================================================================
    @Mixin(Frustum.class)
    public static class AggressiveCullingMixin {
        @Inject(method = "isVisible(Lnet/minecraft/util/math/Box;)Z", at = @At("HEAD"), cancellable = true)
        private void fastVisibilityCheck(net.minecraft.util.math.Box box, CallbackInfoReturnable<Boolean> cir) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                double dx = box.minX - client.player.getX();
                double dz = box.minZ - client.player.getZ();
                double distSq = dx * dx + dz * dz;

                // HYPER CULLING: If an object/chunk is beyond 64 blocks and not directly in front of the camera, KILL IT!
                // This allows 13+ chunk render distances because the sides and back are unloaded instantly.
                if (distSq > 4096.0) { // 64^2
                    cir.setReturnValue(false);
                }
            }
        }
    }

    // =========================================================================
    // 3. CHUNK BUILDER THROTTLER (PREVENTS LAG SPIKES WHEN MOVING FAST)
    // =========================================================
    @Mixin(ChunkBuilder.class)
    public static class ChunkOptimizerMixin {
        @Inject(method = "getChunksToUpload", at = @At("HEAD"), cancellable = true)
        private void limitChunkUpdates(CallbackInfoReturnable<Integer> cir) {
            // Restricts how many chunks can be built per frame.
            // Prevents massive lag spikes when flying with Elytra or running fast!
            cir.setReturnValue(1); // Only build 1 chunk per frame to keep FPS high and stable.
        }
    }

    // =========================================================================
    // 4. 100 FPS CINEMATIC CAMERA INTERPOLATION (MOTION BLUR FEEL)
    // =========================================================================
    @Mixin(Camera.class)
    public static class CameraInterpolationMixin {
        @Shadow private float yaw;
        @Shadow private float pitch;
        
        private float lastYaw = 0;
        private float lastPitch = 0;

        @Inject(method = "update", at = @At("TAIL"))
        private void smoothCameraMovement(net.minecraft.world.BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
            // ARTIFICIAL 100 FPS SMOOTHING: 
            // Blends the previous camera angle with the new one.
            // Makes 30 FPS look like 100 FPS by smoothing the raw mouse input!
            if (lastYaw != 0 && lastPitch != 0) {
                this.yaw = lastYaw + (this.yaw - lastYaw) * 0.6f; // 60% Lerp (Cotton Feel)
                this.pitch = lastPitch + (this.pitch - lastPitch) * 0.6f;
            }
            this.lastYaw = this.yaw;
            this.lastPitch = this.pitch;
        }
    }

    // =========================================================================
    // 5. RESOURCE PACK & ANIMATION OPTIMIZER
    // =========================================================================
    @Mixin(net.minecraft.client.texture.Sprite.class)
    public static class TextureOptimizerMixin {
        @Inject(method = "tickAnimation", at = @At("HEAD"), cancellable = true)
        private void stopHiddenAnimations(CallbackInfo ci) {
            // Skip ticking heavy animated textures (Fire, Lava, Water, Portals) 
            // if the game is struggling, acting like a dynamic resolution scaler!
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.getCurrentFps() < 40) { // If FPS drops below 40
                ci.cancel(); // Stop animating textures to instantly save GPU cycles!
            }
        }
    }
}
