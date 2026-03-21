package com.zzhalex233.ae2cellrender.client.drive;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CellSpriteColorAnalyzerTest {

    @Test
    void bodyBeatsBlackOutlineAndTinyBrightAccent() {
        GeneratedCellSpriteFixtures.SpritePixels sprite = GeneratedCellSpriteFixtures.bodyWithIndicatorAndOutline();

        int resolved = CellSpriteMainColorExtractor.mainColor(
                sprite.pixels,
                sprite.width,
                sprite.height,
                GeneratedCellSpriteFixtures.opaque(0xFF, 0xFF, 0xFF)
        );

        assertCloserToBody(resolved, sprite);
    }

    @Test
    void bodyBeatsDarkContourLines() {
        GeneratedCellSpriteFixtures.SpritePixels sprite = GeneratedCellSpriteFixtures.darkBodyWithInternalOutline();

        int resolved = CellSpriteMainColorExtractor.mainColor(
                sprite.pixels,
                sprite.width,
                sprite.height,
                GeneratedCellSpriteFixtures.opaque(0xFF, 0xFF, 0xFF)
        );

        assertCloserToBody(resolved, sprite);
    }

    @Test
    void bodyBeatsWhiteHighlightSpecks() {
        GeneratedCellSpriteFixtures.SpritePixels sprite = GeneratedCellSpriteFixtures.bodyWithWhiteHighlightSpecks();

        int resolved = CellSpriteMainColorExtractor.mainColor(
                sprite.pixels,
                sprite.width,
                sprite.height,
                GeneratedCellSpriteFixtures.opaque(0xFF, 0xFF, 0xFF)
        );

        assertCloserToBody(resolved, sprite);
    }

    @Test
    void bodyNearBorderStillWinsAfterRingDownweighting() {
        GeneratedCellSpriteFixtures.SpritePixels sprite = GeneratedCellSpriteFixtures.bodyNearBorderWithTinyAccent();

        int resolved = CellSpriteMainColorExtractor.mainColor(
                sprite.pixels,
                sprite.width,
                sprite.height,
                GeneratedCellSpriteFixtures.opaque(0xFF, 0xFF, 0xFF)
        );

        assertCloserToBody(resolved, sprite);
    }

    @Test
    void grayBodyBeatsDarkStructureFamilies() {
        GeneratedCellSpriteFixtures.SpritePixels sprite = GeneratedCellSpriteFixtures.grayBodyWithDarkStructureAndPurpleAccent();

        int resolved = CellSpriteMainColorExtractor.mainColor(
                sprite.pixels,
                sprite.width,
                sprite.height,
                GeneratedCellSpriteFixtures.opaque(0xFF, 0xFF, 0xFF)
        );

        assertTrue(
                CellColorMath.colorDistance(resolved, sprite.bodyColor)
                        < CellColorMath.colorDistance(resolved, sprite.outlineColor),
                "resolved=#" + Integer.toHexString(resolved)
                        + " body=#" + Integer.toHexString(sprite.bodyColor)
                        + " outline=#" + Integer.toHexString(sprite.outlineColor)
        );
        assertCloserToBody(resolved, sprite);
    }

    @Test
    void litBodyToneBeatsSameHueShadowBand() {
        GeneratedCellSpriteFixtures.SpritePixels sprite = GeneratedCellSpriteFixtures.bodyWithShadowedLowerHalf();

        int resolved = CellSpriteMainColorExtractor.mainColor(
                sprite.pixels,
                sprite.width,
                sprite.height,
                GeneratedCellSpriteFixtures.opaque(0xFF, 0xFF, 0xFF)
        );

        assertCloserToBody(resolved, sprite);
    }

    @Test
    void warmBodyIgnoresTinyYellowTierIndicator() {
        GeneratedCellSpriteFixtures.SpritePixels sprite = GeneratedCellSpriteFixtures.warmBodyWithYellowTierIndicator();

        int resolved = CellSpriteMainColorExtractor.mainColor(
                sprite.pixels,
                sprite.width,
                sprite.height,
                GeneratedCellSpriteFixtures.opaque(0xFF, 0xFF, 0xFF)
        );

        assertCloserToBody(resolved, sprite);
    }

    @Test
    void warmBodyIgnoresTinyRedTierIndicator() {
        GeneratedCellSpriteFixtures.SpritePixels sprite = GeneratedCellSpriteFixtures.warmBodyWithRedTierIndicator();

        int resolved = CellSpriteMainColorExtractor.mainColor(
                sprite.pixels,
                sprite.width,
                sprite.height,
                GeneratedCellSpriteFixtures.opaque(0xFF, 0xFF, 0xFF)
        );

        assertCloserToBody(resolved, sprite);
    }

    @Test
    void sameSeriesWarmBodyStaysStableAcrossTierIndicatorColors() {
        GeneratedCellSpriteFixtures.SpritePixels yellowTier = GeneratedCellSpriteFixtures.warmBodyWithYellowTierIndicator();
        GeneratedCellSpriteFixtures.SpritePixels redTier = GeneratedCellSpriteFixtures.warmBodyWithRedTierIndicator();

        int yellowResolved = CellSpriteMainColorExtractor.mainColor(
                yellowTier.pixels,
                yellowTier.width,
                yellowTier.height,
                GeneratedCellSpriteFixtures.opaque(0xFF, 0xFF, 0xFF)
        );
        int redResolved = CellSpriteMainColorExtractor.mainColor(
                redTier.pixels,
                redTier.width,
                redTier.height,
                GeneratedCellSpriteFixtures.opaque(0xFF, 0xFF, 0xFF)
        );

        assertCloserToBody(yellowResolved, yellowTier);
        assertCloserToBody(redResolved, redTier);
        assertTrue(
                CellColorMath.colorDistance(yellowResolved, redResolved) <= 16.0F,
                "yellow=#" + Integer.toHexString(yellowResolved)
                        + " red=#" + Integer.toHexString(redResolved)
        );
    }

    @Test
    void candidateExposesOpaqueAndClusterSupportMetadata() {
        GeneratedCellSpriteFixtures.SpritePixels sprite = GeneratedCellSpriteFixtures.bodyWithIndicatorAndOutline();

        CellSpriteColorCandidate candidate = CellSpriteColorAnalyzer.mainBodyCandidate(
                sprite.pixels,
                sprite.width,
                sprite.height,
                GeneratedCellSpriteFixtures.opaque(0xFF, 0xFF, 0xFF)
        );

        assertCloserToBody(candidate.argb(), sprite);
        assertEquals(countOpaque(sprite.pixels), candidate.opaqueSampleCount());
        assertTrue(candidate.clusterWeight() > 0.0F, "cluster weight should be positive");
        assertTrue(candidate.familyWeight() >= candidate.clusterWeight(), "family support should include the winning cluster");
        assertTrue(candidate.innerWeightRatio() > 0.0F, "body candidate should have inner support");
        assertTrue(candidate.clusterCount() >= 1, "cluster count should be exposed");
    }

    @Test
    void candidateKeepsBodyLikeMetricsForGraySprite() {
        GeneratedCellSpriteFixtures.SpritePixels sprite = GeneratedCellSpriteFixtures.grayBodyWithDarkStructureAndPurpleAccent();

        CellSpriteColorCandidate candidate = CellSpriteColorAnalyzer.mainBodyCandidate(
                sprite.pixels,
                sprite.width,
                sprite.height,
                GeneratedCellSpriteFixtures.opaque(0xFF, 0xFF, 0xFF)
        );

        assertCloserToBody(candidate.argb(), sprite);
        assertTrue(candidate.innerWeightRatio() >= 0.40F, "gray body should still look body-like");
        assertTrue(candidate.averageLightness() > 25.0F, "candidate should expose representative lightness");
    }

    private static void assertCloserToBody(int resolved, GeneratedCellSpriteFixtures.SpritePixels sprite) {
        assertTrue(
                CellColorMath.colorDistance(resolved, sprite.bodyColor)
                        < CellColorMath.colorDistance(resolved, sprite.accentColor),
                "resolved=#" + Integer.toHexString(resolved)
                        + " body=#" + Integer.toHexString(sprite.bodyColor)
                        + " accent=#" + Integer.toHexString(sprite.accentColor)
        );
    }

    private static int countOpaque(int[] pixels) {
        int count = 0;
        for (int pixel : pixels) {
            if (((pixel >>> 24) & 0xFF) >= CellSpriteWeightedSamples.ALPHA_MIN) {
                count++;
            }
        }
        return count;
    }
}
