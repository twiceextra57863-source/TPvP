package com.tpvp.hud;

import com.tpvp.config.ModConfig;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderTickCounter;
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

        double maxDistance = client.options.getClampedViewDistance() * 16.0;
        List<AbstractClientPlayerEntity> nearbyPlayers = client.world.getPlayers().stream()
                .filter(p -> p != client.player && p.distanceTo(client.player) <= maxDistance)
                .sorted(Comparator.comparingDouble(p -> p.distanceTo(client.player)))
                .limit(5).toList();

        if (nearbyPlayers.isEmpty()) return;

        context.getMatrices().push();
        context.getMatrices().translate(ModConfig.radarX, ModConfig.radarY, 0);
        context.getMatrices().scale(ModConfig.radarScale, ModConfig.radarScale, 1.0f);

        int startY = 0;
        context.drawTextWithShadow(client.textRenderer, "§l📡 Radar", 0, startY, 0xFFAAFF);
        startY += 12;

        for (AbstractClientPlayerEntity target : nearbyPlayers) {
            double distance = client.player.distanceTo(target);
            Vec3d playerPos = client.player.getPos();
            Vec3d targetPos = target.getPos();
            
            double dx = targetPos.x - playerPos.x;
            double dz = targetPos.z - playerPos.z;
            double angleToTarget = Math.toDegrees(Math.atan2(dz, dx)) - 90;
            double relativeYaw = MathHelper.wrapDegrees(angleToTarget - client.player.getYaw());

            String arrow = "▼";
            if (relativeYaw >= -45 && relativeYaw < 45) arrow = "▲";
            else if (relativeYaw >= 45 && relativeYaw < 135) arrow = "▶";
            else if (relativeYaw >= -135 && relativeYaw < -45) arrow = "◀";

            double dy = targetPos.y - playerPos.y;
            String elevation = (dy > 3) ? "§a↑" : (dy < -3) ? "§c↓" : "§7-";

            context.fill(0, startY, 130, startY + 14, 0x66000000);
            context.drawTexture(RenderLayer::getGuiTextured, target.getSkinTextures().texture(), 2, startY + 2, 8f, 8f, 10, 10, 64, 64);

            String name = target.getName().getString();
            if (name.length() > 8) name = name.substring(0, 8) + "..";

            String displayText = String.format("§e%s §f%s §7(%.0fm) %s", arrow, name, distance, elevation);
            context.drawTextWithShadow(client.textRenderer, displayText, 16, startY + 3, 0xFFFFFF);
            
            startY += 16;
        }
        context.getMatrices().pop();
    }
}
