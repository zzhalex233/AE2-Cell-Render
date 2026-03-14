package com.zzhalex233.ae2cellrender.client.drive;

import java.util.function.IntUnaryOperator;

public final class CellColorMath {

    private CellColorMath() {
    }

    public static int averageOpaqueColor(int[] pixels, int fallback) {
        long alpha = 0L;
        long red = 0L;
        long green = 0L;
        long blue = 0L;
        int count = 0;

        for (int pixel : pixels) {
            int pixelAlpha = (pixel >>> 24) & 0xFF;
            if (pixelAlpha == 0) {
                continue;
            }

            alpha += pixelAlpha;
            red += (pixel >>> 16) & 0xFF;
            green += (pixel >>> 8) & 0xFF;
            blue += pixel & 0xFF;
            count++;
        }

        if (count == 0) {
            return fallback;
        }

        return argb((int) (alpha / count), (int) (red / count), (int) (green / count), (int) (blue / count));
    }

    public static int opaqueTintOr(int tint, int fallback) {
        int rgb = tint & 0xFFFFFF;
        if (tint == -1 || rgb == 0) {
            return fallback;
        }

        return 0xFF000000 | rgb;
    }

    public static int firstUsableTint(int[] tintIndices, IntUnaryOperator tintLookup, int fallback) {
        for (int tintIndex : tintIndices) {
            int tint = opaqueTintOr(tintLookup.applyAsInt(tintIndex), fallback);
            if (tint != fallback) {
                return tint;
            }
        }

        return fallback;
    }

    public static float hueDistance(float left, float right) {
        float delta = Math.abs(left - right);
        return Math.min(delta, 360.0F - delta);
    }

    public static int colorDistance(int left, int right) {
        int redDelta = ((left >>> 16) & 0xFF) - ((right >>> 16) & 0xFF);
        int greenDelta = ((left >>> 8) & 0xFF) - ((right >>> 8) & 0xFF);
        int blueDelta = (left & 0xFF) - (right & 0xFF);
        return (redDelta * redDelta) + (greenDelta * greenDelta) + (blueDelta * blueDelta);
    }

    static HsvColor hsv(int color) {
        float red = ((color >>> 16) & 0xFF) / 255.0F;
        float green = ((color >>> 8) & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;

        float max = Math.max(red, Math.max(green, blue));
        float min = Math.min(red, Math.min(green, blue));
        float delta = max - min;

        float hue;
        if (delta == 0.0F) {
            hue = 0.0F;
        } else if (max == red) {
            hue = 60.0F * (((green - blue) / delta) % 6.0F);
        } else if (max == green) {
            hue = 60.0F * (((blue - red) / delta) + 2.0F);
        } else {
            hue = 60.0F * (((red - green) / delta) + 4.0F);
        }

        if (hue < 0.0F) {
            hue += 360.0F;
        }

        float saturation = max == 0.0F ? 0.0F : delta / max;
        return new HsvColor(hue, saturation, max);
    }

    static int argb(int alpha, int red, int green, int blue) {
        return ((alpha & 0xFF) << 24)
                | ((red & 0xFF) << 16)
                | ((green & 0xFF) << 8)
                | (blue & 0xFF);
    }

    static final class HsvColor {
        private final float hue;
        private final float saturation;
        private final float value;

        private HsvColor(float hue, float saturation, float value) {
            this.hue = hue;
            this.saturation = saturation;
            this.value = value;
        }

        float hue() {
            return hue;
        }

        float saturation() {
            return saturation;
        }

        float value() {
            return value;
        }
    }
}
