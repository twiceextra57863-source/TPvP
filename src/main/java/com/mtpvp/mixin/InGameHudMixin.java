package com.mtpvp.mixin;

import com.mtpvp.gui.MtpvpDashboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) return;
        TextRenderer tr = client.textRenderer;

        // --- 1. TARGET HUD LOGIC ---
        if (MtpvpDashboard.hudEnabled && client.crosshairTarget != null && client.crosshairTarget.getType() == HitResult.Type.ENTITY) {
            if (((EntityHitResult) client.crosshairTarget).getEntity() instanceof PlayerEntity target) {
                renderTargetBox(context, client, target, tr);
            }
        }
    }

    private void renderTargetBox(DrawContext context, MinecraftClient client, PlayerEntity target, TextRenderer tr) {
        int sw = client.getWindow().getScaledWidth();
        float hp = target.getHealth();
        
        // Dynamic Info based on Style
        String info = switch (MtpvpDashboard.styleIndex) {
            case 1 -> "Hits: " + (int) Math.ceil(hp / 3.5f);
            case 2 -> (int)hp + " HP";
            default -> "❤ " + (int)hp;
        };

        int bx = sw / 2 - 70;
        int by = 20;
        
        // UI Box
        context.fill(bx, by, bx + 140, by + 50, 0xCC121212);
        context.fill(bx, by, bx + 140, by + 1, 0xFF00AAFF);
        
        context.drawText(tr, target.getName().getString(), sw/2 - tr.getWidth(target.getName().getString())/2, by + 5, 0xFFFFFF, true);
        context.drawText(tr, info + " | " + String.format("%.1f blocks", client.player.distanceTo(target)), sw/2 - tr.getWidth(info + " | 0.0 blocks")/2, by + 16, 0x00FF00, false);

        // Armor
        int ax = bx + 25;
        for (int i = 3; i >= 0; i--) {
            ItemStack stack = target.getInventory().getArmorStack(i);
            if (!stack.isEmpty()) {
                context.drawItem(stack, ax, by + 28);
                context.drawStackOverlay(tr, stack, ax, by + 28);
                ax += 20;
            }
        }
    }
}
