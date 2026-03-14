package com.zzhalex233.ae2cellrender.client.drive;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DriveOverlayTextureTest {

    @Test
    void overlayFrontRegionContainsBrightNoiseWhileKeepingLampHole() throws IOException {
        try (InputStream stream = getClass().getResourceAsStream("/assets/ae2cellrender/textures/blocks/drive_overlay_white.png")) {
            assertNotNull(stream);
            BufferedImage image = ImageIO.read(stream);
            assertNotNull(image);

            Set<Integer> luminanceValues = new HashSet<>();
            for (int y = 0; y < 2; y++) {
                for (int x = 0; x < 6; x++) {
                    int argb = image.getRGB(x, y);
                    int alpha = (argb >>> 24) & 0xFF;
                    if (x == 4 && y == 1) {
                        assertEquals(0, alpha);
                        continue;
                    }

                    assertEquals(0xFF, alpha);
                    int red = (argb >>> 16) & 0xFF;
                    int green = (argb >>> 8) & 0xFF;
                    int blue = argb & 0xFF;
                    assertTrue(red >= 240 && green >= 240 && blue >= 240);
                    luminanceValues.add((red + green + blue) / 3);
                }
            }

            assertTrue(luminanceValues.size() >= 3);
        }
    }
}
