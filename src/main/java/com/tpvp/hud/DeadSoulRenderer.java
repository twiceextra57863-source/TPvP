package com.tpvp.hud;

import com.tpvp.config.ModConfig;
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
        Vec3d pos; Identifier skin; long startTime;
        DeadSoul(Vec3d pos, Identifier skin) { this.pos = pos; this.skin = skin; this.startTime = System.currentTimeMillis(); }
    }

    public static void checkKills(LivingEntity target, PlayerEntity clientPlayer) {
        int id = target.getId();
        float health = target.getHealth();

        if (lastHealthMap.containsKey(id)) {
            float lastHealth = lastHealthMap.get(id);
            if (lastHealth > 0 && health <= 0) { // Target Died!
                
                LivingEntity attacker = target.getAttacker();
                boolean isFriend = target.getName().getString().equals(ModConfig.taggedFriendName);
                
                // ONLY show banner if WE killed them, or if a tagged Friend died.
                if (attacker == clientPlayer || isFriend) {
                    String killerName = attacker != null ? attacker.getName().getString() : "Environment";
                    Identifier kSkin = (attacker instanceof AbstractClientPlayerEntity pk) ? pk.getSkinTextures().texture() : Identifier.ofVanilla("textures/entity/steve.png");
                    Identifier vSkin = (target instanceof AbstractClientPlayerEntity pt) ? pt.getSkinTextures().texture() : Identifier.ofVanilla("textures/entity/steve.png");

                    KillBannerHud.addKill(killerName, kSkin, target.getName().getString(), vSkin, isFriend); 
                }
                
                // Spawn Monster Execution Entity
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
            if (age > 4000) { iter.remove(); continue; } // Animation lasts 4 seconds

            float life = age / 4000.0f; // 0.0 to 1.0 progression
            
            // --- JJK CURSE EXECUTION ANIMATION MATH ---
            // Phase 1 (0.0 to 0.2): Monster rises, jaw opens.
            // Phase 2 (0.2 to 0.4): Jaw snaps shut. Player model disappears.
            // Phase 3 (0.4 to 0.7): Chewing.
            // Phase 4 (0.7 to 1.0): Sinks back down.

            float monsterY = -3.0f; // Starts underground
            float jawAngle = 0f;
            boolean showPlayer = true;
            
            if (life < 0.2f) { // Rising
                monsterY = -3.0f + (life / 0.2f) * 4.0f;
                jawAngle = (life / 0.2f) * 60f; // Opens mouth to 60 degrees
            } else if (life < 0.4f) { // Snapping Shut
                monsterY = 1.0f;
                jawAngle = 60f - ((life - 0.2f) / 0.2f) * 60f; // Closes rapidly
                if (life > 0.35f) showPlayer = false; // Eaten!
            } else if (life < 0.7f) { // Chewing
                monsterY = 1.0f;
                showPlayer = false;
                jawAngle = (float) Math.abs(Math.sin((life - 0.4f) * 30)) * 10f; // Small chewing movement
            } else { // Sinking
                showPlayer = false;
                monsterY = 1.0f - ((life - 0.7f) / 0.3f) * 4.0f; // Sinks back down
            }

            matrices.push();
            matrices.translate(soul.pos.x - camPos.x, soul.pos.y - camPos.y, soul.pos.z - camPos.z);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw())); // Always faces you
            
            // --- RENDER DEAD PLAYER (Limp Ragdoll inside mouth) ---
            if (showPlayer) {
                matrices.push();
                matrices.translate(0, 1.0, 0); // Floats slightly up into the jaws
                matrices.scale(0.8F, 0.8F, 0.8F); 
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180f)); 
                
                // Ragdoll poses (Limp, arms hanging down, head tilted forward)
                RenderUtils3D.drawDoll(matrices, immediate, soul.skin, 1.0f, 0f, 0f, 45f, 0f);
                matrices.pop();
            }

            // --- RENDER JJK CURSE MONSTER (RIKA STYLE) ---
            VertexConsumer solidBuffer = immediate.getBuffer(RenderLayer.getGui()); // Solid colored block layer
            int l = LightmapTextureManager.MAX_LIGHT_COORDINATE;
            Matrix4f mat = matrices.peek().getPositionMatrix();

            // Monster Colors
            float mr = 0.1f, mg = 0.0f, mb = 0.2f; // Deep Dark Purple / Black
            float tr = 0.9f, tg = 0.9f, tb = 0.9f; // Dirty White Teeth
            float er = 1.0f, eg = 0.0f, eb = 0.0f; // Glowing Red Eyes

            matrices.push();
            matrices.translate(0, monsterY, 0);
            mat = matrices.peek().getPositionMatrix();

            // LOWER JAW (Base head)
            RenderUtils3D.drawSolidBox(mat, solidBuffer, -1.5f, 0f, -1.5f, 1.5f, 1.0f, 1.5f, mr, mg, mb, 1f, l);
            // Lower Teeth
            RenderUtils3D.drawSolidBox(mat, solidBuffer, -1.4f, 1.0f, -1.4f, -1.2f, 1.4f, 1.4f, tr, tg, tb, 1f, l); // Left Row
            RenderUtils3D.drawSolidBox(mat, solidBuffer, 1.2f, 1.0f, -1.4f, 1.4f, 1.4f, 1.4f, tr, tg, tb, 1f, l); // Right Row
            RenderUtils3D.drawSolidBox(mat, solidBuffer, -1.2f, 1.0f, -1.4f, 1.2f, 1.3f, -1.2f, tr, tg, tb, 1f, l); // Front Row

            // UPPER JAW (Hinged at the back)
            matrices.push();
            matrices.translate(0, 1.0f, 1.5f); // Move hinge to back of head
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-jawAngle)); // Rotate mouth open
            mat = matrices.peek().getPositionMatrix();
            
            // Top Head Block
            RenderUtils3D.drawSolidBox(mat, solidBuffer, -1.5f, 0f, -3.0f, 1.5f, 1.5f, 0f, mr, mg, mb, 1f, l);
            
            // Glowing Cursed Eyes
            RenderUtils3D.drawSolidBox(mat, solidBuffer, -1.2f, 0.5f, -3.1f, -0.5f, 0.8f, -2.9f, er, eg, eb, 1f, l); // Left Eye
            RenderUtils3D.drawSolidBox(mat, solidBuffer, 0.5f, 0.5f, -3.1f, 1.2f, 0.8f, -2.9f, er, eg, eb, 1f, l); // Right Eye
            
            // Upper Teeth
            RenderUtils3D.drawSolidBox(mat, solidBuffer, -1.4f, -0.4f, -3.0f, -1.2f, 0f, -0.2f, tr, tg, tb, 1f, l); // Left
            RenderUtils3D.drawSolidBox(mat, solidBuffer, 1.2f, -0.4f, -3.0f, 1.4f, 0f, -0.2f, tr, tg, tb, 1f, l); // Right
            RenderUtils3D.drawSolidBox(mat, solidBuffer, -1.2f, -0.3f, -3.0f, 1.2f, 0f, -2.8f, tr, tg, tb, 1f, l); // Front

            matrices.pop(); // End Upper Jaw
            matrices.pop(); // End Monster
            
            matrices.pop(); // End Transform
        }
    }
}
