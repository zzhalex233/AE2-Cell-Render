package com.zzhalex233.ae2cellrender.client.drive;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CellSpriteWeightedSamplesTest {

    @Test
    void ignoresPixelsWithAlphaBelowThreshold() {
        int[] pixels = {
                GeneratedCellSpriteFixtures.argb(31, 0x80, 0x80, 0x80),
                GeneratedCellSpriteFixtures.argb(32, 0x80, 0x80, 0x80)
        };

        List<CellSpriteWeightedSamples.WeightedSample> samples = CellSpriteWeightedSamples.build(pixels, 2, 1);

        assertEquals(1, samples.size());
        assertEquals(32, samples.get(0).alpha());
    }

    @Test
    void outerRingWeightIsLowerThanInnerWeight() {
        int[] pixels = solidPixels(3, 3, GeneratedCellSpriteFixtures.opaque(0x6A, 0x9E, 0xD8));

        List<CellSpriteWeightedSamples.WeightedSample> samples = CellSpriteWeightedSamples.build(pixels, 3, 3);
        CellSpriteWeightedSamples.WeightedSample outer = findSample(samples, 0, 0);
        CellSpriteWeightedSamples.WeightedSample inner = findSample(samples, 1, 1);

        assertTrue(outer.weight() < inner.weight());
    }

    @Test
    void secondRingWeightSitsBetweenOuterAndInner() {
        int[] pixels = solidPixels(5, 5, GeneratedCellSpriteFixtures.opaque(0x6A, 0x9E, 0xD8));

        List<CellSpriteWeightedSamples.WeightedSample> samples = CellSpriteWeightedSamples.build(pixels, 5, 5);
        CellSpriteWeightedSamples.WeightedSample outer = findSample(samples, 0, 0);
        CellSpriteWeightedSamples.WeightedSample secondRing = findSample(samples, 1, 1);
        CellSpriteWeightedSamples.WeightedSample inner = findSample(samples, 2, 2);

        assertTrue(outer.weight() < secondRing.weight());
        assertTrue(secondRing.weight() < inner.weight());
    }

    @Test
    void darkOutlineCandidateGetsStrongPenalty() {
        int[] pixels = {
                GeneratedCellSpriteFixtures.opaque(0x12, 0x12, 0x12)
        };

        CellSpriteWeightedSamples.WeightedSample sample = CellSpriteWeightedSamples.build(pixels, 1, 1).get(0);

        assertTrue(sample.isOutlineCandidate());
        assertTrue(sample.outlinePenalty() < 0.2F);
        assertTrue(sample.weight() < 0.02F);
    }

    @Test
    void brightHighlightCandidateGetsHighlightPenalty() {
        int[] pixels = {
                GeneratedCellSpriteFixtures.opaque(0xF8, 0xF8, 0xF8)
        };

        CellSpriteWeightedSamples.WeightedSample sample = CellSpriteWeightedSamples.build(pixels, 1, 1).get(0);

        assertTrue(sample.isHighlightCandidate());
        assertTrue(sample.highlightPenalty() < 0.5F);
    }

    @Test
    void darkSaturatedStructureDoesNotOutweighNeutralBodyFill() {
        int body = GeneratedCellSpriteFixtures.opaque(0x9C, 0x9C, 0xA2);
        int darkStructure = GeneratedCellSpriteFixtures.opaque(0x36, 0x20, 0x14);

        CellSpriteWeightedSamples.WeightedSample bodySample = CellSpriteWeightedSamples.build(new int[]{body}, 1, 1).get(0);
        CellSpriteWeightedSamples.WeightedSample structureSample = CellSpriteWeightedSamples.build(new int[]{darkStructure}, 1, 1).get(0);

        assertTrue(
                bodySample.weight() > structureSample.weight(),
                "bodyWeight=" + bodySample.weight() + " structureWeight=" + structureSample.weight()
        );
    }

    private static int[] solidPixels(int width, int height, int color) {
        int[] pixels = new int[width * height];
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = color;
        }
        return pixels;
    }

    private static CellSpriteWeightedSamples.WeightedSample findSample(List<CellSpriteWeightedSamples.WeightedSample> samples, int x, int y) {
        for (CellSpriteWeightedSamples.WeightedSample sample : samples) {
            if (sample.x() == x && sample.y() == y) {
                return sample;
            }
        }
        throw new AssertionError("No sample at (" + x + ", " + y + ")");
    }
}
