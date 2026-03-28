package com.mtpvp.mixin;

import com.mtpvp.gui.MtpvpDashboard;
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
public abstract class EntityRendererMixin<T extends Entity> {

    @Inject(method = "render", at = @At("HEAD"))
    private void renderStyledIndicator(T entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (!MtpvpDashboard.heartEnabled) return;

        if (entity instanceof PlayerEntity target && target != MinecraftClient.getInstance().player) {
            if (target.isInvisible()) return;

            // Simple Hit Math (Using previous logic)
            float hp = target.getHealth();
            int hits = (int) Math.ceil(hp / 4.0f); // Default 4 damage assume kar rahe hain simplify ke liye
            
            matrices.push();
            matrices.translate(0.0D, entity.getHeight() + 0.5D, 0.0D);
            matrices.multiply(MinecraftClient.getInstance().getEntityRenderDispatcher().getRotation());
            matrices.scale(-0.025F, -0.025F, 0.025F);

            Matrix4f matrix = matrices.peek().getPositionMatrix();
            TextRenderer tr = MinecraftClient.getInstance().textRenderer;

            String displayText = "";
            int color = 0xFFFFFF;

            // --- STYLE LOGIC ---
            switch (MtpvpDashboard.styleIndex) {
                case 0: // Status Bar Style
                    displayText = "[||||||||||] " + (int)hp + " HP";
                    color = (hp > 10) ? 0x00FF00 : 0xFF0000;
                    break;
                case 1: // Classic Hearts
                    displayText = "❤ " + (int)hp;
                    color = 0xFF5555;
                    break;
                case 2: // Player Head + Hits (Advanced)
                    displayText = "Hits: " + hits + " | " + target.getName().getString();
                    color = 0xFFAA00;
                    break;
            }

            float x = (float)(-tr.getWidth(displayText) / 2);
            // Background Draw
            tr.draw(displayText, x, 0, 0x20FFFFFF, false, matrix, vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, 0x80000000, light);
            // Foreground Draw
            tr.draw(displayText, x, 0, color, false, matrix, vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, light);

            matrices.pop();
        }
    }
}
