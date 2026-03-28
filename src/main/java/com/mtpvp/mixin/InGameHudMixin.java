package com.mtpvp.mixin;

import com.mtpvp.gui.MtpvpDashboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void renderTargetHud(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.crosshairTarget != null && client.crosshairTarget.getType() == HitResult.Type.ENTITY) {
            Entity entity = ((EntityHitResult) client.crosshairTarget).getEntity();
            
            if (entity instanceof PlayerEntity target) {
                int width = client.getWindow().getScaledWidth();
                int height = client.getWindow().getScaledHeight();

                String name = target.getName().getString();
                String hpText = (int)target.getHealth() + " HP";
                
                // Modern Web-style Target HUD box
                context.fill(width / 2 - 50, 20, width / 2 + 50, 45, 0xAA000000); // Black Transparent BG
                context.fill(width / 2 - 50, 20, width / 2 + 50, 21, 0xFF00AAFF); // Neon Top Border

                context.drawText(client.textRenderer, name, width / 2 - (client.textRenderer.getWidth(name) / 2), 25, 0xFFFFFF, true);
                context.drawText(client.textRenderer, hpText, width / 2 - (client.textRenderer.getWidth(hpText) / 2), 34, 0xFF5555, true);
            }
        }
    }
}
