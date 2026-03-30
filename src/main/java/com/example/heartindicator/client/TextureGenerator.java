package com.example.heartindicator.client;

import com.example.heartindicator.HeartIndicatorMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.*;
import java.nio.file.*;

public class TextureGenerator {
    
    public static void generateHeartTextures() {
        try {
            generateHeartTexture(Identifier.of("heartindicator", "textures/gui/heart.png"), true);
            generateHeartTexture(Identifier.of("heartindicator", "textures/gui/half_heart.png"), false);
        } catch (Exception e) {
            HeartIndicatorMod.LOGGER.error("Failed to generate heart textures", e);
        }
    }
    
    private static void generateHeartTexture(Identifier identifier, boolean full) throws IOException {
        BufferedImage image = new BufferedImage(9, 9, BufferedImage.TYPE_INT_ARGB);
        
        // Heart shape coordinates (simple 9x9 pixel art)
        boolean[][] heartPixels = getHeartShape();
        
        for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 9; y++) {
                if (heartPixels[x][y]) {
                    int color;
                    if (full) {
                        // Full heart - red
                        color = 0xFFFF0000;
                    } else {
                        // Half heart - darker red for half
                        if (x < 5) {
                            color = 0xFFFF0000;
                        } else {
                            color = 0x88FF0000;
                        }
                    }
                    image.setRGB(x, y, color);
                } else {
                    image.setRGB(x, y, 0x00000000);
                }
            }
        }
        
        // Save to temp directory and register
        Path tempDir = Files.createTempDirectory("heartindicator_textures");
        Path filePath = tempDir.resolve(identifier.getPath().replace("textures/gui/", ""));
        Files.createDirectories(filePath.getParent());
        ImageIO.write(image, "png", filePath.toFile());
        
        // The texture will be loaded when needed by Minecraft
        HeartIndicatorMod.LOGGER.info("Generated texture: " + identifier);
    }
    
    private static boolean[][] getHeartShape() {
        boolean[][] heart = new boolean[9][9];
        // Simple heart pattern for 9x9
        int[][] pattern = {
            {0,0,1,0,0,0,1,0,0},
            {0,1,1,1,0,1,1,1,0},
            {1,1,1,1,1,1,1,1,1},
            {1,1,1,1,1,1,1,1,1},
            {0,1,1,1,1,1,1,1,0},
            {0,0,1,1,1,1,1,0,0},
            {0,0,0,1,1,1,0,0,0},
            {0,0,0,0,1,0,0,0,0},
            {0,0,0,0,0,0,0,0,0}
        };
        
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                heart[i][j] = pattern[j][i] == 1;
            }
        }
        return heart;
    }
}
