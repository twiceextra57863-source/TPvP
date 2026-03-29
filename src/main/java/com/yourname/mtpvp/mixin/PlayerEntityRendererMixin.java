package com.mtpvp.mixin;

import net.minecraft.client.render.entity.PlayerEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin {
    // PlayerRenderer LivingEntityRenderer ko extend karta hai, 
    // isliye upar wala Mixin hi kaafi hai, lekin ye file structure ke liye zaroori ho sakti hai.
}
