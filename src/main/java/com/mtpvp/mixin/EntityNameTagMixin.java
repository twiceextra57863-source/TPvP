package com.tpvp.mixin;

import com.mtpvp.gui.MtpvpDashboard;
import com.tpvp.accessor.IEntityRenderState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityNameTagMixin<S extends EntityRenderState> {

    @Inject(method = "renderLabelIfPresent", at = @At("HEAD"), cancellable = true)
    private void onRenderLabel(S state, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (!MtpvpDashboard.headEnabled || !(state instanceof PlayerEntityRenderState)) return;

        if (state instanceof IEntityRenderState data) {
            float health = data.tpvp$getHealth();
            float maxHp = data.tpvp$getMaxHealth();
            if (maxHp <= 0) return;

            matrices.push();
            matrices.translate(0, 0.45f, 0); 
            matrices.scale(-0.025f, -0.025f, 0.025f);
            
            TextRenderer tr = MinecraftClient.getInstance().textRenderer;
            Matrix4f matrix = matrices.peek().getPositionMatrix();

            // --- STYLE 1: 10 HEARTS (❤❤❤❤❤) ---
            if (MtpvpDashboard.styleIndex == 0) {
                String hearts = "❤".repeat((int)Math.ceil(health / 2));
                tr.draw(hearts, -tr.getWidth(hearts)/2f, 0, 0xFF5555, false, matrix, vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, 0x40000000, light);
            }
            // --- STYLE 2: LEVEL BAR (STATUS BAR) ---
            else if (MtpvpDashboard.styleIndex == 1) {
                float barWidth = 40f;
                float progress = (health / maxHp) * barWidth;
                int color = (health > 14) ? 0xFF55FF55 : (health > 7 ? 0xFFFFFF55 : 0xFFFF5555);
                
                // Draw GUI Bar (High Coding Math)
                drawRect(matrices, vertexConsumers, -barWidth/2, -1, barWidth/2, 4, 0xAA000000); // BG
                drawRect(matrices, vertexConsumers, -barWidth/2, 0, -barWidth/2 + progress, 3, color); // Fill
            }
            // --- STYLE 3: PRO SKIN + WEAPON MATH HITS ---
            else if (MtpvpDashboard.styleIndex == 2) {
                double damage = data.tpvp$getAttackDamage();
                int hits = (int) Math.ceil(health / (damage <= 0 ? 1.0 : damage));
                String info = "⚔ " + hits + " HITS";
                tr.draw(info, -tr.getWidth(info)/2f, 0, 0xFFAA00, true, matrix, vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, 0x40000000, light);
            }

            matrices.pop();
        }
    }

    private void drawRect(MatrixStack matrices, VertexConsumerProvider vcp, float x1, float y1, float x2, float y2, int color) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        float a = (float)(color >> 24 & 255) / 255.0F;
        float r = (float)(color >> 16 & 255) / 255.0F;
        float g = (float)(color >> 8 & 255) / 255.0F;
        float b = (float)(color & 255) / 255.0F;
        var buffer = vcp.getBuffer(net.minecraft.client.render.RenderLayer.getGuiOverlay());
        buffer.vertex(matrix, x1, y1, 0).color(r, g, b, a);
        buffer.vertex(matrix, x1, y2, 0).color(r, g, b, a);
        buffer.vertex(matrix, x2, y2, 0).color(r, g, b, a);
        buffer.vertex(matrix, x2, y1, 0).color(r, g, b, a);
    }
}
