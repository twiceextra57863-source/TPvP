package com.example.heartindicator.mixin;

import com.example.heartindicator.client.HeartIndicatorClient;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
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
public abstract class PlayerEntityRendererMixin extends LivingEntityRenderer<PlayerEntity, PlayerEntityRenderer.PlayerModel> {
    
    private static final Identifier HEART_ICON = Identifier.of("heartindicator", "textures/gui/heart.png");
    private static final Identifier HALF_HEART_ICON = Identifier.of("heartindicator", "textures/gui/half_heart.png");
    
    public PlayerEntityRendererMixin(EntityRendererFactory.Context ctx, PlayerEntityRenderer.PlayerModel model, float shadowRadius) {
        super(ctx, model, shadowRadius);
    }
    
    @Inject(method = "renderLabelIfPresent", at = @At("HEAD"), cancellable = true)
    private void onRenderLabel(PlayerEntity entity, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (HeartIndicatorClient.isShowing() && entity != null) {
            renderHeartIndicator(entity, matrices, vertexConsumers, light);
            // Don't cancel, let name render normally
        }
    }
    
    @Unique
    private void renderHeartIndicator(PlayerEntity player, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        try {
            float health = player.getHealth();
            float maxHealth = player.getMaxHealth();
            int heartCount = MathHelper.ceil(health / 2.0f);
            int maxHeartCount = MathHelper.ceil(maxHealth / 2.0f);
            
            // Calculate position above the player
            matrices.push();
            matrices.translate(0, player.getHeight() + 0.5f, 0);
            matrices.scale(0.025f, -0.025f, 0.025f);
            
            // Calculate total width
            int totalWidth = maxHeartCount * 8;
            int startX = -totalWidth / 2;
            
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            
            for (int i = 0; i < maxHeartCount; i++) {
                float heartValue = Math.min(2.0f, health - (i * 2));
                int x = startX + (i * 8);
                int y = 0;
                
                // Get renderer
                TextRenderer textRenderer = this.getTextRenderer();
                var matrices2 = matrices.peek().getPositionMatrix();
                
                if (heartValue >= 1.5f) {
                    // Full heart
                    RenderSystem.setShaderTexture(0, HEART_ICON);
                } else if (heartValue >= 0.5f) {
                    // Half heart
                    RenderSystem.setShaderTexture(0, HALF_HEART_ICON);
                } else {
                    // Empty heart (use a different texture or skip)
                    continue;
                }
                
                // Draw heart using immediate mode or Tessellator
                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
                
                float u1 = 0;
                float v1 = 0;
                float u2 = 9;
                float v2 = 9;
                
                buffer.vertex(matrices2, x, y + 9, 0).texture(u1, v2);
                buffer.vertex(matrices2, x + 9, y + 9, 0).texture(u2, v2);
                buffer.vertex(matrices2, x + 9, y, 0).texture(u2, v1);
                buffer.vertex(matrices2, x, y, 0).texture(u1, v1);
                
                BufferDrawer drawer = new BufferDrawer(buffer.build());
                drawer.draw();
            }
            
            RenderSystem.disableBlend();
            matrices.pop();
        } catch (Exception e) {
            HeartIndicatorMod.LOGGER.error("Error rendering heart indicator", e);
        }
    }
}
