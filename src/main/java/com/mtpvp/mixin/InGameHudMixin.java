package com.mtpvp.mixin;

import com.mtpvp.gui.MtpvpDashboard;
import net.minecraft.client.MinecraftClient;
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
                
                String name = target.getName().getString();
                String hpText = (int)target.getHealth() + " HP";
                float distance = client.player.distanceTo(target);
                String distText = String.format("%.1f blocks", distance);
                
                int boxWidth = 140;
                int boxX = screenWidth / 2 - (boxWidth / 2);
                int boxY = 25;

                // --- Modern Web Panel UI ---
                context.fill(boxX, boxY, boxX + boxWidth, boxY + 50, 0xCC121212); // Main BG
                context.fill(boxX, boxY, boxX + boxWidth, boxY + 1, 0xFF00AAFF); // Top Glow

                // Name & HP
                context.drawText(client.textRenderer, name, screenWidth / 2 - (client.textRenderer.getWidth(name) / 2), boxY + 5, 0xFFFFFF, true);
                context.drawText(client.textRenderer, hpText + " | " + distText, screenWidth / 2 - (client.textRenderer.getWidth(hpText + " | " + distText) / 2), boxY + 16, 0x00FF00, false);

                // --- Armor Display ---
                int armorX = boxX + 30;
                int armorY = boxY + 28;
                
                // Helmet, Chest, Legs, Boots + Hand Item
                ItemStack[] armor = {
                    target.getInventory().getArmorStack(3),
                    target.getInventory().getArmorStack(2),
                    target.getInventory().getArmorStack(1),
                    target.getInventory().getArmorStack(0),
                    target.getMainHandStack()
                };

                for (ItemStack stack : armor) {
                    if (!stack.isEmpty()) {
                        context.drawItem(stack, armorX, armorY);
                        // Armor durability color bar (Minecraft default logic)
                        context.drawItemInGui(client.textRenderer, stack, armorX, armorY);
                        armorX += 18;
                    }
                }
            }
        }
    }
}
