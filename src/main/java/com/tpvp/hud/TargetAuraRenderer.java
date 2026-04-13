package com.tpvp.hud;

import com.tpvp.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Map;

public class TargetAuraRenderer {
    
    // Trackers for animations
    public static final Map<Integer, Long> totemPopMap = new HashMap<>();
    private static final Map<Integer, Vec3d> dollPosMap = new HashMap<>(); // For Smooth Doll Movement

    public static void render(LivingEntity target, MatrixStack matrices, VertexConsumerProvider.Immediate immediate, Camera camera, double x, double y, double z, String activeTarget) {
        MinecraftClient client = MinecraftClient.getInstance();
        String tName = target.getName().getString();

        // ---------------------------------------------------------
        // 1. DRAGON AURA (ENEMIES) - WITH ROAR, BELLY & TOTEM BLAST
        // ---------------------------------------------------------
        if (tName.equals(activeTarget) && ModConfig.dragonAuraEnabled) {
            long popTime = totemPopMap.getOrDefault(target.getId(), 0L);
            long now = System.currentTimeMillis();
            boolean isTotemPop = (now - popTime) < 3000;

            matrices.push(); 
            matrices.translate(x, y, z);
            Matrix4f mat = matrices.peek().getPositionMatrix();
            VertexConsumer quadBuffer = immediate.getBuffer(RenderLayer.getGui()); 
            
            float h = target.getHeight();
            float radius = target.getWidth() + 0.4f;
            float headX = 0, headY = 0, headZ = 0;
            
            // --- Dragon Color Themes ---
            float dr = 1f, dg = 0.1f, db = 0.1f; // 0=Ruby Red
            if (ModConfig.dragonColor == 1) { dr = 0.6f; dg = 0f; db = 1f; } // 1=Void Purple
            else if (ModConfig.dragonColor == 2) { dr = 0f; dg = 0.8f; db = 1f; } // 2=Frost Blue

            long cycle = 6000; 
            float t = (System.currentTimeMillis() % cycle) / (float) cycle;
            
            float progress = 0f, shatter = 0f, alpha = 0.8f, yOff = 0f; 
            float lookYaw = target.getYaw(); 
            
            // --- CINEMATIC TIMING ---
            if (isTotemPop) {
                float popT = (now - popTime) / 3000.0f;
                if (popT < 0.2f) { // Phase 1: Shoot Up
                    progress = 1.0f; 
                    yOff = (popT / 0.2f) * 6.0f; 
                } else if (popT < 0.5f) { // Phase 2: Dive Down
                    progress = 1.0f; 
                    yOff = 6.0f - (((popT - 0.2f) / 0.3f) * 6.0f); 
                } else { // Phase 3: Shatter into Dust
                    progress = 1.0f; 
                    shatter = (popT - 0.5f) / 0.5f; 
                    alpha = 0.8f * (1.0f - shatter); 
                } 
            } else {
                if (t < 0.3f) { // Rise Up
                    progress = t / 0.3f; 
                } else if (t < 0.6f) { // Pause & Roar
                    progress = 1.0f;
                    // Position head exactly where player is looking
                    headX = (float) Math.cos(Math.toRadians(lookYaw + 90)) * radius;
                    headZ = (float) Math.sin(Math.toRadians(lookYaw + 90)) * radius;
                    headY = h + 0.2f;
                } else if (t < 0.8f) { // Upward Shatter Dust
                    progress = 1.0f; 
                    shatter = (t - 0.6f) / 0.2f; 
                    alpha = 0.8f * (1.0f - shatter); 
                } else { 
                    alpha = 0f; // Cooldown
                } 
            }

            // --- DRAW DRAGON BODY (TRIPLE HELIX) ---
            if (alpha > 0.05f) {
                float rotSpeed = (now % 2000) / 2000.0f;
                int segments = 30;
                int activeSegs = (int)(segments * progress);
                boolean drawHead = (t >= 0.3f && t < 0.6f && !isTotemPop); 

                for (int layer = 0; layer < 3; layer++) {
                    float offset = layer * 0.05f;
                    
                    for (int i = 0; i < activeSegs; i++) {
                        float pt1 = (i / (float) segments);
                        float pt2 = ((i + 1) / (float) segments);
                        
                        float py1 = pt1 * h + yOff;
                        float px1 = (float) Math.cos((pt1 + rotSpeed + offset) * Math.PI * 4) * radius;
                        float pz1 = (float) Math.sin((pt1 + rotSpeed + offset) * Math.PI * 4) * radius;

                        float py2 = pt2 * h + yOff;
                        float px2 = (float) Math.cos((pt2 + rotSpeed + offset) * Math.PI * 4) * radius;
                        float pz2 = (float) Math.sin((pt2 + rotSpeed + offset) * Math.PI * 4) * radius;

                        // DUST SCATTER EFFECT (Flying upwards & outwards)
                        if (shatter > 0) {
                            float sx = (float) Math.sin(i * 13) * shatter * 3.0f;
                            float sy = shatter * 5.0f; // Dust flies UP
                            float sz = (float) Math.sin(i * 23) * shatter * 3.0f;
                            px1 += sx; py1 += sy; pz1 += sz;
                            px2 += sx; py2 += sy; pz2 += sz;
                        }

                        // Save Top Segment coordinates for Dragon Head rendering
                        if (!drawHead && layer == 1 && i == activeSegs - 1) { 
                            headX = px2; headY = py2; headZ = pz2; drawHead = true; 
                        }

                        // Colors & Belly Logic
                        float cr = dr, cg = dg, cb = db;
                        if (layer == 0) { cr *= 0.5f; cg *= 0.5f; cb *= 0.5f; } // Darker inner scales (Belly)
                        if (i > segments - 5) { cg = 0.8f; cb = 0f; } // Golden Tip
                        if (isTotemPop) { cg = 0.8f; cb = 0.2f; } // Golden Blast on Pop

                        RenderUtils3D.drawQuad(mat, quadBuffer, px1, py1, pz1, px1, py1+0.15f, pz1, px2, py2+0.15f, pz2, px2, py2, pz2, cr, cg, cb, alpha, 15728880);
                    }
                }
                
                // Draw 3D Dragon Head Block
                if (drawHead && shatter == 0) {
                    RenderUtils3D.draw3DBox(mat, quadBuffer, headX, headY+0.1f, headZ, 0.2f, 1f, isTotemPop ? 0.8f : 0f, 0f, alpha, 15728880);
                }
            }
            matrices.pop();

            // Target Down Arrow (Red)
            matrices.push(); 
            double bounce = Math.sin(now / 150.0) * 0.2; 
            matrices.translate(x, y + target.getHeight() + 1.8 + bounce, z); 
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw())); 
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch())); 
            matrices.scale(-0.1F, -0.1F, 0.1F); 
            client.textRenderer.draw("▼", -client.textRenderer.getWidth("▼") / 2f, 0, 0xFFFF2222, true, matrices.peek().getPositionMatrix(), immediate, TextRenderer.TextLayerType.SEE_THROUGH, 0x00000000, 15728880);
            matrices.pop();
        }

        // ---------------------------------------------------------
        // 2. FRIEND PET DOLL (Cute Companion System)
        // ---------------------------------------------------------
        if (tName.equals(ModConfig.taggedFriendName) && ModConfig.dragonAuraEnabled) {
            
            // Detect if Friend is standing near the player
            boolean isStill = target.getVelocity().lengthSquared() < 0.01 && target.distanceTo(client.player) < 5.0;
            long now = System.currentTimeMillis();
            
            // Get exact 3D coordinates in the world
            Vec3d targetBasePos = target.getPos();
            Vec3d playerPos = client.player.getPos();
            
            // The position the doll wants to go to
            Vec3d wantedDollPos;
            float dollRot = 0f;
            
            float hover = (float) Math.sin(now / 300.0) * 0.2f; 
            
            if (isStill) {
                // Stand in front of player offering handshake (Hug Pose)
                wantedDollPos = playerPos.add(Math.cos(Math.toRadians(target.getYaw() + 90)) * 1.5, target.getHeight() / 2 + hover, Math.sin(Math.toRadians(target.getYaw() + 90)) * 1.5);
                dollRot = -camera.getYaw(); // Look at the camera
            } else {
                // Fly behind the friend running (Chura ke bhagna)
                wantedDollPos = targetBasePos.add(Math.cos(Math.toRadians(target.getYaw() - 90)) * 1.5, target.getHeight() + hover + 0.5f, Math.sin(Math.toRadians(target.getYaw() - 90)) * 1.5);
                dollRot = target.getYaw(); // Look in the direction they are running
            }

            // --- SMOOTH LERP MOVEMENT (Floating towards target) ---
            Vec3d currentDollPos = dollPosMap.getOrDefault(target.getId(), targetBasePos); // Start at friend if new
            currentDollPos = currentDollPos.lerp(wantedDollPos, 0.08); // 8% move speed per frame (Smooth glide)
            dollPosMap.put(target.getId(), currentDollPos); // Save for next frame

            matrices.push();
            
            // Transform directly to the doll's interpolated position relative to camera
            matrices.translate(currentDollPos.x - camera.getPos().x, currentDollPos.y - camera.getPos().y, currentDollPos.z - camera.getPos().z);
            
            // Set rotations
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(dollRot));
            matrices.scale(0.3F, 0.3F, 0.3F); // Miniature Size
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180f)); // Upright fix
            
            Identifier fSkin = (target instanceof AbstractClientPlayerEntity pt) ? pt.getSkinTextures().texture() : Identifier.ofVanilla("textures/entity/steve.png");
            
            // --- PANIC / SCREAMING ANIMATION IF RUNNING ---
            float armP = isStill ? -90f : 160f + (float) Math.sin(now / 50.0) * 20f; // Hug vs Hands flailing up screaming!
            float legP = isStill ? 0f : (float) Math.sin(now / 50.0) * 60f; // Fast kicking
            float headP = isStill ? -10f : -30f; // Looking up in panic if running

            // Draw Full 3D Cute Doll
            RenderUtils3D.drawDoll(matrices, immediate, fSkin, 1.0f, armP, legP, headP, 0f);
            
            matrices.pop();

            // Friend Name Tag (Green Arrow over Friend's Head)
            matrices.push(); 
            double bounce = Math.sin(now / 150.0) * 0.2; 
            matrices.translate(x, y + target.getHeight() + 1.8 + bounce, z); 
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw())); 
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch())); 
            matrices.scale(-0.1F, -0.1F, 0.1F); 
            client.textRenderer.draw("▼", -client.textRenderer.getWidth("▼") / 2f, 0, 0xFF00FF00, true, matrices.peek().getPositionMatrix(), immediate, TextRenderer.TextLayerType.SEE_THROUGH, 0x00000000, 15728880);
            matrices.pop();
        }
    }
            }
