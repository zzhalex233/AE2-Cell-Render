package com.zzhalex233.ae2cellrender.client.drive;

import java.awt.Color;
import java.util.function.IntUnaryOperator;

final class GeneratedCellSpriteFixtures {

    private GeneratedCellSpriteFixtures() {
    }

    static SpritePixels bodyWithIndicatorAndOutline() {
        int width = 16;
        int height = 16;
        int[] pixels = new int[width * height];

        int body = hsv(220.0F, 0.78F, 0.92F);
        int bodyShadeA = transform(body, hsvDelta(0.0F, -0.10F, -0.08F));
        int bodyShadeB = transform(body, hsvDelta(0.0F, -0.05F, 0.00F));
        int outline = transform(body, hsvDelta(0.0F, -0.30F, -0.55F));
        int indicator = shiftHue(body, 140.0F, 0.92F, 0.88F);

        fill(pixels, transparent());
        paintRect(pixels, width, 2, 2, 13, 13, bodyShadeA);
        paintRect(pixels, width, 4, 3, 11, 12, bodyShadeB);
        paintBorder(pixels, width, 2, 2, 13, 13, outline);
        paintLine(pixels, width, 7, 4, 7, 11, outline);
        paintRect(pixels, width, 11, 10, 12, 11, indicator);

        return new SpritePixels(width, height, pixels, body, indicator);
    }

    static SpritePixels darkBodyWithInternalOutline() {
        int width = 16;
        int height = 16;
        int[] pixels = new int[width * height];

        int body = hsv(215.0F, 0.48F, 0.34F);
        int bodyShade = transform(body, hsvDelta(0.0F, 0.02F, 0.05F));
        int outline = transform(body, hsvDelta(0.0F, 0.00F, -0.18F));

        fill(pixels, transparent());
        paintRect(pixels, width, 2, 2, 13, 13, body);
        paintRect(pixels, width, 4, 4, 11, 11, bodyShade);
        paintLine(pixels, width, 4, 8, 11, 8, outline);
        paintLine(pixels, width, 8, 4, 8, 11, outline);

        return new SpritePixels(width, height, pixels, body, outline);
    }

    static SpritePixels bodyWithShadowedLowerHalf() {
        int width = 16;
        int height = 16;
        int[] pixels = new int[width * height];

        int body = hsv(18.0F, 0.74F, 0.80F);
        int shadow = transform(body, hsvDelta(0.0F, 0.06F, -0.28F));
        int outline = transform(body, hsvDelta(0.0F, -0.18F, -0.45F));

        fill(pixels, transparent());
        paintRect(pixels, width, 2, 2, 13, 13, shadow);
        paintRect(pixels, width, 3, 3, 12, 7, body);
        paintBorder(pixels, width, 2, 2, 13, 13, outline);

        return new SpritePixels(width, height, pixels, body, shadow);
    }

    static SpritePixels bodyWithSmallBrightFaceAndLargeShadowBand() {
        int width = 16;
        int height = 16;
        int[] pixels = new int[width * height];

        int body = hsv(18.0F, 0.74F, 0.86F);
        int shadow = transform(body, hsvDelta(0.0F, 0.04F, -0.34F));
        int outline = transform(body, hsvDelta(0.0F, -0.18F, -0.45F));

        fill(pixels, transparent());
        paintRect(pixels, width, 2, 2, 13, 13, shadow);
        paintRect(pixels, width, 3, 3, 12, 4, body);
        paintBorder(pixels, width, 2, 2, 13, 13, outline);

        return new SpritePixels(width, height, pixels, body, shadow);
    }

    static SpritePixels bodyWithWhiteHighlightSpecks() {
        int width = 16;
        int height = 16;
        int[] pixels = new int[width * height];

        int body = hsv(28.0F, 0.70F, 0.82F);
        int outline = transform(body, hsvDelta(0.0F, -0.30F, -0.50F));
        int highlight = opaque(0xF8, 0xF8, 0xF2);

        fill(pixels, transparent());
        paintRect(pixels, width, 2, 2, 13, 13, body);
        paintBorder(pixels, width, 2, 2, 13, 13, outline);
        paintRect(pixels, width, 5, 4, 5, 4, highlight);
        paintRect(pixels, width, 10, 6, 10, 6, highlight);

        return new SpritePixels(width, height, pixels, body, highlight);
    }

    static SpritePixels bodyNearBorderWithTinyAccent() {
        int width = 16;
        int height = 16;
        int[] pixels = new int[width * height];

        int body = hsv(210.0F, 0.48F, 0.88F);
        int bodyShade = transform(body, hsvDelta(0.0F, -0.04F, -0.08F));
        int accent = hsv(150.0F, 0.82F, 0.84F);

        fill(pixels, transparent());
        paintRect(pixels, width, 1, 1, 14, 12, bodyShade);
        paintRect(pixels, width, 2, 2, 13, 11, body);
        paintRect(pixels, width, 12, 10, 12, 10, accent);

        return new SpritePixels(width, height, pixels, body, accent);
    }

    static SpritePixels grayBodyWithDarkStructureAndPurpleAccent() {
        int width = 16;
        int height = 16;
        int[] pixels = new int[width * height];

        int body = opaque(0x9C, 0x9C, 0xA2);
        int bodyShade = opaque(0x88, 0x89, 0x90);
        int darkStructure = opaque(0x36, 0x20, 0x14);
        int accent = opaque(0x8B, 0x54, 0xC2);

        fill(pixels, transparent());
        paintRect(pixels, width, 2, 2, 13, 13, bodyShade);
        paintRect(pixels, width, 3, 3, 12, 12, body);
        paintBorder(pixels, width, 2, 2, 13, 13, darkStructure);
        paintLine(pixels, width, 5, 3, 5, 12, darkStructure);
        paintLine(pixels, width, 10, 3, 10, 12, darkStructure);
        paintLine(pixels, width, 3, 5, 12, 5, darkStructure);
        paintLine(pixels, width, 3, 10, 12, 10, darkStructure);
        paintRect(pixels, width, 7, 7, 8, 8, accent);

        return new SpritePixels(width, height, pixels, body, accent, darkStructure);
    }

    static int argb(int alpha, int red, int green, int blue) {
        return ((alpha & 0xFF) << 24)
                | ((red & 0xFF) << 16)
                | ((green & 0xFF) << 8)
                | (blue & 0xFF);
    }

    static int opaque(int red, int green, int blue) {
        return argb(0xFF, red, green, blue);
    }

    static int transparent() {
        return argb(0x00, 0x00, 0x00, 0x00);
    }

    static int hsv(float hue, float saturation, float value) {
        return 0xFF000000 | (Color.HSBtoRGB(hue / 360.0F, saturation, value) & 0x00FFFFFF);
    }

    static int shiftHue(int color, float hueOffset, float saturation, float value) {
        float[] hsb = Color.RGBtoHSB((color >>> 16) & 0xFF, (color >>> 8) & 0xFF, color & 0xFF, null);
        float hue = (hsb[0] * 360.0F + hueOffset) % 360.0F;
        if (hue < 0.0F) {
            hue += 360.0F;
        }
        return hsv(hue, saturation, value);
    }

    static IntUnaryOperator hsvDelta(float hueOffset, float saturationDelta, float valueDelta) {
        return color -> {
            float[] hsb = Color.RGBtoHSB((color >>> 16) & 0xFF, (color >>> 8) & 0xFF, color & 0xFF, null);
            float hue = (hsb[0] * 360.0F + hueOffset) % 360.0F;
            if (hue < 0.0F) {
                hue += 360.0F;
            }
            float saturation = clamp(hsb[1] + saturationDelta);
            float value = clamp(hsb[2] + valueDelta);
            return hsv(hue, saturation, value);
        };
    }

    static int transform(int color, IntUnaryOperator transform) {
        return transform.applyAsInt(color);
    }

    static void fill(int[] pixels, int color) {
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = color;
        }
    }

    static void paintRect(int[] pixels, int width, int minX, int minY, int maxX, int maxY, int color) {
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                pixels[(y * width) + x] = color;
            }
        }
    }

    static void paintBorder(int[] pixels, int width, int minX, int minY, int maxX, int maxY, int color) {
        paintLine(pixels, width, minX, minY, maxX, minY, color);
        paintLine(pixels, width, minX, maxY, maxX, maxY, color);
        paintLine(pixels, width, minX, minY, minX, maxY, color);
        paintLine(pixels, width, maxX, minY, maxX, maxY, color);
    }

    static void paintLine(int[] pixels, int width, int x0, int y0, int x1, int y1, int color) {
        if (x0 == x1) {
            for (int y = Math.min(y0, y1); y <= Math.max(y0, y1); y++) {
                pixels[(y * width) + x0] = color;
            }
            return;
        }

        for (int x = Math.min(x0, x1); x <= Math.max(x0, x1); x++) {
            pixels[(y0 * width) + x] = color;
        }
    }

    static float clamp(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }

    static final class SpritePixels {
        final int width;
        final int height;
        final int[] pixels;
        final int bodyColor;
        final int accentColor;
        final int outlineColor;

        private SpritePixels(int width, int height, int[] pixels, int bodyColor, int accentColor) {
            this(width, height, pixels, bodyColor, accentColor, accentColor);
        }

        private SpritePixels(int width, int height, int[] pixels, int bodyColor, int accentColor, int outlineColor) {
            this.width = width;
            this.height = height;
            this.pixels = pixels;
            this.bodyColor = bodyColor;
            this.accentColor = accentColor;
            this.outlineColor = outlineColor;
        }
    }
}
