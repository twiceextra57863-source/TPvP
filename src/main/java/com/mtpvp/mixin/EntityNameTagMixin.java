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
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityNameTagMixin<S extends EntityRenderState> {

    // Variable to store animated health for smooth transition
    private float animatedHp = -1f;

    @Inject(method = "renderLabelIfPresent", at = @At("HEAD"))
    private void onRenderLabel(S state, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (!MtpvpDashboard.headEnabled || !(state instanceof PlayerEntityRenderState playerState)) return;

        if (state instanceof IEntityRenderState data) {
            float actualHp = data.tpvp$getHealth();
            float maxHp = data.tpvp$getMaxHealth();
            
            // --- SMOOTH ANIMATION LOGIC ---
            if (animatedHp < 0) animatedHp = actualHp; 
            // 0.1f speed se bar slide karega (isey change karke speed badha sakte ho)
            animatedHp = MathHelper.lerp(0.1f, animatedHp, actualHp); 

            // Dynamic Color Logic (Green -> Yellow -> Red)
            int healthColor = (actualHp > 14) ? 0xFF55FF55 : (actualHp > 7 ? 0xFFFFFF55 : 0xFFFF5555);

            matrices.push();
            // Position Fix: Sir ke upar 360 view ke liye
            matrices.translate(0, 2.6f, 0); 
            matrices.scale(-0.025f, -0.025f, 0.025f);
            
            TextRenderer tr = MinecraftClient.getInstance().textRenderer;
            Matrix4f matrix = matrices.peek().getPositionMatrix();

            // --- STYLE 1: 10 HEARTS (With Black Empty Hearts) ---
            if (MtpvpDashboard.styleIndex == 0) {
                int fullHearts = (int) Math.ceil(actualHp / 2);
                int emptyHearts = 10 - fullHearts;
                // §c = Red, §8 = Dark Gray (Black feel)
                String heartText = "§c" + "❤".repeat(fullHearts) + "§8" + "❤".repeat(Math.max(0, emptyHearts));
                
                float x = -tr.getWidth(heartText.replaceAll("§[0-9a-fk-or]", ""))/2f;
                tr.draw(heartText, x, 0, 0xFFFFFF, false, matrix, vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, 0, light);
            } 
            
            // --- STYLE 2: STATUS BAR (Smooth + Borders) ---
            else if (MtpvpDashboard.styleIndex == 1) {
                float w = 50f;
                float h = 4f;
                float progress = (animatedHp / maxHp) * w; // Use animatedHp here
                
                // 1. Black Border Outline
                drawRect(matrices, vertexConsumers, -w/2 - 1, -1, w/2 + 1, h + 1, 0xFF000000); 
                // 2. Dark Background
                drawRect(matrices, vertexConsumers, -w/2, 0, w/2, h, 0xAA333333); 
                // 3. Smooth Health Fill
                drawRect(matrices, vertexConsumers, -w/2, 0, -w/2 + progress, h, healthColor);
            }
            
            // --- STYLE 3: PRO FACE + SMOOTH HITS ---
            else if (MtpvpDashboard.styleIndex == 2) {
                double dmg = data.tpvp$getAttackDamage();
                int hits = (int) Math.ceil(actualHp / (dmg <= 0 ? 1.5 : dmg));
                String info = "§l" + hits + " HITS";
                
                Identifier skin = playerState.skinTextures.texture();
                drawFace(matrices, vertexConsumers, skin, -22, -4, 14);
                tr.draw(info, 0, 0, healthColor, true, matrix, vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, 0, light);
            }
            
            matrices.pop();
        }
    }

    private void drawFace(MatrixStack matrices, VertexConsumerProvider vcp, Identifier skin, float x, float y, float size) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        var buffer = vcp.getBuffer(net.minecraft.client.render.RenderLayer.getGuiTextured(skin));
        float u1 = 8f/64f, v1 = 8f/64f, u2 = 16f/64f, v2 = 16f/64f;

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
