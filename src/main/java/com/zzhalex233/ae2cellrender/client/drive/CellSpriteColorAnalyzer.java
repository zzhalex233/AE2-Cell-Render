package com.zzhalex233.ae2cellrender.client.drive;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class CellSpriteColorAnalyzer {

    private static final float FAMILY_HUE_THRESHOLD = 18.0F;
    private static final float FAMILY_SATURATION_THRESHOLD = 0.20F;
    private static final float FAMILY_VALUE_THRESHOLD = 0.14F;
    private static final float OUTLINE_MAX_SOLIDITY = 0.35F;
    private static final float OUTLINE_MAX_AREA_RATIO = 0.40F;
    private static final float SAME_FAMILY_MIN_AREA_RATIO = 0.20F;
    private static final float DARK_FAMILY_VALUE_THRESHOLD = 0.25F;
    private static final float DARK_FAMILY_SPREAD_THRESHOLD = 0.20F;
    private static final float TONE_BIAS_MIN_VALUE_RANGE = 0.12F;
    private static final float BRIGHT_TONE_MIN_NORMALIZED_VALUE = 0.68F;
    private static final float BRIGHT_TONE_MIN_SAMPLE_RATIO = 0.10F;
    private static final float BRIGHT_TONE_BLEND = 0.65F;
    private static final float TONE_BIAS_BASE_WEIGHT = 0.35F;

    private CellSpriteColorAnalyzer() {
    }

    public static int mainBodyColor(int[] pixels, int width, int height, int fallback) {
        if (width <= 0 || height <= 0 || pixels.length == 0) {
            return fallback;
        }

        int boundedSize = Math.min(pixels.length, width * height);
        Sample[] sampleByIndex = new Sample[boundedSize];
        List<Sample> opaqueSamples = collectOpaqueSamples(pixels, width, height, boundedSize, sampleByIndex);
        if (opaqueSamples.isEmpty()) {
            return fallback;
        }

        List<ColorFamily> families = clusterFamilies(opaqueSamples);
        if (families.isEmpty()) {
            return CellColorMath.averageOpaqueColor(pixels, fallback);
        }

        int[] familyByPixel = new int[boundedSize];
        Arrays.fill(familyByPixel, -1);
        for (Sample sample : opaqueSamples) {
            familyByPixel[sample.index] = sample.familyId;
        }

        List<Region> regions = buildRegions(width, height, familyByPixel, sampleByIndex, families);
        ColorFamily bodyFamily = pickBodyFamily(families, opaqueSamples.size(), width, height);
        if (bodyFamily == null) {
            return CellColorMath.averageOpaqueColor(pixels, fallback);
        }

        Region bodyRegion = pickBodyRegion(bodyFamily, opaqueSamples.size(), width, height);
        if (bodyRegion == null) {
            return bodyFamily.averageColor();
        }

        List<Sample> accepted = new ArrayList<>();
        for (Region region : regions) {
            ColorFamily family = families.get(region.familyId);
            if (!family.matchesBodyBand(bodyFamily)) {
                continue;
            }
            if (region.looksLikeOutline(opaqueSamples.size())) {
                continue;
            }
            if (family.id != bodyFamily.id) {
                if (!region.isAdjacentTo(bodyRegion, width)) {
                    continue;
                }
            } else if (region != bodyRegion
                    && !region.isAdjacentTo(bodyRegion, width)
                    && region.area < Math.max(1, Math.round(bodyRegion.area * SAME_FAMILY_MIN_AREA_RATIO))) {
                continue;
            }
            accepted.addAll(region.samples);
        }

        if (accepted.isEmpty()) {
            return bodyRegion.averageColor();
        }

        return representativeColor(accepted, fallback);
    }

    private static List<Sample> collectOpaqueSamples(int[] pixels, int width, int height, int boundedSize, Sample[] sampleByIndex) {
        List<Sample> samples = new ArrayList<>();
        for (int index = 0; index < boundedSize; index++) {
            int pixel = pixels[index];
            int alpha = (pixel >>> 24) & 0xFF;
            if (alpha == 0) {
                continue;
            }

            int x = index % width;
            int y = index / width;
            Sample sample = new Sample(index, x, y, pixel, alpha);
            sampleByIndex[index] = sample;
            samples.add(sample);
        }
        return samples;
    }

    private static List<ColorFamily> clusterFamilies(List<Sample> samples) {
        List<ColorFamily> families = new ArrayList<>();
        for (Sample sample : samples) {
            ColorFamily bestFamily = null;
            float bestDistance = Float.MAX_VALUE;
            for (ColorFamily family : families) {
                if (!family.accepts(sample)) {
                    continue;
                }

                float distance = family.distanceTo(sample);
                if (distance < bestDistance) {
                    bestDistance = distance;
                    bestFamily = family;
                }
            }

            if (bestFamily == null) {
                bestFamily = new ColorFamily(families.size());
                families.add(bestFamily);
            }

            bestFamily.add(sample);
        }
        return families;
    }

    private static List<Region> buildRegions(
            int width,
            int height,
            int[] familyByPixel,
            Sample[] sampleByIndex,
            List<ColorFamily> families
    ) {
        List<Region> regions = new ArrayList<>();
        boolean[] visited = new boolean[familyByPixel.length];

        for (int index = 0; index < familyByPixel.length; index++) {
            int familyId = familyByPixel[index];
            if (familyId < 0 || visited[index]) {
                continue;
            }

            Region region = floodFill(index, familyId, width, height, familyByPixel, sampleByIndex, visited);
            if (region != null) {
                families.get(region.familyId).regions.add(region);
                regions.add(region);
            }
        }

        return regions;
    }

    private static Region floodFill(
            int startIndex,
            int familyId,
            int width,
            int height,
            int[] familyByPixel,
            Sample[] sampleByIndex,
            boolean[] visited
    ) {
        Region region = new Region(familyId);
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        queue.add(startIndex);
        visited[startIndex] = true;

        while (!queue.isEmpty()) {
            int index = queue.removeFirst();
            Sample sample = sampleByIndex[index];
            if (sample == null) {
                continue;
            }

            region.add(sample);

            int x = index % width;
            int y = index / width;

            if (x > 0) {
                tryVisit(index - 1, familyId, familyByPixel, visited, queue);
            }
            if (x + 1 < width) {
                tryVisit(index + 1, familyId, familyByPixel, visited, queue);
            }
            if (y > 0) {
                tryVisit(index - width, familyId, familyByPixel, visited, queue);
            }
            if (y + 1 < height) {
                tryVisit(index + width, familyId, familyByPixel, visited, queue);
            }
        }

        return region.samples.isEmpty() ? null : region;
    }

    private static void tryVisit(int index, int familyId, int[] familyByPixel, boolean[] visited, ArrayDeque<Integer> queue) {
        if (visited[index] || familyByPixel[index] != familyId) {
            return;
        }

        visited[index] = true;
        queue.addLast(index);
    }

    private static ColorFamily pickBodyFamily(List<ColorFamily> families, int opaqueCount, int width, int height) {
        ColorFamily bestFamily = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (ColorFamily family : families) {
            double score = family.score(opaqueCount, width, height);
            if (score > bestScore) {
                bestScore = score;
                bestFamily = family;
            }
        }

        return bestFamily;
    }

    private static Region pickBodyRegion(ColorFamily bodyFamily, int opaqueCount, int width, int height) {
        Region bestRegion = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (Region region : bodyFamily.regions) {
            double score = region.score(opaqueCount, width, height);
            if (score > bestScore) {
                bestScore = score;
                bestRegion = region;
            }
        }

        return bestRegion;
    }

    private static int representativeColor(List<Sample> samples, int fallback) {
        float minValue = Float.MAX_VALUE;
        float maxValue = Float.MIN_VALUE;
        for (Sample sample : samples) {
            minValue = Math.min(minValue, sample.value);
            maxValue = Math.max(maxValue, sample.value);
        }

        float valueRange = maxValue - minValue;
        if (valueRange < TONE_BIAS_MIN_VALUE_RANGE) {
            return averageSamples(samples, fallback);
        }

        int weightedColor = weightedAverageSamples(samples, fallback, minValue, valueRange);
        List<Sample> brightSamples = collectBrightToneSamples(samples, minValue, valueRange);
        if (brightSamples.size() < Math.max(1, Math.round(samples.size() * BRIGHT_TONE_MIN_SAMPLE_RATIO))) {
            return weightedColor;
        }

        int brightColor = averageSamples(brightSamples, fallback);
        return blendColors(weightedColor, brightColor, BRIGHT_TONE_BLEND);
    }

    private static List<Sample> collectBrightToneSamples(List<Sample> samples, float minValue, float valueRange) {
        List<Sample> brightSamples = new ArrayList<>();
        for (Sample sample : samples) {
            float normalizedValue = (sample.value - minValue) / valueRange;
            if (normalizedValue >= BRIGHT_TONE_MIN_NORMALIZED_VALUE) {
                brightSamples.add(sample);
            }
        }
        return brightSamples;
    }

    private static int weightedAverageSamples(List<Sample> samples, int fallback, float minValue, float valueRange) {
        double alpha = 0.0D;
        double red = 0.0D;
        double green = 0.0D;
        double blue = 0.0D;
        double totalWeight = 0.0D;

        for (Sample sample : samples) {
            double normalizedValue = (sample.value - minValue) / valueRange;
            double weight = TONE_BIAS_BASE_WEIGHT + (normalizedValue * normalizedValue);
            totalWeight += weight;
            alpha += sample.alpha * weight;
            red += sample.red * weight;
            green += sample.green * weight;
            blue += sample.blue * weight;
        }

        if (totalWeight <= 0.0D) {
            return averageSamples(samples, fallback);
        }

        return CellColorMath.argb(
                (int) Math.round(alpha / totalWeight),
                (int) Math.round(red / totalWeight),
                (int) Math.round(green / totalWeight),
                (int) Math.round(blue / totalWeight)
        );
    }

    private static int blendColors(int baseColor, int accentColor, float accentWeight) {
        float baseWeight = 1.0F - accentWeight;
        int alpha = Math.round((((baseColor >>> 24) & 0xFF) * baseWeight) + (((accentColor >>> 24) & 0xFF) * accentWeight));
        int red = Math.round((((baseColor >>> 16) & 0xFF) * baseWeight) + (((accentColor >>> 16) & 0xFF) * accentWeight));
        int green = Math.round((((baseColor >>> 8) & 0xFF) * baseWeight) + (((accentColor >>> 8) & 0xFF) * accentWeight));
        int blue = Math.round(((baseColor & 0xFF) * baseWeight) + ((accentColor & 0xFF) * accentWeight));
        return CellColorMath.argb(alpha, red, green, blue);
    }

    private static int averageSamples(List<Sample> samples, int fallback) {
        long alpha = 0L;
        long red = 0L;
        long green = 0L;
        long blue = 0L;

        for (Sample sample : samples) {
            alpha += sample.alpha;
            red += sample.red;
            green += sample.green;
            blue += sample.blue;
        }

        if (samples.isEmpty()) {
            return fallback;
        }

        return CellColorMath.argb(
                (int) (alpha / samples.size()),
                (int) (red / samples.size()),
                (int) (green / samples.size()),
                (int) (blue / samples.size())
        );
    }

    private static final class Sample {
        private final int index;
        private final int x;
        private final int y;
        private final int color;
        private final int alpha;
        private final int red;
        private final int green;
        private final int blue;
        private final float hue;
        private final float saturation;
        private final float value;
        private int familyId = -1;

        private Sample(int index, int x, int y, int color, int alpha) {
            this.index = index;
            this.x = x;
            this.y = y;
            this.color = color;
            this.alpha = alpha;
            this.red = (color >>> 16) & 0xFF;
            this.green = (color >>> 8) & 0xFF;
            this.blue = color & 0xFF;
            CellColorMath.HsvColor hsvColor = CellColorMath.hsv(color);
            this.hue = hsvColor.hue();
            this.saturation = hsvColor.saturation();
            this.value = hsvColor.value();
        }
    }

    private static final class ColorFamily {
        private final int id;
        private final List<Sample> samples = new ArrayList<>();
        private final List<Region> regions = new ArrayList<>();
        private double hueX;
        private double hueY;
        private double saturationSum;
        private double valueSum;
        private int redSum;
        private int greenSum;
        private int blueSum;
        private int minX = Integer.MAX_VALUE;
        private int minY = Integer.MAX_VALUE;
        private int maxX = Integer.MIN_VALUE;
        private int maxY = Integer.MIN_VALUE;

        private ColorFamily(int id) {
            this.id = id;
        }

        private void add(Sample sample) {
            sample.familyId = id;
            samples.add(sample);
            double radians = Math.toRadians(sample.hue);
            hueX += Math.cos(radians);
            hueY += Math.sin(radians);
            saturationSum += sample.saturation;
            valueSum += sample.value;
            redSum += sample.red;
            greenSum += sample.green;
            blueSum += sample.blue;
            minX = Math.min(minX, sample.x);
            minY = Math.min(minY, sample.y);
            maxX = Math.max(maxX, sample.x);
            maxY = Math.max(maxY, sample.y);
        }

        private boolean accepts(Sample sample) {
            if (samples.isEmpty()) {
                return true;
            }

            return CellColorMath.hueDistance(meanHue(), sample.hue) <= FAMILY_HUE_THRESHOLD
                    && Math.abs(meanSaturation() - sample.saturation) <= FAMILY_SATURATION_THRESHOLD
                    && Math.abs(meanValue() - sample.value) <= FAMILY_VALUE_THRESHOLD;
        }

        private float distanceTo(Sample sample) {
            float hueDistance = CellColorMath.hueDistance(meanHue(), sample.hue) / FAMILY_HUE_THRESHOLD;
            float saturationDistance = Math.abs(meanSaturation() - sample.saturation) / FAMILY_SATURATION_THRESHOLD;
            float valueDistance = Math.abs(meanValue() - sample.value) / FAMILY_VALUE_THRESHOLD;
            return hueDistance + saturationDistance + valueDistance;
        }

        private boolean isNear(ColorFamily other) {
            return CellColorMath.hueDistance(meanHue(), other.meanHue()) <= FAMILY_HUE_THRESHOLD
                    && Math.abs(meanSaturation() - other.meanSaturation()) <= FAMILY_SATURATION_THRESHOLD
                    && Math.abs(meanValue() - other.meanValue()) <= FAMILY_VALUE_THRESHOLD;
        }

        private boolean matchesBodyBand(ColorFamily other) {
            return CellColorMath.hueDistance(meanHue(), other.meanHue()) <= FAMILY_HUE_THRESHOLD
                    && Math.abs(meanSaturation() - other.meanSaturation()) <= FAMILY_SATURATION_THRESHOLD;
        }

        private double score(int opaqueCount, int width, int height) {
            double coverage = samples.size() / (double) opaqueCount;
            double spread = boundingBoxArea() / (double) (width * height);
            double score = (coverage * 3.0D) + (spread * 1.5D) + (meanSaturation() * 0.5D);

            if (meanValue() < DARK_FAMILY_VALUE_THRESHOLD && spread < DARK_FAMILY_SPREAD_THRESHOLD) {
                score -= 0.75D;
            }

            return score;
        }

        private int averageColor() {
            return CellColorMath.argb(
                    0xFF,
                    redSum / samples.size(),
                    greenSum / samples.size(),
                    blueSum / samples.size()
            );
        }

        private int boundingBoxArea() {
            return (maxX - minX + 1) * (maxY - minY + 1);
        }

        private float meanHue() {
            double angle = Math.toDegrees(Math.atan2(hueY, hueX));
            if (angle < 0.0D) {
                angle += 360.0D;
            }
            return (float) angle;
        }

        private float meanSaturation() {
            return (float) (saturationSum / samples.size());
        }

        private float meanValue() {
            return (float) (valueSum / samples.size());
        }
    }

    private static final class Region {
        private final int familyId;
        private final List<Sample> samples = new ArrayList<>();
        private final Set<Integer> positions = new HashSet<>();
        private int minX = Integer.MAX_VALUE;
        private int minY = Integer.MAX_VALUE;
        private int maxX = Integer.MIN_VALUE;
        private int maxY = Integer.MIN_VALUE;
        private int redSum;
        private int greenSum;
        private int blueSum;
        private int area;

        private Region(int familyId) {
            this.familyId = familyId;
        }

        private void add(Sample sample) {
            samples.add(sample);
            positions.add(sample.index);
            area++;
            redSum += sample.red;
            greenSum += sample.green;
            blueSum += sample.blue;
            minX = Math.min(minX, sample.x);
            minY = Math.min(minY, sample.y);
            maxX = Math.max(maxX, sample.x);
            maxY = Math.max(maxY, sample.y);
        }

        private double score(int opaqueCount, int width, int height) {
            double areaRatio = area / (double) opaqueCount;
            double bboxRatio = boundingBoxArea() / (double) (width * height);
            double score = (areaRatio * 4.0D) + (bboxRatio * 1.5D) + solidity();
            if (looksLikeOutline(opaqueCount)) {
                score -= 2.0D;
            }
            return score;
        }

        private boolean looksLikeOutline(int opaqueCount) {
            return minDimension() <= 1
                    || (solidity() <= OUTLINE_MAX_SOLIDITY && area / (double) opaqueCount <= OUTLINE_MAX_AREA_RATIO);
        }

        private boolean isAdjacentTo(Region other, int width) {
            for (Integer position : positions) {
                int x = position % width;
                int y = position / width;

                if (x > 0 && other.positions.contains(position - 1)) {
                    return true;
                }
                if (x + 1 < width && other.positions.contains(position + 1)) {
                    return true;
                }
                if (y > 0 && other.positions.contains(position - width)) {
                    return true;
                }
                if (other.positions.contains(position + width)) {
                    return true;
                }
            }
            return false;
        }

        private int averageColor() {
            return CellColorMath.argb(0xFF, redSum / area, greenSum / area, blueSum / area);
        }

        private int boundingBoxArea() {
            return (maxX - minX + 1) * (maxY - minY + 1);
        }

        private int minDimension() {
            return Math.min(maxX - minX + 1, maxY - minY + 1);
        }

        private double solidity() {
            return area / (double) boundingBoxArea();
        }
    }
}
