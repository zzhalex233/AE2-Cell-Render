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

    static LabColor lab(int color) {
        float red = srgbChannelToLinear((color >>> 16) & 0xFF);
        float green = srgbChannelToLinear((color >>> 8) & 0xFF);
        float blue = srgbChannelToLinear(color & 0xFF);

        float x = ((red * 0.4124F) + (green * 0.3576F) + (blue * 0.1805F)) / 0.95047F;
        float y = ((red * 0.2126F) + (green * 0.7152F) + (blue * 0.0722F));
        float z = ((red * 0.0193F) + (green * 0.1192F) + (blue * 0.9505F)) / 1.08883F;

        float fx = labPivot(x);
        float fy = labPivot(y);
        float fz = labPivot(z);
        return new LabColor(
                (116.0F * fy) - 16.0F,
                500.0F * (fx - fy),
                200.0F * (fy - fz)
        );
    }

    static float deltaE(LabColor left, LabColor right) {
        float lightnessDelta = left.lightness() - right.lightness();
        float aDelta = left.a() - right.a();
        float bDelta = left.b() - right.b();
        return (float) Math.sqrt((lightnessDelta * lightnessDelta) + (aDelta * aDelta) + (bDelta * bDelta));
    }

    static LabColor lab(float lightness, float a, float b) {
        return new LabColor(lightness, a, b);
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

    static float clampUnit(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }

    private static float srgbChannelToLinear(int channel) {
        float normalized = clampUnit(channel / 255.0F);
        if (normalized <= 0.04045F) {
            return normalized / 12.92F;
        }
        return (float) Math.pow((normalized + 0.055F) / 1.055F, 2.4D);
    }

    private static float labPivot(float value) {
        if (value > 0.008856F) {
            return (float) Math.cbrt(value);
        }
        return (7.787F * value) + (16.0F / 116.0F);
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

    static final class LabColor {
        private final float lightness;
        private final float a;
        private final float b;

        private LabColor(float lightness, float a, float b) {
            this.lightness = lightness;
            this.a = a;
            this.b = b;
        }

        float lightness() {
            return lightness;
        }

        float a() {
            return a;
        }

        float b() {
            return b;
        }
    }
}
