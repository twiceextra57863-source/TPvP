package com.tpvp.mixin;

import com.mtpvp.gui.MtpvpDashboard;
import com.tpvp.accessor.IEntityRenderState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityNameTagMixin<S extends EntityRenderState> {

    @Unique private float animatedHp = -1f;

    @Inject(method = "renderLabelIfPresent", at = @At("HEAD"))
    private void onRenderLabel(S state, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (!MtpvpDashboard.headEnabled || !(state instanceof PlayerEntityRenderState playerState)) return;

        if (state instanceof IEntityRenderState data) {
            float hp = data.tpvp$getHealth();
            float maxHp = data.tpvp$getMaxHealth();
            
            // --- SMOOTH ANIMATION ---
            if (animatedHp < 0) animatedHp = hp;
            animatedHp = MathHelper.lerp(0.15f, animatedHp, hp);

            matrices.push();
            // Position: NameTag ke upar (Perfectly balanced)
            matrices.translate(0, 3.2f, 0); 
            matrices.scale(-0.025f, -0.025f, 0.025f);
            
            TextRenderer tr = MinecraftClient.getInstance().textRenderer;
            Matrix4f matrix = matrices.peek().getPositionMatrix();

            // 1. DISTANCE (FIXED: Using distanceToCamera)
            if (MtpvpDashboard.showDistance) {
                float dist = playerState.distanceToCamera; 
                String dText = String.format("§e%.1fm", dist);
                tr.draw(dText, -tr.getWidth(dText)/2f, -12, 0xFFFFFF, true, matrix, vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, 0, light);
            }

            // 2. HEALTH STYLES
            int color = (hp > 14) ? 0xFF55FF55 : (hp > 7 ? 0xFFFFFF55 : 0xFFFF5555);
            if (MtpvpDashboard.styleIndex == 0) { // HEARTS
                int full = (int) Math.ceil(hp / 2);
                String hText = "§c" + "❤".repeat(Math.max(0, full)) + "§8" + "❤".repeat(Math.max(0, 10 - full));
                tr.draw(hText, -tr.getWidth(hText.replaceAll("§.", ""))/2f, 0, 0xFFFFFF, false, matrix, vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, 0, light);
            } 
            else if (MtpvpDashboard.styleIndex == 1) { // SMOOTH BAR
                float w = 50f;
                float progress = (animatedHp / maxHp) * w;
                drawRect(matrices, vertexConsumers, -w/2-1, -1, w/2+1, 5, 0xFF000000); // Border
                drawRect(matrices, vertexConsumers, -w/2, 0, -w/2 + progress, 4, color); // Fill
            }
            else if (MtpvpDashboard.styleIndex == 2) { // PRO FACE + HITS
                int hits = (int) Math.ceil(hp / (data.tpvp$getAttackDamage() <= 0 ? 1.5 : data.tpvp$getAttackDamage()));
                Identifier skin = playerState.skinTextures.texture();
                drawFace(matrices, vertexConsumers, skin, -25, -4, 14);
                tr.draw("§l" + hits + " HITS", 0, 0, color, true, matrix, vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, 0, light);
            }

            // 3. ADVANCED INFO (FIXED: Accessing item state)
            if (MtpvpDashboard.showAdvancedInfo) {
                // 1.21.4 uses 'item' for current held item in render state
                ItemStack hand = playerState.item; 
                if (hand != null && !hand.isEmpty()) {
                    String itemInfo = "§b" + hand.getName().getString();
                    if (hand.isDamageable()) {
                        int dur = hand.getMaxDamage() - hand.getDamage();
                        itemInfo += " §f[" + dur + "]";
                    }
                    tr.draw(itemInfo, -tr.getWidth(itemInfo.replaceAll("§.", ""))/2f, 12, 0xFFFFFF, true, matrix, vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, 0, light);
                }
            }

            matrices.pop();
        }
    }

    @Unique
    private void drawFace(MatrixStack m, VertexConsumerProvider v, Identifier s, float x, float y, float sz) {
        Matrix4f mat = m.peek().getPositionMatrix();
        var b = v.getBuffer(net.minecraft.client.render.RenderLayer.getGuiTextured(s));
        float u1=8f/64f, v1=8f/64f, u2=16f/64f, v2=16f/64f;
        b.vertex(mat, x, y+sz, 0).color(1f,1f,1f,1f).texture(u1,v2);
        b.vertex(mat, x+sz, y+sz, 0).color(1f,1f,1f,1f).texture(u2,v2);
        b.vertex(mat, x+sz, y, 0).color(1f,1f,1f,1f).texture(u2,v1);
        b.vertex(mat, x, y, 0).color(1f,1f,1f,1f).texture(u1,v1);
    }

    @Unique
    private void drawRect(MatrixStack m, VertexConsumerProvider v, float x1, float y1, float x2, float y2, int c) {
        Matrix4f mat = m.peek().getPositionMatrix();
        float a=(c>>24&255)/255f, r=(c>>16&255)/255f, g=(c>>8&255)/255f, b=(c&255)/255f;
        var buf = v.getBuffer(net.minecraft.client.render.RenderLayer.getGuiOverlay());
        buf.vertex(mat, x1, y1, 0).color(r, g, b, a);
        buf.vertex(mat, x1, y2, 0).color(r, g, b, a);
        buf.vertex(mat, x2, y2, 0).color(r, g, b, a);
        buf.vertex(mat, x2, y1, 0).color(r, g, b, a);
    }
}
