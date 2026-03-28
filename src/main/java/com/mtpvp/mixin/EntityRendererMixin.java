package com.mtpvp.mixin;

import com.mtpvp.gui.MtpvpDashboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<S extends EntityRenderState> {

    // 1.21.4 uses (S state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light)
    @Inject(method = "render", at = @At("HEAD"))
    private void renderStyledIndicator(S state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (!MtpvpDashboard.heartEnabled) return;

        // Check if the entity being rendered is a player
        if (state instanceof PlayerEntityRenderState playerState) {
            // Hum khud ko render nahi karna chahte (Unless in F5, but logic usually excludes self)
            // Note: 1.21.4 mein 'state' ke pass saari info hoti hai render karne ke liye
            
            // Player ki health aur info nikalne ke liye humein client side player ki target entity dekhni hogi
            // Kyunki 'state' sirf rendering data hai.
            var target = MinecraftClient.getInstance().world.getPlayerByUuid(playerState.uuid);
            
            if (target == null || target == MinecraftClient.getInstance().player || target.isInvisible()) return;

            float hp = target.getHealth();
            // Basic hit calculation
            int hits = (int) Math.ceil(hp / 4.0f); 
            
            matrices.push();
            // 1.21.4 mein height state se milti hai
            matrices.translate(0.0D, playerState.height + 0.5D, 0.0D);
            matrices.multiply(MinecraftClient.getInstance().getEntityRenderDispatcher().getRotation());
            matrices.scale(-0.025F, -0.025F, 0.025F);

            Matrix4f matrix = matrices.peek().getPositionMatrix();
            TextRenderer tr = MinecraftClient.getInstance().textRenderer;

            String displayText = "";
            int color = 0xFFFFFF;

            // Dashboard settings ke hisaab se style chunna
            switch (MtpvpDashboard.styleIndex) {
                case 0: // Status Bar
                    displayText = "[||||||||||] " + (int)hp + " HP";
                    color = (hp > 10) ? 0x00FF00 : 0xFF0000;
                    break;
                case 1: // Classic Hearts
                    displayText = "❤ " + (int)hp;
                    color = 0xFF5555;
                    break;
                case 2: // Head + Hits
                    displayText = "Hits: " + hits + " | " + playerState.displayName.getString();
                    color = 0xFFAA00;
                    break;
            }

            float x = (float)(-tr.getWidth(displayText) / 2);
            
            // Background Layer
            tr.draw(displayText, x, 0, 0x20FFFFFF, false, matrix, vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, 0x80000000, light);
            // Main Text Layer
            tr.draw(displayText, x, 0, color, false, matrix, vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, light);

            matrices.pop();
        }
    }
}
