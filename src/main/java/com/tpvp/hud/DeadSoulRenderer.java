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
            // Agar entity just abhi mari hai
            if (lastHealth > 0 && health <= 0) {
                
                LivingEntity attacker = target.getAttacker();
                String vName = target.getName().getString();
                boolean isFriend = vName.equals(ModConfig.taggedFriendName);

                // 1. BANNER LOGIC: Sirf tabhi aayega jab ATTACKER TUM HO!
                if (attacker == clientPlayer) {
                    Identifier kSkin = ((AbstractClientPlayerEntity)clientPlayer).getSkinTextures().texture();
                    Identifier vSkin = (target instanceof AbstractClientPlayerEntity pt) ? pt.getSkinTextures().texture() : Identifier.ofVanilla("textures/entity/steve.png");
                    KillBannerHud.addKill(clientPlayer.getName().getString(), kSkin, vName, vSkin, isFriend); 
                }
                
                // 2. SOUL SPAWN LOGIC (Sabke marne pe niklegi)
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
            if (age > 4000) { iter.remove(); continue; } // 4 seconds duration

            float life = age / 4000.0f; // 0.0 to 1.0
            float upY = life * 5.0f; // Hawa me 5 block upar jayega
            
            // WAVY MATH (Left/Right lehrayega)
            double waveX = Math.sin(life * 15) * 0.6;
            
            float alpha = life > 0.7f ? (1.0f - life) / 0.3f : 1.0f; // Aakhiri me fade hoga
            int aColor = ((int)(alpha * 255) << 24);
            int skullColor = aColor | 0x55FFFF; // Cyan Glowing Skull

            matrices.push();
            matrices.translate(soul.pos.x - camPos.x + waveX, soul.pos.y - camPos.y + upY + 1.0, soul.pos.z - camPos.z);
            
            // Always face camera
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw())); 
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch())); 
            matrices.scale(-0.06F, -0.06F, 0.06F); // Bada Size
            
            Matrix4f mat = matrices.peek().getPositionMatrix();
            int l = LightmapTextureManager.MAX_LIGHT_COORDINATE;

            // DRAW GLOWING SKULL TEXT (☠)
            String text = "☠";
            float stX = -client.textRenderer.getWidth(text) / 2f;
            client.textRenderer.draw(text, stX, 0, skullColor, true, mat, immediate, TextRenderer.TextLayerType.SEE_THROUGH, 0, l);

            matrices.pop();
        }
    }
}
