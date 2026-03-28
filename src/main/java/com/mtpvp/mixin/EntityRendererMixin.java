package com.mtpvp.mixin;

import com.mtpvp.gui.MtpvpDashboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<S extends EntityRenderState> {

    @Inject(method = "renderLabelIfPresent", at = @At("HEAD"))
    private void renderMtpvpAdvancedGui(S state, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (!MtpvpDashboard.headEnabled || !(state instanceof PlayerEntityRenderState playerState)) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;

        String targetName = playerState.displayName.getString();
        PlayerEntity target = client.world.getPlayers().stream()
                .filter(p -> p.getName().getString().equals(targetName))
                .findFirst().orElse(null);

        if (target == null || target == client.player) return;

        float hp = target.getHealth();
        float maxHp = target.getMaxHealth();
        TextRenderer tr = client.textRenderer;

        matrices.push();
        matrices.translate(0.0D, 0.4D, 0.0D); 
        matrices.scale(-0.025f, -0.025f, 0.025f); 
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        if (MtpvpDashboard.styleIndex == 0) {
            // Style 1: 10 Hearts
            String hearts = "❤".repeat((int)Math.ceil(hp/2));
            tr.draw(hearts, -tr.getWidth(hearts)/2f, 0, 0xFF5555, false, matrix, vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, 0, light);
        } 
        else if (MtpvpDashboard.styleIndex == 1) {
            // Style 2: Level Bar (GUI Progress Bar)
            float barWidth = 40f;
            float progress = (hp / maxHp) * barWidth;
            int color = (hp > 14) ? 0xFF55FF55 : (hp > 7 ? 0xFFFFFF55 : 0xFFFF5555);
            
            drawRect(matrices, vertexConsumers, -barWidth/2, -1, barWidth/2, 5, 0xAA000000);
            drawRect(matrices, vertexConsumers, -barWidth/2, 0, -barWidth/2 + progress, 4, color);
            
            // HP Number over bar
            String hpVal = String.format("%.1f", hp);
            tr.draw(hpVal, -tr.getWidth(hpVal)/2f, -10, 0xFFFFFF, false, matrix, vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, 0, light);
        }
        else if (MtpvpDashboard.styleIndex == 2) {
            // Style 3: Pro Skin + Hits (Weapon Math Fix for 1.21.4)
            ItemStack hand = client.player.getMainHandStack();
            float dmg = 1.0f;
            
            // New way to get damage in 1.21.4
            AttributeModifiersComponent modifiers = hand.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
            if (modifiers != null) {
                dmg += (float) modifiers.modifiers().stream()
                        .filter(m -> m.attribute().value().getTranslationKey().contains("attack_damage"))
                        .mapToDouble(m -> m.modifier().value()).sum();
            }
            
            int hits = (int) Math.ceil(hp / (dmg > 1 ? dmg : 1.5f));
            String info = "⚔ " + hits + " HITS";
            tr.draw(info, -tr.getWidth(info)/2f, 0, 0xFFAA00, true, matrix, vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, 0, light);
        }

        matrices.pop();
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
