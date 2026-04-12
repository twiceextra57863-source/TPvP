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

import java.util.*;

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
                if (target.distanceTo(clientPlayer) < 30.0) {
                    Identifier vSkin = (target instanceof AbstractClientPlayerEntity pt) ? pt.getSkinTextures().texture() : Identifier.ofVanilla("textures/entity/steve.png");
                    Identifier cSkin = ((AbstractClientPlayerEntity)clientPlayer).getSkinTextures().texture();
                    KillBannerHud.addKill(clientPlayer.getName().getString(), cSkin, target.getName().getString(), vSkin); 
                }
                if (target instanceof AbstractClientPlayerEntity pt) {
                    activeSouls.add(new DeadSoul(target.getPos(), pt.getSkinTextures() != null ? pt.getSkinTextures().texture() : Identifier.ofVanilla("textures/entity/steve.png"))); 
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
            if (age > 5000) { iter.remove(); continue; } 

            float life = age / 5000.0f; 
            double upY = life * 6.0; 
            
            matrices.push();
            matrices.translate(soul.pos.x - camPos.x, soul.pos.y - camPos.y + upY, soul.pos.z - camPos.z);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(life * 360f * 4f)); 
            matrices.scale(0.8F, 0.8F, 0.8F); 
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180f)); 
            
            VertexConsumer buffer = immediate.getBuffer(RenderLayer.getEntityTranslucent(soul.skin));
            int l = LightmapTextureManager.MAX_LIGHT_COORDINATE;
            
            float armPitch = (float) Math.sin(life * Math.PI) * 160f; 
            float legPitch = (float) Math.sin(life * Math.PI) * 90f; 
            float headPitch = (float) Math.sin(life * Math.PI) * -45f; 

            // Head
            matrices.push(); matrices.translate(0, -1.5f, 0); matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(headPitch));
            drawSkinBox(matrices.peek().getPositionMatrix(), buffer, -0.25f, -0.25f, -0.25f, 0.5f, 0.5f, 0.5f, 8, 8, 8, 8, l);
            matrices.pop();

            // Body
            matrices.push(); matrices.translate(0, -0.75f, 0);
            drawSkinBox(matrices.peek().getPositionMatrix(), buffer, -0.25f, -0.375f, -0.125f, 0.5f, 0.75f, 0.25f, 20, 16, 8, 12, l);
            matrices.pop();

            // Right Arm
            matrices.push(); matrices.translate(-0.375f, -1.0f, 0); matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-armPitch));
            drawSkinBox(matrices.peek().getPositionMatrix(), buffer, -0.125f, 0, -0.125f, 0.25f, 0.75f, 0.25f, 44, 16, 4, 12, l);
            matrices.pop();

            // Left Arm
            matrices.push(); matrices.translate(0.375f, -1.0f, 0); matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-armPitch));
            drawSkinBox(matrices.peek().getPositionMatrix(), buffer, -0.125f, 0, -0.125f, 0.25f, 0.75f, 0.25f, 36, 52, 4, 12, l);
            matrices.pop();

            // Right Leg
            matrices.push(); matrices.translate(-0.125f, -0.375f, 0); matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-legPitch));
            drawSkinBox(matrices.peek().getPositionMatrix(), buffer, -0.125f, 0, -0.125f, 0.25f, 0.75f, 0.25f, 0, 16, 4, 12, l);
            matrices.pop();

            // Left Leg
            matrices.push(); matrices.translate(0.125f, -0.375f, 0); matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-legPitch));
            drawSkinBox(matrices.peek().getPositionMatrix(), buffer, -0.125f, 0, -0.125f, 0.25f, 0.75f, 0.25f, 16, 52, 4, 12, l);
            matrices.pop();

            matrices.pop();
        }
    }

    private static void drawSkinBox(Matrix4f m, VertexConsumer v, float x, float y, float z, float w, float h, float d, float u, float vTex, float texW, float texH, int l) {
        float pU = 1f/64f; 
        RenderUtils3D.drawSoulQuad(m, v, x, y, z-d, x+w, y, z-d, x+w, y+h, z-d, x, y+h, z-d, (u+d)*pU, (vTex+d)*pU, (u+d+w)*pU, (vTex+d+h)*pU, 1f,1f,1f,1f, l);
        RenderUtils3D.drawSoulQuad(m, v, x+w, y, z, x, y, z, x, y+h, z, x+w, y+h, z, (u+d+w+d)*pU, (vTex+d)*pU, (u+d+w+d+w)*pU, (vTex+d+h)*pU, 1f,1f,1f,1f, l);
        RenderUtils3D.drawSoulQuad(m, v, x, y, z, x, y, z-d, x, y+h, z-d, x, y+h, z, u*pU, (vTex+d)*pU, (u+d)*pU, (vTex+d+h)*pU, 1f,1f,1f,1f, l);
        RenderUtils3D.drawSoulQuad(m, v, x+w, y, z-d, x+w, y, z, x+w, y+h, z, x+w, y+h, z-d, (u+d+w)*pU, (vTex+d)*pU, (u+d+w+d)*pU, (vTex+d+h)*pU, 1f,1f,1f,1f, l);
    }
}
