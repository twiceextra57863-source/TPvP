package com.heartindicator.hud;

import com.heartindicator.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.List;

public class HeartIndicatorHud {

    // Heart colours (gradient from green → yellow → red)
    private static int heartColor(float ratio) {
        // ratio: 1.0 = full, 0.0 = dead
        if (ratio > 0.6f) {
            // green → yellow
            float t = (ratio - 0.6f) / 0.4f;
            int r = (int) ((1 - t) * 255);
            return 0xFF000000 | (r << 16) | (0xCC << 8);
        } else if (ratio > 0.3f) {
            // yellow → orange
            return 0xFFFF9900;
        } else {
            // red (danger)
            return 0xFFFF2222;
        }
    }

    /**
     * Called by PlayerEntityRendererMixin after the nametag is rendered.
     * Draws the health bar + hearts above the player's head.
     */
    public static void renderAbovePlayer(
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            PlayerEntity target,
            int light
    ) {
        ModConfig cfg = ModConfig.get();
        if (!cfg.indicatorEnabled) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        // Range check
        Vec3d selfPos   = mc.player.getPos();
        Vec3d targetPos = target.getPos();
        if (selfPos.distanceTo(targetPos) > cfg.range.blocks) return;

        // Don't render on yourself (unless you want to)
        if (target.getUuid().equals(mc.player.getUuid())) return;

        float maxHp      = target.getMaxHealth();
        float currentHp  = target.getHealth();
        float ratio      = currentHp / maxHp;
        int   heartCount = Math.round(maxHp / 2f); // each heart = 2 hp
        int   filledHearts = Math.round(currentHp / 2f);

        TextRenderer tr = mc.textRenderer;

        matrices.push();

        // Position: slightly above the nametag (nametag is at ~0.5 above head)
        matrices.translate(0.0, 0.35, 0.0);

        // Always face camera
        matrices.multiply(mc.getEntityRenderDispatcher().getRotation());
        matrices.scale(-0.025f, -0.025f, 0.025f);

        Matrix4f mat = matrices.peek().getPositionMatrix();

        // ── Background pill ──────────────────────────────────────────────────────
        if (cfg.showBackground) {
            int totalWidth = heartCount * 10 + 4;
            drawRect(mat, vertexConsumers,
                    -totalWidth / 2f - 2, -9, totalWidth / 2f + 2, 2,
                    0xCC000000);
        }

        // ── Hearts / icons ───────────────────────────────────────────────────────
        String heartFull  = getFullIcon(cfg.iconStyle);
        String heartEmpty = getEmptyIcon(cfg.iconStyle);

        int totalWidth = heartCount * 10;
        int startX     = -totalWidth / 2;

        BufferBuilder buf = Tessellator.getInstance().begin(
                VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        for (int i = 0; i < heartCount; i++) {
            int x = startX + i * 10;
            boolean filled = (i < filledHearts);
            int color      = filled ? heartColor(ratio) : 0x66FFFFFF;
            String icon    = filled ? heartFull : heartEmpty;

            // Pulsing glow on critically low health
            if (filled && ratio < 0.3f) {
                long tick = System.currentTimeMillis();
                float pulse = (float)(Math.sin(tick / 200.0) * 0.5 + 0.5);
                int alpha = (int)(pulse * 180 + 40);
                // Draw glow behind heart
                drawRect(mat, vertexConsumers,
                        x - 1, -8, x + 8, 1,
                        (alpha << 24) | 0xFF0000);
            }

            tr.draw(icon, x, -7, color, false, mat, vertexConsumers,
                    TextRenderer.TextLayerType.NORMAL, 0, light);
        }

        // ── HP text ───────────────────────────────────────────────────────────────
        String hpText;
        if (cfg.showPercentage) {
            hpText = String.format("%.0f%%", ratio * 100);
        } else {
            hpText = String.format("%.1f / %.0f", currentHp, maxHp);
        }
        int textW  = tr.getWidth(hpText);
        int textCol = heartColor(ratio);

        tr.draw(hpText, -textW / 2f, -17, textCol, false, mat, vertexConsumers,
                TextRenderer.TextLayerType.SEE_THROUGH, 0, light);

        matrices.pop();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────────

    private static String getFullIcon(ModConfig.IconStyle style) {
        return switch (style) {
            case CLASSIC -> "❤";
            case PIXEL   -> "♥";
            case MODERN  -> "◆";
        };
    }

    private static String getEmptyIcon(ModConfig.IconStyle style) {
        return switch (style) {
            case CLASSIC -> "♡";
            case PIXEL   -> "♡";
            case MODERN  -> "◇";
        };
    }

    private static void drawRect(
            Matrix4f mat,
            VertexConsumerProvider vcp,
            float x1, float y1, float x2, float y2,
            int color
    ) {
        int a = (color >> 24) & 0xFF;
        int r = (color >> 16) & 0xFF;
        int g = (color >>  8) & 0xFF;
        int b =  color        & 0xFF;

        VertexConsumer vc = vcp.getBuffer(RenderLayer.getGuiOverlay());
        vc.vertex(mat, x1, y2, 0).color(r, g, b, a);
        vc.vertex(mat, x2, y2, 0).color(r, g, b, a);
        vc.vertex(mat, x2, y1, 0).color(r, g, b, a);
        vc.vertex(mat, x1, y1, 0).color(r, g, b, a);
    }
}
