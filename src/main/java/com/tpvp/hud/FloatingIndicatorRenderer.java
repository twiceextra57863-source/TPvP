package com.tpvp.hud;

import com.tpvp.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

public class FloatingIndicatorRenderer {
    public static void render(LivingEntity target, MatrixStack matrices, VertexConsumerProvider.Immediate immediate, Camera camera, double x, double y, double z, double weaponDmg) {
        if (!ModConfig.indicatorEnabled) return;
        MinecraftClient client = MinecraftClient.getInstance();

        matrices.push(); 
        matrices.translate(x, y + target.getHeight() + 1.2, z); 
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw())); 
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch())); 
        matrices.scale(-0.025F, -0.025F, 0.025F);
        
        Matrix4f pM = matrices.peek().getPositionMatrix(); 
        int l = LightmapTextureManager.MAX_LIGHT_COORDINATE;
        float health = target.getHealth(), maxHealth = target.getMaxHealth();
        float hpPercent = Math.max(0, Math.min(1, health / maxHealth));

        if (ModConfig.indicatorStyle == 0) { 
            int tH = (int) Math.ceil(Math.max(maxHealth, health) / 2.0f);
            Sprite fH = client.getGuiAtlasManager().getSprite(Identifier.ofVanilla("hud/heart/full"));
            Sprite hH = client.getGuiAtlasManager().getSprite(Identifier.ofVanilla("hud/heart/half"));
            Sprite eH = client.getGuiAtlasManager().getSprite(Identifier.ofVanilla("hud/heart/container"));
            VertexConsumer hc = immediate.getBuffer(RenderLayer.getTextSeeThrough(fH.getAtlasId())); 
            float stX = -(tH * 9f) / 2f;
            for (int i = 0; i < tH; i++) {
                // FIX: Used proper 'float' notations (e.g. 0f, 9f) to match RenderUtils3D
                RenderUtils3D.drawTextureQuad(pM, hc, stX+(i*9f), 0f, 9f, 9f, eH.getMinU(), eH.getMinV(), eH.getMaxU(), eH.getMaxV(), 1f, 1f, 1f, 1f, l);
                if (health >= (i*2)+2) RenderUtils3D.drawTextureQuad(pM, hc, stX+(i*9f), 0f, 9f, 9f, fH.getMinU(), fH.getMinV(), fH.getMaxU(), fH.getMaxV(), 1f, 1f, 1f, 1f, l);
                else if (health > (i*2)) RenderUtils3D.drawTextureQuad(pM, hc, stX+(i*9f), 0f, 9f, 9f, hH.getMinU(), hH.getMinV(), hH.getMaxU(), hH.getMaxV(), 1f, 1f, 1f, 1f, l);
            }
        } else if (ModConfig.indicatorStyle == 1) { 
            float cW = 50f * hpPercent; 
            int bC = (hpPercent < 0.3f) ? 0xFFFF3333 : (hpPercent < 0.6f) ? 0xFFFFAA00 : 0xFF00FF00;
            VertexConsumer bc = immediate.getBuffer(RenderLayer.getTextBackgroundSeeThrough());
            
            // FIX: Used proper 'float' notations
            RenderUtils3D.drawColorQuad(pM, bc, -26f, 0f, 52f, 7f, 0xFF000000, l); 
            RenderUtils3D.drawColorQuad(pM, bc, -25f, 1f, 50f, 5f, 0xFF333333, l); 
            if (cW > 0) RenderUtils3D.drawColorQuad(pM, bc, -25f, 1f, cW, 5f, bC, l); 
            
            String pt = (int)(hpPercent * 100) + "%"; 
            client.textRenderer.draw(pt, -client.textRenderer.getWidth(pt)/2f, -9, 0xFFFFFF, false, pM, immediate, TextRenderer.TextLayerType.SEE_THROUGH, 0, l);
        } else if (ModConfig.indicatorStyle == 2) { 
            int hitsToKill = (int) Math.ceil(health / weaponDmg);
            String text = (target instanceof PlayerEntity) ? target.getName().getString() + " | Hits: " + hitsToKill : "Hits: " + hitsToKill;
            int txtColor = (hpPercent < 0.3f) ? 0xFF0000 : (hpPercent < 0.6f) ? 0xFFFF00 : 0x00FF00; 
            float stX = -client.textRenderer.getWidth(text) / 2f;
            if (target instanceof AbstractClientPlayerEntity pt) {
                RenderUtils3D.drawTextureQuad(pM, immediate.getBuffer(RenderLayer.getTextSeeThrough(pt.getSkinTextures().texture())), stX - 12f, -1f, 10f, 10f, 8f/64f, 8f/64f, 16f/64f, 16f/64f, 1f, 1f, 1f, 1f, l);
            }
            client.textRenderer.draw(text, stX, 0, txtColor, false, pM, immediate, TextRenderer.TextLayerType.SEE_THROUGH, 0x40000000, l);
        }
        matrices.pop();
    }
}
