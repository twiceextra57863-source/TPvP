package com.tpvp.hud;

import com.tpvp.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
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
    public static boolean clientWasDead = false;

    private static class DeadSoul {
        Vec3d pos; long startTime;
        DeadSoul(Vec3d pos) { this.pos = pos; this.startTime = System.currentTimeMillis(); }
    }

    public static void checkKills(LivingEntity target, PlayerEntity clientPlayer) {
        int id = target.getId();
        float health = target.getHealth();

        // 1. --- CHECK IF CLIENT DIED ---
        boolean isClientDead = clientPlayer.getHealth() <= 0;
        if (isClientDead && !clientWasDead) {
            String mode = PvPStatsManager.detectCurrentMode(clientPlayer);
            
            // IF CLIENT DIES, ONLY ADD DEATH IF THE ATTACKER WAS A REAL PLAYER
            LivingEntity clientAttacker = clientPlayer.getAttacker();
            if (clientAttacker instanceof PlayerEntity) {
                String enemyName = clientAttacker.getName().getString();
                PvPStatsManager.addDeath(enemyName, clientPlayer.getName().getString(), mode); 
            } else {
                // Died to environment or mob (Still adds death but lists as Environment)
                PvPStatsManager.addDeath("Environment", clientPlayer.getName().getString(), mode); 
            }
        }
        clientWasDead = isClientDead;

        // 2. --- CHECK IF ANY OTHER ENTITY DIED ---
        if (lastHealthMap.containsKey(id)) {
            float lastHealth = lastHealthMap.get(id);
            
            if (lastHealth > 0 && health <= 0) { 
                
                LivingEntity attacker = target.getAttacker();
                String vName = target.getName().getString();
                boolean isFriend = vName.equals(ModConfig.taggedFriendName);

                String killerName = "Environment";
                Identifier kSkin = Identifier.ofVanilla("textures/entity/steve.png");
                
                if (attacker instanceof AbstractClientPlayerEntity pk) {
                    killerName = pk.getName().getString();
                    kSkin = pk.getSkinTextures() != null ? pk.getSkinTextures().texture() : kSkin;
                } else if (target.distanceTo(clientPlayer) < 5.0) { 
                    // Fallback to client player if killed nearby (assuming sweep/fire)
                    killerName = clientPlayer.getName().getString();
                    kSkin = ((AbstractClientPlayerEntity)clientPlayer).getSkinTextures().texture();
                }

                Identifier vSkin = (target instanceof AbstractClientPlayerEntity pt) ? 
                        (pt.getSkinTextures() != null ? pt.getSkinTextures().texture() : kSkin) : kSkin;

                // --- STRICT PVP FILTER: ONLY RECORD IF VICTIM IS A REAL PLAYER ---
                if (target instanceof PlayerEntity) {
                    
                    // If Client Player got the kill, add to Tier Progress!
                    if (attacker == clientPlayer || killerName.equals(clientPlayer.getName().getString())) {
                        String mode = PvPStatsManager.detectCurrentMode(clientPlayer);
                        PvPStatsManager.addKill(killerName, vName, mode);
                    }

                    // Trigger Kill Banner ONLY for Real Players
                    KillBannerHud.addKill(killerName, kSkin, vName, vSkin, isFriend); 
                }
                
                // Spawn K.O Hologram Marker (Only for Real Players)
                if (target instanceof AbstractClientPlayerEntity) {
                    activeSouls.add(new DeadSoul(target.getPos())); 
                }
            }
        }
        lastHealthMap.put(id, health);
    }

    public static void renderSouls(MatrixStack matrices, VertexConsumerProvider.Immediate immediate, Camera camera, Vec3d camPos) {
        if (!ModConfig.soulAnimationEnabled) return;
        MinecraftClient client = MinecraftClient.getInstance();
        long now = System.currentTimeMillis();
        Iterator<DeadSoul> iter = activeSouls.iterator();
        
        while (iter.hasNext()) {
            DeadSoul soul = iter.next();
            long age = now - soul.startTime;
            if (age > 4000) { iter.remove(); continue; } 

            float life = age / 4000.0f; // 0 to 1
            float upY = life * 5.0f; 
            float alpha = life > 0.7f ? (1.0f - life) / 0.3f : 1.0f; 
            
            // EPIC ABYSSAL VOID SHOCKWAVE RING
            if (life < 0.3f) {
                matrices.push();
                matrices.translate(soul.pos.x - camPos.x, soul.pos.y - camPos.y + 0.1, soul.pos.z - camPos.z);
                float waveRadius = (life / 0.3f) * 4.0f; 
                float waveAlpha = 1.0f - (life / 0.3f);
                Matrix4f mat = matrices.peek().getPositionMatrix();
                VertexConsumer lineBuf = immediate.getBuffer(RenderLayer.getLines());
                
                for (int i = 0; i < 360; i += 10) {
                    float px1 = (float)Math.cos(Math.toRadians(i)) * waveRadius, pz1 = (float)Math.sin(Math.toRadians(i)) * waveRadius;
                    float px2 = (float)Math.cos(Math.toRadians(i+10)) * waveRadius, pz2 = (float)Math.sin(Math.toRadians(i+10)) * waveRadius;
                    RenderUtils3D.drawLine(mat, lineBuf, px1, 0, pz1, px2, 0, pz2, 1f, 0.8f, 0f, waveAlpha);
                }
                matrices.pop();
            }

            // BOUNCING K.O. TEXT ANIMATION
            float bounceY = 2.0f; 
            if (life < 0.2f) bounceY += (life / 0.2f) * 2.0f; 
            else bounceY += 2.0f + (float) Math.sin((life - 0.2f) * 15) * 0.3f;

            matrices.push();
            matrices.translate(soul.pos.x - camPos.x, soul.pos.y - camPos.y + bounceY, soul.pos.z - camPos.z);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw())); 
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch())); 
            
            if (life < 0.2f) matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)Math.sin(life*100) * 15f)); 
            matrices.scale(-0.06F, -0.06F, 0.06F); 
            
            Matrix4f mat = matrices.peek().getPositionMatrix();
            int l = LightmapTextureManager.MAX_LIGHT_COORDINATE;
            int aColor = ((int)(alpha * 255) << 24);
            int koColor = aColor | 0xFFD700; 
            int outlineColor = aColor | 0xFF0000; 

            String text = "K.O.";
            float stX = -client.textRenderer.getWidth(text) / 2f;
            
            client.textRenderer.draw(text, stX + 2, 2, outlineColor, true, mat, immediate, TextRenderer.TextLayerType.SEE_THROUGH, 0, l);
            client.textRenderer.draw(text, stX, 0, koColor, true, mat, immediate, TextRenderer.TextLayerType.SEE_THROUGH, 0, l);
            matrices.pop();
        }
    }
}
