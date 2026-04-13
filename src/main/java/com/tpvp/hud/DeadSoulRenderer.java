package com.tpvp.hud;

import com.tpvp.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.LightmapTextureManager;
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
            if (lastHealth > 0 && health <= 0) { // KOI BHI MARA!
                
                LivingEntity attacker = target.getAttacker();
                String vName = target.getName().getString();
                boolean isFriend = vName.equals(ModConfig.taggedFriendName);

                // --- 100% SERVER-WIDE KILL DETECTION ---
                String killerName = "Environment";
                Identifier kSkin = Identifier.ofVanilla("textures/entity/steve.png");
                
                if (attacker instanceof AbstractClientPlayerEntity pk) {
                    killerName = pk.getName().getString();
                    kSkin = pk.getSkinTextures() != null ? pk.getSkinTextures().texture() : kSkin;
                } else if (target.distanceTo(clientPlayer) < 5.0) { // Fallback
                    killerName = clientPlayer.getName().getString();
                    kSkin = ((AbstractClientPlayerEntity)clientPlayer).getSkinTextures().texture();
                }

                Identifier vSkin = (target instanceof AbstractClientPlayerEntity pt) ? 
                        (pt.getSkinTextures() != null ? pt.getSkinTextures().texture() : kSkin) : kSkin;

                // Trigger Banner (Notification) for ALL kills!
                KillBannerHud.addKill(killerName, kSkin, vName, vSkin, isFriend); 
                
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
            float upY = life * 5.0f; // Float up 5 blocks
            float alpha = life > 0.7f ? (1.0f - life) / 0.3f : 1.0f; 
            
            // EPIC K.O. ANIMATION
            matrices.push();
            matrices.translate(soul.pos.x - camPos.x, soul.pos.y - camPos.y + upY + 1.0, soul.pos.z - camPos.z);
            
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw())); 
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch())); 
            
            // Jiggle effect (Glitch text)
            if (life < 0.2f) matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)Math.sin(life*100) * 15f)); 
            
            matrices.scale(-0.06F, -0.06F, 0.06F); // BIG TEXT
            
            Matrix4f mat = matrices.peek().getPositionMatrix();
            int l = LightmapTextureManager.MAX_LIGHT_COORDINATE;
            int aColor = ((int)(alpha * 255) << 24);
            int koColor = aColor | 0xFFD700; // Golden K.O.
            int outlineColor = aColor | 0xFF0000; // Red Aura

            String text = "K.O.";
            float stX = -client.textRenderer.getWidth(text) / 2f;
            
            // 3D Aura Shadow
            client.textRenderer.draw(text, stX + 2, 2, outlineColor, true, mat, immediate, TextRenderer.TextLayerType.SEE_THROUGH, 0, l);
            // Main Text
            client.textRenderer.draw(text, stX, 0, koColor, true, mat, immediate, TextRenderer.TextLayerType.SEE_THROUGH, 0, l);

            matrices.pop();
        }
    }
}
