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

    private static class DeadSoul {
        Vec3d pos; long startTime;
        DeadSoul(Vec3d pos) { this.pos = pos; this.startTime = System.currentTimeMillis(); }
    }

    public static void checkKills(LivingEntity target, PlayerEntity clientPlayer) {
        int id = target.getId();
        float health = target.getHealth();

        if (lastHealthMap.containsKey(id)) {
            float lastHealth = lastHealthMap.get(id);
            if (lastHealth > 0 && health <= 0) { // IF ANYONE DIES!
                
                LivingEntity attacker = target.getAttacker();
                String vName = target.getName().getString();
                boolean isFriend = vName.equals(ModConfig.taggedFriendName);

                // --- 100% SERVER-WIDE KILL BANNER ---
                String killerName = "Environment";
                Identifier kSkin = Identifier.ofVanilla("textures/entity/steve.png");
                
                if (attacker instanceof AbstractClientPlayerEntity pk) {
                    killerName = pk.getName().getString();
                    kSkin = pk.getSkinTextures() != null ? pk.getSkinTextures().texture() : kSkin;
                } else if (target.distanceTo(clientPlayer) < 5.0) { 
                    killerName = clientPlayer.getName().getString();
                    kSkin = ((AbstractClientPlayerEntity)clientPlayer).getSkinTextures().texture();
                }

                Identifier vSkin = (target instanceof AbstractClientPlayerEntity pt) ? 
                        (pt.getSkinTextures() != null ? pt.getSkinTextures().texture() : kSkin) : kSkin;

                KillBannerHud.addKill(killerName, kSkin, vName, vSkin, isFriend); 
                
                // Spawn K.O Hologram Marker
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
            if (age > 4000) { iter.remove(); continue; } // 4s animation

            float life = age / 4000.0f; // 0 to 1
            float alpha = life > 0.7f ? (1.0f - life) / 0.3f : 1.0f; 
            
            // EPIC SHOCKWAVE RING ON THE GROUND
            if (life < 0.3f) {
                matrices.push();
                matrices.translate(soul.pos.x - camPos.x, soul.pos.y - camPos.y + 0.1, soul.pos.z - camPos.z);
                
                float waveRadius = (life / 0.3f) * 4.0f; // Expands from 0 to 4 blocks wide
                float waveAlpha = 1.0f - (life / 0.3f);
                
                Matrix4f mat = matrices.peek().getPositionMatrix();
                VertexConsumer lineBuf = immediate.getBuffer(RenderLayer.getLines());
                
                for (int i = 0; i < 360; i += 10) {
                    float px1 = (float)Math.cos(Math.toRadians(i)) * waveRadius;
                    float pz1 = (float)Math.sin(Math.toRadians(i)) * waveRadius;
                    float px2 = (float)Math.cos(Math.toRadians(i+10)) * waveRadius;
                    float pz2 = (float)Math.sin(Math.toRadians(i+10)) * waveRadius;
                    
                    // Golden Shockwave Line
                    RenderUtils3D.drawLine(mat, lineBuf, px1, 0, pz1, px2, 0, pz2, 1f, 0.8f, 0f, waveAlpha);
                }
                matrices.pop();
            }

            // K.O. BOUNCING TEXT ANIMATION
            float upY = 2.0f; // Starts 2 blocks high
            if (life < 0.2f) { // Shoot up quickly
                upY += (life / 0.2f) * 2.0f; 
            } else { // Float and bounce
                upY += 2.0f + (float) Math.sin((life - 0.2f) * 15) * 0.3f;
            }

            matrices.push();
            matrices.translate(soul.pos.x - camPos.x, soul.pos.y - camPos.y + upY, soul.pos.z - camPos.z);
            
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw())); 
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch())); 
            
            // Jiggle effect if friend was killed
            if (life < 0.3f) matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)Math.sin(life*50) * 10f)); 
            
            matrices.scale(-0.08F, -0.08F, 0.08F); // Giant Text Size
            
            Matrix4f mat = matrices.peek().getPositionMatrix();
            int l = LightmapTextureManager.MAX_LIGHT_COORDINATE;
            int aColor = ((int)(alpha * 255) << 24);
            int koColor = aColor | 0xFFD700; // Golden K.O.
            int outlineColor = aColor | 0xFF0000; // Red Aura Shadow

            String text = "K.O.";
            float stX = -client.textRenderer.getWidth(text) / 2f;
            
            // 3D Aura Shadow (Draw slightly behind and offset)
            client.textRenderer.draw(text, stX + 2, 2, outlineColor, true, mat, immediate, TextRenderer.TextLayerType.SEE_THROUGH, 0, l);
            // Main Text
            client.textRenderer.draw(text, stX, 0, koColor, true, mat, immediate, TextRenderer.TextLayerType.SEE_THROUGH, 0, l);

            matrices.pop();
        }
    }
        }
