package com.tpvp.hud;

import com.tpvp.config.ModConfig;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DeadSoulRenderer {
    public static final Map<Integer, Float> lastHealthMap = new HashMap<>();
    public static final List<DeadSoul> activeSouls = new ArrayList<>();

    private static class DeadSoul {
        Vec3d pos; Identifier skin; long startTime;
        DeadSoul(Vec3d pos, Identifier skin) { this.pos = pos; this.skin = skin; this.startTime = System.currentTimeMillis(); }
    }

    public static void checkKills(LivingEntity target, PlayerEntity clientPlayer) {
        int id = target.getId();
        float health = target.getHealth();

        if (lastHealthMap.containsKey(id)) {
            float lastHealth = lastHealthMap.get(id);
            if (lastHealth > 0 && health <= 0) {
                
                LivingEntity attacker = target.getAttacker();
                String killerName = "Environment";
                Identifier kSkin = Identifier.ofVanilla("textures/entity/steve.png");

                if (attacker instanceof AbstractClientPlayerEntity pk) {
                    killerName = pk.getName().getString();
                    kSkin = pk.getSkinTextures() != null ? pk.getSkinTextures().texture() : kSkin;
                } else if (target.distanceTo(clientPlayer) < 10.0) {
                    killerName = clientPlayer.getName().getString(); 
                    kSkin = ((AbstractClientPlayerEntity)clientPlayer).getSkinTextures().texture();
                }

                String vName = target.getName().getString();
                Identifier vSkin = (target instanceof AbstractClientPlayerEntity pt) ? (pt.getSkinTextures() != null ? pt.getSkinTextures().texture() : kSkin) : kSkin;

                boolean isFriend = vName.equals(ModConfig.taggedFriendName);
                
                KillBannerHud.addKill(killerName, kSkin, vName, vSkin, isFriend); 
                
                if (target instanceof AbstractClientPlayerEntity) {
                    activeSouls.add(new DeadSoul(target.getPos(), vSkin)); 
                }
            }
        }
        lastHealthMap.put(id, health);
    }

    public static void renderSouls(MatrixStack matrices, VertexConsumerProvider.Immediate immediate, Camera camera, Vec3d camPos) {
        if (!ModConfig.soulAnimationEnabled) return;
        long now = System.currentTimeMillis();
        Iterator<DeadSoul> iter = activeSouls.iterator();
        
        while (iter.hasNext()) {
            DeadSoul soul = iter.next();
            long age = now - soul.startTime;
            if (age > 6000) { iter.remove(); continue; } 

            float life = age / 6000.0f; 
            double upY = life * 8.0; 
            float alpha = life > 0.8f ? (1.0f - life) / 0.2f : 1.0f; 
            
            matrices.push();
            matrices.translate(soul.pos.x - camPos.x, soul.pos.y - camPos.y + upY, soul.pos.z - camPos.z);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw())); 
            
            // PK Dance
            float wiggleX = (float) Math.sin(life * 50) * 15f; 
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(wiggleX)); 
            
            matrices.scale(0.8F, 0.8F, 0.8F); 
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180f)); 
            
            float armP = (float) Math.sin(life * 60) * 120f + 60f; 
            float legP = (float) Math.cos(life * 60) * 90f; 
            float headP = (float) Math.sin(life * 40) * 30f; 

            // Renders 100% accurate player body!
            RenderUtils3D.drawDoll(matrices, immediate, soul.skin, alpha, armP, legP, headP, 0f);
            matrices.pop();
        }
    }
}
