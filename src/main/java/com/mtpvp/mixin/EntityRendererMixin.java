package com.mtpvp.mixin;

import com.mtpvp.gui.MtpvpDashboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRendererMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private <E extends Entity, S extends EntityRenderState> void renderMtpvpIndicator(E entity, double x, double y, double z, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (!MtpvpDashboard.heartEnabled) return;

        // Sirf dusre players ke liye
        if (entity instanceof PlayerEntity target && target != MinecraftClient.getInstance().player) {
            if (target.isInvisible() || !target.isAlive()) return;

            float hp = target.getHealth();
            // Calculation based on target's health
            int hits = (int) Math.ceil(hp / 3.5f);

            matrices.push();
            // Position: Player ke sir ke upar (y + height + offset)
            matrices.translate(x, y + entity.getHeight() + 0.5, z);
            
            // Player ki taraf rotate karna
            matrices.multiply(MinecraftClient.getInstance().getEntityRenderDispatcher().getRotation());
            matrices.scale(-0.025F, -0.025F, 0.025F);

            Matrix4f matrix = matrices.peek().getPositionMatrix();
            TextRenderer tr = MinecraftClient.getInstance().textRenderer;

            String info = "";
            int color = 0xFFFFFF;

            // Style Logic from Dashboard
            switch (MtpvpDashboard.styleIndex) {
                case 0 -> { info = "❤ " + (int)hp; color = 0xFF5555; }
                case 1 -> { info = "Hits: " + hits; color = 0xFFAA00; }
                case 2 -> { info = "HP: " + (int)hp + " | " + target.getName().getString(); color = 0x55FFFF; }
            }

            float xOffset = (float)(-tr.getWidth(info) / 2);

            // DRAWING (SEE_THROUGH layer ensures it shows up even with server-side custom nametags)
            tr.draw(info, xOffset, 0, color, false, matrix, vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, 0, light);

            matrices.pop();
        }
    }
}
