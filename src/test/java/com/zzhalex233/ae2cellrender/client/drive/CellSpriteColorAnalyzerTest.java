package com.zzhalex233.ae2cellrender.client.drive;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CellSpriteColorAnalyzerTest {

    @Test
    void choosesBodyColorOverSmallOffHueIndicatorRegion() {
        GeneratedCellSpriteFixtures.SpritePixels sprite = GeneratedCellSpriteFixtures.bodyWithIndicatorAndOutline();

        int resolved = CellSpriteColorAnalyzer.mainBodyColor(sprite.pixels, sprite.width, sprite.height, GeneratedCellSpriteFixtures.opaque(0xFF, 0xFF, 0xFF));

        assertTrue(
                CellColorMath.colorDistance(resolved, sprite.bodyColor)
                        < CellColorMath.colorDistance(resolved, sprite.indicatorColor)
        );
    }

    @Test
    void ignoresThinInternalOutlineWithoutKillingDarkBodyRegions() {
        GeneratedCellSpriteFixtures.SpritePixels sprite = GeneratedCellSpriteFixtures.darkBodyWithInternalOutline();

        int resolved = CellSpriteColorAnalyzer.mainBodyColor(sprite.pixels, sprite.width, sprite.height, GeneratedCellSpriteFixtures.opaque(0xFF, 0xFF, 0xFF));

        assertTrue(
                CellColorMath.colorDistance(resolved, sprite.bodyColor)
                        < CellColorMath.colorDistance(resolved, sprite.indicatorColor)
        );
    }

    @Test
    void prefersLitBodyToneOverSameHueShadowBand() {
        GeneratedCellSpriteFixtures.SpritePixels sprite = GeneratedCellSpriteFixtures.bodyWithShadowedLowerHalf();

        int resolved = CellSpriteColorAnalyzer.mainBodyColor(sprite.pixels, sprite.width, sprite.height, GeneratedCellSpriteFixtures.opaque(0xFF, 0xFF, 0xFF));
        int flatAverage = CellColorMath.averageOpaqueColor(sprite.pixels, GeneratedCellSpriteFixtures.opaque(0xFF, 0xFF, 0xFF));

        assertTrue(
                CellColorMath.colorDistance(resolved, sprite.bodyColor)
                        < CellColorMath.colorDistance(resolved, sprite.indicatorColor)
        );
        assertTrue(
                CellColorMath.colorDistance(resolved, sprite.bodyColor)
                        < CellColorMath.colorDistance(flatAverage, sprite.bodyColor)
        );
    }

    @Test
    void prefersSmallBrightFaceOverLargeSameHueShadowBand() {
        GeneratedCellSpriteFixtures.SpritePixels sprite = GeneratedCellSpriteFixtures.bodyWithSmallBrightFaceAndLargeShadowBand();

        int resolved = CellSpriteColorAnalyzer.mainBodyColor(sprite.pixels, sprite.width, sprite.height, GeneratedCellSpriteFixtures.opaque(0xFF, 0xFF, 0xFF));
        float bodyValue = CellColorMath.hsv(sprite.bodyColor).value();
        float shadowValue = CellColorMath.hsv(sprite.indicatorColor).value();
        float resolvedValue = CellColorMath.hsv(resolved).value();

        assertTrue(
                CellColorMath.colorDistance(resolved, sprite.bodyColor)
                        < CellColorMath.colorDistance(resolved, sprite.indicatorColor)
        );
        assertTrue(
                resolvedValue > shadowValue + ((bodyValue - shadowValue) * 0.60F)
        );
    }
}
