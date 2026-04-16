package com.tpvp.mixin;

import com.tpvp.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class PerformanceMixin {

    // =========================================================
    // 1. AGGRESSIVE BLIND CULLING & FAST MATH (NON-VANILLA)
    // =========================================================
    @Mixin(EntityRenderDispatcher.class)
    public static class EntityRenderMixin {
        
        @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
        private void fastFrustumReject(Entity entity, net.minecraft.client.render.Frustum frustum, double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
            if (ModConfig.fpsBoostEnabled) {
                MinecraftClient client = MinecraftClient.getInstance();
                
                // INSTANT KILL: Invisible Armor Stands (Server Holograms)
                if (entity instanceof ArmorStandEntity && entity.isInvisible()) {
                    cir.setReturnValue(false);
                    return;
                }

                if (client.player != null && entity != client.player) {
                    double dx = entity.getX() - client.player.getX();
                    double dy = entity.getY() - client.player.getY();
                    double dz = entity.getZ() - client.player.getZ();
                    
                    // FastMath: Avoids expensive Math.sqrt()
                    double distSq = (dx * dx) + (dy * dy) + (dz * dz);

                    // 1. Distance Culling (32 Blocks Strict)
                    if (distSq > 1024.0) { 
                        cir.setReturnValue(false);
                        return;
                    }

                    // 2. DIRECTIONAL BLIND CULLING (Non-Vanilla)
                    // If entity is within 10-32 blocks, do a fast Dot Product check
                    if (distSq > 100.0) { 
                        Vec3d look = client.player.getRotationVec(1.0F);
                        double dot = (dx * look.x) + (dy * look.y) + (dz * look.z);
                        if (dot < 0) { // It's behind the camera!
                            cir.setReturnValue(false);
                            return;
                        }
                    }
                }
            }
        }
        
        @Inject(method = "renderFire", at = @At("HEAD"), cancellable = true)
        private void removeOtherPlayersFire(CallbackInfo ci) {
            if (ModConfig.fpsBoostEnabled) ci.cancel(); 
        }
    }

    // =========================================================
    // 2. PHYSICS ENGINE THROTTLE (NO SPINNING ITEMS)
    // =========================================================
    @Mixin(ItemEntity.class)
    public static class ItemEntityOptimizer {
        @Inject(method = "tick", at = @At("HEAD"))
        private void stopItemSpinningLag(CallbackInfo ci) {
            if (ModConfig.fpsBoostEnabled) {
                ItemEntity item = (ItemEntity)(Object)this;
                item.setYaw(0); // Locks rotation physics (Massive CPU saver on death drops)
            }
        }
    }

    // =========================================================
    // 3. BLOCK ENTITY FAST CULLING (NO DISTANT CHESTS)
    // =========================================================
    @Mixin(BlockEntityRenderDispatcher.class)
    public static class BlockEntityCullingMixin {
        @Inject(method = "render(Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V", at = @At("HEAD"), cancellable = true)
        private <E extends BlockEntity> void cullBlockEntities(E blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
            if (ModConfig.fpsBoostEnabled) {
                MinecraftClient client = MinecraftClient.getInstance();
                if (client.player != null) {
                    // Cull heavy blocks beyond 16 blocks (256 squared)
                    if (client.player.squaredDistanceTo(blockEntity.getPos().toCenterPos()) > 256.0) { 
                        ci.cancel();
                    }
                }
            }
        }
    }

    // =========================================================
    // 4. WEATHER & FOG KILLER (GPU FILL-RATE FIX)
    // =========================================================
    @Mixin(WorldRenderer.class)
    public static class WorldOptimizeMixin {
        
        // FIX: Parameterless Injection. Safe from ANY future updates!
        @Inject(method = "renderWeather", at = @At("HEAD"), cancellable = true)
        private void stopRainLag(CallbackInfo ci) {
            if (ModConfig.fpsBoostEnabled) ci.cancel(); 
        }
        
        // FIX: Removed parameter list from here too!
        @Inject(method = "renderClouds", at = @At("HEAD"), cancellable = true)
        private void stopCloudLag(CallbackInfo ci) {
            if (ModConfig.fpsBoostEnabled) ci.cancel(); 
        }
    }

    // =========================================================
    // 5. EXPLOSION & PARTICLE THROTTLER
    // =========================================================
    @Mixin(ParticleManager.class)
    public static class ParticleMixin {
        private int dropCounter = 0;

        @Inject(method = "addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)Lnet/minecraft/client/particle/Particle;", at = @At("HEAD"), cancellable = true)
        private void reduceLagParticles(CallbackInfoReturnable<?> cir) {
            if (ModConfig.fpsBoostEnabled) {
                dropCounter++;
                // Renders 1 out of 5 particles (80% drop)
                if (dropCounter % 5 != 0) {
                    cir.setReturnValue(null); 
                }
            }
        }
    }

    // =========================================================
    // 6. LTW RENEW & VBO BATCHER (UNLIMITED FPS FORCER)
    // =========================================================
    @Mixin(GameRenderer.class)
    public static class SmoothCameraMixin {
        @Inject(method = "render", at = @At("HEAD"))
        private void smoothAndCool(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
            MinecraftClient client = MinecraftClient.getInstance();

            // LTW TRANSLATION FIX
            if (ModConfig.fpsBoostEnabled && client.options != null) {
                if (client.options.getMaxFps().getValue() < 260) {
                    client.options.getMaxFps().setValue(260); 
                }
            }

            if (ModConfig.smoothGameEnabled && client.options != null) {
                client.options.smoothCameraEnabled = true; 
            } else if (!ModConfig.smoothGameEnabled && client.options != null) {
                if (client.options.smoothCameraEnabled) client.options.smoothCameraEnabled = false; 
            }
        }
    }

    // =========================================================
    // 7. TEXT SHADOW CULLING (MASSIVE 2D UI BOOST)
    // =========================================================
    @Mixin(net.minecraft.client.font.TextRenderer.class)
    public static class TextShadowCullingMixin {
        @ModifyVariable(method = "draw(Ljava/lang/String;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/font/TextRenderer$TextLayerType;II)I", at = @At("HEAD"), ordinal = 0, argsOnly = true)
        private boolean disableShadows(boolean shadow) {
            // Disables all text shadows
            if (ModConfig.fpsBoostEnabled) return false;
            return shadow;
        }
    }

    // =========================================================
    // 8. COTTON SENSITIVITY CONTROLLER
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
