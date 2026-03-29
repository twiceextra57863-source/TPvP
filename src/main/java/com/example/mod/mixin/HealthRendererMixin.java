package com.example.mod.mixin;

import com.example.mod.ModSettings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class HealthRendererMixin<S extends EntityRenderState> {

    @Inject(method = "render", at = @At("TAIL"))
    public void onRender(S state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        // Sirf players ke liye render karein (Client player ko chhor kar)
        if (!(state instanceof PlayerEntityRenderState playerState) || playerState.name == null) return;
        
        // Agar player khud ko F5 mein dekh raha hai toh indicator na dikhe (optional)
        // if (playerState.name.getString().equals(MinecraftClient.getInstance().player.getName().getString())) return;

        matrices.push();
        // Player ke sir ke upar position (1.21 mein height state se milti hai)
        matrices.translate(0, 2.5f, 0); 
        matrices.multiply(MinecraftClient.getInstance().getEntityRenderDispatcher().getRotation());
        matrices.scale(-0.025f, -0.025f, 0.025f);

        TextRenderer tr = MinecraftClient.getInstance().textRenderer;
        float hp = playerState.health;
        float max = playerState.maxHealth;

        if (ModSettings.currentStyle == 0) { // STYLE 1: Hearts
            String text = "§c❤ §f" + String.format("%.1f", hp);
            tr.draw(text, -tr.getWidth(text)/2f, 0, -1, false, matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, 0, light);
        } 
        else if (ModSettings.currentStyle == 1) { // STYLE 2: Hits
            int hits = (int) Math.ceil(hp / 2.0f);
            String text = "§e" + hits + " Hits Left";
            tr.draw(text, -tr.getWidth(text)/2f, 0, -1, false, matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, 0, light);
        } 
        else if (ModSettings.currentStyle == 2) { // STYLE 3: Color Bar
            String color = hp > 10 ? "§a" : (hp > 5 ? "§e" : "§c");
            int barWidth = (int)((hp / (max > 0 ? max : 20)) * 10);
            String bar = color + "▮".repeat(Math.max(0, barWidth)) + "§8" + "▮".repeat(Math.max(0, 10 - barWidth));
            tr.draw(bar, -tr.getWidth(bar)/2f, 0, -1, false, matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, 0, light);
        }

        matrices.pop();
    }
}
