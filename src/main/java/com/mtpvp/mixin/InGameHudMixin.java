package com.mtpvp.mixin;

import com.mtpvp.gui.MtpvpDashboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.Entity;
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
    private void renderTargetHud(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (!MtpvpDashboard.heartEnabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.crosshairTarget != null && client.crosshairTarget.getType() == HitResult.Type.ENTITY) {
            Entity entity = ((EntityHitResult) client.crosshairTarget).getEntity();
            
            if (entity instanceof PlayerEntity target) {
                int screenWidth = client.getWindow().getScaledWidth();
                TextRenderer tr = client.textRenderer;
                
                String name = target.getName().getString();
                String hpText = (int)target.getHealth() + " HP";
                float distance = client.player.distanceTo(target);
                String distText = String.format("%.1f blocks", distance);
                
                int boxWidth = 140;
                int boxX = screenWidth / 2 - (boxWidth / 2);
                int boxY = 25;

                // --- Background Panel ---
                context.fill(boxX, boxY, boxX + boxWidth, boxY + 52, 0xCC121212); 
                context.fill(boxX, boxY, boxX + boxWidth, boxY + 1, 0xFF00AAFF); 

                // Name & Stats
                context.drawText(tr, name, screenWidth / 2 - (tr.getWidth(name) / 2), boxY + 5, 0xFFFFFF, true);
                context.drawText(tr, hpText + " | " + distText, screenWidth / 2 - (tr.getWidth(hpText + " | " + distText) / 2), boxY + 16, 0x00FF00, false);

                // --- Armor Display Fix ---
                int armorX = boxX + 25;
                int armorY = boxY + 28;
                
                // 1.21.4 Compatible Item Rendering
                ItemStack[] items = {
                    target.getInventory().getArmorStack(3), // Helmet
                    target.getInventory().getArmorStack(2), // Chest
                    target.getInventory().getArmorStack(1), // Legs
                    target.getInventory().getArmorStack(0), // Boots
                    target.getMainHandStack()               // Main Hand
                };

                for (ItemStack stack : items) {
                    if (!stack.isEmpty()) {
                        context.drawItem(stack, armorX, armorY);
                        // Is method se durability bar aur stack count (agar arrow hai) dikhega
                        context.drawStackOverlay(tr, stack, armorX, armorY);
                        armorX += 20;
                    }
                }
            }
        }
    }
}
