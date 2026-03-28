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
    private void renderMtpvpLabel(S state, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (!MtpvpDashboard.heartEnabled) return;

        // Sirf players ke liye check karein
        if (state instanceof PlayerEntityRenderState playerState) {
            String name = playerState.displayName.getString();
            
            // Player data fetch karna health ke liye
            PlayerEntity target = MinecraftClient.getInstance().world.getPlayers().stream()
                    .filter(p -> p.getName().getString().equals(name))
                    .findFirst().orElse(null);

            if (target == null || target == MinecraftClient.getInstance().player || target.isInvisible()) return;

            float hp = target.getHealth();
            int hits = (int) Math.ceil(hp / 3.5f); // 3.5 average damage consider kiya hai

            matrices.push();
            // Nametag ke thoda upar (Minecraft standard label height)
            matrices.translate(0.0D, 0.25D, 0.0D); 

            Matrix4f matrix = matrices.peek().getPositionMatrix();
            TextRenderer tr = MinecraftClient.getInstance().textRenderer;

            String infoText = "";
            int color = 0xFFFFFF;

            switch (MtpvpDashboard.styleIndex) {
                case 0 -> { infoText = "❤ " + (int)hp; color = 0xFF5555; }
                case 1 -> { infoText = "Hits: " + hits; color = 0xFFAA00; }
                case 2 -> { infoText = "[" + (int)hp + " HP]"; color = 0x55FFFF; }
            }

            float xPos = (float)(-tr.getWidth(infoText) / 2);

            // Shadow layer
            tr.draw(infoText, xPos, 0, 0x20FFFFFF, false, matrix, vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, 0x80000000, light);
            // Main layer
            tr.draw(infoText, xPos, 0, color, false, matrix, vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, light);

            matrices.pop();
        }
    }
}
