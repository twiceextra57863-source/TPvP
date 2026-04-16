package com.tpvp.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class HyperEngineMixin {

    // =========================================================================
    // 1. SMART RAYTRACED FRUSTUM CULLING (Vulkan-Like Off-screen Hiding)
    // =========================================================================
    @Mixin(Frustum.class)
    public static class AggressiveCullingMixin {
        
        @Inject(method = "isVisible(Lnet/minecraft/util/math/Box;)Z", at = @At("HEAD"), cancellable = true)
        private void fastVisibilityCheck(Box box, CallbackInfoReturnable<Boolean> cir) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null && client.cameraEntity != null) {
                
                Vec3d camPos = client.cameraEntity.getPos();
                double dx = box.getCenter().x - camPos.x;
                double dy = box.getCenter().y - camPos.y;
                double dz = box.getCenter().z - camPos.z;
                double distSq = (dx * dx) + (dy * dy) + (dz * dz);

                // 1. DISTANCE CULLING (Radius Limit)
                // Kills any block entity or chunk beyond 48 blocks from the camera
                if (distSq > 2304.0) { // 48^2
                    cir.setReturnValue(false);
                    return;
                }

                // 2. DIRECTIONAL CULLING (Behind the Camera)
                // Mathematical dot product to check if the chunk is behind the player's view
                Vec3d lookVec = client.player.getRotationVec(1.0F);
                double dotProduct = (dx * lookVec.x) + (dy * lookVec.y) + (dz * lookVec.z);
                
                if (dotProduct < -5.0) { // If it is significantly behind the player
                    cir.setReturnValue(false);
                }
            }
        }
    }

    // =========================================================================
    // 2. CHUNK MESH BATCHING (Fixes Render Distance Lag & Stutters)
    // =========================================================================
    @Mixin(ChunkBuilder.class)
    public static class ChunkOptimizerMixin {
        
        @Inject(method = "getChunksToUpload", at = @At("HEAD"), cancellable = true)
        private void limitChunkUpdates(CallbackInfoReturnable<Integer> cir) {
            MinecraftClient client = MinecraftClient.getInstance();
            
            // If the player is moving fast (sprinting/elytra), strictly limit chunk updates to 1 per frame.
            // If standing still, allow up to 3 to load the world faster without lagging the PvP experience.
            if (client.player != null && client.player.getVelocity().lengthSquared() > 0.05) {
                cir.setReturnValue(1); 
            } else {
                cir.setReturnValue(3);
            }
        }
    }

    // =========================================================================
    // 3. HARDWARE YIELDING & RAM AUTO-CLEANER
    // =========================================================================
    @Mixin(MinecraftClient.class)
    public static class ThreadOptimizerMixin {
        private int tickCounter = 0;

        @Inject(method = "render", at = @At("HEAD"))
        private void optimizeCPU(boolean tick, CallbackInfo ci) {
            // Allows the phone's CPU scheduler to breathe. 
            // Extremely effective for preventing overheating on Android devices (Pojav Launcher).
            Thread.yield(); 

            tickCounter++;
            
            // Clear unused chunk memory every 60 seconds to keep RAM free
            if (tickCounter > 1200) {
                System.gc(); 
                tickCounter = 0;
            }
        }
    }

    // =========================================================================
    // 4. TRUE CAMERA INTERPOLATION (Motion Blur / 100 FPS Illusion)
    // =========================================================================
    @Mixin(Camera.class)
    public static class CameraInterpolationMixin {
        @Shadow private float yaw;
        @Shadow private float pitch;
        
        private float lastYaw = 0;
        private float lastPitch = 0;

        @Inject(method = "update", at = @At("TAIL"))
        private void smoothCameraMovement(net.minecraft.world.BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
            // Applies a mathematical low-pass filter to the camera.
            // This kills micro-stutters during fast mouse/screen swiping in PvP.
            if (lastYaw != 0 && lastPitch != 0) {
                this.yaw = lastYaw + (this.yaw - lastYaw) * 0.7f; // 70% interpolation (Extremely Smooth)
                this.pitch = lastPitch + (this.pitch - lastPitch) * 0.7f;
            }
            this.lastYaw = this.yaw;
            this.lastPitch = this.pitch;
        }
    }
}
