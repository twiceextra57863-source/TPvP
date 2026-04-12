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
                
                // --- ACCURATE KILLER DETECTION ---
                LivingEntity attacker = target.getAttacker();
                boolean isFriend = target.getName().getString().equals(ModConfig.taggedFriendName);
                
                if (attacker == clientPlayer || isFriend) {
                    String killerName = attacker != null ? attacker.getName().getString() : "Environment";
                    Identifier kSkin = (attacker instanceof AbstractClientPlayerEntity pk) ? pk.getSkinTextures().texture() : Identifier.ofVanilla("textures/entity/steve.png");
                    Identifier vSkin = (target instanceof AbstractClientPlayerEntity pt) ? pt.getSkinTextures().texture() : Identifier.ofVanilla("textures/entity/steve.png");

                    KillBannerHud.addKill(killerName, kSkin, target.getName().getString(), vSkin, isFriend); 
                }
                
                if (target instanceof AbstractClientPlayerEntity pt) {
                    activeSouls.add(new DeadSoul(target.getPos(), pt.getSkinTextures().texture())); 
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
            if (age > 6000) { iter.remove(); continue; } // 6 seconds long

            float life = age / 6000.0f; 
            double upY = life * 8.0; // Fly up high
            
            // Fades only at the very end
            float alpha = life > 0.8f ? (1.0f - life) / 0.2f : 1.0f; 
            
            matrices.push();
            matrices.translate(soul.pos.x - camPos.x, soul.pos.y - camPos.y + upY, soul.pos.z - camPos.z);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw())); 
            
            // "PK" Jhatka Wiggle
            float wiggleX = (float) Math.sin(life * 80) * 15f; 
            float wiggleZ = (float) Math.cos(life * 90) * 15f;
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(wiggleX)); 
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(wiggleZ)); 
            
            matrices.scale(0.8F, 0.8F, 0.8F); 
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180f)); // Upright fix
            
            // PK Dance Limbs
            float armP = (float) Math.sin(life * 50) * 120f + 45f; // Arms flailing up
            float legP = (float) Math.cos(life * 40) * 90f; // Legs kicking
            float headP = (float) Math.sin(life * 35) * 40f; 
            float headY = (float) Math.cos(life * 25) * 30f;

            RenderUtils3D.drawDoll(matrices, immediate, soul.skin, alpha, armP, legP, headP, headY);
            matrices.pop();
        }
    }
}
