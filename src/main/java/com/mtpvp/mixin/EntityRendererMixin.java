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
import net.minecraft.text.Text;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<S extends EntityRenderState> {

    @Inject(method = "renderLabelIfPresent", at = @At("HEAD"))
    private void renderMtpvpIndicator(S state, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (!MtpvpDashboard.heartEnabled) return;

        if (state instanceof PlayerEntityRenderState playerState) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.world == null) return;

            // Player ko name se find karna (1.21.4 compatible way)
            String targetName = playerState.displayName.getString();
            PlayerEntity target = client.world.getPlayers().stream()
                    .filter(p -> p.getName().getString().equals(targetName))
                    .findFirst().orElse(null);

            if (target == null || target == client.player || target.isInvisible()) return;

            float hp = target.getHealth();
            String info = switch (MtpvpDashboard.styleIndex) {
                case 1 -> "Hits: " + (int) Math.ceil(hp / 3.5f);
                case 2 -> (int)hp + " HP";
                default -> "❤ " + (int)hp;
            };

            int color = (hp > 10) ? 0x55FF55 : 0xFF5555;

            matrices.push();
            matrices.translate(0.0D, 0.35D, 0.0D); 

            Matrix4f matrix = matrices.peek().getPositionMatrix();
            TextRenderer tr = client.textRenderer;
            float xOffset = (float)(-tr.getWidth(info) / 2);

            // Text Rendering with Shadow
            tr.draw(info, xOffset, 0, color, false, matrix, vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, 0x80000000, light);

            matrices.pop();
        }
    }
}
