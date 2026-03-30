package com.heartindicator.client.gui;

import com.heartindicator.config.ModConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class HeartIndicatorSettingsScreen extends Screen {

    private final Screen parent;
    private ModConfig cfg;

    // Animated pulse for the preview hearts
    private float animTick = 0f;

    public HeartIndicatorSettingsScreen(Screen parent) {
        super(Text.of("Heart Indicator Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        cfg = ModConfig.get();

        int cx   = this.width  / 2;
        int startY = 80;
        int btnW   = 220;
        int btnH   = 22;
        int gap    = 28;

        // ── Toggle ON/OFF ─────────────────────────────────────────────────────
        addDrawableChild(ButtonWidget.builder(
                getToggleText(),
                btn -> {
                    cfg.indicatorEnabled = !cfg.indicatorEnabled;
                    ModConfig.save();
                    btn.setMessage(getToggleText());
                })
                .dimensions(cx - btnW / 2, startY, btnW, btnH)
                .build());

        // ── Icon Style ────────────────────────────────────────────────────────
        addDrawableChild(ButtonWidget.builder(
                Text.of("Icon Style: " + cfg.iconStyle.label),
                btn -> {
                    cfg.iconStyle = cfg.iconStyle.next();
                    ModConfig.save();
                    btn.setMessage(Text.of("Icon Style: " + cfg.iconStyle.label));
                })
                .dimensions(cx - btnW / 2, startY + gap, btnW, btnH)
                .build());

        // ── Detection Range ───────────────────────────────────────────────────
        addDrawableChild(ButtonWidget.builder(
                Text.of("Range: " + cfg.range.label),
                btn -> {
                    cfg.range = cfg.range.next();
                    ModConfig.save();
                    btn.setMessage(Text.of("Range: " + cfg.range.label));
                })
                .dimensions(cx - btnW / 2, startY + gap * 2, btnW, btnH)
                .build());

        // ── Background pill toggle ────────────────────────────────────────────
        addDrawableChild(ButtonWidget.builder(
                Text.of("Background: " + (cfg.showBackground ? "§aON" : "§cOFF")),
                btn -> {
                    cfg.showBackground = !cfg.showBackground;
                    ModConfig.save();
                    btn.setMessage(Text.of("Background: " + (cfg.showBackground ? "§aON" : "§cOFF")));
                })
                .dimensions(cx - btnW / 2, startY + gap * 3, btnW, btnH)
                .build());

        // ── Animate hearts toggle ─────────────────────────────────────────────
        addDrawableChild(ButtonWidget.builder(
                Text.of("Animate Low HP: " + (cfg.animateHearts ? "§aON" : "§cOFF")),
                btn -> {
                    cfg.animateHearts = !cfg.animateHearts;
                    ModConfig.save();
                    btn.setMessage(Text.of("Animate Low HP: " + (cfg.animateHearts ? "§aON" : "§cOFF")));
                })
                .dimensions(cx - btnW / 2, startY + gap * 4, btnW, btnH)
                .build());

        // ── Show percentage toggle ────────────────────────────────────────────
        addDrawableChild(ButtonWidget.builder(
                Text.of("Show %: " + (cfg.showPercentage ? "§aON" : "§cOFF")),
                btn -> {
                    cfg.showPercentage = !cfg.showPercentage;
                    ModConfig.save();
                    btn.setMessage(Text.of("Show %: " + (cfg.showPercentage ? "§aON" : "§cOFF")));
                })
                .dimensions(cx - btnW / 2, startY + gap * 5, btnW, btnH)
                .build());

        // ── Back ──────────────────────────────────────────────────────────────
        addDrawableChild(ButtonWidget.builder(
                Text.of("← Back"),
                btn -> this.client.setScreen(parent))
                .dimensions(cx - 60, startY + gap * 6 + 6, 120, btnH)
                .build());
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        animTick += delta;

        // ── Dark blurred background ───────────────────────────────────────────
        this.renderBackground(ctx, mouseX, mouseY, delta);

        int cx = this.width / 2;

        // ── Gradient top bar ──────────────────────────────────────────────────
        ctx.fillGradient(0, 0, this.width, 60, 0xFF1a0000, 0xFF3d0000);

        // ── Decorative side lines ─────────────────────────────────────────────
        ctx.fill(0, 60, this.width, 62, 0xFFAA0000);

        // ── Title ─────────────────────────────────────────────────────────────
        // Big heart icon
        ctx.drawCenteredTextWithShadow(this.textRenderer,
                Text.of("§c❤"), cx, 14, 0xFFFFFF);

        ctx.drawCenteredTextWithShadow(this.textRenderer,
                Text.of("§fHEART  INDICATOR"), cx, 26, 0xFFFFFF);

        ctx.drawCenteredTextWithShadow(this.textRenderer,
                Text.of("§7Settings & Preferences"), cx, 38, 0xAAAAAA);

        // ── Live preview strip ────────────────────────────────────────────────
        renderPreviewHearts(ctx, cx, 68, delta);

        // ── Render buttons ────────────────────────────────────────────────────
        super.render(ctx, mouseX, mouseY, delta);

        // ── Section labels (small, grey) ──────────────────────────────────────
        int startY = 80;
        int gap    = 28;
        renderLabel(ctx, cx, startY - 10,          "§7── Indicator ──");
        renderLabel(ctx, cx, startY + gap - 10,     "§7── Appearance ──");
        renderLabel(ctx, cx, startY + gap * 2 - 10, "§7── Range ──");
        renderLabel(ctx, cx, startY + gap * 5 - 10, "§7── Display ──");
    }

    // ── Animated heart preview row ────────────────────────────────────────────
    private void renderPreviewHearts(DrawContext ctx, int cx, int y, float delta) {
        ModConfig cfg = ModConfig.get();
        String full  = getFullIcon(cfg.iconStyle);
        String empty = getEmptyIcon(cfg.iconStyle);

        int total  = 10;
        int filled = 7; // preview: 7/10 hearts
        int totalW = total * 10;
        int startX = cx - totalW / 2;

        // Background pill
        if (cfg.showBackground) {
            ctx.fill(startX - 4, y - 2, startX + totalW + 4, y + 10, 0xCC000000);
        }

        for (int i = 0; i < total; i++) {
            boolean f = (i < filled);
            int color;
            if (!f) {
                color = 0x66FFFFFF;
            } else if (i < 3) {
                // Simulate critical — pulsing red
                float pulse = (float)(Math.sin(animTick / 5.0) * 0.5 + 0.5);
                int alpha = (int)(pulse * 180 + 75);
                color = (alpha << 24) | 0xFF2222;
            } else {
                color = 0xFF22CC22;
            }
            ctx.drawTextWithShadow(this.textRenderer,
                    Text.of(f ? full : empty),
                    startX + i * 10, y, color);
        }

        // HP label preview
        String hpLabel = cfg.showPercentage ? "70%" : "14.0 / 20";
        ctx.drawCenteredTextWithShadow(this.textRenderer,
                Text.of("§f" + hpLabel), cx, y - 10, 0x22CC22);
    }

    private void renderLabel(DrawContext ctx, int cx, int y, String text) {
        ctx.drawCenteredTextWithShadow(this.textRenderer, Text.of(text), cx, y, 0x888888);
    }

    private Text getToggleText() {
        return cfg.indicatorEnabled
                ? Text.of("§a■ Indicator: ENABLED")
                : Text.of("§c■ Indicator: DISABLED");
    }

    private String getFullIcon(ModConfig.IconStyle s) {
        return switch (s) { case CLASSIC -> "❤"; case PIXEL -> "♥"; default -> "◆"; };
    }
    private String getEmptyIcon(ModConfig.IconStyle s) {
        return switch (s) { case CLASSIC -> "♡"; case PIXEL -> "♡"; default -> "◇"; };
    }

    @Override
    public boolean shouldPause() { return false; }
}
