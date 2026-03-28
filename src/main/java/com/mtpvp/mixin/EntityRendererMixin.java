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
    private void renderMtpvpHeadIndicator(S state, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (!MtpvpDashboard.headEnabled) return;

        if (state instanceof PlayerEntityRenderState playerState) {
            MinecraftClient client = MinecraftClient.getInstance();
            String name = playerState.displayName.getString();
            PlayerEntity target = client.world.getPlayers().stream().filter(p -> p.getName().getString().equals(name)).findFirst().orElse(null);

            if (target == null || target == client.player) return;

            float hp = target.getHealth();
            String info = switch (MtpvpDashboard.styleIndex) {
                case 1 -> (int) Math.ceil(hp / 3.5f) + " Hits";
                case 2 -> (int)hp + " HP";
                default -> "❤ " + (int)hp;
            };

            matrices.push();
            matrices.translate(0.0D, 0.35D, 0.0D); 
            Matrix4f matrix = matrices.peek().getPositionMatrix();
            TextRenderer tr = client.textRenderer;
            tr.draw(info, (float)(-tr.getWidth(info)/2), 0, 0xFF5555, false, matrix, vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, 0, light);
            matrices.pop();
        }
    }
}
