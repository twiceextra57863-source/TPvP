package com.mtpvp.mixin;

import com.mtpvp.gui.MtpvpDashboard;
import com.tpvp.accessor.IEntityRenderState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
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
    @Unique private static final Identifier GUI_ICONS = Identifier.ofVanilla("textures/gui/icons.png");

    @Inject(method = "renderLabelIfPresent", at = @At("HEAD"), cancellable = true)
    private void onRenderLabel(S state, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (!(state instanceof PlayerEntityRenderState playerState)) return;
        if (!MtpvpDashboard.headEnabled) return;

        if (state instanceof IEntityRenderState data) {
            // CANCEL original tag so our custom heavy one shows up
            ci.cancel();

            float hp = data.tpvp$getHealth();
            float maxHp = data.tpvp$getMaxHealth();
            String nameStr = playerState.name; 

            if (animatedHp < 0) animatedHp = hp;
            animatedHp = MathHelper.lerp(0.12f, animatedHp, hp);
            int healthColor = (hp > 14) ? 0xFF55FF55 : (hp > 7 ? 0xFFFFFF55 : 0xFFFF5555);

            matrices.push();
            var camera = MinecraftClient.getInstance().gameRenderer.getCamera();
            matrices.multiply(camera.getRotation()); 
            matrices.translate(0, 0.45f, 0); 
            matrices.scale(-0.025f, -0.025f, 0.025f);
            
            TextRenderer tr = MinecraftClient.getInstance().textRenderer;
            Matrix4f matrix = matrices.peek().getPositionMatrix();

            // --- DISTANCE DISPLAY ---
            double dx = playerState.x - camera.getPos().x;
            double dy = playerState.y - camera.getPos().y;
            double dz = playerState.z - camera.getPos().z;
            float dist = (float) Math.sqrt(dx*dx + dy*dy + dz*dz);

            if (MtpvpDashboard.showDistance) {
                String dText = String.format("§e%.1fm", dist);
                tr.draw(dText, -tr.getWidth(dText)/2f, -14, 0xFFFFFF, true, matrix, vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, 0, light);
            }

            // --- GANG WAR TARGET BRACKETS (THE FEATURE YOU WANTED) ---
            boolean isTarget = nameStr.equals(MtpvpDashboard.targetPlayerName) || (MtpvpDashboard.autoTargetLowHp && hp <= 6.0f);
            if (isTarget) {
                matrices.push();
                float tScale = Math.max(1.0f, dist / 10f);
                matrices.scale(tScale, tScale, 1.0f);
                
                String L = "{", R = "}";
                int tColor = 0xFFFF0000;
                switch (MtpvpDashboard.targetStyle) {
                    case 1 -> { L = "«"; R = "»"; tColor = 0xFF00FFFF; } 
                    case 2 -> { L = "["; R = "]"; tColor = 0xFFFF5555; } 
                    case 3 -> { L = ">"; R = "<"; tColor = 0xFFFFFF55; } 
                    case 4 -> { L = "★"; R = "★"; tColor = 0xFFFFAA00; }
                }

                // Brackets ko name ke bagal mein chamkana
                float offset = (tr.getWidth(text) / 2f) + 12;
                tr.draw("§l" + L, -offset, 0, tColor, true, matrix, vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, 0, 15728880);
                tr.draw("§l" + R, offset, 0, tColor, true, matrix, vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, 0, 15728880);
                matrices.pop();
            }

            // DRAW NAME MANUALLY
            tr.draw(text, -tr.getWidth(text)/2f, 0, 0xFFFFFF, true, matrix, vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, 0, light);

            // --- ALL 3 INDICATOR STYLES RESTORED ---
            
            // STYLE 0: 10 HEARTS
            if (MtpvpDashboard.styleIndex == 0) {
                int full = (int) Math.ceil(hp / 2);
                String heartText = "§c" + "❤".repeat(Math.max(0, full)) + "§8" + "❤".repeat(Math.max(0, 10 - full));
                tr.draw(heartText, -tr.getWidth(heartText.replaceAll("§.", ""))/2f, 10, 0xFFFFFF, false, matrix, vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, 0, light);
            } 
            // STYLE 1: SMOOTH PROGRESS BAR
            else if (MtpvpDashboard.styleIndex == 1) {
                float w = 50f, prog = (animatedHp / maxHp) * w;
                drawRect(matrices, vertexConsumers, -w/2 - 1, 9, w/2 + 1, 15, 0xFF000000); 
                drawRect(matrices, vertexConsumers, -w/2, 10, w/2, 14, 0xAA333333); 
                drawRect(matrices, vertexConsumers, -w/2, 10, -w/2 + prog, 14, healthColor);
            }
            // STYLE 2: PRO FACE & HP
            else if (MtpvpDashboard.styleIndex == 2) {
                Identifier skin = playerState.skinTextures.texture();
                drawFace(matrices, vertexConsumers, skin, -25, 10, 12);
                tr.draw("§l" + (int)hp + " HP", 0, 10, healthColor, true, matrix, vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, 0, light);
            }

            // ARMOR ICON
            if (MtpvpDashboard.showAdvancedInfo) {
                drawIcon(matrices, vertexConsumers, 34, 9, 9, 9, -5, 22); 
            }

            matrices.pop();
        }
    }

    // Helper functions for Heavy Rendering
    @Unique
    private void drawFace(MatrixStack m, VertexConsumerProvider v, Identifier s, float x, float y, float sz) {
        Matrix4f mat = m.peek().getPositionMatrix();
        var b = v.getBuffer(net.minecraft.client.render.RenderLayer.getGuiTextured(s));
        float u1=8f/64f, v1=8f/64f, u2=16f/64f, v2=16f/64f;
        b.vertex(mat, x, y+sz, 0).color(1f,1f,1f,1f).texture(u1,v2).light(15728880);
        b.vertex(mat, x+sz, y+sz, 0).color(1f,1f,1f,1f).texture(u2,v2).light(15728880);
        b.vertex(mat, x+sz, y, 0).color(1f,1f,1f,1f).texture(u2,v1).light(15728880);
        b.vertex(mat, x, y, 0).color(1f,1f,1f,1f).texture(u1,v1).light(15728880);
    }

    @Unique
    private void drawIcon(MatrixStack m, VertexConsumerProvider v, float u, float v1, float uw, float vh, float x, float y) {
        Matrix4f mat = m.peek().getPositionMatrix();
        var b = v.getBuffer(net.minecraft.client.render.RenderLayer.getGuiTextured(GUI_ICONS));
        float ts = 256f;
        b.vertex(mat, x, y+8, 0).color(1f,1f,1f,1f).texture(u/ts, (v1+vh)/ts).light(15728880);
        b.vertex(mat, x+8, y+8, 0).color(1f,1f,1f,1f).texture((u+uw)/ts, (v1+vh)/ts).light(15728880);
        b.vertex(mat, x+8, y, 0).color(1f,1f,1f,1f).texture((u+uw)/ts, v1/ts).light(15728880);
        b.vertex(mat, x, y, 0).color(1f,1f,1f,1f).texture(u/ts, v1/ts).light(15728880);
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
