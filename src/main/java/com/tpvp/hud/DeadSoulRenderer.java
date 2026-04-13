package com.tpvp.hud;

import com.tpvp.config.ModConfig;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

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
            if (age > 4000) { iter.remove(); continue; } // 4 Second Cinematic

            float life = age / 4000.0f; // 0 to 1
            float voidRadius = (float) Math.sin(life * Math.PI) * 2.0f; // Portal opens and closes
            float sinkY = life < 0.3f ? 0f : (life - 0.3f) * -3.0f; // Player gets pulled down
            
            matrices.push();
            matrices.translate(soul.pos.x - camPos.x, soul.pos.y - camPos.y, soul.pos.z - camPos.z);
            
            // 1. THE ABYSSAL VOID (Blackhole on ground)
            Matrix4f mat = matrices.peek().getPositionMatrix();
            VertexConsumer voidBuf = immediate.getBuffer(RenderLayer.getGui()); // Solid flat layer
            for (int i = 0; i < 36; i++) {
                float r1 = (float)Math.toRadians(i*10), r2 = (float)Math.toRadians((i+1)*10);
                float px1 = (float)Math.cos(r1)*voidRadius, pz1 = (float)Math.sin(r1)*voidRadius;
                float px2 = (float)Math.cos(r2)*voidRadius, pz2 = (float)Math.sin(r2)*voidRadius;
                // Draw black circle on ground
                RenderUtils3D.drawQuad(mat, voidBuf, 0, 0.01f, 0, px1, 0.01f, pz1, px2, 0.01f, pz2, 0, 0.01f, 0, 0f, 0f, 0f, 1f, 15728880);
            }

            // 2. SHADOW TENDRILS (Wrapping the player)
            if (life > 0.1f && life < 0.8f) {
                VertexConsumer lineBuf = immediate.getBuffer(RenderLayer.getLines());
                for(int t = 0; t < 5; t++) {
                    float tRot = (now % 1000) / 1000.0f * (float)Math.PI * 2;
                    float tx = (float)Math.cos(tRot + t) * 0.5f;
                    float tz = (float)Math.sin(tRot + t) * 0.5f;
                    float ty = (float)Math.sin(life * Math.PI) * 2.0f;
                    // Purple/Black tendrils grabbing the body
                    RenderUtils3D.drawLine(mat, lineBuf, tx, 0, tz, 0, ty, 0, 0.4f, 0f, 0.8f, 1f); 
                }
            }

            // 3. THE SINKING BODY (PK Wiggle + Pulled into Void)
            if (life < 0.8f) {
                matrices.translate(0, sinkY + 1.5, 0); // Moving down into the void
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw())); 
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)Math.sin(life * 30) * 20f)); // Wiggle struggle
                matrices.scale(0.8F, 0.8F, 0.8F); 
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180f)); 
                
                float armP = 160f + (float) Math.sin(life * 50) * 20f; // Arms up reaching for help
                float legP = (float) Math.cos(life * 60) * 30f; 
                
                RenderUtils3D.drawDoll(matrices, immediate, soul.skin, 1.0f, armP, legP, -30f, 0f);
            }
            matrices.pop();
        }
    }
}
