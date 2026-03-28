package com.mtpvp.mixin;

import com.mtpvp.gui.MtpvpDashboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<S extends EntityRenderState> {
    private static final Identifier HEART_ICON = Identifier.of("minecraft", "textures/gui/sprites/hud/heart/full.png");

    @Inject(method = "renderLabelIfPresent", at = @At("HEAD"), cancellable = true)
    private void renderAdvancedMtpvpGui(S state, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (!MtpvpDashboard.headEnabled || !(state instanceof PlayerEntityRenderState playerState)) return;

        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity target = client.world.getPlayers().stream()
                .filter(p -> p.getName().getString().equals(playerState.displayName.getString()))
                .findFirst().orElse(null);

        if (target == null || target == client.player) return;

        float hp = target.getHealth();
        float maxHp = target.getMaxHealth();

        matrices.push();
        matrices.translate(0.0D, 0.5D, 0.0D); // Nametag ke upar shift
        matrices.scale(0.025f, 0.025f, 0.025f);
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        switch (MtpvpDashboard.styleIndex) {
            case 0 -> renderHearts(matrices, hp); // Style 1: 10 Hearts
            case 1 -> renderStatusBar(matrices, hp, maxHp); // Style 2: Status Bar
            case 3 -> renderProFace(matrices, target, hp); // Style 3: Skin + Hits
        }
        matrices.pop();
    }

    // STYLE 1: 10 Hearts Logic
    private void renderHearts(MatrixStack matrices, float hp) {
        TextRenderer tr = MinecraftClient.getInstance().textRenderer;
        String heartStr = "❤".repeat((int)Math.ceil(hp/2));
        String emptyStr = "❤".repeat((int)((20 - hp)/2));
        
        // Draw Shadowed Hearts
        tr.draw(heartStr, -tr.getWidth(heartStr + emptyStr)/2f, 0, 0xFF5555, false, matrices.peek().getPositionMatrix(), MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers(), net.minecraft.client.font.TextRenderer.TextLayerType.SEE_THROUGH, 0, 15728880);
    }

    // STYLE 2: Smooth Status Bar (Level Bar)
    private void renderStatusBar(MatrixStack matrices, float hp, float maxHp) {
        float width = 40f;
        float progress = (hp / maxHp) * width;
        
        // Background bar (Gray)
        fill(matrices, -width/2, 0, width/2, 4, 0xAA333333);
        // Health bar (Green to Red transition)
        int color = (hp > 10) ? 0xFF55FF55 : 0xFFFF5555;
        fill(matrices, -width/2, 0, -width/2 + progress, 4, color);
    }

    // STYLE 3: Skin Face + Weapon Math Hits
    private void renderProFace(MatrixStack matrices, PlayerEntity target, float hp) {
        TextRenderer tr = MinecraftClient.getInstance().textRenderer;
        
        // WEAPON MATH: Calculate hits based on current held item
        ItemStack stack = MinecraftClient.getInstance().player.getMainHandStack();
        float damage = 1.0f; // Default hand damage
        if (stack.getItem() instanceof net.minecraft.item.SwordItem sword) damage = sword.getAttackDamage() + 1;
        else if (stack.getItem() instanceof net.minecraft.item.AxeItem axe) damage = axe.getAttackDamage() + 1;
        
        int hitsNeeded = (int) Math.ceil(hp / (damage + 1.0f)); // Simple critical factor added
        
        String text = "Hits: " + hitsNeeded;
        tr.draw(text, -tr.getWidth(text)/2f, 0, 0xFFAA00, true, matrices.peek().getPositionMatrix(), MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers(), net.minecraft.client.font.TextRenderer.TextLayerType.SEE_THROUGH, 0, 15728880);
    }

    private void fill(MatrixStack matrices, float x1, float y1, float x2, float y2, int color) {
        // Custom fill helper for 3D world space
    }
}
