package com.tpvp.mixin;

import com.tpvp.TPvPConfig;
import com.tpvp.accessor.IEntityRenderState; // Import naya package
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityNameTagMixin<S extends EntityRenderState> {

    @Inject(method = "renderLabelIfPresent", at = @At("HEAD"))
    private void onRenderLabel(S state, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (!TPvPConfig.heartIndicatorEnabled) return;

        if (state instanceof IEntityRenderState data) {
            float health = data.tpvp$getHealth();
            float maxHealth = data.tpvp$getMaxHealth();

            if (maxHealth > 0) {
                double damage = data.tpvp$getAttackDamage();
                int hitsToKill = (int) Math.ceil(health / (damage <= 0 ? 1 : damage));

                int color = 0x55FF55; // Green
                if (health < maxHealth * 0.5) color = 0xFFFF55; // Yellow
                if (health < maxHealth * 0.2) color = 0xFF5555; // Red

                String info = String.format("%.0f HP | %d Hits", health, hitsToKill);
                
                matrices.push();
                matrices.translate(0, 0.4f, 0); 
                matrices.scale(0.7f, 0.7f, 0.7f);

                TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
                float xPos = (float)(-textRenderer.getWidth(info) / 2);
                Matrix4f matrix4f = matrices.peek().getPositionMatrix();
                
                // Color formatting fix for 1.21
                textRenderer.draw(info, xPos, 0, color, false, matrix4f, vertexConsumers, 
                    TextRenderer.TextLayerType.SEE_THROUGH, 0x40000000, light);

                matrices.pop();
            }
        }
    }
}
