package com.zzhalex233.ae2cellrender.client.drive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class CellSpriteWeightedSamples {

    static final int ALPHA_MIN = 32;
    static final float OUTER_RING_WEIGHT = 0.20F;
    static final float SECOND_RING_WEIGHT = 0.45F;
    static final float INNER_WEIGHT = 1.00F;
    static final float DARK_OUTLINE_LUMINANCE_MAX = 0.22F;
    static final float DARK_OUTLINE_SATURATION_MAX = 0.20F;
    static final int DARK_OUTLINE_RGB_RANGE_MAX = 28;
    static final float DARK_OUTLINE_PENALTY = 0.12F;
    static final float BRIGHT_HIGHLIGHT_LUMINANCE_MIN = 0.94F;
    static final float BRIGHT_HIGHLIGHT_SATURATION_MAX = 0.10F;
    static final float BRIGHT_HIGHLIGHT_PENALTY = 0.35F;
    static final float VERY_DARK_LUMINANCE = 0.12F;
    static final float VERY_DARK_PENALTY = 0.25F;
    static final float VERY_BRIGHT_LUMINANCE = 0.94F;
    static final float VERY_BRIGHT_PENALTY = 0.40F;
    static final float SATURATION_WEIGHT_BASE = 0.35F;
    static final float SATURATION_WEIGHT_GAIN = 0.65F;
    static final float SATURATION_BOOST_DARK_LUMINANCE = 0.18F;
    static final float SATURATION_BOOST_BRIGHT_LUMINANCE = 0.45F;
    static final float DARK_STRUCTURE_LUMINANCE_MAX = 0.24F;
    static final int DARK_STRUCTURE_RGB_RANGE_MAX = 96;
    static final float DARK_STRUCTURE_PENALTY = 0.65F;

    private CellSpriteWeightedSamples() {
    }

    static List<WeightedSample> build(int[] pixels, int width, int height) {
        if (pixels.length != width * height) {
            throw new IllegalArgumentException("Pixel data does not match width/height");
        }

        List<WeightedSample> samples = new ArrayList<>(pixels.length);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = pixels[(y * width) + x];
                int alpha = (pixel >>> 24) & 0xFF;
                if (alpha < ALPHA_MIN) {
                    continue;
                }

                int red = (pixel >>> 16) & 0xFF;
                int green = (pixel >>> 8) & 0xFF;
                int blue = pixel & 0xFF;
                int rgbRange = Math.max(red, Math.max(green, blue)) - Math.min(red, Math.min(green, blue));
                float luminance = luminance(red, green, blue);
                float saturation = saturation(red, green, blue);
                Ring ring = ringFor(x, y, width, height);
                boolean outlineCandidate = luminance < DARK_OUTLINE_LUMINANCE_MAX
                        && saturation < DARK_OUTLINE_SATURATION_MAX
                        && rgbRange < DARK_OUTLINE_RGB_RANGE_MAX;
                boolean highlightCandidate = luminance > BRIGHT_HIGHLIGHT_LUMINANCE_MIN
                        && saturation < BRIGHT_HIGHLIGHT_SATURATION_MAX;

                float ringWeight = ring.weight();
                float saturationWeight = SATURATION_WEIGHT_BASE
                        + (SATURATION_WEIGHT_GAIN * saturation * saturationBoostFactor(luminance));
                float luminanceWeight = luminanceWeight(luminance);
                float outlinePenalty = outlineCandidate ? DARK_OUTLINE_PENALTY : 1.0F;
                float highlightPenalty = highlightCandidate ? BRIGHT_HIGHLIGHT_PENALTY : 1.0F;
                float darkStructurePenalty = darkStructurePenalty(luminance, rgbRange);
                float finalWeight = (alpha / 255.0F)
                        * ringWeight
                        * saturationWeight
                        * luminanceWeight
                        * outlinePenalty
                        * darkStructurePenalty
                        * highlightPenalty;

                samples.add(new WeightedSample(
                        x,
                        y,
                        pixel,
                        alpha,
                        red,
                        green,
                        blue,
                        rgbRange,
                        luminance,
                        saturation,
                        ring,
                        outlineCandidate,
                        highlightCandidate,
                        outlinePenalty,
                        highlightPenalty,
                        finalWeight,
                        CellColorMath.lab(pixel)
                ));
            }
        }
        return Collections.unmodifiableList(samples);
    }

    private static Ring ringFor(int x, int y, int width, int height) {
        if (x == 0 || y == 0 || x == width - 1 || y == height - 1) {
            return Ring.OUTER;
        }
        if (x == 1 || y == 1 || x == width - 2 || y == height - 2) {
            return Ring.SECOND;
        }
        return Ring.INNER;
    }

    private static float luminance(int red, int green, int blue) {
        return ((0.2126F * red) + (0.7152F * green) + (0.0722F * blue)) / 255.0F;
    }

    private static float saturation(int red, int green, int blue) {
        float redUnit = red / 255.0F;
        float greenUnit = green / 255.0F;
        float blueUnit = blue / 255.0F;
        float max = Math.max(redUnit, Math.max(greenUnit, blueUnit));
        float min = Math.min(redUnit, Math.min(greenUnit, blueUnit));
        if (max == 0.0F) {
            return 0.0F;
        }
        return (max - min) / max;
    }

    private static float luminanceWeight(float luminance) {
        if (luminance < VERY_DARK_LUMINANCE) {
            return VERY_DARK_PENALTY;
        }
        if (luminance > VERY_BRIGHT_LUMINANCE) {
            return VERY_BRIGHT_PENALTY;
        }
        return 1.0F;
    }

    private static float saturationBoostFactor(float luminance) {
        if (luminance <= SATURATION_BOOST_DARK_LUMINANCE) {
            return 0.0F;
        }
        if (luminance >= SATURATION_BOOST_BRIGHT_LUMINANCE) {
            return 1.0F;
        }
        return (luminance - SATURATION_BOOST_DARK_LUMINANCE)
                / (SATURATION_BOOST_BRIGHT_LUMINANCE - SATURATION_BOOST_DARK_LUMINANCE);
    }

    private static float darkStructurePenalty(float luminance, int rgbRange) {
        if (luminance < DARK_STRUCTURE_LUMINANCE_MAX && rgbRange < DARK_STRUCTURE_RGB_RANGE_MAX) {
            return DARK_STRUCTURE_PENALTY;
        }
        return 1.0F;
    }

    enum Ring {
        OUTER(OUTER_RING_WEIGHT),
        SECOND(SECOND_RING_WEIGHT),
        INNER(INNER_WEIGHT);

        private final float weight;

        Ring(float weight) {
            this.weight = weight;
        }

        float weight() {
            return weight;
        }
    }

    static final class WeightedSample {
        private final int x;
        private final int y;
        private final int argb;
        private final int alpha;
        private final int red;
        private final int green;
        private final int blue;
        private final int rgbRange;
        private final float luminance;
        private final float saturation;
        private final Ring ring;
        private final boolean outlineCandidate;
        private final boolean highlightCandidate;
        private final float outlinePenalty;
        private final float highlightPenalty;
        private final float weight;
        private final CellColorMath.LabColor lab;

        private WeightedSample(int x, int y, int argb, int alpha, int red, int green, int blue, int rgbRange,
                               float luminance, float saturation, Ring ring, boolean outlineCandidate,
                               boolean highlightCandidate, float outlinePenalty, float highlightPenalty,
                               float weight, CellColorMath.LabColor lab) {
            this.x = x;
            this.y = y;
            this.argb = argb;
            this.alpha = alpha;
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.rgbRange = rgbRange;
            this.luminance = luminance;
            this.saturation = saturation;
            this.ring = ring;
            this.outlineCandidate = outlineCandidate;
            this.highlightCandidate = highlightCandidate;
            this.outlinePenalty = outlinePenalty;
            this.highlightPenalty = highlightPenalty;
            this.weight = weight;
            this.lab = lab;
        }

        int x() {
            return x;
        }

        int y() {
            return y;
        }

        int argb() {
            return argb;
        }

        int alpha() {
            return alpha;
        }

        int red() {
            return red;
        }

        int green() {
            return green;
        }

        int blue() {
            return blue;
        }

        int rgbRange() {
            return rgbRange;
        }

        float luminance() {
            return luminance;
        }

        float saturation() {
            return saturation;
        }

        Ring ring() {
            return ring;
        }

        boolean isOutlineCandidate() {
            return outlineCandidate;
        }

        boolean isHighlightCandidate() {
            return highlightCandidate;
        }

        float outlinePenalty() {
            return outlinePenalty;
        }

        float highlightPenalty() {
            return highlightPenalty;
        }

        float weight() {
            return weight;
        }

        CellColorMath.LabColor lab() {
            return lab;
        }
    }
}
