package com.example.mod.mixin;

import com.example.mod.ModSettings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class HealthRendererMixin<T extends Entity> {

    @Inject(method = "render", at = @At("TAIL"))
    public void onRender(T entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (!(entity instanceof PlayerEntity player) || player == MinecraftClient.getInstance().player || player.isInvisible()) return;

        matrices.push();
        // Player ke sir ke upar position set karein
        matrices.translate(0, player.getHeight() + 0.5f, 0);
        // Indicator hamesha player ki taraf face kare (Front & Back dono dikhega)
        matrices.multiply(MinecraftClient.getInstance().getEntityRenderDispatcher().getRotation());
        matrices.scale(-0.025f, -0.025f, 0.025f);

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        TextRenderer tr = MinecraftClient.getInstance().textRenderer;
        float health = player.getHealth();
        float maxH = player.getMaxHealth();
        int color = (health > maxH * 0.5) ? 0x55FF55 : (health > maxH * 0.25 ? 0xFFFF55 : 0xFF5555);

        if (ModSettings.currentStyle == 0) { // DESIGN 1: HEARTS
            String text = "❤ " + String.format("%.1f", health);
            tr.draw(text, -tr.getWidth(text) / 2f, 0, 0xFF5555, false, matrix, vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, 0, light);
        } 
        else if (ModSettings.currentStyle == 1) { // DESIGN 2: HEAD & HITS
            String text = "HITS TO KILL: " + (int)Math.ceil(health / 1.5f); // 1.5 avg dmg
            tr.draw(text, -tr.getWidth(text) / 2f, 0, 0xFFFFFF, false, matrix, vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, 0, light);
        }
        else if (ModSettings.currentStyle == 2) { // DESIGN 3: STATUS BAR
            String bar = "||||||||||";
            int activeBars = (int)((health / maxH) * 10);
            tr.draw(bar.substring(0, Math.max(0, activeBars)), -20, 0, color, false, matrix, vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, 0, light);
        }

        matrices.pop();
    }
}

