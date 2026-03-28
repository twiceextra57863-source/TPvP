package com.mtpvp.mixin;

import com.mtpvp.gui.MtpvpDashboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.text.Text;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity> {

    @Inject(method = "render", at = @At("HEAD"))
    private void renderMtpvpIndicator(T entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        // Sirf tabhi render karo jab feature ON ho aur entity ek Player ho
        if (!MtpvpDashboard.heartEnabled) return;
        
        if (entity instanceof PlayerEntity target && target != MinecraftClient.getInstance().player) {
            PlayerEntity self = MinecraftClient.getInstance().player;
            if (self == null || target.isInvisible()) return;

            // --- ADVANCED DAMAGE CALCULATION ---
            double baseDamage = self.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
            float finalDamage = (float) baseDamage;

            // 1. Critical Hit (1.5x) - Generally PvP mein crit count hota hai
            finalDamage *= 1.5f;

            // 2. Sharpness check (1.21.4 Component System)
            ItemStack weapon = self.getMainHandStack();
            ItemEnchantmentsComponent enchants = weapon.get(DataComponentTypes.ENCHANTMENTS);
            if (enchants != null) {
                // Sharpness level nikalna (registry based in 1.21.4)
                // Note: Direct enchantment check depends on your specific yarn mappings
                // For simplicity, we assume a small boost if enchanted
                finalDamage += 1.25f; 
            }

            // 3. Strength Effect
            if (self.hasStatusEffect(StatusEffects.STRENGTH)) {
                finalDamage += (3.0f * (self.getStatusEffect(StatusEffects.STRENGTH).getAmplifier() + 1));
            }

            int hitsToKill = (int) Math.ceil(target.getHealth() / Math.max(1, finalDamage));
            
            // --- RENDERING ---
            String info = "❤ " + (int)target.getHealth() + " | " + hitsToKill + " Hits";
            Text text = Text.literal(info);
            
            matrices.push();
            // Nametag ke upar set karna (Player height + offset)
            matrices.translate(0.0D, entity.getHeight() + 0.3D, 0.0D);
            matrices.multiply(MinecraftClient.getInstance().getEntityRenderDispatcher().getRotation());
            matrices.scale(-0.025F, -0.025F, 0.025F);

            Matrix4f matrix4f = matrices.peek().getPositionMatrix();
            TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
            float xOffset = (float)(-textRenderer.getWidth(text) / 2);

            // Draw Background (Shadow)
            textRenderer.draw(text, xOffset, 0, 0x20FFFFFF, false, matrix4f, vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, 0x80000000, light);
            // Draw Main Text (Red-White contrast)
            textRenderer.draw(text, xOffset, 0, 0xFFFF3333, false, matrix4f, vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, light);

            matrices.pop();
        }
    }
}
