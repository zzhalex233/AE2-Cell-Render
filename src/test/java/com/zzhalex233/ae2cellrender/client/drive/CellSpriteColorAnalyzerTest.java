package com.zzhalex233.ae2cellrender.client.drive;

import org.junit.jupiter.api.Test;

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

    private static void assertCloserToBody(int resolved, GeneratedCellSpriteFixtures.SpritePixels sprite) {
        assertTrue(
                CellColorMath.colorDistance(resolved, sprite.bodyColor)
                        < CellColorMath.colorDistance(resolved, sprite.accentColor),
                "resolved=#" + Integer.toHexString(resolved)
                        + " body=#" + Integer.toHexString(sprite.bodyColor)
                        + " accent=#" + Integer.toHexString(sprite.accentColor)
        );
    }
}
