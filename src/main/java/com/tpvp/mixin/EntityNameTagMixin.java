package com.tpvp.mixin;

import com.tpvp.TPvPConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.text.Text;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityNameTagMixin<T extends Entity> {

    @Inject(method = "renderLabelIfPresent", at = @At("HEAD"))
    private void onRenderLabel(T entity, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (!TPvPConfig.heartIndicatorEnabled) return;

        // Sirf Living Entities (Players/Mobs) ke liye
        if (entity instanceof LivingEntity target) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || target == client.player) return;

            float health = target.getHealth();
            float maxHealth = target.getMaxHealth();
            
            // Damage calculation
            double attackDamage = client.player.getAttributeValue(EntityAttributes.ATTACK_DAMAGE);
            int hitsToKill = (int) Math.ceil(health / (attackDamage <= 0 ? 1 : attackDamage));

            // Color logic
            int color = 0x00FF00; // Green
            if (health < maxHealth * 0.5) color = 0xFFFF00; // Yellow
            if (health < maxHealth * 0.2) color = 0xFF0000; // Red

            String info = String.format("%.1f HP | %d Hits", health, hitsToKill);
            
            // Text render karne ki position (Name ke thoda upar)
            matrices.push();
            matrices.translate(0, 0.25f, 0); // Name tag se 0.25 block upar
            
            TextRenderer textRenderer = client.textRenderer;
            float backgroundOpacity = client.options.getTextBackgroundOpacity(0.25f);
            int backgroundColor = (int)(backgroundOpacity * 255.0f) << 24;
            
            float xPos = (float)(-textRenderer.getWidth(info) / 2);
            Matrix4f matrix4f = matrices.peek().getPositionMatrix();
            
            // Text draw karna
            textRenderer.draw(info, xPos, 0, color, false, matrix4f, vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, backgroundColor, light);
            
            matrices.pop();
        }
    }
}
