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
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityNameTagMixin<S extends EntityRenderState> {

    @Inject(method = "renderLabelIfPresent", at = @At("HEAD"))
    private void onRenderLabel(S state, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (!MtpvpDashboard.headEnabled || !(state instanceof PlayerEntityRenderState playerState)) return;

        if (state instanceof IEntityRenderState data) {
            float health = data.tpvp$getHealth();
            float maxHp = data.tpvp$getMaxHealth();

            matrices.push();
            // FIX: Position changed to 2.4f (This is above player name tag)
            matrices.translate(0, 2.4f, 0); 
            matrices.scale(-0.025f, -0.025f, 0.025f);
            
            TextRenderer tr = MinecraftClient.getInstance().textRenderer;
            Matrix4f matrix = matrices.peek().getPositionMatrix();

            if (MtpvpDashboard.styleIndex == 0) { // STYLE 1: 10 Hearts
                String hearts = "❤".repeat((int)Math.ceil(health / 2));
                tr.draw(hearts, -tr.getWidth(hearts)/2f, 0, 0xFF5555, false, matrix, vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, 0, light);
            } 
            else if (MtpvpDashboard.styleIndex == 1) { // STYLE 2: Status Bar
                float w = 40f;
                float progress = (health / maxHp) * w;
                int color = (health > 14) ? 0xFF55FF55 : (health > 7 ? 0xFFFFFF55 : 0xFFFF5555);
                drawRect(matrices, vertexConsumers, -w/2, 0, w/2, 4, 0xAA000000);
                drawRect(matrices, vertexConsumers, -w/2, 0, -w/2 + progress, 3, color);
            }
            else if (MtpvpDashboard.styleIndex == 2) { // STYLE 3: Face + Hits (Crash Fixed)
                int hits = (int) Math.ceil(health / (data.tpvp$getAttackDamage() <= 0 ? 1.5 : data.tpvp$getAttackDamage()));
                String info = "⚔ " + hits;
                
                // Drawing Face with GUI layer to prevent world-space crash
                Identifier skin = playerState.skinTextures.texture();
                drawFace(matrices, vertexConsumers, skin, -18, -2, 12);
                
                tr.draw(info, 2, 0, 0xFFAA00, true, matrix, vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, 0, light);
            }
            matrices.pop();
        }
    }

    private void drawFace(MatrixStack matrices, VertexConsumerProvider vcp, Identifier skin, float x, float y, float size) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        // Changed to RenderLayer.getGui to avoid light-related crashes
        var buffer = vcp.getBuffer(net.minecraft.client.render.RenderLayer.getGuiTextured(skin));
        
        float u1 = 8f / 64f, v1 = 8f / 64f;
        float u2 = 16f / 64f, v2 = 16f / 64f;

        buffer.vertex(matrix, x, y + size, 0).color(1f, 1f, 1f, 1f).texture(u1, v2);
        buffer.vertex(matrix, x + size, y + size, 0).color(1f, 1f, 1f, 1f).texture(u2, v2);
        buffer.vertex(matrix, x + size, y, 0).color(1f, 1f, 1f, 1f).texture(u2, v1);
        buffer.vertex(matrix, x, y, 0).color(1f, 1f, 1f, 1f).texture(u1, v1);
    }

    private void drawRect(MatrixStack m, VertexConsumerProvider v, float x1, float y1, float x2, float y2, int c) {
        Matrix4f mat = m.peek().getPositionMatrix();
        float a = (c >> 24 & 255) / 255f, r = (c >> 16 & 255) / 255f, g = (c >> 8 & 255) / 255f, b = (c & 255) / 255f;
        var buffer = v.getBuffer(net.minecraft.client.render.RenderLayer.getGuiOverlay());
        buffer.vertex(mat, x1, y1, 0).color(r, g, b, a);
        buffer.vertex(mat, x1, y2, 0).color(r, g, b, a);
        buffer.vertex(mat, x2, y2, 0).color(r, g, b, a);
        buffer.vertex(mat, x2, y1, 0).color(r, g, b, a);
    }
}
