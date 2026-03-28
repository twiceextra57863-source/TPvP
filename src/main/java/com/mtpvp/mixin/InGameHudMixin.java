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
import org.spongepowered.asm.mixin.injection.callback.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void renderTargetHud(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.crosshairTarget != null && client.crosshairTarget.getType() == HitResult.Type.ENTITY) {
            Entity entity = ((EntityHitResult) client.crosshairTarget).getEntity();
            
            if (entity instanceof PlayerEntity target) {
                int screenWidth = client.getWindow().getScaledWidth();
                
                String name = target.getName().getString();
                String hpText = (int)target.getHealth() + " HP";
                // Distance calculate karna
                float distance = client.player.distanceTo(target);
                String distText = String.format("%.1f blocks", distance);
                
                int boxX = screenWidth / 2 - 60;
                int boxY = 25;

                // Web-Style Panel for Target HUD
                context.fill(boxX, boxY, boxX + 120, boxY + 35, 0xCC121212); // BG
                context.fill(boxX, boxY, boxX + 120, boxY + 1, 0xFF00AAFF); // Top Accent

                context.drawText(client.textRenderer, name, screenWidth / 2 - (client.textRenderer.getWidth(name) / 2), boxY + 5, 0xFFFFFF, true);
                context.drawText(client.textRenderer, hpText + " | " + distText, screenWidth / 2 - (client.textRenderer.getWidth(hpText + " | " + distText) / 2), boxY + 18, 0x00FF00, false);
            }
        }
    }
}
