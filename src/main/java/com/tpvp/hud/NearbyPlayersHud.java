package com.tpvp.hud;

import com.tpvp.config.ModConfig;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;
import java.util.List;

public class NearbyPlayersHud implements HudRenderCallback {

    @Override
    public void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        if (!ModConfig.nearbyEnabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;
        
        // Custom Scale aur Position lagane ke liye Matrix push karna
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(ModConfig.hudX, ModConfig.hudY, 0);
        matrices.scale(ModConfig.hudScale, ModConfig.hudScale, 1f);

        double maxDistance = client.options.getClampedViewDistance() * 16.0;

        List<AbstractClientPlayerEntity> nearbyPlayers = client.world.getPlayers().stream()
                .filter(p -> p != client.player)
                .filter(p -> p.distanceTo(client.player) <= maxDistance)
                .sorted(Comparator.comparingDouble(p -> p.distanceTo(client.player)))
                .limit(5)
                .toList();

        if (!nearbyPlayers.isEmpty()) {
            int localX = 0; // Ab 0,0 use karenge kyunki Matrix Translate ho chuka hai
            int localY = 0;

            context.drawTextWithShadow(client.textRenderer, "§l📡 Nearby Players", localX, localY, 0xFFAAFF);
            localY += 12;

            for (AbstractClientPlayerEntity target : nearbyPlayers) {
                double distance = client.player.distanceTo(target);

                Vec3d playerPos = client.player.getPos();
                Vec3d targetPos = target.getPos();
                double dx = targetPos.x - playerPos.x;
                double dz = targetPos.z - playerPos.z;
                
                double angleToTarget = Math.toDegrees(Math.atan2(dz, dx)) - 90;
                double relativeYaw = MathHelper.wrapDegrees(angleToTarget - client.player.getYaw());

                String arrow;
                if (relativeYaw >= -45 && relativeYaw < 45) arrow = "▲";
                else if (relativeYaw >= 45 && relativeYaw < 135) arrow = "▶";
                else if (relativeYaw >= -135 && relativeYaw < -45) arrow = "◀";
                else arrow = "▼";

                double dy = targetPos.y - playerPos.y;
                String elevation = (dy > 3) ? "§a↑" : (dy < -3) ? "§c↓" : "§7-";

                context.fill(localX, localY, localX + 130, localY + 14, 0x66000000);

                Identifier skin = target.getSkinTextures().texture();
                context.drawTexture(RenderLayer::getGuiTextured, skin, localX + 2, localY + 2, 8f, 8f, 10, 10, 64, 64);

                String name = target.getName().getString();
                if (name.length() > 8) name = name.substring(0, 8) + "..";

                String displayText = String.format("§e%s §f%s §7(%.0fm) %s", arrow, name, distance, elevation);
                context.drawTextWithShadow(client.textRenderer, displayText, localX + 16, localY + 3, 0xFFFFFF);

                localY += 16;
            }
        }
        
        matrices.pop(); // Matrix ko reset karna zaroori hai
    }
}
