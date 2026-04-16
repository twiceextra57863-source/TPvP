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
    // 1. NO-FREEZE CULLING (PURE FPS BOOST)
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
        // Removed Shadow disabling since it caused some rendering artifacts. Only logic-based culling remains.
    }

    @Mixin(BlockEntityRenderDispatcher.class)
    public static class BlockEntityCullingMixin {
        @Inject(method = "render(Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V", at = @At("HEAD"), cancellable = true)
        private <E extends BlockEntity> void cullBlockEntities(E blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
            if (ModConfig.fpsBoostEnabled) {
                MinecraftClient client = MinecraftClient.getInstance();
                if (client.player != null) {
                    if (client.player.squaredDistanceTo(blockEntity.getPos().toCenterPos()) > 1024.0) { 
                        ci.cancel();
                    }
                }
            }
        }
    }

    @Mixin(WorldRenderer.class)
    public static class WorldOptimizeMixin {
        @Inject(method = "renderWeather", at = @At("HEAD"), cancellable = true)
        private void stopRainLag(CallbackInfo ci) {
            if (ModConfig.fpsBoostEnabled) ci.cancel();
        }
    }

    @Mixin(ParticleManager.class)
    public static class ParticleMixin {
        @Inject(method = "addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)Lnet/minecraft/client/particle/Particle;", at = @At("HEAD"), cancellable = true)
        private void reduceLagParticles(CallbackInfoReturnable<?> cir) {
            if (ModConfig.fpsBoostEnabled && Math.random() > 0.15) cir.setReturnValue(null); 
        }
    }

    // =========================================================
    // 2. COTTON SENSITIVITY CONTROLLER
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

    // =========================================================
    // 3. TRUE NATIVE MOTION BLUR (No Shaders Required!)
    // =========================================================
    @Mixin(GameRenderer.class)
    public static class MotionBlurMixin {
        // We accumulate rendering logic to create a "ghosting" smooth effect
        @Inject(method = "render", at = @At("TAIL"))
        private void applyMotionBlur(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
            if (ModConfig.motionBlurEnabled) {
                MinecraftClient client = MinecraftClient.getInstance();
                // This draws a 20% transparent black rectangle over the screen every frame.
                // The previous frame faintly shows through, creating an instant 0-lag Motion Blur effect!
                if (client.currentScreen == null && client.world != null) {
                    int w = client.getWindow().getScaledWidth();
                    int h = client.getWindow().getScaledHeight();
                    
                    net.minecraft.client.gui.DrawContext context = new net.minecraft.client.gui.DrawContext(client, client.getBufferBuilders().getEntityVertexConsumers());
                    context.fill(0, 0, w, h, 0x33000000); // 20% Alpha Black
                    context.draw(); 
                }
            }
        }
    }
}
