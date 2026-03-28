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

    @Inject(method = "render", at = @At("HEAD"))
    private void renderStyledIndicator(S state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (!MtpvpDashboard.heartEnabled) return;

        // 1.21.4 uses PlayerEntityRenderState for players
        if (state instanceof PlayerEntityRenderState playerState) {
            
            // Player ko find karne ka safe tarika display name se (1.21.4 compatible)
            String name = playerState.displayName != null ? playerState.displayName.getString() : "";
            PlayerEntity target = MinecraftClient.getInstance().world.getPlayers().stream()
                    .filter(p -> p.getName().getString().equals(name))
                    .findFirst()
                    .orElse(null);

            // Apne upar render mat karo aur check target exists
            if (target == null || target == MinecraftClient.getInstance().player || target.isInvisible()) return;

            float hp = target.getHealth();
            int hits = (int) Math.ceil(hp / 4.0f); 
            
            matrices.push();
            // Naye state system mein height variable use hota hai
            float renderHeight = playerState.height + 0.5F;
            matrices.translate(0.0D, renderHeight, 0.0D);
            matrices.multiply(MinecraftClient.getInstance().getEntityRenderDispatcher().getRotation());
            matrices.scale(-0.025F, -0.025F, 0.025F);

            Matrix4f matrix = matrices.peek().getPositionMatrix();
            TextRenderer tr = MinecraftClient.getInstance().textRenderer;

            String displayText = "";
            int color = 0xFFFFFF;

            // Dashboard style selection
            switch (MtpvpDashboard.styleIndex) {
                case 0: // Status Bar
                    displayText = "[||||||||||] " + (int)hp + " HP";
                    color = (hp > 10) ? 0x00FF00 : 0xFF0000;
                    break;
                case 1: // Classic Hearts
                    displayText = "❤ " + (int)hp;
                    color = 0xFF5555;
                    break;
                case 2: // Player Head + Hits
                    displayText = "Hits: " + hits + " | " + name;
                    color = 0xFFAA00;
                    break;
            }

            float x = (float)(-tr.getWidth(displayText) / 2);
            
            // Draw logic (1.21.4 compatible)
            tr.draw(displayText, x, 0, 0x20FFFFFF, false, matrix, vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, 0x80000000, light);
            tr.draw(displayText, x, 0, color, false, matrix, vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, light);

            matrices.pop();
        }
    }
}
