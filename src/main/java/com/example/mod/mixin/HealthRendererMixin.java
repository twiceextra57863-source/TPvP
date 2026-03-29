package com.example.mod.mixin;

import com.example.mod.ModSettings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class HealthRendererMixin<T extends Entity> {

    @Inject(method = "render", at = @At("TAIL"))
    public void onRender(T entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (!(entity instanceof PlayerEntity player) || player == MinecraftClient.getInstance().player || player.isInvisible()) return;

        double dist = player.squaredDistanceTo(MinecraftClient.getInstance().player);
        if (dist > 400) return; // 20 blocks se zyada dur na dikhe

        matrices.push();
        // Dynamic Height based on distance to prevent clipping
        float height = player.getHeight() + 0.5f;
        matrices.translate(0, height, 0);
        matrices.multiply(MinecraftClient.getInstance().getEntityRenderDispatcher().getRotation());
        
        // Distance Scaling: Thoda scale adjust kiya hai taaki clear dikhe
        float scale = 0.025f;
        matrices.scale(-scale, -scale, scale);

        TextRenderer tr = MinecraftClient.getInstance().textRenderer;
        float health = player.getHealth();
        float maxH = player.getMaxHealth();
        
        // Color transition logic (Green -> Yellow -> Red)
        float ratio = MathHelper.clamp(health / maxH, 0.0f, 1.0f);
        int color = MathHelper.hsvToRgb(ratio / 3.0f, 1.0f, 1.0f) | 0xFF000000;

        if (ModSettings.currentStyle == 0) {
            String text = "❤ " + (int)health + " / " + (int)maxH;
            tr.draw(text, -tr.getWidth(text) / 2f, 0, 0xFF5555, false, matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, 0, light);
        } 
        else if (ModSettings.currentStyle == 1) {
            // New Feature: Head indicator logic placeholder + Hits
            String text = "⚠ " + (int)Math.ceil(health / 2.0f) + " Hits Left";
            tr.draw(text, -tr.getWidth(text) / 2f, 0, 0xFFAA00, false, matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, 0, light);
        }
        else if (ModSettings.currentStyle == 2) {
            // Advanced Feature: Real-time progress bar using ASCII
            String fullBar = "■■■■■■■■■■";
            int active = (int)(ratio * 10);
            String display = "[" + fullBar.substring(0, active) + "§8" + fullBar.substring(active) + "§r]";
            tr.draw(display, -tr.getWidth(display) / 2f, 0, color, false, matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, 0, light);
        }

        matrices.pop();
    }
}
