package com.tpvp.hud;

import com.tpvp.config.ModConfig;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
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

        // Render distance in blocks (Render Distance Chunk * 16)
        double maxDistance = client.options.getClampedViewDistance() * 16.0;

        // Sabhi players ki list nikalna aur sort karna
        List<AbstractClientPlayerEntity> nearbyPlayers = client.world.getPlayers().stream()
                .filter(p -> p != client.player) // Khud ko hatana
                .filter(p -> p.distanceTo(client.player) <= maxDistance) // Range check
                .sorted(Comparator.comparingDouble(p -> p.distanceTo(client.player))) // Sabse pass wala pehle
                .limit(5) // Max 5 players
                .toList();

        if (nearbyPlayers.isEmpty()) return;

        int startX = 10; // Screen ke left side me
        int startY = 30; // Thoda upar se start hoga

        context.drawTextWithShadow(client.textRenderer, "§l📡 Nearby Players", startX, startY, 0xFFAAFF);
        startY += 12;

        for (AbstractClientPlayerEntity target : nearbyPlayers) {
            double distance = client.player.distanceTo(target);

            // ---- DIRECTION CALCULATIONS ----
            Vec3d playerPos = client.player.getPos();
            Vec3d targetPos = target.getPos();
            double dx = targetPos.x - playerPos.x;
            double dz = targetPos.z - playerPos.z;
            
            // Angle to target (-90 because MC yaw 0 is +Z)
            double angleToTarget = Math.toDegrees(Math.atan2(dz, dx)) - 90;
            double relativeYaw = MathHelper.wrapDegrees(angleToTarget - client.player.getYaw());

            // Simple 2D Direction Arrow
            String arrow = "▲"; // Aage
            if (relativeYaw >= -45 && relativeYaw < 45) arrow = "▲"; // Front
            else if (relativeYaw >= 45 && relativeYaw < 135) arrow = "▶"; // Right
            else if (relativeYaw >= -135 && relativeYaw < -45) arrow = "◀"; // Left
            else arrow = "▼"; // Back

            // Elevation Indicator (Upar/Neeche)
            double dy = targetPos.y - playerPos.y;
            String elevation = (dy > 3) ? "§a↑" : (dy < -3) ? "§c↓" : "§7-";

            // Modern Dark Background Box for each player
            context.fill(startX, startY, startX + 130, startY + 14, 0x66000000);

            // Render Player Head (Skin)
            Identifier skin = target.getSkinTextures().texture();
            // x, y, u, v, width, height, textureWidth, textureHeight
            context.drawTexture(skin, startX + 2, startY + 2, 8, 8, 10, 10, 64, 64);

            // Render Text: [Arrow] Name [Distance] [Elevation]
            String name = target.getName().getString();
            // Lamba naam ho toh cut karna
            if (name.length() > 8) name = name.substring(0, 8) + "..";

            String displayText = String.format("§e%s §f%s §7(%.0fm) %s", arrow, name, distance, elevation);
            context.drawTextWithShadow(client.textRenderer, displayText, startX + 16, startY + 3, 0xFFFFFF);

            startY += 16; // Next player ke liye niche shift
        }
    }
}
