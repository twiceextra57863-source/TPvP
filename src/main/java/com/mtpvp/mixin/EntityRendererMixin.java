package com.mtpvp.mixin;

import com.mtpvp.gui.MtpvpDashboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRendererMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private <E extends net.minecraft.entity.Entity, S extends EntityRenderState> void renderIndicator(E entity, double x, double y, double z, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (!MtpvpDashboard.heartEnabled) return;

        if (entity instanceof PlayerEntity target && target != MinecraftClient.getInstance().player) {
            if (target.isInvisible() || !target.isAlive()) return;

            float hp = target.getHealth();
            String name = target.getName().getString();
            
            matrices.push();
            // Player ke sir ke upar position
            matrices.translate(x, y + entity.getHeight() + 0.5, z);
            matrices.multiply(MinecraftClient.getInstance().getEntityRenderDispatcher().getRotation());
            matrices.scale(-0.025F, -0.025F, 0.025F);

            Matrix4f matrix = matrices.peek().getPositionMatrix();
            TextRenderer tr = MinecraftClient.getInstance().textRenderer;

            String displayText = "";
            int color = 0xFFFFFF;

            // Styles
            switch (MtpvpDashboard.styleIndex) {
                case 0 -> { displayText = "❤ " + (int)hp + " HP"; color = 0xFF5555; }
                case 1 -> { displayText = "[HITS: " + (int)Math.ceil(hp/4.0) + "]"; color = 0xFFAA00; }
                case 2 -> { displayText = name + " | " + (int)hp; color = 0x55FFFF; }
            }

            float textWidth = (float)(-tr.getWidth(displayText) / 2);
            
            // Modern Bordered Background (No PNG)
            int bgWidth = tr.getWidth(displayText);
            // context.fill() ki jagah direct tr.draw backup color use karenge
            tr.draw(displayText, textWidth, 0, color, false, matrix, vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, light);

            matrices.pop();
        }
    }
}
