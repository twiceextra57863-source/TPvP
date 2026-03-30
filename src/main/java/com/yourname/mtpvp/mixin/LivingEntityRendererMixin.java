package com.yourname.mtpvp.mixin;

import com.yourname.mtpvp.client.render.HeartIndicatorRenderer;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin<T extends LivingEntity> {
    
    @Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", 
            at = @At("RETURN"))
    private void renderHeartIndicator(T entity, float yaw, float tickDelta, MatrixStack matrices,
                                      VertexConsumerProvider vertexConsumers, int light,
                                      CallbackInfo ci) {
        if (HeartIndicatorRenderer.currentDesign == HeartIndicatorRenderer.DesignType.DISABLED) return;
        if (!(entity instanceof PlayerEntity player)) return;
        
        var client = net.minecraft.client.MinecraftClient.getInstance();
        if (client.player == player) return;
        
        matrices.push();
        
        matrices.translate(0, entity.getHeight() + 0.6, 0);
        matrices.scale(0.025f, -0.025f, 0.025f);
        
        TextRenderer textRenderer = client.textRenderer;
        float health = player.getHealth();
        float maxHealth = player.getMaxHealth();
        
        switch (HeartIndicatorRenderer.currentDesign) {
            case VANILLA:
                renderVanillaHearts(matrices, vertexConsumers, textRenderer, health, maxHealth);
                break;
            case STATUS_BAR:
                renderStatusBar(matrices, vertexConsumers, textRenderer, health, maxHealth);
                break;
            case PLAYER_HEAD:
                renderPlayerHead(matrices, vertexConsumers, textRenderer, client, player, health, maxHealth);
                break;
        }
        
        matrices.pop();
    }
    
    private void renderVanillaHearts(MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                                     TextRenderer textRenderer, float health, float maxHealth) {
        int heartCount = (int) Math.ceil(maxHealth / 2);
        int displayedHearts = (int) Math.ceil(health / 2);
        int startX = -(heartCount * 8);
        
        for (int i = 0; i < heartCount; i++) {
            matrices.push();
            matrices.translate(startX + i * 16, 0, 0);
            String heart = "❤";
            int color = (i >= displayedHearts) ? 0x663333 : 0xFF5555;
            textRenderer.draw(heart, 0, 0, color, false, 
                             matrices.peek().getPositionMatrix(), vertexConsumers,
                             TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);
            matrices.pop();
        }
    }
    
    private void renderStatusBar(MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                                 TextRenderer textRenderer, float health, float maxHealth) {
        float percent = health / maxHealth;
        int color = percent > 0.66 ? 0x55FF55 : (percent > 0.33 ? 0xFFAA55 : 0xFF5555);
        int barLength = 50;
        int filled = (int)(barLength * percent);
        
        String bar = "[" + "=".repeat(filled) + " ".repeat(barLength - filled) + "]";
        
        matrices.push();
        matrices.translate(-30, 0, 0);
        textRenderer.draw(bar, 0, 0, color, false,
                         matrices.peek().getPositionMatrix(), vertexConsumers,
                         TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);
        matrices.pop();
        
        matrices.push();
        matrices.translate(35, 0, 0);
        textRenderer.draw(String.format("%d%%", (int)(percent * 100)), 0, 0, 0xFFFFFF, false,
                         matrices.peek().getPositionMatrix(), vertexConsumers,
                         TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);
        matrices.pop();
    }
    
    private void renderPlayerHead(MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                                  TextRenderer textRenderer, net.minecraft.client.MinecraftClient client,
                                  PlayerEntity player, float health, float maxHealth) {
        matrices.push();
        matrices.translate(-45, 0, 0);
        textRenderer.draw(String.format("%.0f/%.0f ❤", health, maxHealth), 0, 0, 0xFFFFFF, false,
                         matrices.peek().getPositionMatrix(), vertexConsumers,
                         TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);
        matrices.pop();
        
        int hitsToKill = HeartIndicatorRenderer.calculateHitsToKill(client.player, player);
        matrices.push();
        matrices.translate(-45, 12, 0);
        textRenderer.draw(String.format("⚔️ HTK: %d", hitsToKill), 0, 0, 0xFFFF55, false,
                         matrices.peek().getPositionMatrix(), vertexConsumers,
                         TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);
        matrices.pop();
        
        if (health <= maxHealth * 0.25) {
            matrices.push();
            matrices.translate(-45, 24, 0);
            textRenderer.draw("⚠️ DEATH ZONE", 0, 0, 0xFF5555, false,
                             matrices.peek().getPositionMatrix(), vertexConsumers,
                             TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);
            matrices.pop();
        }
        
        matrices.push();
        matrices.translate(40, -5, 0);
        textRenderer.draw("👤", 0, 0, 0x55AAFF, false,
                         matrices.peek().getPositionMatrix(), vertexConsumers,
                         TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);
        matrices.pop();
    }
}
