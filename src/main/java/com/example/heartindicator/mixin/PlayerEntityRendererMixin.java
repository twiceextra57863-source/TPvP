package com.example.heartindicator.mixin;

import com.example.heartindicator.HeartIndicatorMod;
import com.example.heartindicator.client.HeartIndicatorClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin {
    
    @Unique
    private static final Identifier HEART_ICON = Identifier.of("heartindicator", "textures/gui/heart.png");
    @Unique
    private static final Identifier HALF_HEART_ICON = Identifier.of("heartindicator", "textures/gui/half_heart.png");
    
    @Inject(method = "renderLabelIfPresent", at = @At("HEAD"))
    private void onRenderLabel(PlayerEntity entity, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (HeartIndicatorClient.showHeartIndicator && entity != null) {
            renderHeartIndicator(entity, matrices, vertexConsumers, light);
        }
    }
    
    @Unique
    private void renderHeartIndicator(PlayerEntity player, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        try {
            float health = player.getHealth();
            float maxHealth = player.getMaxHealth();
            int heartCount = MathHelper.ceil(health / 2.0f);
            int maxHeartCount = MathHelper.ceil(maxHealth / 2.0f);
            
            matrices.push();
            matrices.translate(0, player.getHeight() + 0.5f, 0);
            matrices.scale(0.025f, -0.025f, 0.025f);
            
            int totalWidth = maxHeartCount * 9;
            int startX = -totalWidth / 2;
            
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            
            Tessellator tessellator = Tessellator.getInstance();
            
            for (int i = 0; i < maxHeartCount; i++) {
                float heartValue = Math.min(2.0f, health - (i * 2));
                int x = startX + (i * 9);
                int y = 0;
                
                Identifier texture;
                if (heartValue >= 1.5f) {
                    texture = HEART_ICON;
                } else if (heartValue >= 0.5f) {
                    texture = HALF_HEART_ICON;
                } else {
                    continue;
                }
                
                RenderSystem.setShader(GameRenderer::getPositionTexProgram);
                RenderSystem.setShaderTexture(0, texture);
                
                MatrixStack.Entry entry = matrices.peek();
                Matrix4f matrix = entry.getPositionMatrix();
                
                BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
                buffer.vertex(matrix, x, y + 9, 0).texture(0, 1);
                buffer.vertex(matrix, x + 9, y + 9, 0).texture(1, 1);
                buffer.vertex(matrix, x + 9, y, 0).texture(1, 0);
                buffer.vertex(matrix, x, y, 0).texture(0, 0);
                BufferBuilder.BuiltBuffer builtBuffer = buffer.end();
                BufferRenderer.drawWithGlobalProgram(builtBuffer);
            }
            
            RenderSystem.disableBlend();
            matrices.pop();
        } catch (Exception e) {
            HeartIndicatorMod.LOGGER.error("Error rendering heart indicator", e);
        }
    }
}
