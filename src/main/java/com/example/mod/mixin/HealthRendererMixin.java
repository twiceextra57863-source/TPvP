package com.example.mod.mixin;

import com.example.mod.ModSettings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class HealthRendererMixin<T extends Entity> {
    @Inject(method = "render", at = @At("TAIL"))
    public void onRender(T entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (!(entity instanceof PlayerEntity player) || player == MinecraftClient.getInstance().player || player.isInvisible()) return;

        matrices.push();
        matrices.translate(0, player.getHeight() + 0.5f, 0);
        matrices.multiply(MinecraftClient.getInstance().getEntityRenderDispatcher().getRotation());
        matrices.scale(-0.025f, -0.025f, 0.025f);

        TextRenderer tr = MinecraftClient.getInstance().textRenderer;
        float hp = player.getHealth();
        float max = player.getMaxHealth();

        if (ModSettings.currentStyle == 0) { // STYLE 1: Hearts
            String text = "§c❤ §f" + String.format("%.1f", hp);
            tr.draw(text, -tr.getWidth(text)/2f, 0, -1, false, matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, 0, light);
        } else if (ModSettings.currentStyle == 1) { // STYLE 2: Hits
            int hits = (int) Math.ceil(hp / 2.0f);
            String text = "§e" + hits + " Hits Left";
            tr.draw(text, -tr.getWidth(text)/2f, 0, -1, false, matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, 0, light);
        } else if (ModSettings.currentStyle == 2) { // STYLE 3: Color Bar
            String color = hp > 10 ? "§a" : (hp > 5 ? "§e" : "§c");
            String bar = color + "▮".repeat((int)(hp/max * 10)) + "§8" + "▮".repeat(10 - (int)(hp/max * 10));
            tr.draw(bar, -tr.getWidth(bar)/2f, 0, -1, false, matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, 0, light);
        }
        matrices.pop();
    }
}
