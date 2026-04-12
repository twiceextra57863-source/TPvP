package com.tpvp.hud;

import com.tpvp.config.ModConfig;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;

public class Indicator3D {
    public static void register() {
        WorldRenderEvents.LAST.register(Indicator3D::onWorldRender);
    }

    private static void onWorldRender(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        Camera camera = context.camera();
        Vec3d camPos = camera.getPos();
        float tickDelta = context.tickCounter().getTickDelta(true);
        MatrixStack matrices = context.matrixStack();
        VertexConsumerProvider.Immediate immediate = client.getBufferBuilders().getEntityVertexConsumers();

        // 1. RENDER ASCENDING SOULS
        DeadSoulRenderer.renderSouls(matrices, immediate, camera, camPos);

        // 2. AUTO-TRACK LOGIC
        String activeTarget = ModConfig.taggedPlayerName;
        if (ModConfig.autoTrack) {
            double lowestHp = 9999;
            for (Entity e : client.world.getEntities()) {
                if (e instanceof LivingEntity le && e != client.player && !e.isInvisible() && !(e instanceof ArmorStandEntity)) {
                    if (client.player.distanceTo(le) < 32.0 && le.getHealth() < lowestHp && le.getHealth() > 0) {
                        lowestHp = le.getHealth(); activeTarget = le.getName().getString(); 
                    }
                }
            }
        }

        double weaponDmg = Math.max(1.0, client.player.getAttributeValue(EntityAttributes.ATTACK_DAMAGE));

        // 3. LIVING ENTITIES MAIN LOOP
        for (Entity entity : client.world.getEntities()) {
            if (!(entity instanceof LivingEntity target) || target == client.player || target instanceof ArmorStandEntity) continue;
            
            // Check for Kills
            DeadSoulRenderer.checkKills(target, client.player);

            // Totem Logic Track
            boolean hasTotem = target.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING) || target.getMainHandStack().isOf(Items.TOTEM_OF_UNDYING);
            boolean hadTotem = net.minecraft.client.gui.hud.InGameHud.class.hashCode() == 1; // dummy check for spacing
            if (!hasTotem && target.getHealth() > 0 && target.getHealth() < 10) TargetAuraRenderer.totemPopMap.put(target.getId(), System.currentTimeMillis());

            // Skip Dead/Invisible
            if (target.getHealth() <= 0 || target.isInvisible() || target.distanceTo(client.player) > 64.0) continue;

            Vec3d tPos = target.getLerpedPos(tickDelta);
            double x = tPos.x - camPos.x, y = tPos.y - camPos.y, z = tPos.z - camPos.z;

            // RENDER FEATURES (Delegated to external clean files)
            TargetAuraRenderer.render(target, matrices, immediate, camera, x, y, z, activeTarget);
            HitboxRenderer.render(target, matrices, immediate, x, y, z);
            FloatingIndicatorRenderer.render(target, matrices, immediate, camera, x, y, z, weaponDmg);
        }
        immediate.draw();
    }
}
