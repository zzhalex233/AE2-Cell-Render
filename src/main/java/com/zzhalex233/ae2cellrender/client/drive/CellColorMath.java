package com.zzhalex233.ae2cellrender.client.drive;

import com.zzhalex233.ae2cellrender.config.AE2CellRenderConfig;

import java.util.function.IntUnaryOperator;

public final class CellColorMath {

    private static final float[] DISPLAY_HUE_ANCHORS = {0.0F, 30.0F, 60.0F, 120.0F, 180.0F, 210.0F, 240.0F, 300.0F};

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

    public static int postProcessMainColor(int argb) {
        int alpha = (argb >>> 24) & 0xFF;
        if (alpha == 0) {
            return argb;
        }

        int stageOne = postProcessMainColorStageOne(argb);
        if (!AE2CellRenderConfig.isDisplayColorEnhancementEnabled()) {
            return stageOne;
        }
        return strengthenDisplay(stageOne);
    }

    private static int postProcessMainColorStageOne(int argb) {
        int alpha = (argb >>> 24) & 0xFF;
        if (alpha == 0) {
            return argb;
        }

        HsvColor hsv = hsv(argb);
        float neutrality = clampUnit(1.0F - (hsv.saturation() * 1.1F));
        float liftedValue = clampUnit(hsv.value() + ((1.0F - hsv.value()) * (0.56F + (0.20F * neutrality))));

        float adjustedSaturation;
        if (hsv.saturation() < 0.12F) {
            adjustedSaturation = hsv.saturation() * 0.45F;
        } else if (hsv.saturation() < 0.30F) {
            adjustedSaturation = hsv.saturation() * 0.72F;
        } else {
            adjustedSaturation = hsv.saturation() * 0.80F;
        }

        adjustedSaturation = clampUnit(adjustedSaturation + (0.03F * (1.0F - neutrality)));
        return argb(alpha, rgbFromHsv(hsv.hue(), adjustedSaturation, liftedValue));
    }

    private static int strengthenDisplay(int argb) {
        int alpha = (argb >>> 24) & 0xFF;
        if (alpha == 0) {
            return argb;
        }

        float brightnessBoost = Math.max(0.0F, AE2CellRenderConfig.displayBrightnessBoost());
        float saturationBoost = Math.max(0.0F, AE2CellRenderConfig.displaySaturationBoost());
        float neutralCleanlinessBoost = Math.max(0.0F, AE2CellRenderConfig.neutralCleanlinessBoost());
        boolean preserveSoftPastels = AE2CellRenderConfig.isSoftPastelPreservationEnabled();

        HsvColor hsv = hsv(argb);
        float saturation = hsv.saturation();
        float value = hsv.value();
        float darkness = 1.0F - value;
        float chromaWeight = smoothstep(0.05F, 0.26F, saturation);
        float colorBoostWeight = smoothstep(0.24F, 0.40F, saturation);
        float vividWeight = smoothstep(0.28F, 0.65F, saturation);
        float mutedWeight = chromaWeight * (1.0F - vividWeight);
        float neutralWeight = 1.0F - chromaWeight;

        // Near-neutrals should get cleaner and brighter, while colored tones get a vivid-but-smooth lift.
        float neutralValueLift = (0.015F * neutralWeight) * neutralCleanlinessBoost;
        float coloredValueLift = (0.24F + (0.12F * mutedWeight) + (0.04F * vividWeight)) * brightnessBoost;
        float enhancedValue = clampUnit(value + (darkness * lerp(neutralValueLift, coloredValueLift, chromaWeight)));

        float softSaturation = lerp(
                saturation * (0.70F + (0.08F * darkness)),
                saturation * (0.96F + (0.02F * darkness)),
                smoothstep(0.10F, 0.22F, saturation)
        );
        if (!preserveSoftPastels) {
            // Relax the stage-two pastel guard without changing the low-saturation branch itself.
            softSaturation = lerp(softSaturation, saturation, 0.45F);
        }
        float coloredSaturation = saturation + ((1.0F - saturation) * (0.24F + (0.24F * mutedWeight) + (0.10F * vividWeight) + (0.18F * darkness)) * saturationBoost);
        float saturationWeight = colorBoostWeight;
        if (!preserveSoftPastels) {
            saturationWeight = clampUnit(saturationWeight + (0.35F * smoothstep(0.12F, 0.24F, saturation) * Math.max(0.0F, saturationBoost - 1.0F)));
        }
        float enhancedSaturation = clampUnit(lerp(softSaturation, coloredSaturation, saturationWeight));

        float hue = hsv.hue();
        float huePull = saturationWeight * smoothstep(0.30F, 0.60F, enhancedSaturation) * (0.12F + (0.10F * mutedWeight) + (0.04F * darkness));
        if (huePull > 0.0F) {
            hue = lerpHue(hue, blendedAnchorHue(hue), clampUnit(huePull));
        }

        return argb(alpha, rgbFromHsv(hue, enhancedSaturation, enhancedValue));
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

    private static int argb(int alpha, int rgb) {
        return ((alpha & 0xFF) << 24) | (rgb & 0xFFFFFF);
    }

    static float clampUnit(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }

    private static float lerp(float start, float end, float amount) {
        return start + ((end - start) * clampUnit(amount));
    }

    private static float smoothstep(float edge0, float edge1, float value) {
        if (edge0 == edge1) {
            return value < edge0 ? 0.0F : 1.0F;
        }
        float normalized = clampUnit((value - edge0) / (edge1 - edge0));
        return normalized * normalized * (3.0F - (2.0F * normalized));
    }

    private static float lerpHue(float source, float target, float amount) {
        float delta = hueDelta(source, target);
        return wrapHue(source + (delta * clampUnit(amount)));
    }

    private static float blendedAnchorHue(float hue) {
        double sumX = 0.0D;
        double sumY = 0.0D;
        for (float anchor : DISPLAY_HUE_ANCHORS) {
            float distance = hueDistance(hue, anchor);
            double weight = 1.0D / (1.0D + ((distance * distance) / 400.0D));
            double radians = Math.toRadians(anchor);
            sumX += Math.cos(radians) * weight;
            sumY += Math.sin(radians) * weight;
        }

        return wrapHue((float) Math.toDegrees(Math.atan2(sumY, sumX)));
    }

    private static float hueDelta(float source, float target) {
        float delta = target - source;
        if (delta > 180.0F) {
            delta -= 360.0F;
        } else if (delta < -180.0F) {
            delta += 360.0F;
        }
        return delta;
    }

    private static float wrapHue(float hue) {
        float wrappedHue = hue % 360.0F;
        if (wrappedHue < 0.0F) {
            wrappedHue += 360.0F;
        }
        return wrappedHue;
    }

    private static int rgbFromHsv(float hue, float saturation, float value) {
        float wrappedHue = wrapHue(hue);

        float chroma = value * saturation;
        float hueSector = wrappedHue / 60.0F;
        float x = chroma * (1.0F - Math.abs((hueSector % 2.0F) - 1.0F));
        float m = value - chroma;

        float red;
        float green;
        float blue;
        if (hueSector < 1.0F) {
            red = chroma;
            green = x;
            blue = 0.0F;
        } else if (hueSector < 2.0F) {
            red = x;
            green = chroma;
            blue = 0.0F;
        } else if (hueSector < 3.0F) {
            red = 0.0F;
            green = chroma;
            blue = x;
        } else if (hueSector < 4.0F) {
            red = 0.0F;
            green = x;
            blue = chroma;
        } else if (hueSector < 5.0F) {
            red = x;
            green = 0.0F;
            blue = chroma;
        } else {
            red = chroma;
            green = 0.0F;
            blue = x;
        }

        return ((Math.round((red + m) * 255.0F) & 0xFF) << 16)
                | ((Math.round((green + m) * 255.0F) & 0xFF) << 8)
                | (Math.round((blue + m) * 255.0F) & 0xFF);
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
